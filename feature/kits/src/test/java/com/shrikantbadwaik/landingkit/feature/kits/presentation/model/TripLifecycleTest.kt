package com.shrikantbadwaik.landingkit.feature.kits.presentation.model

import com.shrikantbadwaik.landingkit.core.model.ArrivalCard
import com.shrikantbadwaik.landingkit.core.model.Trip
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Test

class TripLifecycleTest {
    private val today = LocalDate.of(2026, 8, 7)

    @Test
    fun `arrival phase distinguishes upcoming today and past`() {
        assertEquals(ArrivalPhase.PAST, trip("past", today.minusDays(1)).arrivalPhase(today))
        assertEquals(ArrivalPhase.TODAY, trip("today", today).arrivalPhase(today))
        assertEquals(ArrivalPhase.UPCOMING, trip("next", today.plusDays(1)).arrivalPhase(today))
    }

    @Test
    fun `home sections put active trips first and newest past trip first`() {
        val trips = listOf(
            trip("oldest", today.minusDays(8)),
            trip("later", today.plusDays(7)),
            trip("recent", today.minusDays(2)),
            trip("today", today),
            trip("sooner", today.plusDays(2)),
        )

        val sections = trips.toHomeTripSections(today)

        assertEquals(listOf("today", "sooner", "later"), sections.active.map { it.id })
        assertEquals(listOf("recent", "oldest"), sections.past.map { it.id })
    }

    @Test
    fun `arrival date selection allows today and future but rejects past dates`() {
        assertEquals(false, isArrivalDateSelectable(today.minusDays(1), today))
        assertEquals(true, isArrivalDateSelectable(today, today))
        assertEquals(true, isArrivalDateSelectable(today.plusDays(1), today))
    }

    private fun trip(id: String, arrivalDate: LocalDate) = Trip(
        id = id,
        destination = id,
        arrivalDate = arrivalDate,
        arrivalCard = ArrivalCard(accommodationName = "Stay", address = "Address"),
        items = emptyList(),
    )
}
