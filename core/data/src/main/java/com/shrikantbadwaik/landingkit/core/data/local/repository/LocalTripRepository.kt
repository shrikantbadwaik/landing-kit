package com.shrikantbadwaik.landingkit.core.data.local.repository

import com.shrikantbadwaik.landingkit.core.data.TripRepository
import com.shrikantbadwaik.landingkit.core.data.local.dao.TripDao
import com.shrikantbadwaik.landingkit.core.data.local.attachment.EncryptedAttachmentStore
import com.shrikantbadwaik.landingkit.core.data.local.mapper.toEntity
import com.shrikantbadwaik.landingkit.core.data.local.mapper.toModel
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
) : TripRepository {
    private val writeMutex = Mutex()

    override fun observeTrips(): Flow<List<Trip>> = tripDao.observeTrips().map { trips ->
        trips.map { it.toModel() }
    }

    override suspend fun getTrip(id: String): Trip? = tripDao.getTrip(id)?.toModel()

    override suspend fun saveTrip(trip: Trip) = writeMutex.withLock {
        val errors = TripValidator.validate(trip)
        require(errors.isEmpty()) { errors.joinToString() }
        tripDao.replaceTrip(
            trip = trip.toEntity(),
            items = trip.items.mapIndexed { index, item -> item.toEntity(trip.id, index) },
        )
    }

    override suspend fun deleteTrip(id: String) = writeMutex.withLock {
        tripDao.deleteTrip(id)
        attachmentStore.deleteTrip(id)
    }

    override suspend fun setItemComplete(
        tripId: String,
        itemId: String,
        complete: Boolean,
    ) = writeMutex.withLock {
        tripDao.setItemComplete(tripId = tripId, itemId = itemId, complete = complete)
    }
}
