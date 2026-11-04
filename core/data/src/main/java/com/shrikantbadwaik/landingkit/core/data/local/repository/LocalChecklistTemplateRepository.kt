package com.shrikantbadwaik.landingkit.core.data.local.repository

import com.shrikantbadwaik.landingkit.core.data.local.backup.BackupRecoveryAccess
import com.shrikantbadwaik.landingkit.core.data.ChecklistTemplateRepository
import com.shrikantbadwaik.landingkit.core.data.local.dao.ChecklistTemplateDao
import com.shrikantbadwaik.landingkit.core.data.local.mapper.toEntity
import com.shrikantbadwaik.landingkit.core.data.local.mapper.toModel
import com.shrikantbadwaik.landingkit.core.model.ChecklistTemplate
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class LocalChecklistTemplateRepository(
    private val dao: ChecklistTemplateDao,
    private val access: BackupRecoveryAccess = BackupRecoveryAccess(),
) : ChecklistTemplateRepository {
    private val writeMutex = Mutex()

    override fun observeTemplates(): Flow<List<ChecklistTemplate>> =
        access.guard(dao.observeTemplates()).map { templates -> templates.map { it.toModel() } }

    override suspend fun saveTemplate(template: ChecklistTemplate) = writeMutex.withLock {
        access.requireReady()
        require(template.name.isNotBlank()) { "Template name is required" }
        require(template.items.isNotEmpty()) { "A template needs at least one item" }
        require(template.items.all { it.title.isNotBlank() }) {
            "Template items need a title"
        }
        dao.replaceTemplate(
            template = template.copy(name = template.name.trim()).toEntity(System.currentTimeMillis()),
            items = template.items.mapIndexed { index, item ->
                item.copy(title = item.title.trim(), detail = item.detail.trim())
                    .toEntity(template.id, index)
            },
        )
    }

    override suspend fun deleteTemplate(id: String) = writeMutex.withLock {
        access.requireReady()
        dao.deleteTemplate(id)
    }
}
