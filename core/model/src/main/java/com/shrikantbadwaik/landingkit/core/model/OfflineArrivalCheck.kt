package com.shrikantbadwaik.landingkit.core.model

import java.security.MessageDigest

enum class OfflineArrivalCheckStatus {
    NotChecked,
    ReadyOnThisPhone,
    NeedsAttention,
}

enum class OfflineArrivalCheckIssue {
    MissingStayName,
    MissingAddress,
    MissingQuickAccessEvidence,
    UnsupportedEvidenceType,
    UnreadableEvidence,
}

data class OfflineArrivalCheckResult(
    val status: OfflineArrivalCheckStatus,
    val issues: List<OfflineArrivalCheckIssue>,
)

object OfflineArrivalCheckEngine {
    fun evaluate(
        trip: Trip,
        attachments: List<KitAttachment>,
        attachmentReadable: Boolean,
    ): OfflineArrivalCheckResult {
        val issues = buildList {
            if (trip.arrivalCard.accommodationName.isBlank()) {
                add(OfflineArrivalCheckIssue.MissingStayName)
            }
            if (
                trip.arrivalCard.localAddress.isBlank() &&
                trip.arrivalCard.address.isBlank()
            ) {
                add(OfflineArrivalCheckIssue.MissingAddress)
            }

            val quickAccess = attachments.firstOrNull(KitAttachment::isQuickAccess)
            when {
                quickAccess == null ->
                    add(OfflineArrivalCheckIssue.MissingQuickAccessEvidence)
                !quickAccess.isSupportedOfflineEvidence() ->
                    add(OfflineArrivalCheckIssue.UnsupportedEvidenceType)
                !attachmentReadable ->
                    add(OfflineArrivalCheckIssue.UnreadableEvidence)
            }
        }
        return OfflineArrivalCheckResult(
            status = if (issues.isEmpty()) {
                OfflineArrivalCheckStatus.ReadyOnThisPhone
            } else {
                OfflineArrivalCheckStatus.NeedsAttention
            },
            issues = issues,
        )
    }
}

object OfflineArrivalContentFingerprint {
    fun derive(trip: Trip, attachments: List<KitAttachment>): String {
        val card = trip.arrivalCard
        val attachmentState = attachments
            .sortedWith(compareBy(KitAttachment::createdAtMillis, KitAttachment::id))
            .joinToString("\n") {
                listOf(
                    it.id,
                    it.displayName,
                    it.mimeType,
                    it.sizeBytes,
                    it.createdAtMillis,
                    it.isQuickAccess,
                ).joinToString("|")
            }
        val payload = listOf(
            card.accommodationName.trim(),
            card.address.trim(),
            card.localAddress.trim(),
            attachmentState,
        ).joinToString("\u0000")
        return MessageDigest.getInstance("SHA-256")
            .digest(payload.toByteArray(Charsets.UTF_8))
            .joinToString("") { "%02x".format(it) }
    }
}

fun Trip.effectiveOfflineCheckStatus(
    currentFingerprint: String,
): OfflineArrivalCheckStatus =
    if (
        offlineCheckStatus != OfflineArrivalCheckStatus.NotChecked &&
        offlineCheckContentFingerprint == currentFingerprint
    ) {
        offlineCheckStatus
    } else {
        OfflineArrivalCheckStatus.NotChecked
    }

private fun KitAttachment.isSupportedOfflineEvidence(): Boolean =
    mimeType == "application/pdf" || mimeType.startsWith("image/")
