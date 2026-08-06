package com.shrikantbadwaik.landingkit.feature.kits.presentation.component

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import com.shrikantbadwaik.landingkit.core.designsystem.LandingKitColors

@Composable
internal fun TicketIllustration(modifier: Modifier = Modifier) {
    val orbit = rememberInfiniteTransition(label = "Ticket orbit")
    val rotation by orbit.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(durationMillis = 24_000, easing = LinearEasing)),
        label = "Orbit rotation",
    )
    Canvas(modifier.graphicsLayer { rotationZ = rotation }.size(158.dp)) {
        drawCircle(color = LandingKitColors.Coral, radius = size.minDimension * .43f, center = center)
        drawCircle(
            color = LandingKitColors.Sun,
            radius = size.minDimension * .19f,
            center = center + Offset(-18.dp.toPx(), 22.dp.toPx()),
        )
        drawCircle(
            color = Color.White.copy(alpha = .18f),
            radius = size.minDimension * .53f,
            center = center,
            style = Stroke(width = 2.dp.toPx()),
        )
    }
}
