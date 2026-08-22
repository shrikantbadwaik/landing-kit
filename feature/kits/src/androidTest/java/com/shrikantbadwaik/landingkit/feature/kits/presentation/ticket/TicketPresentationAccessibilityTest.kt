package com.shrikantbadwaik.landingkit.feature.kits.presentation.ticket

import android.graphics.Bitmap
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertHeightIsAtLeast
import androidx.compose.ui.test.assertWidthIsAtLeast
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.shrikantbadwaik.landingkit.core.data.PreparedAttachment
import com.shrikantbadwaik.landingkit.core.designsystem.LandingKitTheme
import com.shrikantbadwaik.landingkit.core.model.KitAttachment
import com.shrikantbadwaik.landingkit.feature.kits.R
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.CopyOnWriteArrayList
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class TicketPresentationAccessibilityTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun presenter_orders_attachments_and_restores_window_state() {
        val activity = composeRule.activity
        val exited = AtomicBoolean(false)
        val preparedAttachmentIds = CopyOnWriteArrayList<String>()
        val showingPresenter = mutableStateOf(true)
        val previousBrightness = activity.window.attributes.screenBrightness
        val previouslyKeptAwake = activity.window.attributes.flags
            .and(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON) != 0
        val image = activity.cacheDir.resolve("presenter-test.png")
        Bitmap.createBitmap(24, 36, Bitmap.Config.ARGB_8888).also { bitmap ->
            image.outputStream().use { output ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, output)
            }
            bitmap.recycle()
        }
        val attachment = KitAttachment(
            id = "presenter-test",
            tripId = "trip",
            displayName = "Ticket.png",
            mimeType = "image/png",
            sizeBytes = image.length(),
            createdAtMillis = 1_786_738_500_000L,
            isQuickAccess = true,
        )
        val newerAttachment = attachment.copy(
            id = "newer-presenter-test",
            createdAtMillis = attachment.createdAtMillis + 86_400_000L,
            isQuickAccess = false,
        )

        composeRule.setContent {
            LandingKitTheme(darkTheme = true) {
                if (showingPresenter.value) {
                    TicketPresentationScreen(
                        attachments = listOf(newerAttachment, attachment),
                        initialAttachmentId = newerAttachment.id,
                        attachmentProtectionEnabled = false,
                        isAttachmentAccessUnlocked = true,
                        onPrepareAttachment = { attachmentId, complete ->
                            preparedAttachmentIds += attachmentId
                            complete(Result.success(PreparedAttachment(image, "image/png")))
                        },
                        onExit = {
                            exited.set(true)
                            showingPresenter.value = false
                        },
                    )
                }
            }
        }

        val exitDescription = activity.getString(R.string.ticket_presenter_exit_description)
        composeRule.onNodeWithContentDescription(exitDescription)
            .assertHasClickAction()
            .assertWidthIsAtLeast(48.dp)
            .assertHeightIsAtLeast(48.dp)
        composeRule.onNodeWithText(
            activity.getString(R.string.ticket_presenter_position, 1, 2),
        ).assertExists()
        composeRule.onNodeWithContentDescription(
            activity.getString(R.string.ticket_presenter_next_description),
        ).performClick()
        composeRule.waitUntil { preparedAttachmentIds.size >= 2 }
        assertEquals(attachment.id, preparedAttachmentIds[0])
        assertEquals(newerAttachment.id, preparedAttachmentIds[1])
        composeRule.onNodeWithText(
            activity.getString(R.string.ticket_presenter_position, 2, 2),
        ).assertExists()
        composeRule.runOnIdle {
            assertEquals(1f, activity.window.attributes.screenBrightness)
            assertTrue(
                activity.window.attributes.flags
                    .and(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON) != 0,
            )
        }

        composeRule.onNodeWithContentDescription(exitDescription).performClick()
        assertTrue(exited.get())
        composeRule.runOnIdle {
            assertEquals(previousBrightness, activity.window.attributes.screenBrightness)
            val keepsAwake = activity.window.attributes.flags
                .and(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON) != 0
            if (previouslyKeptAwake) assertTrue(keepsAwake) else assertFalse(keepsAwake)
        }
    }
}
