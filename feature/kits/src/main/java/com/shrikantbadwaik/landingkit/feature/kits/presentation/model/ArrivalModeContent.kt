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
)

internal fun Trip.toArrivalModeContent(
    attachments: List<KitAttachment>,
): ArrivalModeContent {
    val localAddress = arrivalCard.localAddress.trim()
    val regularAddress = arrivalCard.address.trim()
    return ArrivalModeContent(
        address = localAddress.ifBlank { regularAddress },
        supportingAddress = regularAddress.takeIf {
            localAddress.isNotBlank() && !regularAddress.equals(localAddress, ignoreCase = true)
        },
        bookingReference = arrivalCard.bookingReference.trim().ifBlank { null },
        transferInstructions = arrivalCard.transferInstructions.trim().ifBlank { null },
        emergencyContact = arrivalCard.emergencyContact.trim().ifBlank { null },
        // Attachment streams are newest-first, which keeps the unselected fallback deterministic.
        primaryAttachment = attachments.firstOrNull(KitAttachment::isQuickAccess)
            ?: attachments.firstOrNull(),
    )
}
