package com.shrikantbadwaik.landingkit.core.data.local.mapper

import com.shrikantbadwaik.landingkit.core.data.local.entity.AttachmentEntity
import com.shrikantbadwaik.landingkit.core.model.KitAttachment

internal fun AttachmentEntity.toModel() = KitAttachment(
    id = id,
    tripId = tripId,
    displayName = displayName,
    mimeType = mimeType,
    sizeBytes = sizeBytes,
    createdAtMillis = createdAtMillis,
)
