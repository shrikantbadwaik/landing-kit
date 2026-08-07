package com.shrikantbadwaik.landingkit.feature.kits.presentation.model

import com.shrikantbadwaik.landingkit.core.model.ArrivalCard
import com.shrikantbadwaik.landingkit.core.model.ArrivalItem
import com.shrikantbadwaik.landingkit.core.model.Trip
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class KitFormTest {
    @Test
    fun `new kit trims values and receives default checklist`() {
        val trip = KitForm(
            destination = "  Lisbon  ",
            arrivalDate = "2026-10-04",
            stayName = "  Alfama House ",
            address = " Rua da Regueira 10 ",
        ).toTrip()

        assertEquals("Lisbon", trip.destination)
        assertEquals(LocalDate.of(2026, 10, 4), trip.arrivalDate)
        assertEquals("Alfama House", trip.arrivalCard.accommodationName)
        assertEquals("Rua da Regueira 10", trip.arrivalCard.address)
        assertEquals(4, trip.items.size)
    }

    @Test
    fun `editing preserves identity and checklist state`() {
        val completedItem = ArrivalItem(id = "item-1", title = "Save address", isComplete = true)
        val existing = Trip(
            id = "trip-1",
            destination = "Kyoto",
            arrivalDate = LocalDate.of(2026, 8, 18),
            arrivalCard = ArrivalCard(accommodationName = "Old stay", address = "Old address"),
            items = listOf(completedItem),
        )

        val edited = KitForm.fromTrip(existing)
            .copy(stayName = "New stay", address = "New address")
            .toTrip(existing)

        assertEquals(existing.id, edited.id)
        assertEquals(existing.items, edited.items)
        assertTrue(edited.items.single().isComplete)
        assertEquals("New stay", edited.arrivalCard.accommodationName)
        assertEquals("New address", edited.arrivalCard.address)
    }
}
