package com.shrikantbadwaik.landingkit.feature.kits.entry

import com.shrikantbadwaik.landingkit.core.model.ArrivalCard
import com.shrikantbadwaik.landingkit.core.model.Trip
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ArrivalEntryPointPublisherTest {
    private val today = LocalDate.of(2026, 8, 28)

    @Test
    fun next_relevant_arrival_ignores_past_trips_and_chooses_earliest_active_trip() {
        val trips = listOf(
            trip("past", today.minusDays(1)),
            trip("later", today.plusDays(4)),
            trip("today", today),
            trip("tomorrow", today.plusDays(1)),
        )

        assertEquals("today", trips.nextRelevantArrival(today)?.id)
    }

    @Test
    fun no_relevant_arrival_returns_null() {
        val trips = listOf(trip("past", today.minusDays(1)))

        assertNull(trips.nextRelevantArrival(today))
    }

    private fun trip(id: String, arrivalDate: LocalDate) = Trip(
        id = id,
        destination = "Private destination",
        arrivalDate = arrivalDate,
        arrivalCard = ArrivalCard(
            accommodationName = "Private stay",
            address = "Private address",
        ),
        items = emptyList(),
    )
}
