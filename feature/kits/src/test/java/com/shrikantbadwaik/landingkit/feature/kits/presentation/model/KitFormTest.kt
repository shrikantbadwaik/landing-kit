package com.shrikantbadwaik.landingkit.feature.kits.presentation.model

import com.shrikantbadwaik.landingkit.core.model.ArrivalCard
import com.shrikantbadwaik.landingkit.core.model.ArrivalItem
import com.shrikantbadwaik.landingkit.core.model.ChecklistTemplate
import com.shrikantbadwaik.landingkit.core.model.ChecklistTemplateItem
import com.shrikantbadwaik.landingkit.core.model.Trip
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class KitFormTest {
    private val checklistContent = DefaultChecklistContent(
        downloadTickets = "Download tickets and booking",
        saveAddress = "Save the stay address offline",
        confirmTransfer = "Confirm airport transfer",
        localCurrency = "Keep a little local currency",
    )

    @Test
    fun `new kit trims values and receives default checklist`() {
        val trip = KitForm.newKit(checklistContent).copy(
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

    @Test
    fun `checklist can be renamed completed reordered removed and extended`() {
        val first = ArrivalItem(id = "first", title = "Download ticket")
        val second = ArrivalItem(id = "second", title = "Save address")
        val third = ArrivalItem(id = "third", title = "Confirm transfer")
        val form = KitForm(checklist = listOf(first, second, third))

        val edited = form
            .updateChecklistTitle("second", "Save local address")
            .setChecklistComplete("second", true)
            .moveChecklistItem("second", offset = -1)
            .removeChecklistItem("third")
            .addChecklistItem("  Message the host  ")

        assertEquals(listOf("second", "first"), edited.checklist.take(2).map { it.id })
        assertEquals("Save local address", edited.checklist.first().title)
        assertTrue(edited.checklist.first().isComplete)
        assertEquals("Message the host", edited.checklist.last().title)
        assertFalse(edited.checklist.any { it.id == "third" })
    }

    @Test
    fun `invalid checklist moves and blank additions are ignored`() {
        val item = ArrivalItem(id = "only", title = "Only task")
        val form = KitForm(checklist = listOf(item))

        assertEquals(form, form.moveChecklistItem("only", offset = -1))
        assertEquals(form, form.moveChecklistItem("missing", offset = 1))
        assertEquals(form, form.addChecklistItem("   "))
    }

    @Test
    fun `applying a template appends only missing tasks with fresh incomplete items`() {
        val existing = ArrivalItem(
            id = "existing",
            title = "Save address",
            isComplete = true,
        )
        val template = ChecklistTemplate(
            id = "template",
            name = "International",
            items = listOf(
                ChecklistTemplateItem(title = " save ADDRESS "),
                ChecklistTemplateItem(title = "Download offline map"),
            ),
        )

        val result = KitForm(checklist = listOf(existing)).applyTemplate(template)

        assertEquals(1, result.addedCount)
        assertEquals(existing, result.form.checklist.first())
        assertEquals("Download offline map", result.form.checklist.last().title)
        assertFalse(result.form.checklist.last().isComplete)
        assertFalse(result.form.checklist.last().id == existing.id)
    }

    @Test
    fun `personal template drops completion but preserves task order and categories`() {
        val completed = ArrivalItem(
            id = "done",
            title = "  Download map  ",
            isComplete = true,
        )
        val pending = ArrivalItem(id = "pending", title = "Message host")

        val template = KitForm(checklist = listOf(completed, pending))
            .toChecklistTemplate("  My repeatable arrival  ")

        assertEquals("My repeatable arrival", template.name)
        assertEquals(listOf("Download map", "Message host"), template.items.map { it.title })
    }

    @Test
    fun `validation reports required fields without silently replacing the date`() {
        val form = KitForm(
            arrivalDate = "not-a-date",
            checklist = listOf(ArrivalItem(title = "")),
        )

        val validation = form.validate()

        assertTrue(validation.destinationMissing)
        assertFalse(validation.arrivalDateMissing)
        assertTrue(validation.arrivalDateInvalid)
        assertTrue(validation.stayNameMissing)
        assertTrue(validation.addressMissing)
        assertTrue(validation.blankChecklistItem)
        assertEquals(5, validation.errorCount)
        assertFalse(validation.isValid)
        assertThrows(IllegalArgumentException::class.java) { form.toTrip() }
    }

    @Test
    fun `past arrival date is valid and preserved`() {
        val form = KitForm(
            destination = "Lisbon",
            arrivalDate = "2024-05-04",
            stayName = "Alfama House",
            address = "Rua da Regueira 10",
        )

        assertTrue(form.validate().isValid)
        assertEquals(LocalDate.of(2024, 5, 4), form.toTrip().arrivalDate)
    }
}
