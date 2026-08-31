package com.shrikantbadwaik.landingkit.core.data.local.backup

import android.content.Context
import android.net.Uri
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.shrikantbadwaik.landingkit.core.data.BackupUnlockException
import com.shrikantbadwaik.landingkit.core.data.local.attachment.EncryptedAttachmentStore
import com.shrikantbadwaik.landingkit.core.data.local.database.LandingKitDatabase
import com.shrikantbadwaik.landingkit.core.data.local.entity.AttachmentEntity
import com.shrikantbadwaik.landingkit.core.data.local.repository.LocalChecklistTemplateRepository
import com.shrikantbadwaik.landingkit.core.data.local.repository.LocalTripRepository
import com.shrikantbadwaik.landingkit.core.data.preferences.UserPreferences
import com.shrikantbadwaik.landingkit.core.model.ArrivalCard
import com.shrikantbadwaik.landingkit.core.model.ArrivalItem
import com.shrikantbadwaik.landingkit.core.model.ChecklistTemplate
import com.shrikantbadwaik.landingkit.core.model.ChecklistTemplateItem
import com.shrikantbadwaik.landingkit.core.model.Trip
import java.io.ByteArrayInputStream
import java.io.File
import java.io.RandomAccessFile
import java.time.LocalDate
import java.util.UUID
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LocalBackupRepositoryTest {
    private lateinit var context: Context
    private lateinit var sourceDatabase: LandingKitDatabase
    private lateinit var targetDatabase: LandingKitDatabase
    private lateinit var store: EncryptedAttachmentStore
    private lateinit var backupFile: File
    private val tripId = UUID.randomUUID().toString()
    private val templateId = UUID.randomUUID().toString()
    private val attachmentId = UUID.randomUUID().toString()

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        sourceDatabase = Room.inMemoryDatabaseBuilder(context, LandingKitDatabase::class.java).build()
        targetDatabase = Room.inMemoryDatabaseBuilder(context, LandingKitDatabase::class.java).build()
        store = EncryptedAttachmentStore(context)
        backupFile = File(context.cacheDir, "round-trip-${UUID.randomUUID()}.lkb")
    }

    @After
    fun tearDown() {
        sourceDatabase.close()
        targetDatabase.close()
        store.deleteTrip(tripId)
        backupFile.delete()
    }

    @Test
    fun encryptedRoundTripRestoresNewContentAndSkipsDuplicates() = runBlocking {
        val source = repository(sourceDatabase)
        val target = repository(targetDatabase)
        val sourceTripRepository = LocalTripRepository(sourceDatabase.tripDao(), store)
        val sourceTemplateRepository = LocalChecklistTemplateRepository(
            sourceDatabase.checklistTemplateDao(),
        )
        sourceTripRepository.saveTrip(sampleTrip())
        sourceTemplateRepository.saveTemplate(
            ChecklistTemplate(
                id = templateId,
                name = "My arrival routine",
                items = listOf(ChecklistTemplateItem("Download an offline map")),
            ),
        )
        val attachmentBytes = "%PDF-1.4\nLanding Kit backup test\n%%EOF".toByteArray()
        store.encryptFromBackup(
            tripId,
            attachmentId,
            ByteArrayInputStream(attachmentBytes),
            attachmentBytes.size.toLong(),
        )
        sourceDatabase.attachmentDao().insertAttachment(
            AttachmentEntity(
                id = attachmentId,
                tripId = tripId,
                displayName = "booking.pdf",
                mimeType = "application/pdf",
                sizeBytes = attachmentBytes.size.toLong(),
                createdAtMillis = System.currentTimeMillis(),
                isQuickAccess = true,
            ),
        )

        val exported = source.exportBackup(Uri.fromFile(backupFile), PASSWORD.copyOf())
        assertEquals(1, exported.tripCount)
        assertFalse(String(backupFile.readBytes()).contains("Kyoto"))

        val preview = target.inspectBackup(Uri.fromFile(backupFile), PASSWORD.copyOf())
        assertEquals(1, preview.newTripCount)
        assertEquals(1, preview.newTemplateCount)
        assertEquals(1, preview.restorableAttachmentCount)

        val restored = target.restoreBackup(Uri.fromFile(backupFile), PASSWORD.copyOf())
        assertEquals(listOf(tripId), restored.restoredTripIds)
        assertEquals("Kyoto, Japan", targetDatabase.tripDao().getTrip(tripId)?.trip?.destination)
        assertNotNull(targetDatabase.attachmentDao().getAttachment(attachmentId))
        assertTrue(store.verifyDecryptable(tripId, attachmentId))

        val duplicatePreview = target.inspectBackup(Uri.fromFile(backupFile), PASSWORD.copyOf())
        assertFalse(duplicatePreview.hasNewContent)
        val duplicateRestore = target.restoreBackup(Uri.fromFile(backupFile), PASSWORD.copyOf())
        assertEquals(1, duplicateRestore.skippedTripCount)
        assertEquals(1, duplicateRestore.skippedTemplateCount)
    }

    @Test
    fun wrongPasswordAndTamperingCannotBeInspected() = runBlocking {
        val source = repository(sourceDatabase)
        LocalTripRepository(sourceDatabase.tripDao(), store).saveTrip(sampleTrip())
        source.exportBackup(Uri.fromFile(backupFile), PASSWORD.copyOf())

        assertUnlockFails {
            repository(targetDatabase).inspectBackup(
                Uri.fromFile(backupFile),
                "incorrect-password".toCharArray(),
            )
        }

        RandomAccessFile(backupFile, "rw").use { file ->
            file.seek(file.length() - 1)
            val lastByte = file.read()
            file.seek(file.length() - 1)
            file.write(lastByte.xor(0x01))
        }
        assertUnlockFails {
            repository(targetDatabase).inspectBackup(Uri.fromFile(backupFile), PASSWORD.copyOf())
        }
    }

    private fun repository(database: LandingKitDatabase) = LocalBackupRepository(
        context = context,
        tripRepository = LocalTripRepository(database.tripDao(), store),
        templateRepository = LocalChecklistTemplateRepository(database.checklistTemplateDao()),
        attachmentDao = database.attachmentDao(),
        attachmentStore = store,
        userPreferences = UserPreferences(context),
    )

    private fun sampleTrip() = Trip(
        id = tripId,
        destination = "Kyoto, Japan",
        arrivalDate = LocalDate.of(2026, 8, 29),
        arrivalCard = ArrivalCard(
            accommodationName = "Machiya Stay Gion",
            address = "Gionmachi Minamigawa, Kyoto",
            localAddress = "京都市東山区祇園町南側",
            bookingReference = "LK-4821",
        ),
        items = listOf(ArrivalItem(title = "Download tickets", isComplete = true)),
    )

    private suspend fun assertUnlockFails(block: suspend () -> Unit) {
        var failure: Throwable? = null
        try {
            block()
        } catch (error: Throwable) {
            failure = error
        }
        assertTrue(failure is BackupUnlockException)
    }

    private companion object {
        val PASSWORD = "correct horse battery staple".toCharArray()
    }
}
