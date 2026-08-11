package com.shrikantbadwaik.landingkit.feature.kits

import com.shrikantbadwaik.landingkit.core.data.AttachmentRepository
import com.shrikantbadwaik.landingkit.core.data.TripRepository
import com.shrikantbadwaik.landingkit.core.model.KitAttachment
import com.shrikantbadwaik.landingkit.core.model.OfflineArrivalCheckEngine
import com.shrikantbadwaik.landingkit.core.model.OfflineArrivalCheckResult
import com.shrikantbadwaik.landingkit.core.model.OfflineArrivalContentFingerprint
import com.shrikantbadwaik.landingkit.core.model.Trip

internal class OfflineArrivalCheckRunner(
    private val tripRepository: TripRepository,
    private val attachmentRepository: AttachmentRepository,
    private val currentTimeMillis: () -> Long = System::currentTimeMillis,
) {
    suspend fun run(
        trip: Trip,
        attachments: List<KitAttachment>,
    ): OfflineArrivalCheckResult {
        val quickAccess = attachments.firstOrNull(KitAttachment::isQuickAccess)
        val attachmentReadable = quickAccess?.let {
            attachmentRepository.verifyAttachmentReadable(it.id)
        } ?: false
        val result = OfflineArrivalCheckEngine.evaluate(
            trip = trip,
            attachments = attachments,
            attachmentReadable = attachmentReadable,
        )
        tripRepository.recordOfflineArrivalCheck(
            tripId = trip.id,
            status = result.status,
            checkedAtMillis = currentTimeMillis(),
            contentFingerprint = OfflineArrivalContentFingerprint.derive(
                trip,
                attachments,
            ),
        )
        return result
    }
}
