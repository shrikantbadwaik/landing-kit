package com.shrikantbadwaik.landingkit.feature.kits

import android.net.Uri
import com.shrikantbadwaik.landingkit.core.data.AttachmentRepository
import com.shrikantbadwaik.landingkit.core.data.PreparedAttachment
import com.shrikantbadwaik.landingkit.core.data.TripRepository
import com.shrikantbadwaik.landingkit.core.model.ArrivalCard
import com.shrikantbadwaik.landingkit.core.model.KitAttachment
import com.shrikantbadwaik.landingkit.core.model.OfflineArrivalCheckIssue
import com.shrikantbadwaik.landingkit.core.model.OfflineArrivalCheckStatus
import com.shrikantbadwaik.landingkit.core.model.PlanBContentFingerprint
import com.shrikantbadwaik.landingkit.core.model.Trip
import java.io.File
import java.time.LocalDate
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test

class OfflineArrivalCheckRunnerTest {
    @Test
    fun `readable quick access evidence persists ready snapshot`() = runBlocking {
        val tripRepository = FakeTripRepository()
        val runner = OfflineArrivalCheckRunner(
            tripRepository = tripRepository,
            attachmentRepository = FakeAttachmentRepository(readable = true),
            currentTimeMillis = { 123L },
        )

        val result = runner.run(trip(), listOf(attachment()))

        assertEquals(OfflineArrivalCheckStatus.ReadyOnThisPhone, result.status)
        assertEquals(OfflineArrivalCheckStatus.ReadyOnThisPhone, tripRepository.status)
        assertEquals(123L, tripRepository.checkedAtMillis)
    }

    @Test
    fun `unreadable evidence persists needs attention`() = runBlocking {
        val tripRepository = FakeTripRepository()
        val runner = OfflineArrivalCheckRunner(
            tripRepository = tripRepository,
            attachmentRepository = FakeAttachmentRepository(readable = false),
            currentTimeMillis = { 123L },
        )

        val result = runner.run(trip(), listOf(attachment()))

        assertEquals(
            listOf(OfflineArrivalCheckIssue.UnreadableEvidence),
            result.issues,
        )
        assertEquals(OfflineArrivalCheckStatus.NeedsAttention, tripRepository.status)
    }

    @Test
    fun `plan b check persists an independent ready snapshot`() = runBlocking {
        val tripRepository = FakeTripRepository()
        val runner = PlanBCheckRunner(
            tripRepository = tripRepository,
            currentTimeMillis = { 456L },
        )
        val trip = trip().copy(
            arrivalCard = trip().arrivalCard.copy(
                fallbackTransport = "Taxi rank at arrivals",
                accommodationContact = "+81 75 555 1234",
                lateArrivalInstructions = "Use the key safe",
            ),
        )

        val result = runner.run(trip)

        assertEquals(OfflineArrivalCheckStatus.ReadyOnThisPhone, result.status)
        assertEquals(OfflineArrivalCheckStatus.ReadyOnThisPhone, tripRepository.planBStatus)
        assertEquals(456L, tripRepository.planBCheckedAtMillis)
        assertEquals(
            PlanBContentFingerprint.derive(trip),
            tripRepository.planBContentFingerprint,
        )
    }

    private class FakeTripRepository : TripRepository {
        var status: OfflineArrivalCheckStatus? = null
        var checkedAtMillis: Long? = null
        var planBStatus: OfflineArrivalCheckStatus? = null
        var planBCheckedAtMillis: Long? = null
        var planBContentFingerprint: String? = null

        override fun observeTrips(): Flow<List<Trip>> = emptyFlow()
        override suspend fun getTrip(id: String): Trip? = null
        override suspend fun saveTrip(trip: Trip) = Unit
        override suspend fun deleteTrip(id: String) = Unit
        override suspend fun setItemComplete(
            tripId: String,
            itemId: String,
            complete: Boolean,
        ) = Unit

        override suspend fun setArrivalReminderEnabled(tripId: String, enabled: Boolean) = Unit

        override suspend fun recordOfflineArrivalCheck(
            tripId: String,
            status: OfflineArrivalCheckStatus,
            checkedAtMillis: Long,
            contentFingerprint: String,
        ) {
            this.status = status
            this.checkedAtMillis = checkedAtMillis
        }

        override suspend fun recordPlanBCheck(
            tripId: String,
            status: OfflineArrivalCheckStatus,
            checkedAtMillis: Long,
            contentFingerprint: String,
        ) {
            planBStatus = status
            planBCheckedAtMillis = checkedAtMillis
            planBContentFingerprint = contentFingerprint
        }
    }

    private class FakeAttachmentRepository(
        private val readable: Boolean,
    ) : AttachmentRepository {
        override fun observeAttachments(tripId: String): Flow<List<KitAttachment>> = emptyFlow()
        override suspend fun importAttachment(tripId: String, source: Uri): KitAttachment =
            error("Not used")

        override suspend fun loadThumbnail(attachmentId: String): ByteArray? = null
        override suspend fun prepareForViewing(attachmentId: String): PreparedAttachment =
            PreparedAttachment(File("unused"), "application/pdf")

        override fun endViewingSession() = Unit
        override fun cleanInactiveViewingPreviews() = Unit

        override suspend fun verifyAttachmentReadable(attachmentId: String): Boolean = readable
        override suspend fun setQuickAccessAttachment(
            tripId: String,
            attachmentId: String?,
        ) = Unit

        override suspend fun deleteAttachment(attachmentId: String) = Unit
        override suspend fun deleteAttachments(tripId: String): Int = 0
    }

    private fun trip() = Trip(
        id = "trip",
        destination = "Kyoto",
        arrivalDate = LocalDate.of(2026, 8, 22),
        arrivalCard = ArrivalCard("Machiya", "10 Quiet Street"),
        items = emptyList(),
    )

    private fun attachment() = KitAttachment(
        id = "ticket",
        tripId = "trip",
        displayName = "ticket.pdf",
        mimeType = "application/pdf",
        sizeBytes = 100,
        createdAtMillis = 1,
        isQuickAccess = true,
    )
}
