package com.shrikantbadwaik.landingkit.feature.kits.presentation.model

import com.shrikantbadwaik.landingkit.core.model.ArrivalCard
import com.shrikantbadwaik.landingkit.core.model.ArrivalItem
import com.shrikantbadwaik.landingkit.core.model.ItemCategory
import com.shrikantbadwaik.landingkit.core.model.Trip
import java.time.LocalDate

internal data class KitForm(
    val destination: String = "",
    val arrivalDate: String = "",
    val stayName: String = "",
    val address: String = "",
    val localAddress: String = "",
    val bookingReference: String = "",
    val transfer: String = "",
    val currency: String = "",
    val emergencyContact: String = "",
    val dietaryPhrase: String = "",
) {
    fun toTrip(existingTrip: Trip? = null): Trip {
        val parsedArrivalDate = runCatching { LocalDate.parse(arrivalDate.trim()) }
            .getOrElse { LocalDate.now().plusDays(7) }
        val updatedArrivalCard = ArrivalCard(
            accommodationName = stayName.trim(),
            address = address.trim(),
            localAddress = localAddress.trim(),
            bookingReference = bookingReference.trim(),
            transferInstructions = transfer.trim(),
            currencyNote = currency.trim(),
            emergencyContact = emergencyContact.trim(),
            dietaryPhrase = dietaryPhrase.trim(),
        )

        return existingTrip?.copy(
            destination = destination.trim(),
            arrivalDate = parsedArrivalDate,
            arrivalCard = updatedArrivalCard,
        ) ?: Trip(
            destination = destination.trim(),
            arrivalDate = parsedArrivalDate,
            arrivalCard = updatedArrivalCard,
            items = defaultChecklist(),
        )
    }

    companion object {
        fun fromTrip(trip: Trip) = KitForm(
            destination = trip.destination,
            arrivalDate = trip.arrivalDate.toString(),
            stayName = trip.arrivalCard.accommodationName,
            address = trip.arrivalCard.address,
            localAddress = trip.arrivalCard.localAddress,
            bookingReference = trip.arrivalCard.bookingReference,
            transfer = trip.arrivalCard.transferInstructions,
            currency = trip.arrivalCard.currencyNote,
            emergencyContact = trip.arrivalCard.emergencyContact,
            dietaryPhrase = trip.arrivalCard.dietaryPhrase,
        )
    }
}

internal fun defaultChecklist() = listOf(
    ArrivalItem(title = "Download tickets and booking", category = ItemCategory.DOCUMENT),
    ArrivalItem(title = "Save the stay address offline", category = ItemCategory.SAFETY),
    ArrivalItem(title = "Confirm airport transfer", category = ItemCategory.TRANSPORT),
    ArrivalItem(title = "Keep a little local currency", category = ItemCategory.MONEY),
)
