package com.shrikantbadwaik.landingkit.core.data.local.mapper

import com.shrikantbadwaik.landingkit.core.data.local.entity.ChecklistTemplateEntity
import com.shrikantbadwaik.landingkit.core.data.local.entity.ChecklistTemplateItemEntity
import com.shrikantbadwaik.landingkit.core.data.local.model.ChecklistTemplateWithItems
import com.shrikantbadwaik.landingkit.core.model.ChecklistTemplate
import com.shrikantbadwaik.landingkit.core.model.ChecklistTemplateItem
import com.shrikantbadwaik.landingkit.core.model.ItemCategory

internal fun ChecklistTemplateWithItems.toModel() = ChecklistTemplate(
    id = template.id,
    name = template.name,
    items = items.sortedBy(ChecklistTemplateItemEntity::sortOrder).map { item ->
        ChecklistTemplateItem(
            title = item.title,
            detail = item.detail,
            category = ItemCategory.valueOf(item.category),
        )
    },
)

internal fun ChecklistTemplate.toEntity(createdAt: Long) = ChecklistTemplateEntity(
    id = id,
    name = name,
    createdAt = createdAt,
)

internal fun ChecklistTemplateItem.toEntity(
    templateId: String,
    sortOrder: Int,
) = ChecklistTemplateItemEntity(
    templateId = templateId,
    sortOrder = sortOrder,
    title = title,
    detail = detail,
    category = category.name,
)
