package com.shrikantbadwaik.landingkit.core.data.local.backup

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.shrikantbadwaik.landingkit.core.data.local.mapper.toEntity
import com.shrikantbadwaik.landingkit.core.data.preferences.UserPreferences
import com.shrikantbadwaik.landingkit.core.data.local.repository.LocalTripRepository
import com.shrikantbadwaik.landingkit.core.model.ArrivalItem
import java.io.File
import java.io.IOException
import java.util.UUID
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeoutOrNull
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.json.JSONArray
import org.json.JSONObject

@RunWith(AndroidJUnit4::class)
class BackupRestoreRecoveryTest {
    private val context: Context = ApplicationProvider.getApplicationContext()

    @Test fun everyCheckpointRecoversAfterReopeningDiskDatabase() = runBlocking {
        for (stage in RestoreCheckpoint.entries) fixture { f ->
            try {
                f.repository { if (it == stage) throw SimulatedProcessDeath() }
                    .restoreBackup(f.uri, RestoreTestFixture.PASSWORD.copyOf())
                fail("Did not interrupt at $stage")
            } catch (_: SimulatedProcessDeath) { }
            f.reopen()
            val committed = stage >= RestoreCheckpoint.COMMITTED
            f.recovery.recover()
            f.recovery.recover() // Repeated startup does not alter either outcome.
            f.verifyExisting()
            f.verifyRestored(committed)
            assertFalse(File(f.root, "restore.json").exists())
            assertFalse(File(f.root, "work").exists())
            val retry = f.repository().restoreBackup(f.uri, RestoreTestFixture.PASSWORD.copyOf())
            assertEquals(if (committed) 0 else 1, retry.restoredTripIds.size)
            f.verifyRestored(true)
            f.verifyExisting()
        }
    }

    @Test fun cancellationUsesNonCancellableRecovery() = runBlocking { fixture { f ->
        try {
            f.repository { if (it == RestoreCheckpoint.DOCUMENT_WRITTEN) throw CancellationException() }
                .restoreBackup(f.uri, RestoreTestFixture.PASSWORD.copyOf())
            fail("Expected cancellation")
        } catch (_: CancellationException) { }
        f.verifyExisting(); f.verifyRestored(false)
        assertFalse(File(f.root, "restore.json").exists())
    } }

    @Test fun metadataFailureCannotReplaceAnExistingChecklistItem() = runBlocking { fixture { f ->
        // A hostile/colliding backup item ID must abort, never REPLACE existing task content.
        f.target.tripDao().insertRestoredItems(listOf(ArrivalItem(
            id = "${RestoreTestFixture.RESTORED}-item", title = "Existing important task")
            .toEntity(RestoreTestFixture.EXISTING, 1)))
        try {
            f.repository().restoreBackup(f.uri, RestoreTestFixture.PASSWORD.copyOf())
            fail("Expected primary-key conflict")
        } catch (_: Exception) { }
        f.verifyExisting(); f.verifyRestored(false)
        assertTrue(f.target.tripDao().getTrip(RestoreTestFixture.EXISTING)!!.items.any {
            it.title == "Existing important task"
        })
    } }

    @Test fun orphanDirectoryIsRefusedWithoutOverwritingOrDeletingItsFiles() = runBlocking { fixture { f ->
        val orphan = File(f.root, "target-documents/${RestoreTestFixture.RESTORED}/keep.lka")
        orphan.parentFile!!.mkdirs(); orphan.writeText("unclaimed evidence")
        try {
            f.repository().restoreBackup(f.uri, RestoreTestFixture.PASSWORD.copyOf())
            fail("Expected occupied directory refusal")
        } catch (_: Exception) { }
        assertEquals("unclaimed evidence", orphan.readText())
        f.verifyExisting()
        assertNull(f.target.tripDao().getTrip(RestoreTestFixture.RESTORED))
        assertFalse(File(f.root, "restore.json").exists())
    } }

    @Test fun failedFilesystemRollbackRetainsJournalForRetry() = runBlocking { fixture { f ->
        val directory = File(f.root, "target-documents/${RestoreTestFixture.RESTORED}")
        try {
            f.repository { if (it == RestoreCheckpoint.DOCUMENT_WRITTEN) throw SimulatedProcessDeath() }
                .restoreBackup(f.uri, RestoreTestFixture.PASSWORD.copyOf())
        } catch (_: SimulatedProcessDeath) { }
        assertTrue(directory.setWritable(false, false))
        try {
            try { f.recovery.recover(); fail("Expected filesystem failure") } catch (_: IOException) { }
            assertTrue(File(f.root, "restore.json").exists())
        } finally { directory.setWritable(true, false) }
        f.reopen()
        f.recovery.recover(); f.verifyExisting(); f.verifyRestored(false)
        assertFalse(File(f.root, "restore.json").exists())
    } }

    @Test fun malformedJournalFailsClosedAndPreservesExistingContent() = runBlocking { fixture { f ->
        val journal = File(f.root, "restore.json"); journal.writeText("broken journal")
        try { f.recovery.recover(); fail("Expected recovery error") } catch (_: Exception) { }
        assertEquals("broken journal", journal.readText())
        f.verifyExisting()
    } }

