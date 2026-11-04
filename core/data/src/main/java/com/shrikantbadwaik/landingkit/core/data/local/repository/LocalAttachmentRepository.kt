package com.shrikantbadwaik.landingkit.core.data.local.repository

import com.shrikantbadwaik.landingkit.core.data.local.backup.BackupRecoveryAccess
import android.content.ContentResolver
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.provider.OpenableColumns
import com.shrikantbadwaik.landingkit.core.data.AttachmentRepository
import com.shrikantbadwaik.landingkit.core.data.AttachmentTooLargeException
import com.shrikantbadwaik.landingkit.core.data.PreparedAttachment
import com.shrikantbadwaik.landingkit.core.data.UnsupportedAttachmentException
import com.shrikantbadwaik.landingkit.core.data.local.attachment.AttachmentSizeLimitException
import com.shrikantbadwaik.landingkit.core.data.local.attachment.EncryptedAttachmentStore
import com.shrikantbadwaik.landingkit.core.data.local.dao.AttachmentDao
import com.shrikantbadwaik.landingkit.core.data.local.entity.AttachmentEntity
import com.shrikantbadwaik.landingkit.core.data.local.mapper.toModel
import com.shrikantbadwaik.landingkit.core.model.KitAttachment
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileNotFoundException
import java.io.InputStream
import java.util.UUID
import kotlin.math.roundToInt
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

