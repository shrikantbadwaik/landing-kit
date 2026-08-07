package com.shrikantbadwaik.landingkit.core.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "checklist_template_items",
    primaryKeys = ["template_id", "sort_order"],
    foreignKeys = [
        ForeignKey(
            entity = ChecklistTemplateEntity::class,
            parentColumns = ["id"],
            childColumns = ["template_id"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [
        Index(
            value = ["template_id"],
            name = "checklist_template_items_template_id",
        ),
    ],
)
data class ChecklistTemplateItemEntity(
    @ColumnInfo(name = "template_id") val templateId: String,
    @ColumnInfo(name = "sort_order") val sortOrder: Int,
    val title: String,
    val detail: String,
    val category: String,
)
