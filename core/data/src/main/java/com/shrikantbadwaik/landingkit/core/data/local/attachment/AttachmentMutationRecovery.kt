package com.shrikantbadwaik.landingkit.core.data.local.attachment

import android.util.AtomicFile
import androidx.room.withTransaction
import com.shrikantbadwaik.landingkit.core.data.local.backup.BackupRecordId
import com.shrikantbadwaik.landingkit.core.data.local.database.LandingKitDatabase
import com.shrikantbadwaik.landingkit.core.data.local.reminder.ReminderCancellationStore
import java.io.File
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.withContext
import org.json.JSONObject

/**
 * Called only under BackupRestoreRecovery's shared operation lock.
 * Metadata commits an import. A durable deletion intent is completed on retry/startup.
 * IDs cannot be reused until the journal and its owned files have been cleaned.
 */
class AttachmentMutationRecovery(
    private val database: LandingKitDatabase,
    private val store: EncryptedAttachmentStore,
    private val journalFile: File,
    private val checkpoint: (AttachmentMutationCheckpoint) -> Unit = {},
    private val reminderCancellations: ReminderCancellationStore,
    private val afterTripMetadataDeleted: () -> Unit = {},
) {
    private val journal = AtomicFile(journalFile)

    suspend fun <T> import(tripId: String, attachmentId: String, publish: suspend () -> T): T {
        require(database.tripDao().getTrip(tripId) != null) { "This trip no longer exists" }
        require(database.attachmentDao().getAttachment(attachmentId) == null)
        store.requireEmptyImportDestination(tripId, attachmentId)
        return recorded(Record("import", tripId, attachmentId)) { publish() }
    }

    suspend fun deleteAttachment(tripId: String, attachmentId: String) =
        recorded(Record("document", tripId, attachmentId)) { finish(read()) }

    suspend fun deleteAttachments(tripId: String): Int {
        val count = database.attachmentDao().getAttachments(tripId).size
        recorded(Record("documents", tripId)) { finish(read()) }
        return count
    }

    suspend fun deleteTrip(tripId: String) =
        recorded(Record("trip", tripId)) { finish(read()) }

    suspend fun recover() {
        if (!journalFile.exists() && !File("${journalFile.path}.bak").exists()) return
        finish(read())
        clear()
    }

    internal fun documentWritten() = checkpoint(AttachmentMutationCheckpoint.DOCUMENT_WRITTEN)

    private suspend fun <T> recorded(record: Record, block: suspend () -> T): T {
        write(record)
        try {
            checkpoint(AttachmentMutationCheckpoint.JOURNALED)
            val result = block()
            if (record.kind == "import") {
                checkpoint(AttachmentMutationCheckpoint.METADATA_COMMITTED)
                finish(record)
            }
            clear()
            checkpoint(AttachmentMutationCheckpoint.CLEANED)
            return result
        } catch (error: Exception) {
            // Process termination leaves the intent; ordinary failure/cancellation completes it.
            withContext(NonCancellable) {
                try { recover() } catch (cleanup: Exception) { error.addSuppressed(cleanup) }
            }
            throw error
        }
    }

    private suspend fun finish(record: Record) {
        val dao = database.attachmentDao()
        when (record.kind) {
            "import" -> {
                val published = dao.getAttachment(checkNotNull(record.attachmentId))
                require(published == null || published.tripId == record.tripId) {
                    "Recovery document ownership changed"
                }
                if (published == null) store.delete(record.tripId, record.attachmentId)
                else {
                    check(store.exists(record.tripId, record.attachmentId)) {
                        "Published document file is missing"
                    }
                    store.deleteTemporaryFiles(record.tripId, record.attachmentId)
                }
            }
            "document" -> {
                database.withTransaction {
                    val attachment = dao.getAttachment(checkNotNull(record.attachmentId))
                    require(attachment == null || attachment.tripId == record.tripId)
                    dao.deleteAttachment(record.attachmentId)
                }
                checkpoint(AttachmentMutationCheckpoint.METADATA_COMMITTED)
                store.delete(record.tripId, checkNotNull(record.attachmentId))
            }
            "documents" -> {
                dao.deleteAttachments(record.tripId)
                checkpoint(AttachmentMutationCheckpoint.METADATA_COMMITTED)
                store.deleteTrip(record.tripId)
            }
            "trip" -> {
                // Foreign-key cascades remove the checklist and document metadata atomically.
                database.withTransaction { database.tripDao().deleteTrip(record.tripId) }
                afterTripMetadataDeleted()
                // The deletion journal survives the DB-to-queue gap. Never clear it until
                // the reminder effect has its own durable owner independent of the trip row.
                reminderCancellations.enqueue(record.tripId)
                checkpoint(AttachmentMutationCheckpoint.METADATA_COMMITTED)
                store.deleteTrip(record.tripId)
            }
        }
        checkpoint(AttachmentMutationCheckpoint.FILES_SETTLED)
    }

    private fun write(record: Record) {
        validate(record)
        check(!journalFile.exists() && !File("${journalFile.path}.bak").exists())
        journalFile.parentFile?.let { check(it.isDirectory || it.mkdirs()) }
        val output = journal.startWrite()
        try {
            output.write(JSONObject().put("version", 1).put("kind", record.kind)
                .put("trip", record.tripId).put("document", record.attachmentId ?: JSONObject.NULL)
                .toString().toByteArray(Charsets.UTF_8))
            journal.finishWrite(output)
        } catch (error: Throwable) {
            journal.failWrite(output)
            throw error
        }
    }

    private fun read(): Record {
        val bytes = journal.openRead().use { it.readBytesBounded() }
        val json = JSONObject(bytes.toString(Charsets.UTF_8))
        require(json.getInt("version") == 1)
        return Record(json.getString("kind"), json.getString("trip"),
            if (json.isNull("document")) null else json.getString("document")).also(::validate)
    }

    private fun java.io.InputStream.readBytesBounded(): ByteArray {
        val bytes = ByteArray(1025)
        var count = 0
        while (count < bytes.size) {
            val next = read(bytes, count, bytes.size - count)
            if (next == -1) break
            count += next
        }
        require(count <= 1024) { "Oversized document recovery record" }
        return bytes.copyOf(count)
    }

    private fun validate(record: Record) {
        require(record.kind in setOf("import", "document", "documents", "trip"))
        require(BackupRecordId.isValid(record.tripId))
        require(if (record.kind == "import" || record.kind == "document")
            record.attachmentId?.let(BackupRecordId::isValid) == true else record.attachmentId == null)
    }

    private fun clear() {
        journal.delete()
        check(!journalFile.exists() && !File("${journalFile.path}.bak").exists()) {
            "Could not clear document recovery record"
        }
    }

    private data class Record(val kind: String, val tripId: String, val attachmentId: String? = null)
}

enum class AttachmentMutationCheckpoint {
    JOURNALED, DOCUMENT_WRITTEN, METADATA_COMMITTED, FILES_SETTLED, CLEANED,
}
