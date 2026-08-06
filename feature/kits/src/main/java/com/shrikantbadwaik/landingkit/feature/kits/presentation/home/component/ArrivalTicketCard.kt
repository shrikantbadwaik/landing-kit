package com.shrikantbadwaik.landingkit.feature.kits.presentation.home.component

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.shrikantbadwaik.landingkit.core.designsystem.LandingKitColors
import com.shrikantbadwaik.landingkit.core.model.Trip
import com.shrikantbadwaik.landingkit.feature.kits.presentation.component.TicketIllustration
import java.time.format.DateTimeFormatter

@Composable
internal fun ArrivalTicketCard(trip: Trip, onClick: () -> Unit) {
    val completed = trip.items.count { it.isComplete }
    val progress = if (trip.items.isEmpty()) 0f else completed.toFloat() / trip.items.size
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(650, easing = FastOutSlowInEasing),
        label = "Arrival readiness",
    )

    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(30.dp),
        colors = CardDefaults.cardColors(containerColor = LandingKitColors.Forest),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
    ) {
        Box(
            Modifier.background(
                Brush.linearGradient(
                    colors = listOf(LandingKitColors.Forest, Color(0xFF0D3634)),
                    start = Offset.Zero,
                    end = Offset.Infinite,
                ),
            ),
        ) {
            TicketIllustration(Modifier.align(Alignment.TopEnd).offset(x = 38.dp, y = (-36).dp))
            Column(Modifier.padding(24.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "ARRIVAL CARD",
                        style = MaterialTheme.typography.labelSmall,
                        color = LandingKitColors.Seafoam,
                        modifier = Modifier.weight(1f),
                    )
                    Text("↗", style = MaterialTheme.typography.titleMedium, color = Color.White)
                }
                Spacer(Modifier.height(34.dp))
                Text(
                    text = trip.destination,
                    style = MaterialTheme.typography.headlineMedium,
                    color = Color.White,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(Modifier.height(7.dp))
                Text(
                    text = trip.arrivalDate.format(DateTimeFormatter.ofPattern("EEE, d MMM yyyy")),
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = .78f),
                )
                Spacer(Modifier.height(23.dp))
                DashedDivider()
                Spacer(Modifier.height(18.dp))
                Text("STAY", style = MaterialTheme.typography.labelSmall, color = LandingKitColors.Seafoam)
                Text(
                    text = trip.arrivalCard.accommodationName,
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(Modifier.height(18.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    LinearProgressIndicator(
                        progress = { animatedProgress },
                        modifier = Modifier.weight(1f).height(7.dp).clip(CircleShape),
                        color = LandingKitColors.Sun,
                        trackColor = Color.White.copy(alpha = .17f),
                    )
                    Spacer(Modifier.width(13.dp))
                    Text(
                        "$completed/${trip.items.size} ready",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.White,
                    )
                }
            }
        }
    }
}

@Composable
private fun DashedDivider() {
    Canvas(Modifier.fillMaxWidth().height(1.dp)) {
        drawLine(
            color = Color.White.copy(alpha = .28f),
            start = Offset.Zero,
            end = Offset(size.width, 0f),
            strokeWidth = 1.dp.toPx(),
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(8.dp.toPx(), 8.dp.toPx())),
        )
    }
}
