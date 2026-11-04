package com.shrikantbadwaik.landingkit.core.data.local.attachment

import android.content.Context
import android.net.Uri
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import java.io.File
import java.util.UUID
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AttachmentPreviewSessionsTest {
    private val context = ApplicationProvider.getApplicationContext<Context>()
    private val root = File(context.cacheDir, "preview-session-test-${UUID.randomUUID()}")
    private var grantActive = false
    private val uri = Uri.parse("content://test/session")

    private fun sessions() = AttachmentPreviewSessions(
        context = context,
        root = root,
        hasGrant = { candidate, uid -> grantActive && candidate == uri && uid == 12345 },
        legacyRecipients = { emptyList() },
    )

    @After
    fun cleanUp() {
        grantActive = false
        root.deleteRecursively()
    }

    @Test
    fun concurrent_copies_and_thumbnail_release_do_not_replace_a_viewing_copy() {
        val sessions = sessions()
        val viewer = sessions.create("trip-ticket", "img") { it.writeText("viewing") }
        val thumbnail = sessions.create("trip-ticket", "img") { it.writeText("thumbnail") }
        assertNotEquals(viewer, thumbnail)
        sessions.release(thumbnail)
        assertFalse(thumbnail.exists())
        assertEquals("viewing", viewer.readText())
        sessions.release(viewer)
        assertFalse(viewer.exists())
    }

    @Test
    fun background_or_authentication_lock_removes_internal_copies() {
        val sessions = sessions()
        val first = sessions.create("trip-ticket", "pdf") { it.writeText("first") }
        val second = sessions.create("trip-ticket", "pdf") { it.writeText("second") }
        sessions.endSession()
        assertFalse(first.exists())
        assertFalse(second.exists())
    }

    @Test
    fun session_end_during_decryption_does_not_publish_a_late_copy() {
        val sessions = sessions()
        val result = runCatching {
            sessions.create("trip-ticket", "img") {
                it.writeText("plaintext")
                sessions.endSession()
            }
        }
        assertTrue(result.isFailure)
        assertTrue(root.listFiles().orEmpty().isEmpty())
    }

    @Test
    fun restart_cleans_internal_copies_and_incomplete_plaintext() {
        val sessions = sessions()
        val orphan = sessions.create("trip-ticket", "pdf") { it.writeText("orphan") }
        val incomplete = File(root, "interrupted.tmp").apply { writeText("partial") }
        sessions()
        assertFalse(orphan.exists())
        assertFalse(incomplete.exists())
    }

    @Test
    fun active_external_grant_survives_lock_release_and_restart_then_is_cleaned() {
        val sessions = sessions()
        val file = sessions.create("trip-ticket", "img") { it.writeText("external") }
        sessions.retainExternally(file, uri, listOf(12345))
        grantActive = true
        sessions.release(file)
        sessions.endSession()
        assertEquals("external", file.readText())
        val restarted = sessions()
        assertEquals("external", file.readText())
        grantActive = false
        restarted.cleanInactive()
        assertFalse(file.exists())
        assertTrue(root.listFiles().orEmpty().isEmpty())
    }

    @Test
    fun failed_external_launch_and_repeated_release_leave_no_copy_or_journal() {
        val sessions = sessions()
        val file = sessions.create("trip-ticket", "pdf") { it.writeText("external") }
        sessions.retainExternally(file, uri, listOf(12345))
        sessions.release(file)
        sessions.release(file)
        assertTrue(root.listFiles().orEmpty().isEmpty())
    }

    @Test
    fun malformed_journal_without_an_active_grant_does_not_keep_plaintext_forever() {
        val sessions = sessions()
        val file = sessions.create("trip-ticket", "pdf") { it.writeText("external") }
        File(root, "${file.name}.lease").writeText("{interrupted")
        sessions()
        assertFalse(file.exists())
        assertTrue(root.listFiles().orEmpty().isEmpty())
    }
}
