package com.shrikantbadwaik.landingkit.feature.kits.presentation.handoff

import androidx.activity.ComponentActivity
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertHeightIsAtLeast
import androidx.compose.ui.test.assertWidthIsAtLeast
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.shrikantbadwaik.landingkit.core.designsystem.LandingKitTheme
import com.shrikantbadwaik.landingkit.core.model.ArrivalCard
import com.shrikantbadwaik.landingkit.core.model.Trip
import com.shrikantbadwaik.landingkit.feature.kits.R
import java.time.LocalDate
import java.util.concurrent.atomic.AtomicBoolean
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class HandoffAccessibilityTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun presentation_shows_only_selected_content_and_obvious_exit() {
        val ended = AtomicBoolean(false)
        val setupTitle = composeRule.activity.getString(R.string.handoff_setup_header)
        val back = composeRule.activity.getString(R.string.navigate_back)
        val addressOption = composeRule.activity.getString(R.string.handoff_option_address)
        val contactOption = composeRule.activity.getString(R.string.handoff_option_stay_contact)

        composeRule.setContent {
            LandingKitTheme(darkTheme = true) {
                HandoffScreen(
                    trip = trip,
                    onBack = {},
                    onFinish = { ended.set(true) },
                )
            }
        }

        composeRule.onNode(
            SemanticsMatcher.expectValue(SemanticsProperties.PaneTitle, setupTitle),
        ).assertExists()
        composeRule.onNodeWithContentDescription(back)
            .assertHasClickAction()
            .assertWidthIsAtLeast(48.dp)
            .assertHeightIsAtLeast(48.dp)

        composeRule.onNodeWithText(addressOption).performScrollTo().performClick()
        composeRule.onNodeWithText(contactOption).performScrollTo().performClick()
        composeRule.onNodeWithText(
            composeRule.activity.getString(R.string.handoff_start_action),
        ).performScrollTo().performClick()

        composeRule.onNodeWithText(LOCAL_ADDRESS).assertExists()
        composeRule.onNodeWithText(ADDRESS).assertExists()
        composeRule.onNodeWithText(STAY_NAME).assertExists()
        composeRule.onNodeWithText(STAY_CONTACT).assertExists()
        composeRule.onNodeWithContentDescription(
            composeRule.activity.getString(R.string.handoff_end_action),
        ).assertHasClickAction().performClick()
        assertTrue(ended.get())
        assertEquals(
            0,
            composeRule.onAllNodesWithText(
                composeRule.activity.getString(R.string.handoff_setup_title),
            ).fetchSemanticsNodes().size,
        )
        assertEquals(0, composeRule.onAllNodesWithText(BOOKING_REFERENCE).fetchSemanticsNodes().size)
        assertEquals(0, composeRule.onAllNodesWithText(PRIVATE_NOTE).fetchSemanticsNodes().size)
    }

    @Test
    fun stay_address_is_selected_automatically_when_local_address_is_missing() {
        composeRule.setContent {
            LandingKitTheme {
                HandoffScreen(
                    trip = trip.copy(
                        arrivalCard = trip.arrivalCard.copy(localAddress = ""),
                    ),
                    onBack = {},
                    onFinish = {},
                )
            }
        }

        composeRule.onNodeWithText(
            composeRule.activity.getString(R.string.handoff_start_action),
        ).performScrollTo().performClick()

        composeRule.onNodeWithText(ADDRESS).assertExists()
        assertEquals(0, composeRule.onAllNodesWithText(LOCAL_ADDRESS).fetchSemanticsNodes().size)
    }

    @Test
    fun presentation_times_out_to_protected_content() {
        val ended = AtomicBoolean(false)

        composeRule.setContent {
            LandingKitTheme {
                HandoffScreen(
                    trip = trip,
                    onBack = {},
                    onFinish = { ended.set(true) },
                    handoffDurationMillis = 50L,
                )
            }
        }

        composeRule.onNodeWithText(
            composeRule.activity.getString(R.string.handoff_start_action),
        ).performScrollTo().performClick()
        composeRule.waitUntil(timeoutMillis = 2_000L) { ended.get() }
        assertTrue(ended.get())
    }

    private companion object {
        const val STAY_NAME = "Machiya Stay Gion"
        const val LOCAL_ADDRESS = "京都市東山区祇園町南側 570-123 2F"
        const val ADDRESS = "2F, 570-123 Gionmachi Minamigawa"
        const val STAY_CONTACT = "+81 75 555 1234"
        const val BOOKING_REFERENCE = "PRIVATE-BOOKING-4821"
        const val PRIVATE_NOTE = "PRIVATE TRANSFER NOTE"

        val trip = Trip(
            id = "handoff-trip",
            destination = "Kyoto, Japan",
            arrivalDate = LocalDate.of(2026, 8, 22),
            arrivalCard = ArrivalCard(
                accommodationName = STAY_NAME,
                address = ADDRESS,
                localAddress = LOCAL_ADDRESS,
                bookingReference = BOOKING_REFERENCE,
                transferInstructions = PRIVATE_NOTE,
                accommodationContact = STAY_CONTACT,
            ),
            items = emptyList(),
        )
    }
}
