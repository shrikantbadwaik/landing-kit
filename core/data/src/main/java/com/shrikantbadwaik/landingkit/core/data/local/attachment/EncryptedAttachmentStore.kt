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
import java.io.OutputStream
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import android.net.Uri

class EncryptedAttachmentStore(context: Context) {
    private val attachmentRoot = File(context.filesDir, "encrypted_attachments")
    private val previewSessions = AttachmentPreviewSessions(context)

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

    fun encryptFromBackup(
        tripId: String,
        attachmentId: String,
        source: InputStream,
        expectedBytes: Long,
    ): Long {
        require(expectedBytes in 0..MAX_ATTACHMENT_BYTES)
        return encryptFile(
            destination = attachmentFile(tripId, attachmentId),
            source = source,
            maximumBytes = expectedBytes,
        ).also { actual -> require(actual == expectedBytes) }
    }

    fun decryptForBackup(
        tripId: String,
        attachmentId: String,
        output: OutputStream,
    ): Long = decryptFile(
        source = attachmentFile(tripId, attachmentId),
        output = output,
        maximumBytes = MAX_ATTACHMENT_BYTES,
    )

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
        val source = attachmentFile(tripId, attachmentId)
        return previewSessions.create("$tripId-$attachmentId", extension) { temporary ->
            BufferedOutputStream(temporary.outputStream()).use { output ->
                decryptFile(source, output, MAX_ATTACHMENT_BYTES)
            }
        }
    }

    fun exists(tripId: String, attachmentId: String): Boolean =
        attachmentFile(tripId, attachmentId).isFile

    fun verifyDecryptable(tripId: String, attachmentId: String): Boolean {
        val source = attachmentFile(tripId, attachmentId)
        if (!source.isFile) return false
        return try {
            decryptFile(source, DISCARDING_OUTPUT_STREAM, MAX_ATTACHMENT_BYTES)
            true
        } catch (_: Exception) {
            false
        }
    }

    fun delete(tripId: String, attachmentId: String) {
        attachmentFile(tripId, attachmentId).delete()
        thumbnailFile(tripId, attachmentId).delete()
        previewSessions.releaseMatching("$tripId-$attachmentId.")
    }

    fun releaseViewingPreview(file: File) = previewSessions.release(file)

    fun retainExternalPreview(file: File, uri: Uri, recipientUids: List<Int>) =
        previewSessions.retainExternally(file, uri, recipientUids)

    fun endViewingSession() = previewSessions.endSession()

    fun cleanInactiveViewingPreviews() = previewSessions.cleanInactive()

    fun deleteTrip(tripId: String) {
        val tripDirectory = safeChild(attachmentRoot, tripId)
        if (tripDirectory.exists() && !tripDirectory.deleteRecursively()) {
            throw IOException("Could not delete encrypted trip attachments")
        }
        previewSessions.releaseMatching("$tripId-")
    }

    private fun attachmentFile(tripId: String, attachmentId: String): File =
        safeChild(safeChild(attachmentRoot, tripId), "$attachmentId.lka")

    private fun thumbnailFile(tripId: String, attachmentId: String): File =
        safeChild(safeChild(attachmentRoot, tripId), "$attachmentId.lkt")

    private fun decryptFile(source: File, output: java.io.OutputStream, maximumBytes: Long): Long {
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
            return plaintextBytes
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
        private val DISCARDING_OUTPUT_STREAM = object : OutputStream() {
            override fun write(value: Int) = Unit
            override fun write(buffer: ByteArray, offset: Int, length: Int) = Unit
        }
    }
}

class AttachmentSizeLimitException : IllegalArgumentException()
