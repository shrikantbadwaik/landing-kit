package com.shrikantbadwaik.landingkit.feature.kits.presentation.component

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

@Composable
internal fun MotionReveal(
    index: Int,
    animate: Boolean = true,
    onFinished: () -> Unit = {},
    content: @Composable () -> Unit,
) {
    val progress = remember { Animatable(if (animate) 0f else 1f) }
    LaunchedEffect(animate, index) {
        if (!animate) {
            progress.snapTo(1f)
            return@LaunchedEffect
        }
        progress.snapTo(0f)
        delay(index * 65L)
        progress.animateTo(
            targetValue = 1f,
            animationSpec = tween(440, easing = FastOutSlowInEasing),
        )
        onFinished()
    }
    Box(
        modifier = Modifier.graphicsLayer {
            alpha = progress.value
            translationY = (1f - progress.value) * 24.dp.toPx()
        },
    ) {
        content()
    }
}
