package com.shrikantbadwaik.landingkit.feature.kits.share

import com.shrikantbadwaik.landingkit.core.model.ArrivalCard
import com.shrikantbadwaik.landingkit.core.model.ArrivalItem
import com.shrikantbadwaik.landingkit.core.model.Trip
import com.shrikantbadwaik.landingkit.feature.kits.print.PrintableSummaryLabels
import java.time.LocalDate
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ShareableArrivalSummaryTest {
    @Test
    fun `trusted copy contains only selected groups and never contains booking reference`() {
        val trip = Trip(
            destination = "Kyoto, Japan",
            arrivalDate = LocalDate.of(2026, 8, 29),
            arrivalCard = ArrivalCard(
                accommodationName = "Machiya Stay",
                address = "Gion, Kyoto",
                bookingReference = "PRIVATE-BOOKING-123",
                currencyNote = "Use yen",
                fallbackTransport = "Taxi rank",
            ),
            items = listOf(ArrivalItem(title = "Switch on eSIM", detail = "After landing")),
        )

        val text = trip.toShareableArrivalSummary("Saturday, 29 August 2026", labels)
            .asTrustedPersonCopy(setOf(ShareSummarySection.STAY, ShareSummarySection.PLAN_B))

        assertTrue(text.contains("Machiya Stay"))
        assertTrue(text.contains("Taxi rank"))
        assertFalse(text.contains("Use yen"))
        assertFalse(text.contains("Switch on eSIM"))
        assertFalse(text.contains("PRIVATE-BOOKING-123"))
    }

    @Test
    fun `trusted copy explains that the recipient does not need Landing Kit`() {
        val trip = Trip(
            destination = "Kyoto, Japan",
            arrivalDate = LocalDate.of(2026, 8, 29),
            arrivalCard = ArrivalCard(
                accommodationName = "Machiya Stay",
                address = "Gion, Kyoto",
            ),
            items = emptyList(),
        )

        val text = trip.toShareableArrivalSummary("Saturday, 29 August 2026", labels)
            .asTrustedPersonCopy(setOf(ShareSummarySection.STAY))

        assertTrue(text.startsWith("Arrival plan for Kyoto, Japan"))
        assertTrue(text.contains("You do not need Landing Kit to use this copy."))
    }

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
