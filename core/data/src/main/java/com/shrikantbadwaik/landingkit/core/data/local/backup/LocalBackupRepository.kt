package com.shrikantbadwaik.landingkit.core.data.local.backup

import android.content.Context
import android.net.Uri
import com.shrikantbadwaik.landingkit.core.data.BackupExportResult
import com.shrikantbadwaik.landingkit.core.data.BackupPreview
import com.shrikantbadwaik.landingkit.core.data.BackupRepository
import com.shrikantbadwaik.landingkit.core.data.BackupRestoreResult
import com.shrikantbadwaik.landingkit.core.data.BackupUnlockException
import com.shrikantbadwaik.landingkit.core.data.ChecklistTemplateRepository
import com.shrikantbadwaik.landingkit.core.data.TripRepository
import com.shrikantbadwaik.landingkit.core.data.UnsupportedBackupException
import com.shrikantbadwaik.landingkit.core.data.local.attachment.EncryptedAttachmentStore
import com.shrikantbadwaik.landingkit.core.data.local.dao.AttachmentDao
import com.shrikantbadwaik.landingkit.core.data.local.entity.AttachmentEntity
import com.shrikantbadwaik.landingkit.core.data.preferences.UserPreferences
import com.shrikantbadwaik.landingkit.core.model.ArrivalCard
import com.shrikantbadwaik.landingkit.core.model.ArrivalItem
import com.shrikantbadwaik.landingkit.core.model.ChecklistTemplate
import com.shrikantbadwaik.landingkit.core.model.ChecklistTemplateItem
import com.shrikantbadwaik.landingkit.core.model.ItemCategory
import com.shrikantbadwaik.landingkit.core.model.OfflineArrivalCheckStatus
import com.shrikantbadwaik.landingkit.core.model.Trip
import com.shrikantbadwaik.landingkit.core.model.TripValidator
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.ByteArrayOutputStream
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.EOFException
import java.io.File
import java.io.FilterInputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.CipherInputStream
import javax.crypto.CipherOutputStream
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

