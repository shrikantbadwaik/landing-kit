package com.shrikantbadwaik.landingkit.core.data

import com.shrikantbadwaik.landingkit.core.model.Trip
import com.shrikantbadwaik.landingkit.core.model.OfflineArrivalCheckStatus
import kotlinx.coroutines.flow.Flow

interface TripRepository {
    fun observeTrips(): Flow<List<Trip>>
    suspend fun getTrip(id: String): Trip?
    suspend fun saveTrip(trip: Trip)
    suspend fun deleteTrip(id: String)
    suspend fun setItemComplete(tripId: String, itemId: String, complete: Boolean)
    suspend fun setArrivalReminderEnabled(tripId: String, enabled: Boolean)
    suspend fun recordOfflineArrivalCheck(
        tripId: String,
        status: OfflineArrivalCheckStatus,
        checkedAtMillis: Long,
        contentFingerprint: String,
    )
    suspend fun recordPlanBCheck(
        tripId: String,
        status: OfflineArrivalCheckStatus,
        checkedAtMillis: Long,
        contentFingerprint: String,
    )
}
