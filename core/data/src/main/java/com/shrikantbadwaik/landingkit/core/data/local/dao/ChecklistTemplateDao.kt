package com.shrikantbadwaik.landingkit.core.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.shrikantbadwaik.landingkit.core.data.local.entity.ChecklistTemplateEntity
import com.shrikantbadwaik.landingkit.core.data.local.entity.ChecklistTemplateItemEntity
import com.shrikantbadwaik.landingkit.core.data.local.model.ChecklistTemplateWithItems
import kotlinx.coroutines.flow.Flow

@Dao
interface ChecklistTemplateDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertRestoredTemplate(template: ChecklistTemplateEntity)

    @Transaction
    @Query("SELECT * FROM checklist_templates ORDER BY created_at DESC")
    fun observeTemplates(): Flow<List<ChecklistTemplateWithItems>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTemplate(template: ChecklistTemplateEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItems(items: List<ChecklistTemplateItemEntity>)

    @Query("DELETE FROM checklist_template_items WHERE template_id = :templateId")
    suspend fun deleteItems(templateId: String)

    @Query("DELETE FROM checklist_templates WHERE id = :templateId")
    suspend fun deleteTemplate(templateId: String)

    @Transaction
    suspend fun replaceTemplate(
        template: ChecklistTemplateEntity,
        items: List<ChecklistTemplateItemEntity>,
    ) {
        deleteItems(template.id)
        insertTemplate(template)
        insertItems(items)
    }
}
