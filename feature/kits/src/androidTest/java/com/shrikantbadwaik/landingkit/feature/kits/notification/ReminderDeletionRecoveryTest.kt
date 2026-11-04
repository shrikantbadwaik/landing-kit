package com.shrikantbadwaik.landingkit.feature.kits.notification

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import java.io.File
import java.io.IOException
import java.util.UUID
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ReminderDeletionRecoveryTest {
    private val context: Context = ApplicationProvider.getApplicationContext()

    @Test fun completedDeletionCancelsActualAlarmAndNotificationOnRestart() = runBlocking { fixture { f ->
        f.repository().deleteTrip(f.tripId)
        assertTrue(f.alarmExists(f.tripId)); assertTrue(f.posted(f.tripId))
        assertEquals(listOf(f.tripId), f.queue.pending().map { it.tripId })
        f.reopen()
        f.verifyRecovered()
        // A stale caller cannot resurrect either effect after durable cancellation.
        f.scheduler().sync(f.trip); f.scheduler().show(f.tripId)
        assertFalse(f.alarmExists(f.tripId)); assertFalse(f.posted(f.tripId))
    } }

    @Test fun databaseToQueueInterruptionUsesTheDeletionJournalToRecover() = runBlocking { fixture { f ->
        try { f.repository(f.recovery { throw ProcessDeath() }).deleteTrip(f.tripId)
            fail("Expected DB-to-queue termination") } catch (_: ProcessDeath) { }
        assertTrue(f.queue.pending().isEmpty())
        assertNull(f.database.tripDao().getTrip(f.tripId))
        assertTrue(f.alarmExists(f.tripId)); assertTrue(f.posted(f.tripId))
        f.reopen()
        f.verifyRecovered()
    } }

    @Test fun interruptionsDuringCancellationReplayUntilBothEffectsAreComplete() = runBlocking {
        for (stage in ReminderCancellationCheckpoint.entries) fixture { f ->
            f.repository().deleteTrip(f.tripId)
            val scheduler = f.scheduler()
            val processor = ReminderCancellationProcessor(f.queue, scheduler::cancelAlarm,
                scheduler::cancelNotification) { if (it == stage) throw ProcessDeath() }
            try { processor.drain(); fail("Expected $stage termination") } catch (_: ProcessDeath) { }
            assertEquals(stage == ReminderCancellationCheckpoint.ACKNOWLEDGED,
                f.queue.pending().isEmpty())
            f.reopen()
            f.verifyRecovered()
        }
    }

    @Test fun failedNotificationCancellationLeavesDurableRequestForRetry() = runBlocking { fixture { f ->
        f.repository().deleteTrip(f.tripId)
        val scheduler = f.scheduler()
        val processor = ReminderCancellationProcessor(f.queue, scheduler::cancelAlarm,
            { throw IOException("Synthetic notification-service failure") })
        try { processor.drain(); fail("Expected service failure") } catch (_: IOException) { }
        assertFalse(f.alarmExists(f.tripId))
        assertTrue(f.posted(f.tripId)); assertFalse(f.queue.pending().isEmpty())
        f.reopen()
        f.verifyRecovered()
    } }

    @Test fun restoredIdIsRescheduledOnlyAfterItsOldCancellationIsDrained() = runBlocking { fixture { f ->
        f.repository().deleteTrip(f.tripId)
        assertFalse(f.queue.pending().isEmpty())
        f.repository().saveTrip(f.trip)
        f.scheduler().reconcile()
        assertTrue(f.queue.pending().isEmpty())
        assertTrue(f.alarmExists(f.tripId))
        assertFalse(f.posted(f.tripId)) // Old notification removed; not automatically re-posted.
        assertTrue(f.posted(f.otherId))
    } }

    private suspend fun fixture(block: suspend (ReminderDeletionFixture) -> Unit) {
        android.os.ParcelFileDescriptor.AutoCloseInputStream(
            InstrumentationRegistry.getInstrumentation().uiAutomation.executeShellCommand(
                "pm grant ${context.packageName} android.permission.POST_NOTIFICATIONS"),
        ).use { it.readBytes() }
        val root = File(context.filesDir, "reminder-deletion-test-${UUID.randomUUID()}")
        val f = ReminderDeletionFixture(context, root)
        try { f.seed(); block(f) } finally {
            f.cleanupEffects(); f.close(); root.deleteRecursively()
        }
    }
    private class ProcessDeath : Error()
}
