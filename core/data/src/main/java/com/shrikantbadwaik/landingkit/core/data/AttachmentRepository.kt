package com.shrikantbadwaik.landingkit.core.data

import android.net.Uri
import com.shrikantbadwaik.landingkit.core.model.KitAttachment
import java.io.File
import kotlinx.coroutines.flow.Flow

interface AttachmentRepository {
    fun observeAttachments(tripId: String): Flow<List<KitAttachment>>
    suspend fun importAttachment(tripId: String, source: Uri): KitAttachment
    suspend fun loadThumbnail(attachmentId: String): ByteArray?
    suspend fun prepareForViewing(attachmentId: String): PreparedAttachment
    suspend fun verifyAttachmentReadable(attachmentId: String): Boolean
    suspend fun setQuickAccessAttachment(tripId: String, attachmentId: String?)
    suspend fun deleteAttachment(attachmentId: String)
    suspend fun deleteAttachments(tripId: String): Int
}

data class PreparedAttachment(
    val file: File,
    val mimeType: String,
)

class UnsupportedAttachmentException : IllegalArgumentException()
class AttachmentTooLargeException : IllegalArgumentException()
