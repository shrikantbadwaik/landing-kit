package com.shrikantbadwaik.landingkit.core.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.shrikantbadwaik.landingkit.core.data.local.dao.ChecklistTemplateDao
import com.shrikantbadwaik.landingkit.core.data.local.dao.TripDao
import com.shrikantbadwaik.landingkit.core.data.local.entity.ArrivalItemEntity
import com.shrikantbadwaik.landingkit.core.data.local.entity.ChecklistTemplateEntity
import com.shrikantbadwaik.landingkit.core.data.local.entity.ChecklistTemplateItemEntity
import com.shrikantbadwaik.landingkit.core.data.local.entity.TripEntity

@Database(
    entities = [
        TripEntity::class,
        ArrivalItemEntity::class,
        ChecklistTemplateEntity::class,
        ChecklistTemplateItemEntity::class,
    ],
    version = 2,
    exportSchema = true,
)
abstract class LandingKitDatabase : RoomDatabase() {
    abstract fun tripDao(): TripDao
    abstract fun checklistTemplateDao(): ChecklistTemplateDao
}
