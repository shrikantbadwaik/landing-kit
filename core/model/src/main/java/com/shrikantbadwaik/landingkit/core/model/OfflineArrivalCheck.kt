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

enum class PlanBCheckIssue {
    MissingFallbackTransport,
    MissingAccommodationContact,
    MissingCheckInRecovery,
}

data class OfflineArrivalCheckResult(
    val status: OfflineArrivalCheckStatus,
    val issues: List<OfflineArrivalCheckIssue>,
)

data class PlanBCheckResult(
    val status: OfflineArrivalCheckStatus,
    val issues: List<PlanBCheckIssue>,
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

    fun evaluatePlanB(trip: Trip): PlanBCheckResult {
        val issues = buildList {
            if (trip.arrivalCard.fallbackTransport.isBlank()) {
                add(PlanBCheckIssue.MissingFallbackTransport)
            }
            if (trip.arrivalCard.accommodationContact.isBlank()) {
                add(PlanBCheckIssue.MissingAccommodationContact)
            }
            if (
                trip.arrivalCard.checkInCutoff.isBlank() &&
                trip.arrivalCard.lateArrivalInstructions.isBlank()
            ) {
                add(PlanBCheckIssue.MissingCheckInRecovery)
            }
        }
        return PlanBCheckResult(
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

object PlanBContentFingerprint {
    fun derive(trip: Trip): String {
        val card = trip.arrivalCard
        val payload = listOf(
            card.fallbackTransport.trim(),
            card.lastServiceInfo.trim(),
            card.checkInCutoff.trim(),
            card.lateArrivalInstructions.trim(),
            card.accommodationContact.trim(),
            card.expectedCostNote.trim(),
            card.alternateStayNote.trim(),
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

fun Trip.effectivePlanBCheckStatus(
    currentFingerprint: String,
): OfflineArrivalCheckStatus =
    if (
        planBCheckStatus != OfflineArrivalCheckStatus.NotChecked &&
        planBContentFingerprint == currentFingerprint
    ) {
        planBCheckStatus
    } else {
        OfflineArrivalCheckStatus.NotChecked
    }

fun Trip.hasPlanBData(): Boolean = with(arrivalCard) {
    fallbackTransport.isNotBlank() ||
        lastServiceInfo.isNotBlank() ||
        checkInCutoff.isNotBlank() ||
        lateArrivalInstructions.isNotBlank() ||
        accommodationContact.isNotBlank() ||
        expectedCostNote.isNotBlank() ||
        alternateStayNote.isNotBlank()
}

fun Trip.isPlanBCheckStale(currentFingerprint: String): Boolean =
    planBCheckedAtMillis != null &&
        planBCheckStatus != OfflineArrivalCheckStatus.NotChecked &&
        planBContentFingerprint != currentFingerprint

private fun KitAttachment.isSupportedOfflineEvidence(): Boolean =
    mimeType == "application/pdf" || mimeType.startsWith("image/")
