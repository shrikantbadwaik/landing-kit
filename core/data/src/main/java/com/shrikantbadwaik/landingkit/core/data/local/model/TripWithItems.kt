package com.shrikantbadwaik.landingkit.core.data.local.model

import androidx.room.Embedded
import androidx.room.Relation
import com.shrikantbadwaik.landingkit.core.data.local.entity.ArrivalItemEntity
import com.shrikantbadwaik.landingkit.core.data.local.entity.TripEntity

data class TripWithItems(
    @Embedded val trip: TripEntity,
    @Relation(parentColumn = "id", entityColumn = "trip_id")
    val items: List<ArrivalItemEntity>,
)
