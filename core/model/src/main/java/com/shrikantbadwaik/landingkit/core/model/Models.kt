package com.shrikantbadwaik.landingkit.core.model

import java.time.LocalDate
import java.util.UUID

data class Trip(
    val id: String = UUID.randomUUID().toString(),
    val destination: String,
    val arrivalDate: LocalDate,
    val arrivalCard: ArrivalCard,
    val items: List<ArrivalItem>,
    val arrivalReminderEnabled: Boolean = false,
)

data class ArrivalCard(
    val accommodationName: String,
    val address: String,
    val localAddress: String = "",
    val bookingReference: String = "",
    val transferInstructions: String = "",
    val currencyNote: String = "",
    val emergencyContact: String = "",
    val dietaryPhrase: String = "",
)

data class ArrivalItem(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val detail: String = "",
    val category: ItemCategory = ItemCategory.CHECKLIST,
    val isComplete: Boolean = false,
)

data class ChecklistTemplate(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val items: List<ChecklistTemplateItem>,
)

data class ChecklistTemplateItem(
    val title: String,
    val detail: String = "",
    val category: ItemCategory = ItemCategory.CHECKLIST,
)

data class KitAttachment(
    val id: String,
    val tripId: String,
    val displayName: String,
    val mimeType: String,
    val sizeBytes: Long,
    val createdAtMillis: Long,
    val isQuickAccess: Boolean = false,
)

enum class ItemCategory { CHECKLIST, DOCUMENT, TRANSPORT, MONEY, SAFETY }

object TripValidator {
    fun validate(trip: Trip): List<String> = buildList {
        if (trip.destination.isBlank()) add("Destination is required")
        if (trip.arrivalCard.accommodationName.isBlank()) add("Stay name is required")
        if (trip.arrivalCard.address.isBlank()) add("Stay address is required")
    }
}
