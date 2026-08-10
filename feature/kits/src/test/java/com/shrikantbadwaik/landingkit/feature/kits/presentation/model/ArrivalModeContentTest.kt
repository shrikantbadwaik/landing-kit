package com.shrikantbadwaik.landingkit.feature.kits.presentation.model

import com.shrikantbadwaik.landingkit.core.model.ArrivalCard
import com.shrikantbadwaik.landingkit.core.model.KitAttachment
import com.shrikantbadwaik.landingkit.core.model.Trip
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ArrivalModeContentTest {
    @Test
    fun `local address leads and regular address remains available`() {
        val content = trip(
            address = "2F, 570 Gionmachi, Kyoto",
            localAddress = "京都市東山区祇園町南側 570 2F",
        ).toArrivalModeContent(emptyList())

        assertEquals("京都市東山区祇園町南側 570 2F", content.address)
        assertEquals("2F, 570 Gionmachi, Kyoto", content.supportingAddress)
    }

    @Test
    fun `regular address is the fallback without duplicated support text`() {
        val content = trip(address = "10 Quiet Street").toArrivalModeContent(emptyList())

        assertEquals("10 Quiet Street", content.address)
        assertNull(content.supportingAddress)
    }

    @Test
    fun `blank optional details are omitted`() {
        val content = trip(
            address = "10 Quiet Street",
            bookingReference = "  ",
            transferInstructions = "",
            emergencyContact = "\n",
        ).toArrivalModeContent(emptyList())

        assertNull(content.bookingReference)
        assertNull(content.transferInstructions)
        assertNull(content.emergencyContact)
    }

    @Test
    fun `newest repository attachment becomes the quick action`() {
        val newest = attachment("newest")
        val older = attachment("older")

        val content = trip(address = "10 Quiet Street")
            .toArrivalModeContent(listOf(newest, older))

        assertEquals(newest, content.primaryAttachment)
    }

    @Test
    fun `chosen attachment takes priority over repository order`() {
        val newest = attachment("newest")
        val chosen = attachment("chosen", isQuickAccess = true)

        val content = trip(address = "10 Quiet Street")
            .toArrivalModeContent(listOf(newest, chosen))

        assertEquals(chosen, content.primaryAttachment)
    }

    private fun trip(
        address: String,
        localAddress: String = "",
        bookingReference: String = "",
        transferInstructions: String = "",
        emergencyContact: String = "",
    ) = Trip(
        id = "trip",
        destination = "Kyoto, Japan",
        arrivalDate = LocalDate.of(2026, 8, 22),
        arrivalCard = ArrivalCard(
            accommodationName = "Machiya Stay Gion",
            address = address,
            localAddress = localAddress,
            bookingReference = bookingReference,
            transferInstructions = transferInstructions,
            emergencyContact = emergencyContact,
        ),
        items = emptyList(),
    )

    private fun attachment(
        id: String,
        isQuickAccess: Boolean = false,
    ) = KitAttachment(
        id = id,
        tripId = "trip",
        displayName = "$id.pdf",
        mimeType = "application/pdf",
        sizeBytes = 1_024,
        createdAtMillis = 1L,
        isQuickAccess = isQuickAccess,
    )
}
