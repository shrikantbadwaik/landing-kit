package com.shrikantbadwaik.landingkit.feature.kits.presentation.model

import com.shrikantbadwaik.landingkit.core.model.KitAttachment
import com.shrikantbadwaik.landingkit.core.model.Trip

internal data class ArrivalModeContent(
    val address: String,
    val supportingAddress: String?,
    val bookingReference: String?,
    val transferInstructions: String?,
    val emergencyContact: String?,
    val primaryAttachment: KitAttachment?,
    val ifDelayed: IfDelayedContent?,
)

internal data class IfDelayedContent(
    val fallbackTransport: String?,
    val lastServiceInfo: String?,
    val checkInCutoff: String?,
    val lateArrivalInstructions: String?,
    val accommodationContact: String?,
    val expectedCostNote: String?,
    val alternateStayNote: String?,
)

internal fun Trip.toArrivalModeContent(
    attachments: List<KitAttachment>,
): ArrivalModeContent {
    val localAddress = arrivalCard.localAddress.trim()
    val regularAddress = arrivalCard.address.trim()
    val ifDelayed = buildIfDelayedContent(arrivalCard)
    return ArrivalModeContent(
        address = localAddress.ifBlank { regularAddress },
        supportingAddress = regularAddress.takeIf {
            localAddress.isNotBlank() && !regularAddress.equals(localAddress, ignoreCase = true)
        },
        bookingReference = arrivalCard.bookingReference.trim().ifBlank { null },
        transferInstructions = arrivalCard.transferInstructions.trim().ifBlank { null },
        emergencyContact = arrivalCard.emergencyContact.trim().ifBlank { null },
        primaryAttachment = attachments.firstOrNull(KitAttachment::isQuickAccess)
            ?: attachments.firstOrNull(),
        ifDelayed = ifDelayed,
    )
}

private fun buildIfDelayedContent(
    card: com.shrikantbadwaik.landingkit.core.model.ArrivalCard,
): IfDelayedContent? {
    val fallback = card.fallbackTransport.trim().ifBlank { null }
    val lastService = card.lastServiceInfo.trim().ifBlank { null }
    val cutoff = card.checkInCutoff.trim().ifBlank { null }
    val lateInstructions = card.lateArrivalInstructions.trim().ifBlank { null }
    val contact = card.accommodationContact.trim().ifBlank { null }
    val cost = card.expectedCostNote.trim().ifBlank { null }
    val alternate = card.alternateStayNote.trim().ifBlank { null }
    if (listOfNotNull(fallback, lastService, cutoff, lateInstructions, contact, cost, alternate).isEmpty()) {
        return null
    }
    return IfDelayedContent(
        fallbackTransport = fallback,
        lastServiceInfo = lastService,
        checkInCutoff = cutoff,
        lateArrivalInstructions = lateInstructions,
        accommodationContact = contact,
        expectedCostNote = cost,
        alternateStayNote = alternate,
    )
}
