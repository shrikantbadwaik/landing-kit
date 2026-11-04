package com.shrikantbadwaik.landingkit.core.data.local.reminder

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import java.io.File
import java.util.UUID
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ReminderCancellationStoreTest {
    private val context: Context = ApplicationProvider.getApplicationContext()

    @Test fun queuedIdentitySurvivesReopenAndIdempotentEnqueue() = runBlocking { fixture { file ->
        val store = ReminderCancellationStore(file)
        store.enqueue("deleted-trip")
        val request = store.pending().single()
        store.enqueue("deleted-trip")
        val reopened = ReminderCancellationStore(file)
        assertEquals(listOf(request), reopened.pending())
        reopened.acknowledge(request)
        assertTrue(ReminderCancellationStore(file).pending().isEmpty())
        assertFalse(file.readText().contains("deleted-trip"))
    } }

    @Test fun staleAcknowledgementCannotEraseALaterDeletionOfTheSameId() = runBlocking { fixture { file ->
        val store = ReminderCancellationStore(file)
        store.enqueue("restored-trip")
        val previous = store.pending().single()
        store.acknowledge(previous)
        store.enqueue("restored-trip")
        val current = store.pending().single()
        assertNotEquals(previous.token, current.token)
        store.acknowledge(previous)
        assertEquals(listOf(current), store.pending())
    } }

    @Test fun interruptedAtomicAcknowledgementReplaysThePreviousRequest() = runBlocking { fixture { file ->
        val store = ReminderCancellationStore(file)
        store.enqueue("deleted-trip")
        val request = store.pending().single()
        file.copyTo(File(file.path + ".bak"))
        file.writeText("""{"version":1,"requests":[]}""")
        assertEquals(listOf(request), ReminderCancellationStore(file).pending())
        store.acknowledge(request)
        assertTrue(ReminderCancellationStore(file).pending().isEmpty())
    } }

    @Test fun batchAcknowledgementPreservesRequestsAddedDuringCancellation() = runBlocking { fixture { file ->
        val store = ReminderCancellationStore(file)
        store.enqueue("first-trip"); store.enqueue("second-trip")
        val cancelling = store.pending()
        store.enqueue("later-trip")
        store.acknowledgeAll(cancelling)
        assertEquals(listOf("later-trip"), ReminderCancellationStore(file).pending().map { it.tripId })
    } }

    @Test fun invalidIdsAndMalformedRecordsCannotReplacePendingWork() = runBlocking { fixture { file ->
        val store = ReminderCancellationStore(file)
        store.enqueue("keep-trip")
        val request = store.pending().single()
        for (id in listOf("", ".", "..", "../escape", "a\u0000b", "x".repeat(65))) {
            try { store.enqueue(id); fail("Expected invalid ID") } catch (_: IllegalArgumentException) { }
            assertEquals(listOf(request), store.pending())
        }
        file.writeText("corrupt cancellation queue")
        try { store.pending(); fail("Expected malformed record") } catch (_: Exception) { }
        assertEquals("corrupt cancellation queue", file.readText())
    } }

    private suspend fun fixture(block: suspend (File) -> Unit) {
        val root = File(context.filesDir, "reminder-queue-test-${UUID.randomUUID()}").apply { mkdirs() }
        try { block(File(root, "pending.json")) } finally { root.deleteRecursively() }
    }
}
