package com.shrikantbadwaik.landingkit.feature.kits.presentation.detail.component

import androidx.activity.ComponentActivity
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performSemanticsAction
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.unit.Density
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.shrikantbadwaik.landingkit.core.designsystem.LandingKitTheme
import com.shrikantbadwaik.landingkit.feature.kits.R
import org.junit.Assert.assertFalse
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AttachmentPickerAccessibilityTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun add_file_label_does_not_overflow_at_200_percent_text() {
        val label = composeRule.activity.getString(R.string.attachment_picker_title_more)

        composeRule.setContent {
            val density = LocalDensity.current
            CompositionLocalProvider(
                LocalDensity provides Density(density.density, fontScale = 2f),
            ) {
                LandingKitTheme {
                    AttachmentSection(
                        attachments = emptyList(),
                        isImporting = false,
                        feedback = null,
                        protectionEnabled = false,
                        protectionControlEnabled = true,
                        showRetentionCleanup = false,
                        onLoadThumbnail = { null },
                        onImport = {},
                        onOpen = {},
                        onQuickAccess = {},
                        onDelete = {},
                        onProtectionChange = {},
                        onClearAttachments = {},
                    )
                }
            }
        }

        val textLayouts = mutableListOf<TextLayoutResult>()
        composeRule.onNodeWithText(label, useUnmergedTree = true)
            .performSemanticsAction(SemanticsActions.GetTextLayoutResult) { action ->
                action(textLayouts)
            }

        assertFalse(textLayouts.single().hasVisualOverflow)
    }
}
