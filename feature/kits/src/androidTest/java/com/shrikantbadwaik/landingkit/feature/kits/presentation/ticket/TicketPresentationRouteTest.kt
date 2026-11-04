package com.shrikantbadwaik.landingkit.feature.kits.presentation.ticket

import androidx.activity.ComponentActivity
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.shrikantbadwaik.landingkit.core.designsystem.LandingKitTheme
import com.shrikantbadwaik.landingkit.core.model.KitAttachment
import com.shrikantbadwaik.landingkit.feature.kits.R
import java.util.concurrent.CopyOnWriteArrayList
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class TicketPresentationRouteTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    private val attachments = mutableStateOf<List<KitAttachment>?>(null)
    private val tripExists = mutableStateOf(true)
    private val preparedIds = CopyOnWriteArrayList<String>()
    private var homeActions = 0

    @Test
    fun first_empty_emission_replaces_loading_with_actionable_recovery() {
        showRoute()
        text(R.string.loading_offline_kit).assertExists()
        composeRule.runOnIdle { attachments.value = emptyList() }
        text(R.string.loading_offline_kit).assertDoesNotExist()
        text(R.string.missing_ticket_title).assertExists()
        text(R.string.missing_kit_home_action).performClick()
        composeRule.runOnIdle {
            assertEquals(1, homeActions)
            assertEquals(emptyList<String>(), preparedIds)
        }
    }

    @Test
    fun missing_target_does_not_open_a_different_quick_access_document() {
        attachments.value = listOf(document("another-ticket", quickAccess = true))
        showRoute()
        text(R.string.missing_ticket_title).assertExists()
        composeRule.runOnIdle { assertEquals(emptyList<String>(), preparedIds) }
    }

    @Test
    fun deleting_selected_document_exits_presenter_to_recovery() {
        attachments.value = listOf(document("requested-ticket"), document("another-ticket"))
        showRoute()
        composeRule.waitUntil { preparedIds.isNotEmpty() }
        composeRule.runOnIdle {
            assertEquals(listOf("requested-ticket"), preparedIds)
            attachments.value = listOf(document("another-ticket"))
        }
        text(R.string.missing_ticket_title).assertExists()
        composeRule.runOnIdle { assertEquals(listOf("requested-ticket"), preparedIds) }
    }

    @Test
    fun missing_trip_recovers_even_before_attachment_emission() {
        tripExists.value = false
        showRoute()
        text(R.string.missing_kit_title).assertExists()
        text(R.string.loading_offline_kit).assertDoesNotExist()
        text(R.string.missing_kit_home_action).performClick()
        composeRule.runOnIdle {
            assertEquals(1, homeActions)
            assertEquals(emptyList<String>(), preparedIds)
        }
    }

    private fun showRoute() {
        composeRule.setContent {
            LandingKitTheme {
                TicketPresentationRouteContent(
                    tripExists = tripExists.value,
                    attachments = attachments.value,
                    initialAttachmentId = "requested-ticket",
                    attachmentProtectionEnabled = false,
                    isAttachmentAccessUnlocked = true,
                    onPrepareAttachment = { id, complete ->
                        preparedIds += id
                        complete(Result.failure(IllegalStateException("Test file unavailable")))
                    },
                    onExit = {},
                    onHome = { homeActions++ },
                )
            }
        }
    }

    private fun text(resource: Int) =
        composeRule.onNodeWithText(composeRule.activity.getString(resource))

    private fun document(id: String, quickAccess: Boolean = false) = KitAttachment(
        id = id,
        tripId = "trip",
        displayName = "$id.png",
        mimeType = "image/png",
        sizeBytes = 100L,
        createdAtMillis = 1L,
        isQuickAccess = quickAccess,
    )
}
