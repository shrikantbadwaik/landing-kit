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
    fun toTrip() = Trip(
        destination = destination.trim(),
        arrivalDate = runCatching { LocalDate.parse(arrivalDate.trim()) }
            .getOrElse { LocalDate.now().plusDays(7) },
        arrivalCard = ArrivalCard(
            accommodationName = stayName.trim(),
            address = address.trim(),
            localAddress = localAddress.trim(),
            bookingReference = bookingReference.trim(),
            transferInstructions = transfer.trim(),
            currencyNote = currency.trim(),
            emergencyContact = emergencyContact.trim(),
            dietaryPhrase = dietaryPhrase.trim(),
        ),
        items = defaultChecklist(),
    )
}

internal fun defaultChecklist() = listOf(
    ArrivalItem(title = "Download tickets and booking", category = ItemCategory.DOCUMENT),
    ArrivalItem(title = "Save the stay address offline", category = ItemCategory.SAFETY),
    ArrivalItem(title = "Confirm airport transfer", category = ItemCategory.TRANSPORT),
    ArrivalItem(title = "Keep a little local currency", category = ItemCategory.MONEY),
)
