package com.shrikantbadwaik.landingkit.core.data.local.repository

import com.shrikantbadwaik.landingkit.core.data.local.backup.BackupRecoveryAccess
import com.shrikantbadwaik.landingkit.core.data.local.backup.BackupRestoreRecovery
import com.shrikantbadwaik.landingkit.core.data.TripRepository
import com.shrikantbadwaik.landingkit.core.data.local.dao.TripDao
import com.shrikantbadwaik.landingkit.core.data.local.attachment.EncryptedAttachmentStore
import com.shrikantbadwaik.landingkit.core.data.local.mapper.toEntity
import com.shrikantbadwaik.landingkit.core.data.local.mapper.toModel
import com.shrikantbadwaik.landingkit.core.model.OfflineArrivalCheckStatus
import com.shrikantbadwaik.landingkit.core.model.Trip
import com.shrikantbadwaik.landingkit.core.model.TripValidator
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/** Room-backed, durable device-local source of truth. */
class LocalTripRepository(
    private val tripDao: TripDao,
    private val attachmentStore: EncryptedAttachmentStore,
    private val access: BackupRecoveryAccess = BackupRecoveryAccess(),
    private val recovery: BackupRestoreRecovery,
) : TripRepository {
    private val writeMutex = Mutex()

    override fun observeTrips(): Flow<List<Trip>> = access.guard(tripDao.observeTrips()).map { trips ->
        trips.map { it.toModel() }
    }

    override suspend fun getTrip(id: String): Trip? {
        access.requireReady()
        return tripDao.getTrip(id)?.toModel()
    }

    override suspend fun saveTrip(trip: Trip) = locked {
        access.requireReady()
        val errors = TripValidator.validate(trip)
        require(errors.isEmpty()) { errors.joinToString() }
        val existing = tripDao.getTrip(trip.id)?.trip
        val tripToSave = if (existing == null) {
            trip
        } else {
            trip.copy(
                offlineCheckStatus = OfflineArrivalCheckStatus.valueOf(
                    existing.offlineCheckStatus,
                ),
                offlineCheckedAtMillis = existing.offlineCheckedAtMillis,
                offlineCheckContentFingerprint = existing.offlineCheckContentFingerprint,
                planBCheckStatus = OfflineArrivalCheckStatus.valueOf(
                    existing.planBCheckStatus,
                ),
                planBCheckedAtMillis = existing.planBCheckedAtMillis,
                planBContentFingerprint = existing.planBContentFingerprint,
            )
        }
        tripDao.replaceTrip(
            trip = tripToSave.toEntity(),
            items = trip.items.mapIndexed { index, item -> item.toEntity(trip.id, index) },
        )
    }

    override suspend fun deleteTrip(id: String) = locked {
        access.requireReady()
        recovery.mutations.deleteTrip(id)
    }

    override suspend fun setItemComplete(
        tripId: String,
        itemId: String,
        complete: Boolean,
    ) = locked {
        access.requireReady()
        tripDao.setItemComplete(tripId = tripId, itemId = itemId, complete = complete)
    }

    override suspend fun setArrivalReminderEnabled(
        tripId: String,
        enabled: Boolean,
    ) = locked {
        access.requireReady()
        tripDao.setArrivalReminderEnabled(tripId = tripId, enabled = enabled)
    }

    override suspend fun recordOfflineArrivalCheck(
        tripId: String,
        status: OfflineArrivalCheckStatus,
        checkedAtMillis: Long,
        contentFingerprint: String,
    ) = locked {
        access.requireReady()
        tripDao.recordOfflineArrivalCheck(
            tripId = tripId,
            status = status.name,
            checkedAtMillis = checkedAtMillis,
            contentFingerprint = contentFingerprint,
        )
    }

    override suspend fun recordPlanBCheck(
        tripId: String,
        status: OfflineArrivalCheckStatus,
        checkedAtMillis: Long,
        contentFingerprint: String,
    ) = locked {
        access.requireReady()
        tripDao.recordPlanBCheck(
            tripId = tripId,
            status = status.name,
            checkedAtMillis = checkedAtMillis,
            contentFingerprint = contentFingerprint,
        )
    }
    private suspend fun <T> locked(block: suspend () -> T): T =
        recovery.dataOperation { writeMutex.withLock { block() } }

}