class LocalBackupRepository(
    private val context: Context,
    private val tripRepository: TripRepository,
    private val templateRepository: ChecklistTemplateRepository,
    private val attachmentDao: AttachmentDao,
    private val attachmentStore: EncryptedAttachmentStore,
    private val userPreferences: UserPreferences,
) : BackupRepository {
    private val operationMutex = Mutex()

    override suspend fun exportBackup(
        destination: Uri,
        password: CharArray,
    ): BackupExportResult = withContext(Dispatchers.IO) {
        operationMutex.withLock {
            require(password.size >= MIN_PASSWORD_LENGTH)
            val trips = tripRepository.observeTrips().first()
            val templates = templateRepository.observeTemplates().first()
            val attachments = attachmentDao.getAllAttachments()
            val protectionEnabled = userPreferences.attachmentProtectionEnabled.first()
            val payload = temporaryFile("backup-payload")
            try {
                DataOutputStream(BufferedOutputStream(payload.outputStream())).use { output ->
                    writePayloadHeader(output, trips, templates, attachments, protectionEnabled)
                    attachments.forEach { attachment ->
                        val actual = attachmentStore.decryptForBackup(
                            attachment.tripId,
                            attachment.id,
                            output,
                        )
                        require(actual == attachment.sizeBytes) { "Attachment size changed" }
                    }
                }
                val output = context.contentResolver.openOutputStream(destination, "wt")
                    ?: throw IOException("The selected backup location is unavailable")
                output.use { encryptPayload(payload, it, password) }
                BackupExportResult(trips.size, templates.size, attachments.size)
            } finally {
                payload.delete()
            }
        }
    }

    override suspend fun inspectBackup(
        source: Uri,
        password: CharArray,
    ): BackupPreview = withContext(Dispatchers.IO) {
        operationMutex.withLock {
            withDecryptedPayload(source, password) { payloadFile ->
                val payload = readAndValidatePayload(payloadFile)
                val existingTripIds = tripRepository.observeTrips().first().mapTo(mutableSetOf(), Trip::id)
                val existingTemplateIds = templateRepository.observeTemplates().first()
                    .mapTo(mutableSetOf(), ChecklistTemplate::id)
                BackupPreview(
                    createdAtMillis = payload.createdAtMillis,
                    tripCount = payload.trips.size,
                    newTripCount = payload.trips.count { it.id !in existingTripIds },
                    existingTripCount = payload.trips.count { it.id in existingTripIds },
                    templateCount = payload.templates.size,
                    newTemplateCount = payload.templates.count { it.id !in existingTemplateIds },
                    existingTemplateCount = payload.templates.count { it.id in existingTemplateIds },
                    attachmentCount = payload.attachments.size,
                    restorableAttachmentCount = payload.attachments.count {
                        it.tripId !in existingTripIds
                    },
                )
            }
        }
    }

    override suspend fun restoreBackup(
        source: Uri,
        password: CharArray,
    ): BackupRestoreResult = withContext(Dispatchers.IO) {
        operationMutex.withLock {
            withDecryptedPayload(source, password) { payloadFile ->
                val validated = readAndValidatePayload(payloadFile)
                val existingTripIds = tripRepository.observeTrips().first().mapTo(mutableSetOf(), Trip::id)
                val existingTemplateIds = templateRepository.observeTemplates().first()
                    .mapTo(mutableSetOf(), ChecklistTemplate::id)
                val tripsToRestore = validated.trips.filterNot { it.id in existingTripIds }
                val templatesToRestore = validated.templates.filterNot { it.id in existingTemplateIds }
                val restoredTrips = mutableListOf<String>()
                val restoredTemplates = mutableListOf<String>()
                val previousProtection = userPreferences.attachmentProtectionEnabled.first()
                var restoredAttachmentCount = 0
                try {
                    tripsToRestore.forEach { trip ->
                        tripRepository.saveTrip(trip)
                        restoredTrips += trip.id
                    }
                    templatesToRestore.forEach { template ->
                        templateRepository.saveTemplate(template)
                        restoredTemplates += template.id
                    }
                    DataInputStream(BufferedInputStream(payloadFile.inputStream())).use { input ->
                        val payload = readPayload(input) { attachment, bytes ->
                            if (attachment.tripId in restoredTrips) {
                                val encryptedSize = attachmentStore.encryptFromBackup(
                                    attachment.tripId,
                                    attachment.id,
                                    bytes,
                                    attachment.sizeBytes,
                                )
                                attachmentDao.insertAttachment(
                                    AttachmentEntity(
                                        id = attachment.id,
                                        tripId = attachment.tripId,
                                        displayName = attachment.displayName,
                                        mimeType = attachment.mimeType,
                                        sizeBytes = encryptedSize,
                                        createdAtMillis = attachment.createdAtMillis,
                                        isQuickAccess = attachment.isQuickAccess,
                                    ),
                                )
                                restoredAttachmentCount++
                            } else {
                                bytes.skipFully()
                            }
                        }
                        userPreferences.setAttachmentProtectionEnabled(payload.attachmentProtectionEnabled)
                    }
                    BackupRestoreResult(
                        restoredTripIds = restoredTrips,
                        restoredTemplateCount = restoredTemplates.size,
                        restoredAttachmentCount = restoredAttachmentCount,
                        skippedTripCount = validated.trips.size - restoredTrips.size,
                        skippedTemplateCount = validated.templates.size - restoredTemplates.size,
                    )
                } catch (error: Throwable) {
                    restoredTrips.asReversed().forEach { tripRepository.deleteTrip(it) }
                    restoredTemplates.asReversed().forEach { templateRepository.deleteTemplate(it) }
                    userPreferences.setAttachmentProtectionEnabled(previousProtection)
                    throw error
                }
            }
        }
    }

    private fun writePayloadHeader(
        output: DataOutputStream,
        trips: List<Trip>,
        templates: List<ChecklistTemplate>,
        attachments: List<AttachmentEntity>,
        protectionEnabled: Boolean,
    ) {
        output.writeInt(PAYLOAD_MAGIC)
        output.writeLong(System.currentTimeMillis())
        output.writeBoolean(protectionEnabled)
        output.writeCount(trips.size)
        trips.forEach { output.writeTrip(it) }
        output.writeCount(templates.size)
        templates.forEach { output.writeTemplate(it) }
        output.writeCount(attachments.size)
        attachments.forEach { output.writeAttachmentHeader(it) }
    }

    private suspend fun readAndValidatePayload(file: File): BackupPayload =
        DataInputStream(BufferedInputStream(file.inputStream())).use { input ->
            readPayload(input) { _, bytes -> bytes.skipFully() }.also { payload ->
                val tripIds = payload.trips.map(Trip::id).toSet()
                require(tripIds.size == payload.trips.size) { "Duplicate trip IDs" }
                require(payload.templates.map(ChecklistTemplate::id).toSet().size == payload.templates.size) {
                    "Duplicate template IDs"
                }
                require(payload.attachments.map(BackupAttachment::id).toSet().size == payload.attachments.size) {
                    "Duplicate attachment IDs"
                }
                require(payload.attachments.all { it.tripId in tripIds }) { "Orphaned attachment" }
                require(input.read() == -1) { "Unexpected backup data" }
            }
        }

    private suspend fun readPayload(
        input: DataInputStream,
        onAttachment: suspend (BackupAttachment, LimitedInputStream) -> Unit,
    ): BackupPayload {
        require(input.readInt() == PAYLOAD_MAGIC) { "Unsupported payload" }
        val createdAt = input.readLong().also { require(it > 0) }
        val protectionEnabled = input.readBoolean()
        val trips = List(input.readCount()) { input.readTrip() }
        val templates = List(input.readCount()) { input.readTemplate() }
        val attachmentCount = input.readCount()
        val attachments = List(attachmentCount) { input.readAttachmentHeader() }
        attachments.forEach { attachment ->
            val limited = LimitedInputStream(input, attachment.sizeBytes)
            onAttachment(attachment, limited)
            limited.skipFully()
            require(limited.remaining == 0L) { "Incomplete attachment" }
        }
        return BackupPayload(createdAt, protectionEnabled, trips, templates, attachments)
    }

    private fun encryptPayload(payload: File, destination: OutputStream, password: CharArray) {
        val salt = ByteArray(SALT_BYTES).also(SecureRandom()::nextBytes)
        val iv = ByteArray(IV_BYTES).also(SecureRandom()::nextBytes)
        val header = backupHeader(salt, iv)
        val cipher = Cipher.getInstance(TRANSFORMATION).apply {
            init(Cipher.ENCRYPT_MODE, deriveKey(password, salt), GCMParameterSpec(TAG_BITS, iv))
            updateAAD(header)
        }
        BufferedOutputStream(destination).use { output ->
            output.write(header)
            CipherOutputStream(output, cipher).use { encrypted -> payload.inputStream().use { it.copyTo(encrypted) } }
        }
    }

    private suspend fun <T> withDecryptedPayload(
        source: Uri,
        password: CharArray,
        block: suspend (File) -> T,
    ): T {
        require(password.size >= MIN_PASSWORD_LENGTH)
        val payload = temporaryFile("restored-payload")
        try {
            val raw = context.contentResolver.openInputStream(source)
                ?: throw UnsupportedBackupException()
            try {
                DataInputStream(BufferedInputStream(raw)).use { input ->
                    val magic = ByteArray(OUTER_MAGIC.size).also(input::readFully)
                    val version = input.readUnsignedByte()
                    val iterations = input.readInt()
                    val saltSize = input.readUnsignedByte()
                    val salt = ByteArray(saltSize).also(input::readFully)
                    val ivSize = input.readUnsignedByte()
                    val iv = ByteArray(ivSize).also(input::readFully)
                    require(magic.contentEquals(OUTER_MAGIC) && version == FORMAT_VERSION)
                    require(iterations in MIN_ACCEPTED_ITERATIONS..MAX_ACCEPTED_ITERATIONS)
                    require(saltSize == SALT_BYTES && ivSize == IV_BYTES)
                    val header = backupHeader(salt, iv, iterations)
                    val cipher = Cipher.getInstance(TRANSFORMATION).apply {
                        init(Cipher.DECRYPT_MODE, deriveKey(password, salt, iterations), GCMParameterSpec(TAG_BITS, iv))
                        updateAAD(header)
                    }
                    CipherInputStream(input, cipher).use { decrypted ->
                        payload.outputStream().use { output -> decrypted.copyLimitedTo(output) }
                    }
                }
            } catch (error: IllegalArgumentException) {
                throw UnsupportedBackupException(error)
            } catch (error: BackupUnlockException) {
                throw error
            } catch (error: Exception) {
                throw BackupUnlockException(error)
            }
            return try {
                block(payload)
            } catch (error: IllegalArgumentException) {
                throw UnsupportedBackupException(error)
            } catch (error: EOFException) {
                throw UnsupportedBackupException(error)
            }
        } finally {
            payload.delete()
        }
    }

    private fun backupHeader(
        salt: ByteArray,
        iv: ByteArray,
        iterations: Int = KDF_ITERATIONS,
    ): ByteArray = ByteArrayOutputStream().use { buffer ->
        DataOutputStream(buffer).use { output ->
            output.write(OUTER_MAGIC)
            output.writeByte(FORMAT_VERSION)
            output.writeInt(iterations)
            output.writeByte(salt.size)
            output.write(salt)
            output.writeByte(iv.size)
            output.write(iv)
        }
        buffer.toByteArray()
    }

    private fun deriveKey(
        password: CharArray,
        salt: ByteArray,
        iterations: Int = KDF_ITERATIONS,
    ): SecretKeySpec {
        val spec = PBEKeySpec(password, salt, iterations, KEY_BITS)
        return try {
            val bytes = SecretKeyFactory.getInstance(KDF_ALGORITHM).generateSecret(spec).encoded
            SecretKeySpec(bytes, "AES").also { bytes.fill(0) }
        } finally {
            spec.clearPassword()
        }
    }

    private fun temporaryFile(prefix: String): File =
        File.createTempFile(prefix, ".tmp", context.cacheDir)

    private fun InputStream.copyLimitedTo(output: OutputStream) {
        val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
        var total = 0L
        while (true) {
            val read = read(buffer)
            if (read == -1) break
            total += read
            require(total <= MAX_BACKUP_BYTES) { "Backup is too large" }
            output.write(buffer, 0, read)
        }
    }

    private data class BackupPayload(
        val createdAtMillis: Long,
        val attachmentProtectionEnabled: Boolean,
        val trips: List<Trip>,
        val templates: List<ChecklistTemplate>,
        val attachments: List<BackupAttachment>,
    )

    private data class BackupAttachment(
        val id: String,
        val tripId: String,
        val displayName: String,
        val mimeType: String,
        val sizeBytes: Long,
        val createdAtMillis: Long,
        val isQuickAccess: Boolean,
    )

    private class LimitedInputStream(
        source: InputStream,
        length: Long,
    ) : FilterInputStream(source) {
        var remaining: Long = length
            private set

        override fun read(): Int {
            if (remaining == 0L) return -1
            val value = super.read()
            if (value == -1) throw EOFException()
            remaining--
            return value
        }

        override fun read(buffer: ByteArray, offset: Int, length: Int): Int {
            if (remaining == 0L) return -1
            val read = super.read(buffer, offset, minOf(length.toLong(), remaining).toInt())
            if (read == -1) throw EOFException()
            remaining -= read
            return read
        }

        fun skipFully() {
            val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
            while (read(buffer) != -1) Unit
        }
    }

    private fun DataOutputStream.writeCount(value: Int) {
        require(value in 0..MAX_COLLECTION_SIZE)
        writeInt(value)
    }

    private fun DataInputStream.readCount(): Int = readInt().also {
        require(it in 0..MAX_COLLECTION_SIZE)
    }

    private fun DataOutputStream.writeText(value: String) {
        val bytes = value.toByteArray(Charsets.UTF_8)
        require(bytes.size <= MAX_TEXT_BYTES)
        writeInt(bytes.size)
        write(bytes)
    }

    private fun DataInputStream.readText(): String {
        val size = readInt().also { require(it in 0..MAX_TEXT_BYTES) }
        return ByteArray(size).also(::readFully).toString(Charsets.UTF_8)
    }

    private fun DataOutputStream.writeNullableText(value: String?) {
        writeBoolean(value != null)
        if (value != null) writeText(value)
    }

    private fun DataInputStream.readNullableText(): String? =
        if (readBoolean()) readText() else null

    private fun DataOutputStream.writeNullableLong(value: Long?) {
        writeBoolean(value != null)
        if (value != null) writeLong(value)
    }

    private fun DataInputStream.readNullableLong(): Long? =
        if (readBoolean()) readLong() else null

    private fun DataOutputStream.writeTrip(trip: Trip) {
        writeText(trip.id)
        writeText(trip.destination)
        writeLong(trip.arrivalDate.toEpochDay())
        with(trip.arrivalCard) {
            listOf(
                accommodationName, address, localAddress, bookingReference,
                transferInstructions, currencyNote, emergencyContact, dietaryPhrase,
                fallbackTransport, lastServiceInfo, checkInCutoff, lateArrivalInstructions,
                accommodationContact, expectedCostNote, alternateStayNote,
            ).forEach { writeText(it) }
        }
        writeBoolean(trip.arrivalReminderEnabled)
        writeText(trip.offlineCheckStatus.name)
        writeNullableLong(trip.offlineCheckedAtMillis)
        writeNullableText(trip.offlineCheckContentFingerprint)
        writeText(trip.planBCheckStatus.name)
        writeNullableLong(trip.planBCheckedAtMillis)
        writeNullableText(trip.planBContentFingerprint)
        writeCount(trip.items.size)
        trip.items.forEach { item ->
            writeText(item.id)
            writeText(item.title)
            writeText(item.detail)
            writeText(item.category.name)
            writeBoolean(item.isComplete)
        }
    }

    private fun DataInputStream.readTrip(): Trip {
        val id = readText()
        val destination = readText()
        val arrivalDate = java.time.LocalDate.ofEpochDay(readLong())
        val values = List(15) { readText() }
        val reminder = readBoolean()
        val offlineStatus = OfflineArrivalCheckStatus.valueOf(readText())
        val offlineAt = readNullableLong()
        val offlineFingerprint = readNullableText()
        val planBStatus = OfflineArrivalCheckStatus.valueOf(readText())
        val planBAt = readNullableLong()
        val planBFingerprint = readNullableText()
        val items = List(readCount()) {
            ArrivalItem(
                id = readText(),
                title = readText(),
                detail = readText(),
                category = ItemCategory.valueOf(readText()),
                isComplete = readBoolean(),
            )
        }
        return Trip(
            id = id,
            destination = destination,
            arrivalDate = arrivalDate,
            arrivalCard = ArrivalCard(
                accommodationName = values[0], address = values[1], localAddress = values[2],
                bookingReference = values[3], transferInstructions = values[4],
                currencyNote = values[5], emergencyContact = values[6], dietaryPhrase = values[7],
                fallbackTransport = values[8], lastServiceInfo = values[9],
                checkInCutoff = values[10], lateArrivalInstructions = values[11],
                accommodationContact = values[12], expectedCostNote = values[13],
                alternateStayNote = values[14],
            ),
            items = items,
            arrivalReminderEnabled = reminder,
            offlineCheckStatus = offlineStatus,
            offlineCheckedAtMillis = offlineAt,
            offlineCheckContentFingerprint = offlineFingerprint,
            planBCheckStatus = planBStatus,
            planBCheckedAtMillis = planBAt,
            planBContentFingerprint = planBFingerprint,
        ).also { require(TripValidator.validate(it).isEmpty()) }
    }

    private fun DataOutputStream.writeTemplate(template: ChecklistTemplate) {
        writeText(template.id)
        writeText(template.name)
        writeCount(template.items.size)
        template.items.forEach { item ->
            writeText(item.title)
            writeText(item.detail)
            writeText(item.category.name)
        }
    }

    private fun DataInputStream.readTemplate() = ChecklistTemplate(
        id = readText(),
        name = readText(),
        items = List(readCount()) {
            ChecklistTemplateItem(readText(), readText(), ItemCategory.valueOf(readText()))
        },
    ).also { template ->
        require(template.name.isNotBlank() && template.items.isNotEmpty())
        require(template.items.all { it.title.isNotBlank() })
    }

    private fun DataOutputStream.writeAttachmentHeader(attachment: AttachmentEntity) {
        writeText(attachment.id)
        writeText(attachment.tripId)
        writeText(attachment.displayName)
        writeText(attachment.mimeType)
        writeLong(attachment.sizeBytes)
        writeLong(attachment.createdAtMillis)
        writeBoolean(attachment.isQuickAccess)
    }

    private fun DataInputStream.readAttachmentHeader() = BackupAttachment(
        id = readText(),
        tripId = readText(),
        displayName = readText(),
        mimeType = readText(),
        sizeBytes = readLong().also { require(it in 0..EncryptedAttachmentStore.MAX_ATTACHMENT_BYTES) },
        createdAtMillis = readLong().also { require(it > 0) },
        isQuickAccess = readBoolean(),
    ).also { require(it.mimeType == "application/pdf" || it.mimeType.startsWith("image/")) }

    private companion object {
        val OUTER_MAGIC = byteArrayOf(0x4C, 0x4B, 0x42, 0x31)
        const val PAYLOAD_MAGIC = 0x4C4B5031
        const val FORMAT_VERSION = 1
        const val MIN_PASSWORD_LENGTH = 12
        const val SALT_BYTES = 16
        const val IV_BYTES = 12
        const val TAG_BITS = 128
        const val KEY_BITS = 256
        const val KDF_ITERATIONS = 210_000
        const val MIN_ACCEPTED_ITERATIONS = 100_000
        const val MAX_ACCEPTED_ITERATIONS = 2_000_000
        const val KDF_ALGORITHM = "PBKDF2WithHmacSHA256"
        const val TRANSFORMATION = "AES/GCM/NoPadding"
        const val MAX_COLLECTION_SIZE = 10_000
        const val MAX_TEXT_BYTES = 1024 * 1024
        const val MAX_BACKUP_BYTES = 512L * 1024L * 1024L
    }
}
