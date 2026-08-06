package com.shrikantbadwaik.landingkit.core.model

import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TripValidatorTest {
    @Test
    fun `valid trip has no errors`() {
        val trip = trip(destination = "Lisbon", stay = "Alfama House", address = "Rua da Regueira 10")

        assertTrue(TripValidator.validate(trip).isEmpty())
    }

    @Test
    fun `all required fields are reported together`() {
        val errors = TripValidator.validate(trip(destination = "", stay = "", address = ""))

        assertEquals(
            listOf("Destination is required", "Stay name is required", "Stay address is required"),
            errors,
        )
    }

    private fun trip(destination: String, stay: String, address: String) = Trip(
        destination = destination,
        arrivalDate = LocalDate.of(2026, 10, 4),
        arrivalCard = ArrivalCard(accommodationName = stay, address = address),
        items = emptyList(),
    )
}
