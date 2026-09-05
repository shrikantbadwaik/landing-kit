package com.shrikantbadwaik.landingkit.feature.kits.presentation.component

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.shrikantbadwaik.landingkit.core.designsystem.LandingKitColors
import com.shrikantbadwaik.landingkit.feature.kits.R

internal enum class JournalScene(@DrawableRes val drawable: Int) {
    Mountains(R.drawable.journal_mountains),
    Sunrise(R.drawable.journal_sunrise),
    Travelers(R.drawable.journal_travelers),
    Hikers(R.drawable.journal_hikers),
    Vehicle(R.drawable.journal_vehicle),
    Rest(R.drawable.journal_rest),
    Footsteps(R.drawable.journal_footsteps),
    Doodles(R.drawable.journal_doodles),
}

/** Decorative art is excluded from accessibility announcements. */
@Composable
internal fun JournalArtwork(
    scene: JournalScene,
    modifier: Modifier = Modifier,
    monochrome: Boolean = false,
    alpha: Float = 1f,
    tint: Color? = null,
) {
    val night = MaterialTheme.colorScheme.background.luminance() < .3f
    val drawable = when {
        night && scene == JournalScene.Mountains -> R.drawable.journal_mountains_night
        night && scene == JournalScene.Hikers -> R.drawable.journal_hikers_night
        else -> scene.drawable
    }
    Image(
        painter = painterResource(drawable),
        contentDescription = null,
        modifier = modifier,
        contentScale = ContentScale.Fit,
        colorFilter = when {
            tint != null -> ColorFilter.tint(tint)
            monochrome -> ColorFilter.tint(MaterialTheme.colorScheme.primary)
            else -> null
        },
        alpha = alpha,
    )
}

@Composable
internal fun JournalWelcome(title: String, description: String) {
    Surface(
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    ) {
        Column {
            Box(
                Modifier.fillMaxWidth().height(128.dp)
                    .background(LandingKitColors.Sun),
            ) {
                Image(
                    painter = painterResource(JournalScene.Sunrise.drawable),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    alignment = Alignment.TopCenter,
                    modifier = Modifier.matchParentSize(),
                )
            }
            Column(Modifier.padding(22.dp)) {
                Text(
                    title,
                    style = MaterialTheme.typography.displayLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Spacer(Modifier.height(12.dp))
                Text(
                    description,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
internal fun JournalBackground(
    seed: String,
    listState: LazyListState,
    modifier: Modifier = Modifier,
    count: Int = 2,
    content: @Composable () -> Unit,
) {
    Box(modifier.fillMaxSize().clipToBounds().journalDoodles(seed, listState, count)) {
        content()
    }
}
