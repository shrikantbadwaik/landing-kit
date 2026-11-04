package com.shrikantbadwaik.landingkit.core.data.local.attachment

import android.app.Activity
import android.net.Uri
import android.os.Bundle
import android.os.Process
import android.widget.TextView
import com.shrikantbadwaik.landingkit.core.data.local.backup.LocalBackupRepository
import com.shrikantbadwaik.landingkit.core.data.local.repository.LocalChecklistTemplateRepository
import com.shrikantbadwaik.landingkit.core.data.preferences.UserPreferences
import java.io.File
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import com.shrikantbadwaik.landingkit.core.data.local.attachment.AttachmentMutationFixture.Companion.TRIP
import com.shrikantbadwaik.landingkit.core.data.local.attachment.AttachmentMutationFixture.Companion.DOCUMENT
import com.shrikantbadwaik.landingkit.core.data.local.attachment.AttachmentMutationFixture.Companion.KEEP

/** Test APK only: actual process termination, isolated synthetic records and documents. */
class MutationProcessProbeActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val text = TextView(this).apply { textSize = 21f; setPadding(40, 200, 40, 40) }
        setContentView(text)
        val action = intent.getStringExtra("action") ?: "recover"
        val root = File(filesDir, "mutation-process-probe")
        text.text = "Thikana document verification: $action"
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            var fixture: AttachmentMutationFixture? = null
            try {
                if (action == "interrupt") {
                    check(!root.exists() || root.deleteRecursively())
                    val f = AttachmentMutationFixture(this@MutationProcessProbeActivity, root)
                    fixture = f
                    f.seed()
                    val kind = checkNotNull(intent.getStringExtra("kind"))
                    val stage = AttachmentMutationCheckpoint.valueOf(
                        checkNotNull(intent.getStringExtra("stage")))
                    backup(f).exportBackup(Uri.fromFile(File(root, "before.lkb")), PASSWORD.copyOf())
                    val recovery = f.recovery { checkpoint ->
                        if (checkpoint == stage) {
                            File(root, "reached").outputStream().use {
                                it.write("$kind:$stage".toByteArray()); it.fd.sync()
                            }
                            Process.killProcess(Process.myPid())
                        }
                    }
                    when (kind) {
                        "import" -> f.attachments(recovery).importAttachment(TRIP, f.sourceUri)
                        "document" -> f.attachments(recovery).deleteAttachment(DOCUMENT)
                        "documents" -> f.attachments(recovery).deleteAttachments(TRIP)
                        "trip" -> f.trips(recovery).deleteTrip(TRIP)
                        else -> error("Unknown synthetic operation")
                    }
                    error("Did not terminate at $kind:$stage")
                } else {
                    val (kind, rawStage) = File(root, "reached").readText().split(":")
                    val stage = AttachmentMutationCheckpoint.valueOf(rawStage)
                    val f = AttachmentMutationFixture(this@MutationProcessProbeActivity, root)
                    fixture = f
                    val recovery = f.recovery()
                    recovery.recover(); recovery.recover()
                    f.verifyOther()
                    check(!f.journal.exists())
                    when (kind) {
                        "import" -> {
                            val records = f.database.attachmentDao().getAttachments(TRIP)
                            check(records.size == if (stage >= AttachmentMutationCheckpoint.METADATA_COMMITTED) 3 else 2)
                            records.forEach { check(f.store.verifyDecryptable(TRIP, it.id)) }
                            check(File(root, "documents/$TRIP").listFiles()!!
                                .count { it.extension == "lka" } == records.size)
                        }
                        "document" -> {
                            check(f.database.attachmentDao().getAttachment(DOCUMENT) == null)
                            check(!f.store.exists(TRIP, DOCUMENT))
                            check(f.store.verifyDecryptable(TRIP, KEEP))
                            check(f.database.attachmentDao().getAttachment(KEEP)?.isQuickAccess == true)
                            f.attachments(recovery).deleteAttachment(DOCUMENT)
                        }
                        "documents", "trip" -> {
                            check(f.database.attachmentDao().getAttachments(TRIP).isEmpty())
                            check(!File(root, "documents/$TRIP").exists())
                            if (kind == "trip") {
                                check(f.database.tripDao().getTrip(TRIP) == null)
                                f.trips(recovery).deleteTrip(TRIP)
                                val restored = backup(f).restoreBackup(
                                    Uri.fromFile(File(root, "before.lkb")), PASSWORD.copyOf())
                                check(restored.restoredTripIds == listOf(TRIP))
                                check(f.store.verifyDecryptable(TRIP, DOCUMENT))
                                check(f.store.verifyDecryptable(TRIP, KEEP))
                            } else {
                                check(f.database.tripDao().getTrip(TRIP) != null)
                                check(f.attachments(recovery).deleteAttachments(TRIP) == 0)
                            }
                        }
                        else -> error("Unknown synthetic operation")
                    }
                    f.verifyOther()
                    val result = "PASS $kind:$stage; restart/retry safe; other trip/document intact"
                    File(root, "result").writeText(result)
                    runOnUiThread { text.text = "Thikana document verification\n\n$result" }
                }
            } catch (error: Throwable) {
                val failure = "FAIL: ${error.stackTraceToString()}"
                root.mkdirs(); File(root, "result").writeText(failure)
                runOnUiThread { text.text = failure }
            } finally { fixture?.close() }
        }
    }

    private fun backup(f: AttachmentMutationFixture): LocalBackupRepository {
        val recovery = f.recovery()
        return LocalBackupRepository(this, f.trips(recovery),
            LocalChecklistTemplateRepository(f.database.checklistTemplateDao(), f.access, recovery),
            f.database.attachmentDao(), f.store, UserPreferences(this), f.database, recovery)
    }
    private companion object { val PASSWORD = "synthetic-mutation-password".toCharArray() }
}
