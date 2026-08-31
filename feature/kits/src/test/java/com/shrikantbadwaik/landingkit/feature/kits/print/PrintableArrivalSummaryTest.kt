package com.shrikantbadwaik.landingkit.feature.kits.print

import com.shrikantbadwaik.landingkit.core.model.ArrivalCard
import com.shrikantbadwaik.landingkit.core.model.ArrivalItem
import com.shrikantbadwaik.landingkit.core.model.Trip
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PrintableArrivalSummaryTest {
    @Test
    fun `summary omits blank optional sections and limits checklist`() {
        val trip = trip(
            items = (1..8).map { index ->
                ArrivalItem(title = "Task $index", isComplete = index <= 2)
            },
        )

        val summary = trip.toPrintableArrivalSummary("29 August 2026", labels)

        assertEquals(listOf("Stay and arrival", "Before landing"), summary.sections.map { it.title })
        val checklist = summary.sections.last().entries
        assertEquals(6, checklist.size)
        assertEquals("Task 3", checklist.first().label)
        assertFalse(checklist.first().checked == true)
    }

    @Test
    fun `summary includes every nonblank plan B field`() {
        val trip = trip(
            arrivalCard = baseCard.copy(
                fallbackTransport = "Taxi rank at arrivals",
                accommodationContact = "+81 75 555 1234",
                lateArrivalInstructions = "Use the side entrance",
            ),
        )

        val planB = trip.toPrintableArrivalSummary("29 August 2026", labels)
            .sections.single { it.title == "If plans change" }

        assertEquals(3, planB.entries.size)
        assertTrue(planB.entries.any { it.value == "Taxi rank at arrivals" })
        assertTrue(planB.entries.any { it.value == "+81 75 555 1234" })
        assertTrue(planB.entries.any { it.value == "Use the side entrance" })
    }

    @Test
    fun `page range selection removes duplicates and ignores pages outside document`() {
        assertEquals(
            listOf(0, 1, 3),
            requestedPageIndexesForRanges(pageCount = 4, ranges = listOf(0..1, 1..1, 3..8)),
        )
    }

    private fun trip(
        arrivalCard: ArrivalCard = baseCard,
        items: List<ArrivalItem> = emptyList(),
    ) = Trip(
        destination = "Kyoto, Japan",
        arrivalDate = LocalDate.of(2026, 8, 29),
        arrivalCard = arrivalCard,
        items = items,
    )

    private val baseCard = ArrivalCard(
        accommodationName = "Machiya Stay Gion",
        address = "2F, 570-123 Gionmachi Minamigawa, Kyoto",
    )

    private val labels = PrintableSummaryLabels(
        staySection = "Stay and arrival",
        stay = "Stay",
        address = "Address",
        localAddress = "Local-language address",
        bookingReference = "Booking reference",
        gettingThere = "Getting there",
        usefulOnArrival = "Useful on arrival",
        money = "Money",
        emergency = "Emergency",
        dietaryPhrase = "Dietary phrase",
        ifPlansChange = "If plans change",
        alternateTransport = "Alternative way to get there",
        lastDeparture = "Last departure",
        checkInCloses = "Check-in closes at",
        lateArrival = "If arriving late",
        stayContact = "Stay contact",
        expectedCost = "Expected cost",
        alternateStay = "Another place to stay",
        beforeLanding = "Before landing",
    )
}
