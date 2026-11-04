package com.shrikantbadwaik.landingkit.core.data.local.attachment

import android.content.Context
import android.net.Uri
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.shrikantbadwaik.landingkit.core.data.local.backup.BackupRecoveryStatus
import com.shrikantbadwaik.landingkit.core.data.local.backup.LocalBackupRepository
import com.shrikantbadwaik.landingkit.core.data.local.backup.BackupRestoreRecovery
import com.shrikantbadwaik.landingkit.core.data.local.repository.LocalChecklistTemplateRepository
import com.shrikantbadwaik.landingkit.core.data.preferences.UserPreferences
import java.io.ByteArrayInputStream
import java.io.File
import java.io.IOException
import java.util.UUID
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.json.JSONObject
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import com.shrikantbadwaik.landingkit.core.data.local.attachment.AttachmentMutationFixture.Companion.TRIP
import com.shrikantbadwaik.landingkit.core.data.local.attachment.AttachmentMutationFixture.Companion.OTHER
import com.shrikantbadwaik.landingkit.core.data.local.attachment.AttachmentMutationFixture.Companion.DOCUMENT
import com.shrikantbadwaik.landingkit.core.data.local.attachment.AttachmentMutationFixture.Companion.KEEP
import com.shrikantbadwaik.landingkit.core.data.local.attachment.AttachmentMutationFixture.Companion.OTHER_DOC
import com.shrikantbadwaik.landingkit.core.data.local.attachment.AttachmentMutationFixture.Companion.BYTES

@RunWith(AndroidJUnit4::class)
class AttachmentMutationRecoveryTest {
    private val context: Context = ApplicationProvider.getApplicationContext()

    @Test fun importRecoversEveryBoundaryWithoutOrphansOrLostPublishedFiles() = runBlocking {
        for (stage in AttachmentMutationCheckpoint.entries) fixture { f ->
            val recovery = f.recovery { if (it == stage) throw ProcessDeath() }
            try { f.attachments(recovery).importAttachment(TRIP, f.sourceUri); fail("Missed $stage") }
            catch (_: ProcessDeath) { }
            f.reopen()
            f.recovery().recover(); f.recovery().recover()
            val records = f.database.attachmentDao().getAttachments(TRIP)
            assertEquals(if (stage >= AttachmentMutationCheckpoint.METADATA_COMMITTED) 3 else 2,
                records.size)
            val files = File(f.root, "documents/$TRIP").listFiles()!!.filter { it.extension == "lka" }
            assertEquals(records.size, files.size)
            records.forEach { assertTrue(f.store.verifyDecryptable(TRIP, it.id)) }
            assertFalse(f.journal.exists())
            f.verifyOther()
        }
    }

    @Test fun documentDeletionCompletesEveryBoundaryAndKeepsOtherOwners() = runBlocking {
        for (stage in deletionStages) fixture { f ->
            val recovery = f.recovery { if (it == stage) throw ProcessDeath() }
            try { f.attachments(recovery).deleteAttachment(DOCUMENT); fail("Missed $stage") }
            catch (_: ProcessDeath) { }
            f.reopen()
            f.recovery().recover(); f.recovery().recover()
            assertNull(f.database.attachmentDao().getAttachment(DOCUMENT))
            assertFalse(f.store.exists(TRIP, DOCUMENT))
            assertNotNull(f.database.tripDao().getTrip(TRIP))
            assertEquals(true, f.database.attachmentDao().getAttachment(KEEP)?.isQuickAccess)
            assertTrue(f.store.verifyDecryptable(TRIP, KEEP))
            assertFalse(f.journal.exists())
            f.verifyOther()
        }
    }

    @Test fun removeAllDocumentsKeepsParentAndOtherTripAtEveryBoundary() = runBlocking {
        for (stage in deletionStages) fixture { f ->
            val recovery = f.recovery { if (it == stage) throw ProcessDeath() }
            try { f.attachments(recovery).deleteAttachments(TRIP); fail("Missed $stage") }
            catch (_: ProcessDeath) { }
            f.reopen()
            f.recovery().recover(); f.recovery().recover()
            assertTrue(f.database.attachmentDao().getAttachments(TRIP).isEmpty())
            assertFalse(File(f.root, "documents/$TRIP").exists())
            assertNotNull(f.database.tripDao().getTrip(TRIP))
            f.verifyOther()
        }
    }

    @Test fun tripDeletionCompletesEveryBoundaryWithForeignKeyCascades() = runBlocking {
        for (stage in deletionStages) fixture { f ->
            val recovery = f.recovery { if (it == stage) throw ProcessDeath() }
            try { f.trips(recovery).deleteTrip(TRIP); fail("Missed $stage") }
            catch (_: ProcessDeath) { }
            f.reopen()
            f.recovery().recover(); f.recovery().recover()
            assertNull(f.database.tripDao().getTrip(TRIP))
            assertTrue(f.database.attachmentDao().getAttachments(TRIP).isEmpty())
            assertFalse(File(f.root, "documents/$TRIP").exists())
            assertEquals(listOf(TRIP), f.reminderCancellations.pending().map { it.tripId })
            f.verifyOther()
        }
    }

