package com.shrikantbadwaik.landingkit.core.model

import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

class OfflineArrivalCheckTest {
    @Test
    fun `ready requires stay address and readable quick access evidence`() {
        val result = OfflineArrivalCheckEngine.evaluate(
            trip = trip(),
            attachments = listOf(attachment(isQuickAccess = true)),
            attachmentReadable = true,
        )

        assertEquals(OfflineArrivalCheckStatus.ReadyOnThisPhone, result.status)
        assertEquals(emptyList<OfflineArrivalCheckIssue>(), result.issues)
    }

    @Test
    fun `missing stay name needs attention`() {
        val result = OfflineArrivalCheckEngine.evaluate(
            trip = trip(stayName = " "),
            attachments = listOf(attachment(isQuickAccess = true)),
            attachmentReadable = true,
        )

        assertEquals(OfflineArrivalCheckStatus.NeedsAttention, result.status)
        assertEquals(listOf(OfflineArrivalCheckIssue.MissingStayName), result.issues)
    }

    @Test
    fun `local address satisfies address rule`() {
        val result = OfflineArrivalCheckEngine.evaluate(
            trip = trip(address = "", localAddress = "京都市東山区"),
            attachments = listOf(attachment(isQuickAccess = true)),
            attachmentReadable = true,
        )

        assertEquals(OfflineArrivalCheckStatus.ReadyOnThisPhone, result.status)
    }

    @Test
    fun `attachment must be explicitly selected for quick access`() {
        val result = OfflineArrivalCheckEngine.evaluate(
            trip = trip(),
            attachments = listOf(attachment()),
            attachmentReadable = true,
        )

        assertEquals(
            listOf(OfflineArrivalCheckIssue.MissingQuickAccessEvidence),
            result.issues,
        )
    }

    @Test
    fun `quick access evidence must be a pdf or image`() {
        val result = OfflineArrivalCheckEngine.evaluate(
            trip = trip(),
            attachments = listOf(
                attachment(isQuickAccess = true, mimeType = "text/plain"),
            ),
            attachmentReadable = true,
        )

        assertEquals(
            listOf(OfflineArrivalCheckIssue.UnsupportedEvidenceType),
            result.issues,
        )
    }

    @Test
    fun `unreadable quick access evidence needs attention`() {
        val result = OfflineArrivalCheckEngine.evaluate(
            trip = trip(),
            attachments = listOf(attachment(isQuickAccess = true)),
            attachmentReadable = false,
        )

        assertEquals(
            listOf(OfflineArrivalCheckIssue.UnreadableEvidence),
            result.issues,
        )
    }

    @Test
    fun `fingerprint is stable across repository order`() {
        val first = attachment(id = "first", createdAtMillis = 1L)
        val second = attachment(id = "second", createdAtMillis = 2L, isQuickAccess = true)

        assertEquals(
            OfflineArrivalContentFingerprint.derive(trip(), listOf(first, second)),
            OfflineArrivalContentFingerprint.derive(trip(), listOf(second, first)),
        )
    }

    @Test
    fun `fingerprint changes after relevant trip or attachment edits`() {
        val originalTrip = trip()
        val originalAttachments = listOf(attachment(isQuickAccess = true))
        val original = OfflineArrivalContentFingerprint.derive(
            originalTrip,
            originalAttachments,
        )

        assertNotEquals(
            original,
            OfflineArrivalContentFingerprint.derive(
                trip(address = "Changed address"),
                originalAttachments,
            ),
        )
        assertNotEquals(
            original,
            OfflineArrivalContentFingerprint.derive(
                originalTrip,
                originalAttachments + attachment(id = "second"),
            ),
        )
        assertNotEquals(
            original,
            OfflineArrivalContentFingerprint.derive(
                originalTrip,
                listOf(attachment(isQuickAccess = false)),
            ),
        )
    }

    @Test
    fun `stored status becomes not checked when fingerprint is stale`() {
        val checkedTrip = trip().copy(
            offlineCheckStatus = OfflineArrivalCheckStatus.ReadyOnThisPhone,
            offlineCheckContentFingerprint = "old",
        )

        assertEquals(
            OfflineArrivalCheckStatus.NotChecked,
            checkedTrip.effectiveOfflineCheckStatus("new"),
        )
        assertEquals(
            OfflineArrivalCheckStatus.ReadyOnThisPhone,
            checkedTrip.copy(offlineCheckContentFingerprint = "new")
                .effectiveOfflineCheckStatus("new"),
        )
    }

