package com.shrikantbadwaik.landingkit.core.data.local.backup

import android.content.Context
import android.util.AtomicFile
import androidx.room.withTransaction
import com.shrikantbadwaik.landingkit.core.data.local.attachment.EncryptedAttachmentStore
import com.shrikantbadwaik.landingkit.core.data.local.attachment.AttachmentMutationRecovery
import com.shrikantbadwaik.landingkit.core.data.local.reminder.ReminderCancellationStore
import com.shrikantbadwaik.landingkit.core.data.local.database.LandingKitDatabase
import com.shrikantbadwaik.landingkit.core.data.preferences.UserPreferences
import java.io.File
import java.io.IOException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

/**
 * A restore owns only IDs absent before it began. Until its durable commit record,
 * startup rolls those IDs back; after that record it finishes preference/cleanup.
 * Recovery must complete before exposing restored content or accepting another backup.
 */
class BackupRestoreRecovery(
    context: Context,
    private val database: LandingKitDatabase,
    private val store: EncryptedAttachmentStore,
    private val preferences: UserPreferences,
    private val journalFile: File = File(context.filesDir, "backup-restore.json"),
    val workDirectory: File = File(context.cacheDir, "backup_work"),
    private val access: BackupRecoveryAccess = BackupRecoveryAccess(),
    val reminderCancellations: ReminderCancellationStore = ReminderCancellationStore(
        File(journalFile.parentFile, journalFile.name + ".reminders"),
    ),
    val mutations: AttachmentMutationRecovery = AttachmentMutationRecovery(
        database, store, File(journalFile.parentFile, journalFile.name + ".documents"),
        reminderCancellations = reminderCancellations,
    ),
) {
    private val journal = AtomicFile(journalFile)
    private val mutex = Mutex()
    private val legacyWorkRoot = context.cacheDir

    suspend fun recover() = withContext(Dispatchers.IO) {
        mutex.withLock {
            access.recovering()
            recoverLocked()
        }
    }

    internal suspend fun <T> operation(block: suspend () -> T): T = mutex.withLock {
        recoverLocked()
        block()
    }

    internal suspend fun <T> dataOperation(block: suspend () -> T): T = withContext(Dispatchers.IO) {
        mutex.withLock {
            access.requireReady()
            try {
                mutations.recover()
                block()
            } catch (error: Exception) {
                // A failed cleanup must block subsequent ID reuse and unsafe repository access.
                try { withContext(kotlinx.coroutines.NonCancellable) { mutations.recover() } }
                catch (cleanup: Exception) {
                    access.blocked()
                    error.addSuppressed(cleanup)
                }
                throw error
            }
        }
    }

    internal fun begin(tripIds: List<String>, templateIds: List<String>, protection: Boolean) {
        check(!journalFile.exists() && !File("${journalFile.path}.bak").exists())
        require(tripIds.size <= 10_000 && templateIds.size <= 10_000)
        require(tripIds.distinct().size == tripIds.size && templateIds.distinct().size == templateIds.size)
        require((tripIds + templateIds).all(BackupRecordId::isValid)) { "Invalid restore record ID" }
        write(Record(tripIds, templateIds, protection, committed = false))
    }

    internal fun commit() {
        val record = read()
        write(record.copy(committed = true))
    }

    internal suspend fun recoverLocked() {
        try {
            recoverState()
            access.ready()
        } catch (error: Exception) {
            access.blocked()
            throw error
        }
    }

    private suspend fun recoverState() {
        mutations.recover()
        if (journalFile.exists() || File("${journalFile.path}.bak").exists()) {
            val record = read() // Fail closed on unreadable state; never guess deletion ownership.
            if (record.committed) {
                preferences.setAttachmentProtectionEnabled(record.protection)
            } else {
                database.withTransaction {
                    record.tripIds.forEach { database.tripDao().deleteTrip(it) }
                    record.templateIds.forEach { database.checklistTemplateDao().deleteTemplate(it) }
                }
                // Keep the journal until every owned directory is removed. Retry is idempotent.
                record.tripIds.forEach(store::deleteTrip)
            }
            cleanWork()
            journal.delete()
            check(!journalFile.exists() && !File("${journalFile.path}.bak").exists()) {
                "Could not clear restore recovery record"
            }
        } else {
            cleanWork()
        }
        // Upgrade cleanup for plaintext work files left by the old implementation.
        legacyWorkRoot.listFiles().orEmpty().filter {
            it.isFile && it.name.endsWith(".tmp") &&
                (it.name.startsWith("backup-payload") || it.name.startsWith("restored-payload"))
        }.forEach {
            if (!it.delete()) throw IOException("Could not clear old temporary backup data")
        }
    }

    private fun cleanWork() {
        if (workDirectory.exists() && !workDirectory.deleteRecursively()) {
            throw IOException("Could not clear temporary backup data")
        }
    }

    private fun write(record: Record) {
        journalFile.parentFile?.let { check(it.isDirectory || it.mkdirs()) }
        val json = JSONObject().put("version", 1)
            .put("trips", JSONArray(record.tripIds))
            .put("templates", JSONArray(record.templateIds))
            .put("protection", record.protection)
            .put("committed", record.committed)
        val output = journal.startWrite()
        try {
            output.write(json.toString().toByteArray(Charsets.UTF_8))
            journal.finishWrite(output)
        } catch (error: Throwable) {
            journal.failWrite(output)
            throw error
        }
    }

    private fun read(): Record {
        val json = JSONObject(journal.openRead().bufferedReader().use { it.readText() })
        require(json.getInt("version") == 1) { "Unknown restore recovery version" }
        fun ids(key: String): List<String> {
            val array = json.getJSONArray(key)
            require(array.length() <= 10_000)
            return List(array.length()) { array.getString(it) }.also { values ->
                require(values.distinct().size == values.size)
                require(values.all(BackupRecordId::isValid)) { "Invalid restore recovery ID" }
            }
        }
        return Record(ids("trips"), ids("templates"), json.getBoolean("protection"),
            json.getBoolean("committed"))
    }

    private data class Record(
        val tripIds: List<String>,
        val templateIds: List<String>,
        val protection: Boolean,
        val committed: Boolean,
    )

}
