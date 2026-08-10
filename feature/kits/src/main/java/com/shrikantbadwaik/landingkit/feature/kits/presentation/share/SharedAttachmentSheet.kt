package com.shrikantbadwaik.landingkit.feature.kits.presentation.share

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.shrikantbadwaik.landingkit.core.designsystem.LandingKitColors
import com.shrikantbadwaik.landingkit.core.model.Trip
import com.shrikantbadwaik.landingkit.feature.kits.PendingSharedAttachments
import com.shrikantbadwaik.landingkit.feature.kits.R
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun SharedAttachmentSheet(
    pending: PendingSharedAttachments,
    trips: List<Trip>,
    importingTripId: String?,
    onSelectTrip: (Trip) -> Unit,
    onDismiss: () -> Unit,
) {
    ModalBottomSheet(
        onDismissRequest = { if (importingTripId == null) onDismiss() },
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface,
        shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
        dragHandle = null,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 24.dp, top = 28.dp, end = 24.dp, bottom = 20.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Surface(
                    modifier = Modifier.size(56.dp),
                    shape = CircleShape,
                    color = LandingKitColors.Sun,
                    contentColor = LandingKitColors.Ink,
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = pending.sources.size.toString(),
                            style = MaterialTheme.typography.titleMedium,
                        )
                    }
                }
                Column(Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.share_sheet_label),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.secondary,
                    )
                    Text(
                        text = stringResource(R.string.share_sheet_title),
                        style = MaterialTheme.typography.titleLarge,
                    )
                }
            }

            Text(
                text = pluralStringResource(
                    R.plurals.share_sheet_count,
                    pending.sources.size,
                    pending.sources.size,
                ),
                modifier = Modifier.padding(top = 20.dp),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            if (trips.isEmpty()) {
                EmptyKitState(
                    modifier = Modifier.padding(top = 24.dp),
                    onDismiss = onDismiss,
                )
            } else {
                Text(
                    text = stringResource(R.string.share_sheet_choose_kit),
                    modifier = Modifier.padding(top = 24.dp, bottom = 10.dp),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.secondary,
                )
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 380.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    items(trips, key = Trip::id) { trip ->
                        TripDestination(
                            trip = trip,
                            isImporting = importingTripId == trip.id,
                            enabled = importingTripId == null,
                            onClick = { onSelectTrip(trip) },
                        )
                    }
                }
                TextButton(
                    onClick = onDismiss,
                    enabled = importingTripId == null,
                    modifier = Modifier
                        .align(Alignment.End)
                        .padding(top = 8.dp),
                ) {
                    Text(stringResource(R.string.share_sheet_not_now))
                }
            }
        }
    }
}

@Composable
private fun TripDestination(
    trip: Trip,
    isImporting: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    val dateFormatter = DateTimeFormatter.ofPattern(stringResource(R.string.date_format_card))
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = enabled, onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.56f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(Modifier.weight(1f)) {
                Text(
                    text = trip.destination,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = stringResource(
                        R.string.share_sheet_trip_supporting_text,
                        trip.arrivalDate.format(dateFormatter),
                        trip.arrivalCard.accommodationName,
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Spacer(Modifier.size(12.dp))
            if (isImporting) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.secondary,
                )
            } else {
                Text(
                    text = stringResource(R.string.symbol_forward),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.secondary,
                )
            }
        }
    }
}

@Composable
private fun EmptyKitState(
    modifier: Modifier = Modifier,
    onDismiss: () -> Unit,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        color = LandingKitColors.Seafoam.copy(alpha = 0.34f),
    ) {
        Column(Modifier.padding(20.dp)) {
            Text(
                text = stringResource(R.string.share_sheet_empty_title),
                style = MaterialTheme.typography.titleMedium,
            )
            Text(
                text = stringResource(R.string.share_sheet_empty_description),
                modifier = Modifier.padding(top = 6.dp),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.align(Alignment.End),
            ) {
                Text(stringResource(R.string.share_sheet_got_it))
            }
        }
    }
}