    @Test fun deletionJournalBridgesTheDatabaseToReminderQueueGap() = runBlocking { fixture { f ->
        val mutation = AttachmentMutationRecovery(f.database, f.store, f.journal,
            reminderCancellations = f.reminderCancellations,
            afterTripMetadataDeleted = { throw ProcessDeath() })
        val recovery = BackupRestoreRecovery(context, f.database, f.store, UserPreferences(context),
            File(f.root, "restore.json"), File(f.root, "work"), f.access,
            f.reminderCancellations, mutation)
        try { f.trips(recovery).deleteTrip(TRIP); fail("Expected termination before enqueue") }
        catch (_: ProcessDeath) { }
        assertNull(f.database.tripDao().getTrip(TRIP))
        assertTrue(f.reminderCancellations.pending().isEmpty())
        assertTrue(f.journal.exists())
        f.reopen(); f.recovery().recover(); f.recovery().recover()
        assertEquals(listOf(TRIP), f.reminderCancellations.pending().map { it.tripId })
        assertFalse(f.journal.exists())
        assertFalse(File(f.root, "documents/$TRIP").exists())
        f.verifyOther()
    } }

    @Test fun failedFileDeletionBlocksAccessAndRetainsJournalUntilRetry() = runBlocking { fixture { f ->
        val directory = File(f.root, "documents/$TRIP")
        assertTrue(directory.setWritable(false, false))
        try {
            try { f.attachments(f.recovery()).deleteAttachment(DOCUMENT); fail("Expected cleanup failure") }
            catch (_: IOException) { }
            assertNull(f.database.attachmentDao().getAttachment(DOCUMENT))
            assertTrue(f.store.exists(TRIP, DOCUMENT))
            assertTrue(f.journal.exists())
            assertEquals(BackupRecoveryStatus.Failed, f.access.status.value)
            try { f.trips(f.recovery()).saveTrip(f.database.tripDao().getTrip(TRIP)!!.toTestTrip())
                fail("Expected access gate") }
            catch (_: IllegalStateException) { }
        } finally { assertTrue(directory.setWritable(true, false)) }
        f.reopen()
        f.recovery().recover()
        assertEquals(BackupRecoveryStatus.Ready, f.access.status.value)
        assertFalse(f.store.exists(TRIP, DOCUMENT)); assertFalse(f.journal.exists())
        assertTrue(f.store.verifyDecryptable(TRIP, KEEP)); f.verifyOther()
    } }

    @Test fun cancellationRollsBackUnpublishedImportAndRetainsPublishedImport() = runBlocking {
        for (stage in listOf(AttachmentMutationCheckpoint.DOCUMENT_WRITTEN,
                AttachmentMutationCheckpoint.METADATA_COMMITTED)) fixture { f ->
            try { f.attachments(f.recovery { if (it == stage) throw CancellationException() })
                .importAttachment(TRIP, f.sourceUri); fail("Expected cancellation") }
            catch (_: CancellationException) { }
            assertEquals(if (stage == AttachmentMutationCheckpoint.METADATA_COMMITTED) 3 else 2,
                f.database.attachmentDao().getAttachments(TRIP).size)
            assertFalse(f.journal.exists()); f.verifyOther()
        }
    }

    @Test fun malformedOrOversizedJournalFailsClosedBeforeDeletingAnyFile() = runBlocking { fixture { f ->
        for (text in listOf("broken", journal("trip", "../escape"),
                journal("trip", "$TRIP\u0000"), " ".repeat(1025))) {
            f.journal.writeText(text)
            try { f.recovery().recover(); fail("Expected invalid journal") } catch (_: Exception) { }
            assertEquals(text, f.journal.readText())
            assertNotNull(f.database.attachmentDao().getAttachment(DOCUMENT))
            assertTrue(f.store.verifyDecryptable(TRIP, DOCUMENT))
            f.verifyOther()
            f.journal.delete()
        }
    } }

    @Test fun unpublishedImportCleanupRemovesOnlyItsOwnedTemporaryFiles() = runBlocking { fixture { f ->
        val id = "unfinished-import"
        f.journal.writeText(journal("import", TRIP, id))
        for (suffix in listOf(".lka", ".lka.tmp", ".lkt", ".lkt.tmp"))
            File(f.root, "documents/$TRIP/$id$suffix").writeText("synthetic unfinished bytes")
        f.reopen()
        f.recovery().recover(); f.recovery().recover()
        assertFalse(f.journal.exists())
        assertFalse(File(f.root, "documents/$TRIP").listFiles()!!.any { it.name.startsWith(id) })
        assertTrue(f.store.verifyDecryptable(TRIP, KEEP)); f.verifyOther()
    } }

    @Test fun mismatchedDocumentOwnerFailsClosedWithoutUnlinkingEitherTrip() = runBlocking { fixture { f ->
        for (kind in listOf("import", "document")) {
            val record = journal(kind, TRIP, OTHER_DOC)
            f.journal.writeText(record)
            try { f.recovery().recover(); fail("Expected ownership refusal") } catch (_: Exception) { }
            assertEquals(record, f.journal.readText())
            assertTrue(f.store.verifyDecryptable(TRIP, DOCUMENT))
            assertNotNull(f.database.attachmentDao().getAttachment(DOCUMENT))
            f.verifyOther()
            f.journal.delete()
        }
    } }

