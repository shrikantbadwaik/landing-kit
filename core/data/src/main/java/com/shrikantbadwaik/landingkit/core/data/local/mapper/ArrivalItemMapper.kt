package com.shrikantbadwaik.landingkit.core.data.local.mapper

import com.shrikantbadwaik.landingkit.core.data.local.entity.ArrivalItemEntity
import com.shrikantbadwaik.landingkit.core.model.ArrivalItem
import com.shrikantbadwaik.landingkit.core.model.ItemCategory

internal fun ArrivalItemEntity.toModel() = ArrivalItem(
    id = id,
    title = title,
    detail = detail,
    category = ItemCategory.valueOf(category),
    isComplete = isComplete,
)

internal fun ArrivalItem.toEntity(tripId: String, sortOrder: Int) = ArrivalItemEntity(
    id = id,
    tripId = tripId,
    title = title,
    detail = detail,
    category = category.name,
    isComplete = isComplete,
    sortOrder = sortOrder,
)
