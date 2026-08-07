package com.shrikantbadwaik.landingkit.feature.kits.presentation.detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.shrikantbadwaik.landingkit.core.designsystem.LandingKitColors
import com.shrikantbadwaik.landingkit.core.model.ArrivalItem
import com.shrikantbadwaik.landingkit.core.model.Trip
import com.shrikantbadwaik.landingkit.feature.kits.presentation.component.ScreenHeader
import com.shrikantbadwaik.landingkit.feature.kits.presentation.component.TicketIllustration
import com.shrikantbadwaik.landingkit.feature.kits.presentation.detail.component.AnimatedChecklistItem
import com.shrikantbadwaik.landingkit.feature.kits.presentation.detail.component.DeleteKitDialog
import com.shrikantbadwaik.landingkit.feature.kits.presentation.detail.component.DetailCard
import com.shrikantbadwaik.landingkit.feature.kits.presentation.detail.component.EditKitAction
import com.shrikantbadwaik.landingkit.feature.kits.presentation.detail.component.KitDangerZone
import java.time.format.DateTimeFormatter

@Composable
internal fun KitDetailScreen(
    trip: Trip,
    onBack: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onCheck: (ArrivalItem, Boolean) -> Unit,
) {
    var showDeleteConfirmation by remember { mutableStateOf(false) }

    if (showDeleteConfirmation) {
        DeleteKitDialog(
            destination = trip.destination,
            onConfirm = {
                showDeleteConfirmation = false
                onDelete()
            },
            onDismiss = { showDeleteConfirmation = false },
        )
    }

    Scaffold(containerColor = MaterialTheme.colorScheme.background) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(22.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item(key = "detail_header") {
                ScreenHeader(onBack = onBack, label = "ARRIVAL CARD", badge = "Offline")
            }
            item(key = "detail_hero") {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = LandingKitColors.Forest),
                    shape = RoundedCornerShape(30.dp),
                ) {
                    Box {
                        TicketIllustration(
                            Modifier.align(Alignment.TopEnd).offset(x = 42.dp, y = (-40).dp),
                        )
                        Column(Modifier.fillMaxWidth().padding(24.dp)) {
                            Text(
                                "ARRIVING IN",
                                style = MaterialTheme.typography.labelSmall,
                                color = LandingKitColors.Seafoam,
                            )
                            Spacer(Modifier.height(24.dp))
                            Text(
                                trip.destination,
                                style = MaterialTheme.typography.headlineMedium,
                                color = Color.White,
                            )
                            Spacer(Modifier.height(10.dp))
                            Text(
                                trip.arrivalDate.format(
                                    DateTimeFormatter.ofPattern("EEEE, d MMMM yyyy"),
                                ),
                                color = Color.White.copy(alpha = .78f),
                            )
                            Spacer(Modifier.height(22.dp))
                            EditKitAction(
                                onClick = onEdit,
                                modifier = Modifier.align(Alignment.End),
                            )
                        }
                    }
                }
            }
            item(key = "detail_driver_address") {
                DetailCard(
                    "Show your driver",
                    trip.arrivalCard.localAddress.ifBlank { trip.arrivalCard.address },
                    LandingKitColors.Sun,
                )
            }
            item(key = "detail_stay") {
                DetailCard(
                    "Stay",
                    "${trip.arrivalCard.accommodationName}\n${trip.arrivalCard.address}",
                    LandingKitColors.Seafoam,
                )
            }
            if (trip.arrivalCard.bookingReference.isNotBlank()) {
                item(key = "detail_booking") {
                    DetailCard(
                        "Booking reference",
                        trip.arrivalCard.bookingReference,
                        LandingKitColors.Sky,
                    )
                }
            }
            if (trip.arrivalCard.transferInstructions.isNotBlank()) {
                item(key = "detail_transfer") {
                    DetailCard(
                        "From the airport",
                        trip.arrivalCard.transferInstructions,
                        LandingKitColors.Coral.copy(alpha = .55f),
                    )
                }
            }
            if (trip.arrivalCard.currencyNote.isNotBlank()) {
                item(key = "detail_money") {
                    DetailCard("Money", trip.arrivalCard.currencyNote, LandingKitColors.Sun)
                }
            }
            if (trip.arrivalCard.emergencyContact.isNotBlank()) {
                item(key = "detail_emergency") {
                    DetailCard(
                        "Emergency",
                        trip.arrivalCard.emergencyContact,
                        LandingKitColors.Lavender,
                    )
                }
            }
            if (trip.arrivalCard.dietaryPhrase.isNotBlank()) {
                item(key = "detail_dietary") {
                    DetailCard(
                        "Dietary phrase",
                        trip.arrivalCard.dietaryPhrase,
                        LandingKitColors.Seafoam,
                    )
                }
            }
            item(key = "detail_checklist_header") {
                Text("Ready before landing", style = MaterialTheme.typography.titleLarge)
            }
            items(trip.items, key = { it.id }) { item ->
                AnimatedChecklistItem(item = item, onCheck = { onCheck(item, it) })
            }
            item(key = "detail_danger_zone") {
                KitDangerZone(onDelete = { showDeleteConfirmation = true })
            }
            item(key = "detail_privacy_footer") {
                Text(
                    "AVAILABLE WITHOUT SIGNAL  •  STORED ON THIS DEVICE",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}