    @Test
    fun `plan b ready requires transport contact and check in recovery`() {
        val result = OfflineArrivalCheckEngine.evaluatePlanB(
            planBTrip(
                fallbackTransport = "Taxi rank at arrivals",
                accommodationContact = "+81 75 555 1234",
                checkInCutoff = "Call after 22:00",
            ),
        )

        assertEquals(OfflineArrivalCheckStatus.ReadyOnThisPhone, result.status)
        assertEquals(emptyList<PlanBCheckIssue>(), result.issues)
    }

    @Test
    fun `late instructions satisfy plan b check in recovery rule`() {
        val result = OfflineArrivalCheckEngine.evaluatePlanB(
            planBTrip(
                fallbackTransport = "Night bus 42",
                accommodationContact = "+81 75 555 1234",
                lateArrivalInstructions = "Use the key safe by the entrance",
            ),
        )

        assertEquals(OfflineArrivalCheckStatus.ReadyOnThisPhone, result.status)
    }

    @Test
    fun `partial plan b reports each missing essential`() {
        val result = OfflineArrivalCheckEngine.evaluatePlanB(planBTrip())

        assertEquals(OfflineArrivalCheckStatus.NeedsAttention, result.status)
        assertEquals(
            listOf(
                PlanBCheckIssue.MissingFallbackTransport,
                PlanBCheckIssue.MissingAccommodationContact,
                PlanBCheckIssue.MissingCheckInRecovery,
            ),
            result.issues,
        )
    }

    @Test
    fun `plan b fingerprint invalidates checked status after any note changes`() {
        val originalTrip = planBTrip(
            fallbackTransport = "Taxi",
            accommodationContact = "+81 75 555 1234",
            checkInCutoff = "22:00",
        )
        val originalFingerprint = PlanBContentFingerprint.derive(originalTrip)
        val checkedTrip = originalTrip.copy(
            planBCheckStatus = OfflineArrivalCheckStatus.ReadyOnThisPhone,
            planBCheckedAtMillis = 123L,
            planBContentFingerprint = originalFingerprint,
        )
        val changedFingerprint = PlanBContentFingerprint.derive(
            checkedTrip.copy(
                arrivalCard = checkedTrip.arrivalCard.copy(expectedCostNote = "About ¥3,000"),
            ),
        )

        assertEquals(
            OfflineArrivalCheckStatus.NotChecked,
            checkedTrip.effectivePlanBCheckStatus(changedFingerprint),
        )
        assertEquals(true, checkedTrip.isPlanBCheckStale(changedFingerprint))
        assertNotEquals(originalFingerprint, changedFingerprint)
    }

    private fun trip(
        stayName: String = "Machiya Stay",
        address: String = "10 Quiet Street",
        localAddress: String = "",
    ) = Trip(
        id = "trip",
        destination = "Kyoto",
        arrivalDate = LocalDate.of(2026, 8, 22),
        arrivalCard = ArrivalCard(
            accommodationName = stayName,
            address = address,
            localAddress = localAddress,
        ),
        items = emptyList(),
    )

    private fun planBTrip(
        fallbackTransport: String = "",
        accommodationContact: String = "",
        checkInCutoff: String = "",
        lateArrivalInstructions: String = "",
    ) = trip().copy(
        arrivalCard = trip().arrivalCard.copy(
            fallbackTransport = fallbackTransport,
            accommodationContact = accommodationContact,
            checkInCutoff = checkInCutoff,
            lateArrivalInstructions = lateArrivalInstructions,
        ),
    )

    private fun attachment(
        id: String = "ticket",
        mimeType: String = "application/pdf",
        createdAtMillis: Long = 1L,
        isQuickAccess: Boolean = false,
    ) = KitAttachment(
        id = id,
        tripId = "trip",
        displayName = "$id.pdf",
        mimeType = mimeType,
        sizeBytes = 1_024,
        createdAtMillis = createdAtMillis,
        isQuickAccess = isQuickAccess,
    )
}
