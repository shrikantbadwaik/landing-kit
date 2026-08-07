package com.shrikantbadwaik.landingkit.core.data.local.model

import androidx.room.Embedded
import androidx.room.Relation
import com.shrikantbadwaik.landingkit.core.data.local.entity.ChecklistTemplateEntity
import com.shrikantbadwaik.landingkit.core.data.local.entity.ChecklistTemplateItemEntity

data class ChecklistTemplateWithItems(
    @Embedded val template: ChecklistTemplateEntity,
    @Relation(parentColumn = "id", entityColumn = "template_id")
    val items: List<ChecklistTemplateItemEntity>,
)