    @Test fun startupRemovesAbandonedPlaintextWorkAndLegacyPayloads() = runBlocking { fixture { f ->
        val work = File(f.root, "work").apply { mkdirs() }
        File(work, "restored-payload.tmp").writeText("synthetic plaintext")
        val legacy = File(context.cacheDir, "restored-payload-fixture.tmp")
        legacy.writeText("synthetic plaintext")
        f.recovery.recover()
        assertFalse(work.exists()); assertFalse(legacy.exists()); f.verifyExisting()
    } }

    @Test fun interruptedAtomicCommitUsesPreviousJournalAndRollsBack() = runBlocking { fixture { f ->
        try {
            f.repository { if (it == RestoreCheckpoint.DATABASE_COMMITTED) throw SimulatedProcessDeath() }
                .restoreBackup(f.uri, RestoreTestFixture.PASSWORD.copyOf())
        } catch (_: SimulatedProcessDeath) { }
        val journal = File(f.root, "restore.json")
        journal.copyTo(File(f.root, "restore.json.bak"))
        journal.writeText(journal.readText().replace("\"committed\":false", "\"committed\":true"))
        f.reopen(); f.recovery.recover()
        f.verifyExisting(); f.verifyRestored(false)
        assertFalse(File(f.root, "restore.json.bak").exists())
    } }

    @Test fun failedStartupBlocksAccessUntilRecoverySucceeds() = runBlocking { fixture { f ->
        val access = BackupRecoveryAccess(initiallyReady = false)
        val recovery = BackupRestoreRecovery(context, f.target, f.targetStore, f.preferences,
            File(f.root, "restore.json"), File(f.root, "work"), access)
        val trips = LocalTripRepository(f.target.tripDao(), f.targetStore, access, recovery)
        File(f.root, "restore.json").writeText("broken journal")
        try { recovery.recover(); fail("Expected failure") } catch (_: Exception) { }
        assertFalse(access.isReady)
        assertEquals(BackupRecoveryStatus.Failed, access.status.value)
        try { trips.deleteTrip(RestoreTestFixture.EXISTING); fail("Expected blocked mutation") }
        catch (_: IllegalStateException) { }
        assertNull(withTimeoutOrNull(100) { trips.observeTrips().first() })
        File(f.root, "restore.json").delete()
        recovery.recover()
        assertTrue(access.isReady)
        assertEquals(BackupRecoveryStatus.Ready, access.status.value)
        assertEquals(RestoreTestFixture.EXISTING, trips.observeTrips().first().single().id)
        f.verifyExisting()
    } }

    @Test fun invalidIdsAreRejectedBeforeJournalCreation() = runBlocking { fixture { f ->
        val invalid = listOf("", ".", "..", "contains space", "a/b", "a\\b",
            "a\u0000b", "a\nb", "a\tb", "a\u007Fb", "京都", "a".repeat(65))
        for (id in invalid) {
            for (template in listOf(false, true)) {
                try {
                    f.recovery.begin(if (template) emptyList() else listOf(id),
                        if (template) listOf(id) else emptyList(), false)
                    fail("Invalid ID accepted")
                } catch (_: IllegalArgumentException) { }
                assertFalse(File(f.root, "restore.json").exists())
                assertFalse(File(f.root, "restore.json.new").exists())
                assertFalse(File(f.root, "restore.json.bak").exists())
                f.verifyExisting()
            }
        }
        f.recovery.begin(listOf("a".repeat(64), UUID.randomUUID().toString()),
            listOf("curated_international_arrival", "Mixed-Case.ID_1"), false)
        f.recovery.recover()
        f.verifyExisting()
    } }

    @Test fun invalidJournalIdCannotDeleteAnEarlierValidTarget() = runBlocking { fixture { f ->
        val file = File(f.root, "restore.json")
        for (id in listOf("a\u0000b", "a\nb", "a".repeat(65), "../escape")) {
            val record = JSONObject().put("version", 1)
                .put("trips", JSONArray(listOf(RestoreTestFixture.EXISTING, id)))
                .put("templates", JSONArray()).put("protection", false).put("committed", false)
            file.writeText(record.toString())
            try { f.recovery.recover(); fail("Invalid journal accepted") }
            catch (_: IllegalArgumentException) { }
            assertTrue(file.exists())
            f.verifyExisting()
        }
    } }

    @Test fun recoveryStateReportsInProgressWithoutAllowingRepositoryAccess() {
        val access = BackupRecoveryAccess(initiallyReady = false)
        assertEquals(BackupRecoveryStatus.Recovering, access.status.value)
        assertFalse(access.isReady)
        access.blocked()
        assertEquals(BackupRecoveryStatus.Failed, access.status.value)
        access.recovering()
        assertEquals(BackupRecoveryStatus.Recovering, access.status.value)
        assertFalse(access.isReady)
        access.ready()
        assertEquals(BackupRecoveryStatus.Ready, access.status.value)
    }

    private suspend fun fixture(block: suspend (RestoreTestFixture) -> Unit) {
        val previous = UserPreferences(context).attachmentProtectionEnabled.first()
        val root = File(context.filesDir, "restore-fixture-${UUID.randomUUID()}")
        val f = RestoreTestFixture(context, root)
        try { f.seed(); block(f) } finally {
            f.close(); root.deleteRecursively()
            UserPreferences(context).setAttachmentProtectionEnabled(previous)
        }
    }
    private class SimulatedProcessDeath : Error()
}
