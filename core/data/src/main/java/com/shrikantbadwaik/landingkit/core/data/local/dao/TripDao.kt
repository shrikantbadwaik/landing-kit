package com.shrikantbadwaik.landingkit.core.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import com.shrikantbadwaik.landingkit.core.data.local.entity.ArrivalItemEntity
import com.shrikantbadwaik.landingkit.core.data.local.entity.TripEntity
import com.shrikantbadwaik.landingkit.core.data.local.model.TripWithItems
import kotlinx.coroutines.flow.Flow

@Dao
interface TripDao {
    @Transaction
    @Query("SELECT * FROM trips ORDER BY arrival_at ASC")
    suspend fun getAllTrips(): List<TripWithItems>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertRestoredTrip(trip: TripEntity)

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertRestoredItems(items: List<ArrivalItemEntity>)

    @Transaction
    @Query("SELECT * FROM trips ORDER BY arrival_at ASC")
    fun observeTrips(): Flow<List<TripWithItems>>

    @Transaction
    @Query("SELECT * FROM trips WHERE id = :id LIMIT 1")
    suspend fun getTrip(id: String): TripWithItems?

    @Upsert
    suspend fun upsertTrip(trip: TripEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItems(items: List<ArrivalItemEntity>)

    @Query("DELETE FROM items WHERE trip_id = :tripId")
    suspend fun deleteItems(tripId: String)

    @Query("DELETE FROM trips WHERE id = :tripId")
    suspend fun deleteTrip(tripId: String)

    @Query(
        """UPDATE items
            SET is_complete = :complete
            WHERE trip_id = :tripId AND id = :itemId""",
    )
    suspend fun setItemComplete(tripId: String, itemId: String, complete: Boolean)

    @Query(
        """UPDATE trips
            SET arrival_reminder_enabled = :enabled
            WHERE id = :tripId""",
    )
    suspend fun setArrivalReminderEnabled(tripId: String, enabled: Boolean)

    @Query(
        """UPDATE trips
            SET offline_check_status = :status,
                offline_checked_at = :checkedAtMillis,
                offline_check_content_fingerprint = :contentFingerprint
            WHERE id = :tripId""",
    )
    suspend fun recordOfflineArrivalCheck(
        tripId: String,
        status: String,
        checkedAtMillis: Long,
        contentFingerprint: String,
    )

    @Query(
        """UPDATE trips
            SET plan_b_check_status = :status,
                plan_b_checked_at = :checkedAtMillis,
                plan_b_content_fingerprint = :contentFingerprint
            WHERE id = :tripId""",
    )
    suspend fun recordPlanBCheck(
        tripId: String,
        status: String,
        checkedAtMillis: Long,
        contentFingerprint: String,
    )

    @Transaction
    suspend fun replaceTrip(trip: TripEntity, items: List<ArrivalItemEntity>) {
        deleteItems(trip.id)
        upsertTrip(trip)
        insertItems(items)
    }
}
