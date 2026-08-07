package com.shrikantbadwaik.landingkit.feature.kits.presentation.model

import com.shrikantbadwaik.landingkit.core.model.ArrivalCard
import com.shrikantbadwaik.landingkit.core.model.ArrivalItem
import com.shrikantbadwaik.landingkit.core.model.ChecklistTemplate
import com.shrikantbadwaik.landingkit.core.model.ChecklistTemplateItem
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
    val checklist: List<ArrivalItem> = emptyList(),
) {
    fun toTrip(
        existingTrip: Trip? = null,
    ): Trip {
        val validation = validate()
        require(validation.isValid) { "Cannot create a trip from an invalid kit form" }
        val parsedArrivalDate = checkNotNull(validation.arrivalDate)
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
            items = sanitizedChecklist(),
        ) ?: Trip(
            destination = destination.trim(),
            arrivalDate = parsedArrivalDate,
            arrivalCard = updatedArrivalCard,
            items = sanitizedChecklist(),
        )
    }

    fun updateChecklistTitle(itemId: String, title: String) = copy(
        checklist = checklist.map { item ->
            if (item.id == itemId) item.copy(title = title) else item
        },
    )

    fun setChecklistComplete(itemId: String, complete: Boolean) = copy(
        checklist = checklist.map { item ->
            if (item.id == itemId) item.copy(isComplete = complete) else item
        },
    )

    fun addChecklistItem(title: String): KitForm {
        val trimmedTitle = title.trim()
        if (trimmedTitle.isEmpty()) return this

        return copy(
            checklist = checklist + ArrivalItem(
                title = trimmedTitle,
                category = ItemCategory.CHECKLIST,
            ),
        )
    }

    fun removeChecklistItem(itemId: String) = copy(
        checklist = checklist.filterNot { it.id == itemId },
    )

    fun moveChecklistItem(itemId: String, offset: Int): KitForm {
        val fromIndex = checklist.indexOfFirst { it.id == itemId }
        val toIndex = fromIndex + offset
        if (fromIndex == -1 || toIndex !in checklist.indices) return this

        val reordered = checklist.toMutableList().apply {
            add(toIndex, removeAt(fromIndex))
        }
        return copy(checklist = reordered)
    }

    fun applyTemplate(template: ChecklistTemplate): TemplateApplicationResult {
        val existingTitles = checklist
            .mapTo(mutableSetOf()) { it.title.normalizedTitle() }
        val additions = template.items.mapNotNull { templateItem ->
            val normalizedTitle = templateItem.title.normalizedTitle()
            if (normalizedTitle.isEmpty() || !existingTitles.add(normalizedTitle)) {
                null
            } else {
                ArrivalItem(
                    title = templateItem.title.trim(),
                    detail = templateItem.detail.trim(),
                    category = templateItem.category,
                )
            }
        }
        return TemplateApplicationResult(
            form = copy(checklist = checklist + additions),
            addedCount = additions.size,
        )
    }

    fun toChecklistTemplate(name: String) = ChecklistTemplate(
        name = name.trim(),
        items = sanitizedChecklist().map { item ->
            ChecklistTemplateItem(
                title = item.title,
                detail = item.detail,
                category = item.category,
            )
        },
    )

    private fun sanitizedChecklist() = checklist.map { item ->
        item.copy(title = item.title.trim(), detail = item.detail.trim())
    }

    fun validate(): KitFormValidation {
        val parsedArrivalDate = runCatching { LocalDate.parse(arrivalDate.trim()) }.getOrNull()
        return KitFormValidation(
            destinationMissing = destination.isBlank(),
            arrivalDateMissing = arrivalDate.isBlank(),
            arrivalDateInvalid = arrivalDate.isNotBlank() && parsedArrivalDate == null,
            stayNameMissing = stayName.isBlank(),
            addressMissing = address.isBlank(),
            blankChecklistItem = checklist.any { it.title.isBlank() },
            arrivalDate = parsedArrivalDate,
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
            checklist = trip.items,
        )

        fun newKit(checklistContent: DefaultChecklistContent) = KitForm(
            checklist = defaultChecklist(checklistContent),
        )
    }
}

internal data class KitFormValidation(
    val destinationMissing: Boolean,
    val arrivalDateMissing: Boolean,
    val arrivalDateInvalid: Boolean,
    val stayNameMissing: Boolean,
    val addressMissing: Boolean,
    val blankChecklistItem: Boolean,
    val arrivalDate: LocalDate?,
) {
    val isValid: Boolean
        get() = !destinationMissing &&
            !arrivalDateMissing &&
            !arrivalDateInvalid &&
            !stayNameMissing &&
            !addressMissing &&
            !blankChecklistItem

    val errorCount: Int
        get() = listOf(
            destinationMissing,
            arrivalDateMissing || arrivalDateInvalid,
            stayNameMissing,
            addressMissing,
            blankChecklistItem,
        ).count { it }

    val firstInvalidSectionIndex: Int
        get() = when {
            destinationMissing || arrivalDateMissing || arrivalDateInvalid -> 2
            stayNameMissing || addressMissing -> 3
            blankChecklistItem -> 5
            else -> 0
        }
}

internal data class TemplateApplicationResult(
    val form: KitForm,
    val addedCount: Int,
)

private fun String.normalizedTitle() = trim().lowercase()

internal data class DefaultChecklistContent(
    val downloadTickets: String,
    val saveAddress: String,
    val confirmTransfer: String,
    val localCurrency: String,
)

internal fun defaultChecklist(content: DefaultChecklistContent) = listOf(
    ArrivalItem(title = content.downloadTickets, category = ItemCategory.DOCUMENT),
    ArrivalItem(title = content.saveAddress, category = ItemCategory.SAFETY),
    ArrivalItem(title = content.confirmTransfer, category = ItemCategory.TRANSPORT),
    ArrivalItem(title = content.localCurrency, category = ItemCategory.MONEY),
)
