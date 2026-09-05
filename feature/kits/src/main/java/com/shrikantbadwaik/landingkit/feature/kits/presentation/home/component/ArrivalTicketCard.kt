package com.shrikantbadwaik.landingkit.feature.kits.presentation.home.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.shrikantbadwaik.landingkit.core.designsystem.LandingKitColors
import com.shrikantbadwaik.landingkit.core.model.Trip
import com.shrikantbadwaik.landingkit.feature.kits.R
import com.shrikantbadwaik.landingkit.feature.kits.presentation.component.JournalArtwork
import com.shrikantbadwaik.landingkit.feature.kits.presentation.component.JournalScene
import com.shrikantbadwaik.landingkit.feature.kits.presentation.model.ArrivalPhase
import java.time.format.DateTimeFormatter

@Composable
internal fun ArrivalTicketCard(
    trip: Trip,
    phase: ArrivalPhase,
    onClick: () -> Unit,
) {
    val completed = trip.items.count { it.isComplete }
    val dateFormatter = DateTimeFormatter.ofPattern(stringResource(R.string.date_format_card))
    val backgroundColor = if (phase == ArrivalPhase.PAST) {
        LandingKitColors.Ink
    } else {
        LandingKitColors.Forest
    }

    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Box(
            Modifier.background(
                Brush.linearGradient(
                    colors = listOf(backgroundColor, LandingKitColors.Ink),
                    start = Offset.Zero,
                    end = Offset.Infinite,
                ),
            ),
        ) {
            Column(Modifier.padding(22.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        stringResource(
                            when (phase) {
                                ArrivalPhase.UPCOMING -> R.string.arrival_card_label
                                ArrivalPhase.TODAY -> R.string.arrival_card_today
                                ArrivalPhase.PAST -> R.string.arrival_card_past
                            },
                        ),
                        style = MaterialTheme.typography.labelSmall,
                        color = if (phase == ArrivalPhase.PAST) {
                            LandingKitColors.Coral
                        } else {
                            LandingKitColors.Seafoam
                        },
                        modifier = Modifier.weight(1f),
                    )
                    JournalArtwork(
                        JournalScene.Footsteps,
                        Modifier.size(24.dp),
                        monochrome = true,
                        alpha = .65f,
                        tint = LandingKitColors.Seafoam,
                    )
                }
                Spacer(Modifier.height(28.dp))
                Text(
                    text = trip.destination,
                    style = MaterialTheme.typography.headlineMedium,
                    color = Color.White,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(Modifier.height(7.dp))
                Text(
                    text = when (phase) {
                        ArrivalPhase.PAST -> stringResource(
                            R.string.arrival_card_arrived_date,
                            trip.arrivalDate.format(dateFormatter),
                        )
                        ArrivalPhase.TODAY -> stringResource(R.string.arrival_card_arriving_today)
                        ArrivalPhase.UPCOMING -> trip.arrivalDate.format(dateFormatter)
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = .78f),
                )
                Spacer(Modifier.height(22.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = trip.arrivalCard.accommodationName,
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = .82f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    if (phase != ArrivalPhase.PAST && trip.items.isEmpty()) {
                        Text(
                            style = MaterialTheme.typography.labelMedium,
                            text = stringResource(R.string.arrival_no_tasks),
                            color = Color.White.copy(alpha = .78f),
                        )
                    } else if (phase != ArrivalPhase.PAST) {
                        Spacer(Modifier.width(12.dp))
                        Surface(
                            color = LandingKitColors.Sun,
                            contentColor = LandingKitColors.Ink,
                            shape = MaterialTheme.shapes.extraSmall,
                        ) {
                            Text(
                                stringResource(R.string.arrival_readiness, completed, trip.items.size),
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                style = MaterialTheme.typography.labelMedium,
                            )
                        }
                    }
                }
            }
        }
    }
}
