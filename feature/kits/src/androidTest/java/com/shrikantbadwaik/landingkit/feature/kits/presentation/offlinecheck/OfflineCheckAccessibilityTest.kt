package com.shrikantbadwaik.landingkit.feature.kits.presentation.offlinecheck

import androidx.activity.ComponentActivity
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertHeightIsAtLeast
import androidx.compose.ui.test.assertWidthIsAtLeast
import androidx.compose.ui.test.hasScrollAction
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performScrollToKey
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.shrikantbadwaik.landingkit.core.designsystem.LandingKitTheme
import com.shrikantbadwaik.landingkit.core.model.ArrivalCard
import com.shrikantbadwaik.landingkit.core.model.KitAttachment
import com.shrikantbadwaik.landingkit.core.model.OfflineArrivalCheckResult
import com.shrikantbadwaik.landingkit.core.model.OfflineArrivalCheckStatus
import com.shrikantbadwaik.landingkit.core.model.OfflineArrivalContentFingerprint
import com.shrikantbadwaik.landingkit.core.model.PlanBCheckResult
import com.shrikantbadwaik.landingkit.core.model.PlanBContentFingerprint
import com.shrikantbadwaik.landingkit.core.model.Trip
import com.shrikantbadwaik.landingkit.feature.kits.OfflineArrivalCheckUiState
import com.shrikantbadwaik.landingkit.feature.kits.PlanBCheckUiState
import com.shrikantbadwaik.landingkit.feature.kits.R
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class OfflineCheckAccessibilityTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun ready_screen_exposes_pane_navigation_status_and_rehearsal_action() {
        val title = composeRule.activity.getString(R.string.offline_check_header)
        val back = composeRule.activity.getString(R.string.navigate_back)
        val ready = composeRule.activity.getString(R.string.offline_check_status_ready)
        val rehearse = composeRule.activity.getString(R.string.offline_check_rehearse_action)

        composeRule.setContent {
            LandingKitTheme(darkTheme = true) {
                OfflineCheckScreen(
                    trip = trip,
                    attachments = listOf(attachment),
                    state = OfflineArrivalCheckUiState.Complete(
                        tripId = trip.id,
                        result = OfflineArrivalCheckResult(
                            OfflineArrivalCheckStatus.ReadyOnThisPhone,
                            emptyList(),
                        ),
                        contentFingerprint = OfflineArrivalContentFingerprint.derive(
                            trip,
                            listOf(attachment),
                        ),
                    ),
                    planBState = PlanBCheckUiState.Idle,
                    onBack = {},
                    onRunCheck = {},
                    onRunPlanBCheck = {},
                    onEdit = {},
                    onManageEvidence = {},
                    onOpenArrivalMode = {},
                    attachmentProtectionEnabled = false,
                    isAttachmentAccessUnlocked = true,
                    pendingAttachmentAuthentication = null,
                    onPrepareAttachment = { _, _ -> },
                    onSetAttachmentProtection = {},
                    onRequestAttachmentAuthentication = {},
                    onFinishAttachmentAuthentication = {},
                    onUnlockAttachmentAccess = {},
                    onLockAttachmentAccess = {},
                )
            }
        }

        composeRule.onNode(
            SemanticsMatcher.expectValue(SemanticsProperties.PaneTitle, title),
        ).assertExists()
        composeRule.onNodeWithContentDescription(back)
            .assertHasClickAction()
            .assertWidthIsAtLeast(48.dp)
            .assertHeightIsAtLeast(48.dp)
        composeRule.onNodeWithText(ready).assertExists()
        composeRule.onNodeWithText(rehearse).assertHasClickAction()
        assertEquals(
            0,
            composeRule.onAllNodesWithText(
                composeRule.activity.getString(R.string.offline_check_run_again_action),
            ).fetchSemanticsNodes().size,
        )
        assertEquals(
            0,
            composeRule.onAllNodesWithText(
                composeRule.activity.getString(R.string.plan_b_status_not_checked),
            ).fetchSemanticsNodes().size,
        )
    }

    @Test
    fun configured_plan_b_exposes_independent_status_and_check_action() {
        val configuredTrip = trip.copy(
            arrivalCard = trip.arrivalCard.copy(
                fallbackTransport = "Taxi rank at arrivals",
            ),
        )

        composeRule.setContent {
            LandingKitTheme(darkTheme = true) {
                OfflineCheckScreen(
                    trip = configuredTrip,
                    attachments = listOf(attachment),
                    state = OfflineArrivalCheckUiState.Idle,
                    planBState = PlanBCheckUiState.Idle,
                    onBack = {},
                    onRunCheck = {},
                    onRunPlanBCheck = {},
                    onEdit = {},
                    onManageEvidence = {},
                    onOpenArrivalMode = {},
                    attachmentProtectionEnabled = false,
                    isAttachmentAccessUnlocked = true,
                    pendingAttachmentAuthentication = null,
                    onPrepareAttachment = { _, _ -> },
                    onSetAttachmentProtection = {},
                    onRequestAttachmentAuthentication = {},
                    onFinishAttachmentAuthentication = {},
                    onUnlockAttachmentAccess = {},
                    onLockAttachmentAccess = {},
                )
            }
        }

        composeRule.onNode(hasScrollAction()).performScrollToKey("offline_check_plan_b_status")
        composeRule.onNodeWithText(
            composeRule.activity.getString(R.string.plan_b_status_not_checked),
        ).assertExists()
        composeRule.onNode(hasScrollAction()).performScrollToKey("offline_check_plan_b_action")
        composeRule.onNodeWithText(
            composeRule.activity.getString(R.string.plan_b_run_action),
        ).assertHasClickAction()
    }

    @Test
    fun ready_plan_b_hides_check_again_action() {
        val configuredTrip = trip.copy(
            arrivalCard = trip.arrivalCard.copy(
                fallbackTransport = "Taxi rank at arrivals",
                accommodationContact = "+81 75 555 1234",
                lateArrivalInstructions = "Use the key safe",
            ),
        )

        composeRule.setContent {
            LandingKitTheme(darkTheme = true) {
                OfflineCheckScreen(
                    trip = configuredTrip,
                    attachments = listOf(attachment),
                    state = OfflineArrivalCheckUiState.Idle,
                    planBState = PlanBCheckUiState.Complete(
                        tripId = configuredTrip.id,
                        result = PlanBCheckResult(
                            OfflineArrivalCheckStatus.ReadyOnThisPhone,
                            emptyList(),
                        ),
                        contentFingerprint = PlanBContentFingerprint.derive(configuredTrip),
                    ),
                    onBack = {},
                    onRunCheck = {},
                    onRunPlanBCheck = {},
                    onEdit = {},
                    onManageEvidence = {},
                    onOpenArrivalMode = {},
                    attachmentProtectionEnabled = false,
                    isAttachmentAccessUnlocked = true,
                    pendingAttachmentAuthentication = null,
                    onPrepareAttachment = { _, _ -> },
                    onSetAttachmentProtection = {},
                    onRequestAttachmentAuthentication = {},
                    onFinishAttachmentAuthentication = {},
                    onUnlockAttachmentAccess = {},
                    onLockAttachmentAccess = {},
                )
            }
        }

        composeRule.onNode(hasScrollAction()).performScrollToKey("offline_check_plan_b_status")
        composeRule.onNodeWithText(
            composeRule.activity.getString(R.string.plan_b_status_ready),
        ).assertExists()
        assertEquals(
            0,
            composeRule.onAllNodesWithText(
                composeRule.activity.getString(R.string.offline_check_run_again_action),
            ).fetchSemanticsNodes().size,
        )
    }

    private val trip = Trip(
        id = "trip",
        destination = "Kyoto",
        arrivalDate = LocalDate.of(2026, 8, 22),
        arrivalCard = ArrivalCard("Machiya", "10 Quiet Street"),
        items = emptyList(),
    )
    private val attachment = KitAttachment(
        id = "ticket",
        tripId = "trip",
        displayName = "ticket.pdf",
        mimeType = "application/pdf",
        sizeBytes = 100,
        createdAtMillis = 1,
        isQuickAccess = true,
    )
}
