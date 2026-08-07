package com.shrikantbadwaik.landingkit.feature.kits.presentation.model

import com.shrikantbadwaik.landingkit.core.model.Trip
import java.time.LocalDate

internal enum class ArrivalPhase {
    UPCOMING,
    TODAY,
    PAST,
}

internal fun Trip.arrivalPhase(today: LocalDate): ArrivalPhase = when {
    arrivalDate.isBefore(today) -> ArrivalPhase.PAST
    arrivalDate.isEqual(today) -> ArrivalPhase.TODAY
    else -> ArrivalPhase.UPCOMING
}

internal fun isArrivalDateSelectable(candidate: LocalDate, today: LocalDate): Boolean =
    !candidate.isBefore(today)

internal data class HomeTripSections(
    val active: List<Trip>,
    val past: List<Trip>,
)

internal fun List<Trip>.toHomeTripSections(today: LocalDate): HomeTripSections {
    val activeTrips = filter { it.arrivalPhase(today) != ArrivalPhase.PAST }
        .sortedBy(Trip::arrivalDate)
    val pastTrips = filter { it.arrivalPhase(today) == ArrivalPhase.PAST }
        .sortedByDescending(Trip::arrivalDate)
    return HomeTripSections(active = activeTrips, past = pastTrips)
}
