package com.shrikantbadwaik.landingkit.feature.kits.sample

import com.shrikantbadwaik.landingkit.core.model.ArrivalCard
import com.shrikantbadwaik.landingkit.core.model.Trip
import com.shrikantbadwaik.landingkit.feature.kits.presentation.model.DefaultChecklistContent
import com.shrikantbadwaik.landingkit.feature.kits.presentation.model.defaultChecklist
import java.time.LocalDate

internal data class SampleTripContent(
    val destination: String,
    val stayName: String,
    val address: String,
    val localAddress: String,
    val bookingReference: String,
    val transfer: String,
    val currency: String,
    val emergency: String,
    val dietary: String,
    val checklist: DefaultChecklistContent,
)

internal fun sampleTrip(content: SampleTripContent) = Trip(
    destination = content.destination,
    arrivalDate = LocalDate.now().plusDays(12),
    arrivalCard = ArrivalCard(
        accommodationName = content.stayName,
        address = content.address,
        localAddress = content.localAddress,
        bookingReference = content.bookingReference,
        transferInstructions = content.transfer,
        currencyNote = content.currency,
        emergencyContact = content.emergency,
        dietaryPhrase = content.dietary,
    ),
    items = defaultChecklist(content.checklist),
)
