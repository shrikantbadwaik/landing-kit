package com.shrikantbadwaik.landingkit.feature.kits.presentation.model

internal fun shouldOfferAttachmentCleanup(
    arrivalPhase: ArrivalPhase,
    attachmentCount: Int,
): Boolean = arrivalPhase == ArrivalPhase.PAST && attachmentCount > 0
