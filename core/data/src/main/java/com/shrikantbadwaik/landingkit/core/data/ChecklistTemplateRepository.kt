package com.shrikantbadwaik.landingkit.core.data

import com.shrikantbadwaik.landingkit.core.model.ChecklistTemplate
import kotlinx.coroutines.flow.Flow

interface ChecklistTemplateRepository {
    fun observeTemplates(): Flow<List<ChecklistTemplate>>
    suspend fun saveTemplate(template: ChecklistTemplate)
    suspend fun deleteTemplate(id: String)
}
