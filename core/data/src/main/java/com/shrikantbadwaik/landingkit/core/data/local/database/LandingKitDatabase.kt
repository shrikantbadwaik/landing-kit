package com.shrikantbadwaik.landingkit.core.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.shrikantbadwaik.landingkit.core.data.local.dao.TripDao
import com.shrikantbadwaik.landingkit.core.data.local.entity.ArrivalItemEntity
import com.shrikantbadwaik.landingkit.core.data.local.entity.TripEntity

@Database(
    entities = [TripEntity::class, ArrivalItemEntity::class],
    version = 1,
    exportSchema = true,
)
abstract class LandingKitDatabase : RoomDatabase() {
    abstract fun tripDao(): TripDao
}
