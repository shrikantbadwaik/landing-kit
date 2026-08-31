package com.shrikantbadwaik.landingkit.core.data

import android.net.Uri

interface BackupRepository {
    suspend fun exportBackup(destination: Uri, password: CharArray): BackupExportResult
    suspend fun inspectBackup(source: Uri, password: CharArray): BackupPreview
    suspend fun restoreBackup(source: Uri, password: CharArray): BackupRestoreResult
}

data class BackupExportResult(
    val tripCount: Int,
    val templateCount: Int,
    val attachmentCount: Int,
)

data class BackupPreview(
    val createdAtMillis: Long,
    val tripCount: Int,
    val newTripCount: Int,
    val existingTripCount: Int,
    val templateCount: Int,
    val newTemplateCount: Int,
    val existingTemplateCount: Int,
    val attachmentCount: Int,
    val restorableAttachmentCount: Int,
) {
    val hasNewContent: Boolean get() = newTripCount > 0 || newTemplateCount > 0
}

data class BackupRestoreResult(
    val restoredTripIds: List<String>,
    val restoredTemplateCount: Int,
    val restoredAttachmentCount: Int,
    val skippedTripCount: Int,
    val skippedTemplateCount: Int,
)

class BackupUnlockException(cause: Throwable? = null) : Exception(cause)
class UnsupportedBackupException(cause: Throwable? = null) : Exception(cause)
