package com.shrikantbadwaik.landingkit.core.data.local.attachment

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.File
import java.io.InputStream
import java.io.IOException
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

class EncryptedAttachmentStore(context: Context) {
    private val attachmentRoot = File(context.filesDir, "encrypted_attachments")
    private val previewRoot = File(context.cacheDir, "attachment_previews")

    fun encrypt(
        tripId: String,
        attachmentId: String,
        source: InputStream,
    ): Long = encryptFile(
        destination = attachmentFile(tripId, attachmentId),
        source = source,
        maximumBytes = MAX_ATTACHMENT_BYTES,
    )

    fun saveThumbnail(tripId: String, attachmentId: String, bytes: ByteArray) {
        require(bytes.size <= MAX_THUMBNAIL_BYTES)
        encryptFile(
            destination = thumbnailFile(tripId, attachmentId),
            source = ByteArrayInputStream(bytes),
            maximumBytes = MAX_THUMBNAIL_BYTES.toLong(),
        )
    }

    fun readThumbnail(tripId: String, attachmentId: String): ByteArray? {
        val source = thumbnailFile(tripId, attachmentId)
        if (!source.exists()) return null
        val output = ByteArrayOutputStream()
        decryptFile(source, output, MAX_THUMBNAIL_BYTES.toLong())
        return output.toByteArray()
    }

    private fun encryptFile(
        destination: File,
        source: InputStream,
        maximumBytes: Long,
    ): Long {
        destination.parentFile?.mkdirs()
        val temporary = File(destination.parentFile, "${destination.name}.tmp")
        var plaintextBytes = 0L
        try {
            val cipher = Cipher.getInstance(TRANSFORMATION).apply {
                init(Cipher.ENCRYPT_MODE, encryptionKey())
            }
            DataOutputStream(BufferedOutputStream(temporary.outputStream())).use { output ->
                output.write(MAGIC)
                output.writeByte(cipher.iv.size)
                output.write(cipher.iv)
                source.use { input ->
                    val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
                    while (true) {
                        val read = input.read(buffer)
                        if (read == -1) break
                        plaintextBytes += read
                        if (plaintextBytes > maximumBytes) {
                            throw AttachmentSizeLimitException()
                        }
                        cipher.update(buffer, 0, read)?.let(output::write)
                    }
                }
                output.write(cipher.doFinal())
            }
            check(temporary.renameTo(destination)) { "Could not finalize encrypted attachment" }
            return plaintextBytes
        } finally {
            temporary.delete()
        }
    }

    fun decryptToPreview(
        tripId: String,
        attachmentId: String,
        extension: String,
    ): File {
        previewRoot.mkdirs()
        deletePreviews(tripId, attachmentId)
        val source = attachmentFile(tripId, attachmentId)
        val previewName = "$tripId-$attachmentId"
        val destination = safeChild(previewRoot, "$previewName.$extension")
        val temporary = safeChild(previewRoot, "$previewName.tmp")
        try {
            BufferedOutputStream(temporary.outputStream()).use { output ->
                decryptFile(source, output, MAX_ATTACHMENT_BYTES)
            }
            check(temporary.renameTo(destination)) { "Could not prepare attachment preview" }
            return destination
        } finally {
            temporary.delete()
        }
    }

    fun delete(tripId: String, attachmentId: String) {
        attachmentFile(tripId, attachmentId).delete()
        thumbnailFile(tripId, attachmentId).delete()
        deletePreviews(tripId, attachmentId)
    }

    fun clearViewingPreviews(tripId: String, attachmentId: String) {
        deletePreviews(tripId, attachmentId)
    }

    fun deleteTrip(tripId: String) {
        val tripDirectory = safeChild(attachmentRoot, tripId)
        if (tripDirectory.exists() && !tripDirectory.deleteRecursively()) {
            throw IOException("Could not delete encrypted trip attachments")
        }
        previewRoot.listFiles()
            ?.filter { it.name.startsWith("$tripId-") }
            ?.forEach { preview ->
                if (preview.exists() && !preview.delete()) {
                    throw IOException("Could not delete attachment preview")
                }
            }
    }

    private fun deletePreviews(tripId: String, attachmentId: String) {
        val prefix = "$tripId-$attachmentId."
        previewRoot.listFiles()
            ?.filter { it.name.startsWith(prefix) }
            ?.forEach(File::delete)
    }

    private fun attachmentFile(tripId: String, attachmentId: String): File =
        safeChild(safeChild(attachmentRoot, tripId), "$attachmentId.lka")

    private fun thumbnailFile(tripId: String, attachmentId: String): File =
        safeChild(safeChild(attachmentRoot, tripId), "$attachmentId.lkt")

    private fun decryptFile(source: File, output: java.io.OutputStream, maximumBytes: Long) {
        DataInputStream(BufferedInputStream(source.inputStream())).use { input ->
            val magic = ByteArray(MAGIC.size)
            input.readFully(magic)
            require(magic.contentEquals(MAGIC)) { "Unsupported attachment format" }
            val ivSize = input.readUnsignedByte()
            require(ivSize in 12..16) { "Invalid attachment IV" }
            val iv = ByteArray(ivSize).also(input::readFully)
            val cipher = Cipher.getInstance(TRANSFORMATION).apply {
                init(Cipher.DECRYPT_MODE, encryptionKey(), GCMParameterSpec(128, iv))
            }
            val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
            var plaintextBytes = 0L
            while (true) {
                val read = input.read(buffer)
                if (read == -1) break
                cipher.update(buffer, 0, read)?.let { decrypted ->
                    plaintextBytes += decrypted.size
                    require(plaintextBytes <= maximumBytes) { "Attachment exceeds size limit" }
                    output.write(decrypted)
                }
            }
            val finalBytes = cipher.doFinal()
            plaintextBytes += finalBytes.size
            require(plaintextBytes <= maximumBytes) { "Attachment exceeds size limit" }
            output.write(finalBytes)
        }
    }

    private fun encryptionKey(): SecretKey = synchronized(KEY_LOCK) {
        val keyStore = KeyStore.getInstance(KEYSTORE_PROVIDER).apply { load(null) }
        (keyStore.getKey(KEY_ALIAS, null) as? SecretKey) ?: KeyGenerator
            .getInstance(KeyProperties.KEY_ALGORITHM_AES, KEYSTORE_PROVIDER)
            .apply {
                init(
                    KeyGenParameterSpec.Builder(
                        KEY_ALIAS,
                        KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT,
                    )
                        .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                        .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                        .setKeySize(256)
                        .build(),
                )
            }
            .generateKey()
    }

    private fun safeChild(parent: File, child: String): File {
        val resolved = File(parent, child).canonicalFile
        val canonicalParent = parent.canonicalFile
        require(resolved.parentFile == canonicalParent) { "Invalid attachment path" }
        return resolved
    }

    companion object {
        const val MAX_ATTACHMENT_BYTES = 25L * 1024L * 1024L
        private const val MAX_THUMBNAIL_BYTES = 512 * 1024
        private const val KEYSTORE_PROVIDER = "AndroidKeyStore"
        private const val KEY_ALIAS = "landing_kit_attachment_key_v1"
        private const val TRANSFORMATION = "AES/GCM/NoPadding"
        private val MAGIC = byteArrayOf(0x4C, 0x4B, 0x41, 0x31)
        private val KEY_LOCK = Any()
    }
}

class AttachmentSizeLimitException : IllegalArgumentException()
