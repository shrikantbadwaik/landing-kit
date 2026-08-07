package com.shrikantbadwaik.landingkit.core.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.shrikantbadwaik.landingkit.core.data.local.entity.ArrivalItemEntity
import com.shrikantbadwaik.landingkit.core.data.local.entity.TripEntity
import com.shrikantbadwaik.landingkit.core.data.local.model.TripWithItems
import kotlinx.coroutines.flow.Flow

@Dao
interface TripDao {
    @Transaction
    @Query("SELECT * FROM trips ORDER BY arrival_at ASC")
    fun observeTrips(): Flow<List<TripWithItems>>

    @Transaction
    @Query("SELECT * FROM trips WHERE id = :id LIMIT 1")
    suspend fun getTrip(id: String): TripWithItems?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTrip(trip: TripEntity)

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

    @Transaction
    suspend fun replaceTrip(trip: TripEntity, items: List<ArrivalItemEntity>) {
        deleteItems(trip.id)
        insertTrip(trip)
        insertItems(items)
    }
}