class LocalAttachmentRepository(
    private val contentResolver: ContentResolver,
    private val dao: AttachmentDao,
    private val store: EncryptedAttachmentStore,
    private val access: BackupRecoveryAccess = BackupRecoveryAccess(),
) : AttachmentRepository {
    private val attachmentMutex = Mutex()

    override fun observeAttachments(tripId: String): Flow<List<KitAttachment>> =
        access.guard(dao.observeAttachments(tripId)).map { attachments -> attachments.map(AttachmentEntity::toModel) }

    override suspend fun importAttachment(tripId: String, source: Uri): KitAttachment =
        withContext(Dispatchers.IO) {
            attachmentMutex.withLock {
                access.requireReady()
                val metadata = source.metadata()
                if (!metadata.mimeType.isSupportedAttachment()) throw UnsupportedAttachmentException()
                if (metadata.sizeBytes != null && metadata.sizeBytes > EncryptedAttachmentStore.MAX_ATTACHMENT_BYTES) {
                    throw AttachmentTooLargeException()
                }
                val id = UUID.randomUUID().toString()
                try {
                    val input = contentResolver.openInputStream(source) ?: throw FileNotFoundException()
                    val encryptedSize = store.encrypt(tripId, id, input)
                    if (metadata.mimeType.startsWith("image/")) {
                        source.imageThumbnail()?.let { thumbnail ->
                            try {
                                store.saveThumbnail(tripId, id, thumbnail)
                            } catch (_: Exception) {
                                // The original remains usable when a provider cannot supply a preview.
                            }
                        }
                    }
                    val entity = AttachmentEntity(
                        id = id,
                        tripId = tripId,
                        displayName = metadata.displayName,
                        mimeType = metadata.mimeType,
                        sizeBytes = encryptedSize,
                        createdAtMillis = System.currentTimeMillis(),
                        isQuickAccess = false,
                    )
                    try {
                        dao.insertAttachment(entity)
                    } catch (error: Throwable) {
                        store.delete(tripId, id)
                        throw error
                    }
                    entity.toModel()
                } catch (_: AttachmentSizeLimitException) {
                    store.delete(tripId, id)
                    throw AttachmentTooLargeException()
                }
            }
        }

    override suspend fun loadThumbnail(attachmentId: String): ByteArray? =
        withContext(Dispatchers.IO) {
            attachmentMutex.withLock {
                access.requireReady()
                val attachment = dao.getAttachment(attachmentId) ?: return@withLock null
                if (!attachment.mimeType.startsWith("image/")) return@withLock null
                store.readThumbnail(attachment.tripId, attachment.id) ?: run {
                    val preview = store.decryptToPreview(
                        tripId = attachment.tripId,
                        attachmentId = attachment.id,
                        extension = "img",
                    )
                    try {
                        preview.imageThumbnail()?.also { thumbnail ->
                            store.saveThumbnail(attachment.tripId, attachment.id, thumbnail)
                        }
                    } finally {
                        store.releaseViewingPreview(preview)
                    }
                }
            }
        }

    override suspend fun prepareForViewing(attachmentId: String): PreparedAttachment {
        var prepared: PreparedAttachment? = null
        try {
            return withContext(Dispatchers.IO) {
                attachmentMutex.withLock {
                    access.requireReady()
                    val attachment = requireNotNull(dao.getAttachment(attachmentId))
                    val file = store.decryptToPreview(
                        tripId = attachment.tripId,
                        attachmentId = attachment.id,
                        extension = attachment.mimeType.previewExtension(),
                    )
                    PreparedAttachment(
                        file = file,
                        mimeType = attachment.mimeType,
                        onClose = { store.releaseViewingPreview(file) },
                        onExternalAccess = { uri, uids -> store.retainExternalPreview(file, uri, uids) },
                    ).also { prepared = it }
                }
            }
        } catch (error: Throwable) {
            // withContext may cancel delivery after IO has already created the plaintext copy.
            prepared?.close()
            throw error
        }
    }

    override fun endViewingSession() = store.endViewingSession()

    override fun cleanInactiveViewingPreviews() = store.cleanInactiveViewingPreviews()

    override suspend fun verifyAttachmentReadable(attachmentId: String): Boolean =
        withContext(Dispatchers.IO) {
            attachmentMutex.withLock {
                access.requireReady()
                val attachment = dao.getAttachment(attachmentId) ?: return@withLock false
                store.exists(attachment.tripId, attachment.id) &&
                    store.verifyDecryptable(attachment.tripId, attachment.id)
            }
        }

    override suspend fun setQuickAccessAttachment(
        tripId: String,
        attachmentId: String?,
    ) = withContext(Dispatchers.IO) {
        attachmentMutex.withLock {
            access.requireReady()
            dao.setQuickAccessAttachment(tripId, attachmentId)
        }
    }

    override suspend fun deleteAttachment(attachmentId: String) = withContext(Dispatchers.IO) {
        attachmentMutex.withLock {
            access.requireReady()
            val attachment = dao.getAttachment(attachmentId) ?: return@withLock
            store.delete(attachment.tripId, attachment.id)
            dao.deleteAttachment(attachment.id)
        }
    }

    override suspend fun deleteAttachments(tripId: String): Int = withContext(Dispatchers.IO) {
        attachmentMutex.withLock {
            access.requireReady()
            store.deleteTrip(tripId)
            dao.deleteAttachments(tripId)
        }
    }

    private fun Uri.metadata(): SourceMetadata {
        val providerType = contentResolver.getType(this)
        var name: String? = null
        var size: Long? = null
        contentResolver.query(this, null, null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                name = cursor.string(OpenableColumns.DISPLAY_NAME)
                size = cursor.long(OpenableColumns.SIZE)
            }
        }
        val displayName = name?.takeIf(String::isNotBlank) ?: "Arrival attachment"
        val mimeType = providerType ?: when {
            displayName.endsWith(".pdf", ignoreCase = true) -> PDF_MIME_TYPE
            else -> ""
        }
        return SourceMetadata(displayName.take(MAX_DISPLAY_NAME_LENGTH), mimeType, size)
    }

    private fun Uri.imageThumbnail(): ByteArray? = imageThumbnail {
        contentResolver.openInputStream(this)
    }

    private fun File.imageThumbnail(): ByteArray? = imageThumbnail(::inputStream)

    private fun imageThumbnail(openInput: () -> InputStream?): ByteArray? {
        return try {
            val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            openInput()?.use { input ->
                BitmapFactory.decodeStream(input, null, bounds)
            }
            if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return null

            var sampleSize = 1
            while (
                bounds.outWidth / sampleSize > THUMBNAIL_DECODE_EDGE ||
                bounds.outHeight / sampleSize > THUMBNAIL_DECODE_EDGE
            ) {
                sampleSize *= 2
            }
            val options = BitmapFactory.Options().apply { inSampleSize = sampleSize }
            val decoded = openInput()?.use { input ->
                BitmapFactory.decodeStream(input, null, options)
            } ?: return null
            val longestEdge = maxOf(decoded.width, decoded.height)
            val scale = minOf(1f, THUMBNAIL_EDGE.toFloat() / longestEdge)
            val thumbnail = if (scale < 1f) {
                Bitmap.createScaledBitmap(
                    decoded,
                    (decoded.width * scale).roundToInt().coerceAtLeast(1),
                    (decoded.height * scale).roundToInt().coerceAtLeast(1),
                    true,
                )
            } else {
                decoded
            }
            ByteArrayOutputStream().use { output ->
                thumbnail.compress(Bitmap.CompressFormat.JPEG, THUMBNAIL_JPEG_QUALITY, output)
                output.toByteArray()
            }.also {
                if (thumbnail !== decoded) thumbnail.recycle()
                decoded.recycle()
            }
        } catch (_: Exception) {
            null
        } catch (_: OutOfMemoryError) {
            null
        }
    }

    private fun Cursor.string(column: String): String? = getColumnIndex(column)
        .takeIf { it >= 0 && !isNull(it) }
        ?.let(::getString)

    private fun Cursor.long(column: String): Long? = getColumnIndex(column)
        .takeIf { it >= 0 && !isNull(it) }
        ?.let(::getLong)

    private fun String.isSupportedAttachment() = this == PDF_MIME_TYPE || startsWith("image/")
    private fun String.previewExtension() = if (this == PDF_MIME_TYPE) "pdf" else "img"

    private data class SourceMetadata(
        val displayName: String,
        val mimeType: String,
        val sizeBytes: Long?,
    )

    companion object {
        private const val PDF_MIME_TYPE = "application/pdf"
        private const val MAX_DISPLAY_NAME_LENGTH = 180
        private const val THUMBNAIL_EDGE = 320
        private const val THUMBNAIL_DECODE_EDGE = THUMBNAIL_EDGE * 2
        private const val THUMBNAIL_JPEG_QUALITY = 82
    }
}
