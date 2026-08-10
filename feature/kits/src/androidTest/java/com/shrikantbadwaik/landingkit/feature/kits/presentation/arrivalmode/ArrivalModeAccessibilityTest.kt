package com.shrikantbadwaik.landingkit.feature.kits.presentation.arrivalmode

import androidx.activity.ComponentActivity
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertHeightIsAtLeast
import androidx.compose.ui.test.assertWidthIsAtLeast
import androidx.compose.ui.test.hasScrollAction
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performScrollToKey
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.shrikantbadwaik.landingkit.core.designsystem.LandingKitTheme
import com.shrikantbadwaik.landingkit.core.model.ArrivalCard
import com.shrikantbadwaik.landingkit.core.model.KitAttachment
import com.shrikantbadwaik.landingkit.core.model.Trip
import com.shrikantbadwaik.landingkit.feature.kits.R
import java.time.LocalDate
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ArrivalModeAccessibilityTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun screen_exposes_navigation_headings_and_attachment_action() {
        val screenTitle = composeRule.activity.getString(R.string.arrival_mode_header)
        val backDescription = composeRule.activity.getString(R.string.navigate_back)
        val openDescription = composeRule.activity.getString(
            R.string.attachment_open_description,
            ATTACHMENT_NAME,
        )

        composeRule.setContent {
            LandingKitTheme {
                ArrivalModeScreen(
                    trip = sampleTrip,
                    attachments = listOf(sampleAttachment),
                    onBack = {},
                    onPrepareAttachment = { _, _ -> },
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

        composeRule.onNode(
            SemanticsMatcher.expectValue(SemanticsProperties.PaneTitle, screenTitle),
        ).assertExists()
        composeRule.onNodeWithContentDescription(backDescription)
            .assertHasClickAction()
            .assertWidthIsAtLeast(48.dp)
            .assertHeightIsAtLeast(48.dp)
        composeRule.onNodeWithText(screenTitle).assert(isHeading)
        composeRule.onNodeWithText(LOCAL_ADDRESS).assert(isHeading)
        composeRule.onNodeWithText(
            composeRule.activity.getString(R.string.arrival_mode_booking),
        ).assert(isHeading)

        composeRule.onNode(hasScrollAction()).performScrollToKey("arrival_mode_transfer")
        composeRule.onNodeWithText(
            composeRule.activity.getString(R.string.arrival_mode_transfer),
        ).assert(isHeading)
        composeRule.onNode(hasScrollAction()).performScrollToKey("arrival_mode_attachment")
        composeRule.onNodeWithContentDescription(openDescription)
            .assertHasClickAction()
            .assert(isHeading)
        composeRule.onNode(hasScrollAction()).performScrollToKey("arrival_mode_emergency")
        composeRule.onNodeWithText(
            composeRule.activity.getString(R.string.arrival_mode_emergency),
        ).assert(isHeading)
    }

    private companion object {
        val isHeading = SemanticsMatcher.keyIsDefined(SemanticsProperties.Heading)
        const val LOCAL_ADDRESS = "京都市東山区祇園町南側 570-123 2F"
        const val ATTACHMENT_NAME = "Boarding pass.pdf"

        val sampleTrip = Trip(
            id = "trip",
            destination = "Kyoto, Japan",
            arrivalDate = LocalDate.of(2026, 8, 22),
            arrivalCard = ArrivalCard(
                accommodationName = "Machiya Stay Gion",
                address = "2F, 570-123 Gionmachi Minamigawa, Higashiyama-ku, Kyoto",
                localAddress = LOCAL_ADDRESS,
                bookingReference = "LK-4821",
                transferInstructions = "Take the train to Kyoto Station, then a taxi to Gion.",
                emergencyContact = "Japan emergency: 110 police · 119 ambulance/fire",
            ),
            items = emptyList(),
        )

        val sampleAttachment = KitAttachment(
            id = "attachment",
            tripId = sampleTrip.id,
            displayName = ATTACHMENT_NAME,
            mimeType = "application/pdf",
            sizeBytes = 1_024,
            createdAtMillis = 1L,
            isQuickAccess = true,
        )
    }
}
