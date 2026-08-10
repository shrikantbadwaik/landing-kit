package com.shrikantbadwaik.landingkit.feature.kits.presentation.theme

import androidx.activity.ComponentActivity
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.shrikantbadwaik.landingkit.core.designsystem.LandingKitColors
import com.shrikantbadwaik.landingkit.core.designsystem.LandingKitTheme
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DarkThemeContrastTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun semantic_color_pairs_meet_normal_text_contrast() {
        lateinit var colors: ColorScheme

        composeRule.setContent {
            LandingKitTheme(darkTheme = true) {
                colors = MaterialTheme.colorScheme
            }
        }

        composeRule.runOnIdle {
            listOf(
                "background" to (colors.background to colors.onBackground),
                "surface" to (colors.surface to colors.onSurface),
                "primary" to (colors.primary to colors.onPrimary),
                "primaryContainer" to (colors.primaryContainer to colors.onPrimaryContainer),
                "secondary" to (colors.secondary to colors.onSecondary),
                "secondaryContainer" to (colors.secondaryContainer to colors.onSecondaryContainer),
                "tertiary" to (colors.tertiary to colors.onTertiary),
                "tertiaryContainer" to (colors.tertiaryContainer to colors.onTertiaryContainer),
                "error" to (colors.error to colors.onError),
                "errorContainer" to (colors.errorContainer to colors.onErrorContainer),
            ).forEach { (name, pair) ->
                val contrast = contrastRatio(pair.first, pair.second)
                assertTrue(
                    "$name contrast was $contrast; expected at least $MINIMUM_TEXT_CONTRAST",
                    contrast >= MINIMUM_TEXT_CONTRAST,
                )
            }
        }
    }

    @Test
    fun brand_accents_meet_normal_text_contrast_with_ink() {
        listOf(
            "Sun" to LandingKitColors.Sun,
            "Coral" to LandingKitColors.Coral,
            "Seafoam" to LandingKitColors.Seafoam,
            "Sky" to LandingKitColors.Sky,
            "Lavender" to LandingKitColors.Lavender,
        ).forEach { (name, accent) ->
            val contrast = contrastRatio(accent, LandingKitColors.Ink)
            assertTrue(
                "$name contrast was $contrast; expected at least $MINIMUM_TEXT_CONTRAST",
                contrast >= MINIMUM_TEXT_CONTRAST,
            )
        }
    }

    private fun contrastRatio(first: Color, second: Color): Float {
        val lighter = maxOf(first.luminance(), second.luminance())
        val darker = minOf(first.luminance(), second.luminance())
        return (lighter + 0.05f) / (darker + 0.05f)
    }

    private companion object {
        const val MINIMUM_TEXT_CONTRAST = 4.5f
    }
}
