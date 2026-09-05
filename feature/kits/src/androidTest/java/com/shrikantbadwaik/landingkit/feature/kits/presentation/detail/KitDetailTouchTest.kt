package com.shrikantbadwaik.landingkit.feature.kits.presentation.detail

import androidx.activity.ComponentActivity
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.click
import androidx.compose.ui.test.hasScrollAction
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performScrollToKey
import androidx.compose.ui.test.performSemanticsAction
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeUp
import com.shrikantbadwaik.landingkit.core.designsystem.LandingKitTheme
import com.shrikantbadwaik.landingkit.core.model.ArrivalCard
import com.shrikantbadwaik.landingkit.core.model.ArrivalItem
import com.shrikantbadwaik.landingkit.core.model.Trip
import com.shrikantbadwaik.landingkit.feature.kits.R
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class KitDetailTouchTest {
    @get:Rule val composeRule = createAndroidComposeRule<ComponentActivity>()

    private val trip = mutableStateOf(
        Trip(
            destination = "Kyoto, Japan",
            arrivalDate = LocalDate.now().minusDays(1),
            arrivalCard = ArrivalCard(
                accommodationName = "Machiya Stay",
                address = "Gion, Kyoto",
                transferInstructions = "Take the train, then a taxi.",
                currencyNote = "Keep some yen handy.",
                emergencyContact = "Local emergency contact",
                dietaryPhrase = "A checked dietary phrase",
            ),
            items = listOf(ArrivalItem(id = TASK_ID, title = TASK_TITLE)) +
                List(12) { ArrivalItem(title = "Preparation task ${it + 1}") },
        ),
    )
    private var arrivalOpens = 0
    private var edits = 0
    private var checks = 0

    @Test
    fun checklist_text_and_checkbox_toggle_once_without_opening_covered_hero() {
        showDetail()
        val heroCenter = composeRule.onNodeWithText(
            composeRule.activity.getString(R.string.arrival_mode_action),
        ).fetchSemanticsNode().boundsInRoot.center

        alignPanelContentWithHero(TASK_ID, TASK_TITLE, heroCenter.y)
        // Use a real pointer event: a semantics click bypasses overlapping hit testing.
        tapInRoot(heroCenter)
        composeRule.runOnIdle {
            assertTrue(trip.value.items.first().isComplete)
            assertEquals(1, checks)
            assertEquals(0, arrivalOpens)
            assertEquals(0, edits)
        }

        val row = composeRule.onNodeWithText(TASK_TITLE).fetchSemanticsNode().boundsInRoot
        // The checkbox occupies the leading 48dp touch target of the merged row.
        tapInRoot(
            Offset(row.left + 24 * composeRule.activity.resources.displayMetrics.density, row.center.y),
        )
        composeRule.runOnIdle {
            assertTrue(!trip.value.items.first().isComplete)
            assertEquals(2, checks)
            assertEquals(0, arrivalOpens)
            assertEquals(0, edits)
        }
    }

    @Test
    fun panel_heading_blocks_hidden_hero_but_still_allows_scrolling() {
        showDetail()
        val heroCenter = composeRule.onNodeWithText(
            composeRule.activity.getString(R.string.arrival_mode_action),
        ).fetchSemanticsNode().boundsInRoot.center
        // The visible hero remains tappable through the list's transparent top padding.
        tapInRoot(heroCenter)
        composeRule.runOnIdle { assertEquals(1, arrivalOpens) }

        val heading = composeRule.activity.getString(R.string.detail_arrival_checklist)
        alignPanelContentWithHero("detail_checklist_header", heading, heroCenter.y)
        tapInRoot(heroCenter)
        composeRule.runOnIdle {
            assertEquals(1, arrivalOpens)
            assertEquals(0, edits)
            assertEquals(0, checks)
        }

        val before = composeRule.onNodeWithText(heading).fetchSemanticsNode().boundsInRoot.top
        composeRule.onNode(hasScrollAction()).performTouchInput {
            swipeUp(startY = heroCenter.y, endY = heroCenter.y - 180f, durationMillis = 400)
        }
        composeRule.waitForIdle()
        val after = composeRule.onNodeWithText(heading).fetchSemanticsNode().boundsInRoot.top
        assertTrue("Dragging on the panel must still scroll it", after < before - 50f)
        composeRule.runOnIdle { assertEquals(1, arrivalOpens) }
    }

    private fun tapInRoot(position: Offset) {
        val rootOrigin = composeRule.onRoot().fetchSemanticsNode().boundsInRoot.topLeft
        composeRule.onRoot().performTouchInput { click(position - rootOrigin) }
    }

    private fun alignPanelContentWithHero(key: String, text: String, targetY: Float) {
        composeRule.onNode(hasScrollAction()).performScrollToKey(key)
        val centerY = composeRule.onNodeWithText(text).fetchSemanticsNode().boundsInRoot.center.y
        composeRule.onNode(hasScrollAction()).performSemanticsAction(SemanticsActions.ScrollBy) {
            it(0f, centerY - targetY)
        }
        composeRule.waitForIdle()
        assertEquals(
            "Test content must overlap the hidden hero action",
            targetY,
            composeRule.onNodeWithText(text).fetchSemanticsNode().boundsInRoot.center.y,
            3f,
        )
    }

    private fun showDetail() {
        composeRule.setContent {
            LandingKitTheme(darkTheme = false) {
                KitDetailScreen(
                    trip = trip.value,
                    attachments = emptyList(),
                    onBack = {},
                    onArrivalMode = { arrivalOpens++ },
                    onOfflineCheck = {},
                    onPrintableSummary = {},
                    onShareSummary = {},
                    onEdit = { edits++ },
                    onDelete = {},
                    onCheck = { item, complete ->
                        checks++
                        trip.value = trip.value.copy(
                            items = trip.value.items.map {
                                if (it.id == item.id) it.copy(isComplete = complete) else it
                            },
                        )
                    },
                    onImportAttachments = { _, _ -> },
                    onLoadAttachmentThumbnail = { null },
                    onPrepareAttachment = { _, _ -> },
                    onDeleteAttachment = { _, _ -> },
                    onSetQuickAccessAttachment = { _, _ -> },
                    onSetArrivalReminder = { _, _ -> },
                    onDeleteAttachments = {},
                    attachmentProtectionEnabled = false,
                    isAttachmentAccessUnlocked = true,
                    pendingAttachmentAuthentication = null,
                    onSetAttachmentProtection = {},
                    onRequestAttachmentAuthentication = {},
                    onFinishAttachmentAuthentication = {},
                    onUnlockAttachmentAccess = {},
                    onLockAttachmentAccess = {},
                )
            }
        }
    }

    private companion object {
        const val TASK_ID = "detail-touch-task"
        const val TASK_TITLE = "Download tickets and booking"
    }
}
