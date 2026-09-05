package com.shrikantbadwaik.landingkit.feature.kits.presentation.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.shrikantbadwaik.landingkit.core.model.Trip
import com.shrikantbadwaik.landingkit.feature.kits.R
import com.shrikantbadwaik.landingkit.feature.kits.presentation.component.MotionReveal
import com.shrikantbadwaik.landingkit.feature.kits.presentation.component.journalDoodles
import com.shrikantbadwaik.landingkit.feature.kits.presentation.component.JournalWelcome
import com.shrikantbadwaik.landingkit.feature.kits.presentation.home.component.ArrivalTicketCard
import com.shrikantbadwaik.landingkit.feature.kits.presentation.home.component.BrandHeader
import com.shrikantbadwaik.landingkit.feature.kits.presentation.home.component.CoralButton
import com.shrikantbadwaik.landingkit.feature.kits.presentation.home.component.EmptyKitCard
import com.shrikantbadwaik.landingkit.feature.kits.presentation.home.component.NoUpcomingArrivalsCard
import com.shrikantbadwaik.landingkit.feature.kits.presentation.model.ArrivalPhase
import com.shrikantbadwaik.landingkit.feature.kits.presentation.model.toHomeTripSections
import com.shrikantbadwaik.landingkit.feature.kits.presentation.backup.BackupEntryCard
import java.time.LocalDate
import kotlinx.coroutines.delay

@Composable
internal fun HomeScreen(
    trips: List<Trip>,
    animateEntrance: Boolean,
    animatedTripIds: Set<String>,
    onEntrancePlayed: () -> Unit,
    onTripAnimated: (String) -> Unit,
    onCreate: () -> Unit,
    onSample: () -> Unit,
    onOpen: (String) -> Unit,
    onBackup: () -> Unit,
) {
    val today = remember { LocalDate.now() }
    val sections = remember(trips, today) { trips.toHomeTripSections(today) }

    LaunchedEffect(animateEntrance) {
        if (animateEntrance) {
            delay(950)
            onEntrancePlayed()
        }
    }

    val listState = rememberLazyListState()
    LazyColumn(
        state = listState,
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .navigationBarsPadding()
            .journalDoodles("home", listState, count = if (trips.size > 1) 3 else 2),
        contentPadding = PaddingValues(start = 22.dp, top = 22.dp, end = 22.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        item(key = "home_brand") {
            MotionReveal(index = 0, animate = animateEntrance) { BrandHeader() }
        }
        item(key = "home_intro") {
            MotionReveal(index = 1, animate = animateEntrance) {
                JournalWelcome(
                    title = stringResource(R.string.home_headline),
                    description = stringResource(R.string.home_intro),
                )
            }
        }
        item(key = "home_arrivals_header") {
            MotionReveal(index = 2, animate = animateEntrance) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(Modifier.weight(1f)) {
                        Text(
                            text = stringResource(
                                if (trips.isEmpty()) R.string.home_start_here else R.string.home_up_next,
                            ),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.secondary,
                        )
                        Text(
                            text = stringResource(
                                when {
                                    trips.isEmpty() -> R.string.home_first_arrival
                                    sections.active.isEmpty() -> R.string.home_next_arrival
                                    else -> R.string.home_arrivals
                                },
                            ),
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onBackground,
                        )
                    }
                    CoralButton(label = stringResource(R.string.home_new_kit), onClick = onCreate)
                }
            }
        }
        if (trips.isEmpty()) {
            item(key = "home_empty") {
                MotionReveal(index = 4, animate = animateEntrance) {
                    EmptyKitCard(onCreate = onCreate, onSample = onSample)
                }
            }
        } else {
            if (sections.active.isEmpty()) {
                item(key = "home_no_upcoming") {
                    NoUpcomingArrivalsCard()
                }
            }
            itemsIndexed(sections.active, key = { _, trip -> trip.id }) { index, trip ->
                val animateTrip = animateEntrance || trip.id !in animatedTripIds
                MotionReveal(
                    index = if (animateEntrance) 3 + index.coerceAtMost(3) else 0,
                    animate = animateTrip,
                    onFinished = { onTripAnimated(trip.id) },
                ) {
                    ArrivalTicketCard(
                        trip = trip,
                        phase = trip.arrivalDate.let {
                            if (it.isEqual(today)) ArrivalPhase.TODAY else ArrivalPhase.UPCOMING
                        },
                        onClick = { onOpen(trip.id) },
                    )
                }
            }
            if (sections.past.isNotEmpty()) {
                item(key = "home_past_header") {
                    Column {
                        Text(
                            text = stringResource(R.string.home_kept_for_reference),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.secondary,
                        )
                        Text(
                            text = stringResource(R.string.home_past_arrivals),
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onBackground,
                        )
                    }
                }
            }
            itemsIndexed(sections.past, key = { _, trip -> trip.id }) { index, trip ->
                val animateTrip = animateEntrance || trip.id !in animatedTripIds
                MotionReveal(
                    index = if (animateEntrance) 4 + index.coerceAtMost(2) else 0,
                    animate = animateTrip,
                    onFinished = { onTripAnimated(trip.id) },
                ) {
                    ArrivalTicketCard(
                        trip = trip,
                        phase = ArrivalPhase.PAST,
                        onClick = { onOpen(trip.id) },
                    )
                }
            }
        }
        item(key = "home_backup") {
            BackupEntryCard(onClick = onBackup)
        }
    }
}