    @Test fun importFailureAndMissingParentDoNotLeaveEncryptedCopies() = runBlocking { fixture { f ->
        val recovery = f.recovery()
        try { recovery.dataOperation {
            recovery.mutations.import(TRIP, "failed-import") {
                f.store.encrypt(TRIP, "failed-import", ByteArrayInputStream(BYTES))
                throw IOException("Synthetic metadata publication failure")
            }
        }; fail("Expected publication failure") } catch (_: IOException) { }
        assertFalse(f.store.exists(TRIP, "failed-import")); assertFalse(f.journal.exists())
        try { f.attachments(recovery).importAttachment("missing-trip", f.sourceUri)
            fail("Expected missing parent") } catch (_: IllegalArgumentException) { }
        assertFalse(f.journal.exists()); f.verifyOther()
    } }

    @Test fun atomicJournalFallbackCompletesDeletionExactlyOnce() = runBlocking { fixture { f ->
        val previous = journal("document", TRIP, DOCUMENT)
        File(f.journal.path + ".bak").writeText(previous)
        f.journal.writeText("interrupted newer write")
        f.reopen(); f.recovery().recover(); f.recovery().recover()
        assertNull(f.database.attachmentDao().getAttachment(DOCUMENT))
        assertFalse(f.store.exists(TRIP, DOCUMENT))
        assertFalse(File(f.journal.path + ".bak").exists())
        assertTrue(f.store.verifyDecryptable(TRIP, KEEP)); f.verifyOther()
    } }

    @Test fun backupSnapshotRetainsItsFilesWhileConcurrentEditAndDeleteWait() = runBlocking { fixture { f ->
        withTimeout(20_000) { coroutineScope {
            val recovery = f.recovery()
            val captured = CompletableDeferred<Unit>()
            val release = CompletableDeferred<Unit>()
            val backup = File(f.root, "snapshot.lkb")
            val password = "synthetic-snapshot-password".toCharArray()
            val repository = backup(f, recovery) { captured.complete(Unit); release.await() }
            val export = async { repository.exportBackup(Uri.fromFile(backup), password.copyOf()) }
            captured.await()
            val edit = async { f.trips(recovery).setItemComplete(TRIP, "$TRIP-item", true) }
            val deletion = async { f.trips(recovery).deleteTrip(TRIP) }
            try {
                delay(100)
                assertFalse(edit.isCompleted); assertFalse(deletion.isCompleted)
                assertNotNull(f.database.tripDao().getTrip(TRIP))
                assertTrue(f.store.verifyDecryptable(TRIP, DOCUMENT))
            } finally { release.complete(Unit) }
            assertEquals(3, export.await().attachmentCount)
            edit.await(); deletion.await()
            val target = AttachmentMutationFixture(context, File(f.root, "restored-snapshot"))
            try {
                val targetRecovery = target.recovery()
                backup(target, targetRecovery).restoreBackup(Uri.fromFile(backup), password.copyOf())
                assertEquals(false, target.database.tripDao().getTrip(TRIP)!!.items.single().isComplete)
                assertEquals(2, target.database.attachmentDao().getAttachments(TRIP).size)
                assertTrue(target.store.verifyDecryptable(TRIP, DOCUMENT))
                target.verifyOther()
            } finally { target.close() }
            f.verifyOther()
        } }
    } }

    private fun backup(f: AttachmentMutationFixture, recovery: BackupRestoreRecovery,
        snapshot: suspend () -> Unit = {}) = LocalBackupRepository(context, f.trips(recovery),
        LocalChecklistTemplateRepository(f.database.checklistTemplateDao(), f.access, recovery),
        f.database.attachmentDao(), f.store, UserPreferences(context), f.database, recovery,
        snapshotCaptured = snapshot)

    private suspend fun fixture(block: suspend (AttachmentMutationFixture) -> Unit) {
        val root = File(context.filesDir, "mutation-test-${UUID.randomUUID()}")
        val f = AttachmentMutationFixture(context, root)
        try { f.seed(); block(f) } finally { f.close(); root.deleteRecursively() }
    }
    private fun journal(kind: String, trip: String, id: String? = null) =
        JSONObject().put("version", 1).put("kind", kind).put("trip", trip)
            .put("document", id ?: JSONObject.NULL).toString()
    private fun com.shrikantbadwaik.landingkit.core.data.local.model.TripWithItems.toTestTrip() =
        com.shrikantbadwaik.landingkit.core.model.Trip(TRIP, trip.destination,
            java.time.LocalDate.of(2026, 12, 20),
            com.shrikantbadwaik.landingkit.core.model.ArrivalCard(
                accommodationName = "Synthetic hotel", address = "Synthetic address"), emptyList())
    private class ProcessDeath : Error()
    private val deletionStages = AttachmentMutationCheckpoint.entries.filter {
        it != AttachmentMutationCheckpoint.DOCUMENT_WRITTEN
    }
}
