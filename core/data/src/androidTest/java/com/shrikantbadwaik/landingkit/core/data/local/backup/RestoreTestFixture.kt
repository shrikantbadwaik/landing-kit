package com.shrikantbadwaik.landingkit.core.data.local.backup

import android.content.Context
import android.net.Uri
import androidx.room.Room
import com.shrikantbadwaik.landingkit.core.data.local.attachment.EncryptedAttachmentStore
import com.shrikantbadwaik.landingkit.core.data.local.database.LandingKitDatabase
import com.shrikantbadwaik.landingkit.core.data.local.entity.AttachmentEntity
import com.shrikantbadwaik.landingkit.core.data.local.repository.LocalChecklistTemplateRepository
import com.shrikantbadwaik.landingkit.core.data.local.repository.LocalTripRepository
import com.shrikantbadwaik.landingkit.core.data.preferences.UserPreferences
import com.shrikantbadwaik.landingkit.core.model.*
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.time.LocalDate
import kotlinx.coroutines.flow.first

/** Entirely synthetic, isolated disk databases/files; also used by the process-death probe. */
internal class RestoreTestFixture(val context: Context, val root: File) {
    val preferences = UserPreferences(context)
    val targetStore = EncryptedAttachmentStore(context, File(root, "target-documents"))
    val sourceStore = EncryptedAttachmentStore(context, File(root, "source-documents"))
    val backup = File(root, "synthetic.lkb")
    val uri: Uri get() = Uri.fromFile(backup)
    var target = open("target.db")
    val recovery get() = BackupRestoreRecovery(context, target, targetStore, preferences,
        File(root, "restore.json"), File(root, "work"))
    fun repository(hook: (RestoreCheckpoint) -> Unit = {}) = repository(target, targetStore, recovery, hook)

    suspend fun seed() {
        preferences.setAttachmentProtectionEnabled(false)
        LocalTripRepository(target.tripDao(), targetStore, recovery = recovery).saveTrip(trip(EXISTING, "Existing journey"))
        saveDocument(target, targetStore, EXISTING, EXISTING_DOC, "existing private document".toByteArray())
        val source = open("source.db")
        try {
            val sourceRecovery = BackupRestoreRecovery(context, source, sourceStore, preferences,
                File(root, "source.json"), File(root, "source-work"))
            LocalTripRepository(source.tripDao(), sourceStore, recovery = sourceRecovery).saveTrip(trip(RESTORED, "Synthetic restore journey"))
            LocalChecklistTemplateRepository(source.checklistTemplateDao(), recovery = sourceRecovery).saveTemplate(
                ChecklistTemplate(TEMPLATE, "Synthetic routine", listOf(ChecklistTemplateItem("Save an address"))))
            saveDocument(source, sourceStore, RESTORED, DOCUMENT, DOCUMENT_BYTES)
            saveDocument(source, sourceStore, RESTORED, DOCUMENT_TWO, DOCUMENT_TWO_BYTES, quick = false)
            preferences.setAttachmentProtectionEnabled(true)
            repository(source, sourceStore, sourceRecovery).exportBackup(uri, PASSWORD.copyOf())
            preferences.setAttachmentProtectionEnabled(false)
        } finally { source.close() }
    }

    fun reopen() { target.close(); target = open("target.db") }
    suspend fun verifyExisting() {
        check(target.tripDao().getTrip(EXISTING)?.trip?.destination == "Existing journey")
        check(target.attachmentDao().getAttachment(EXISTING_DOC) != null)
        val out = ByteArrayOutputStream()
        targetStore.decryptForBackup(EXISTING, EXISTING_DOC, out)
        check(out.toByteArray().contentEquals("existing private document".toByteArray()))
    }
    suspend fun verifyRestored(present: Boolean) {
        check((target.tripDao().getTrip(RESTORED) != null) == present)
        check((target.attachmentDao().getAttachment(DOCUMENT) != null) == present)
        check((target.attachmentDao().getAttachment(DOCUMENT_TWO) != null) == present)
        check(target.checklistTemplateDao().observeTemplates().first().any { it.template.id == TEMPLATE } == present)
        check(targetStore.exists(RESTORED, DOCUMENT) == present)
        check(targetStore.exists(RESTORED, DOCUMENT_TWO) == present)
        check(preferences.attachmentProtectionEnabled.first() == present)
        if (present) {
            val out = ByteArrayOutputStream()
            targetStore.decryptForBackup(RESTORED, DOCUMENT, out)
            check(out.toByteArray().contentEquals(DOCUMENT_BYTES))
            val second = ByteArrayOutputStream()
            targetStore.decryptForBackup(RESTORED, DOCUMENT_TWO, second)
            check(second.toByteArray().contentEquals(DOCUMENT_TWO_BYTES))
            check(target.attachmentDao().getAttachment(DOCUMENT)?.isQuickAccess == true)
            check(target.tripDao().getTrip(RESTORED)?.items?.single()?.isComplete == true)
            check(target.tripDao().getTrip(RESTORED)?.trip?.arrivalReminderEnabled == true)
        }
    }
    fun close() { target.close() }
    private fun open(name: String): LandingKitDatabase {
        check(root.isDirectory || root.mkdirs())
        return Room.databaseBuilder(context, LandingKitDatabase::class.java, File(root, name).path).build()
    }
    private fun repository(db: LandingKitDatabase, store: EncryptedAttachmentStore,
        recovery: BackupRestoreRecovery, hook: (RestoreCheckpoint) -> Unit = {}) = LocalBackupRepository(
        context, LocalTripRepository(db.tripDao(), store, recovery = recovery),
        LocalChecklistTemplateRepository(db.checklistTemplateDao(), recovery = recovery), db.attachmentDao(), store,
        preferences, db, recovery, hook)
    private suspend fun saveDocument(db: LandingKitDatabase, store: EncryptedAttachmentStore,
        trip: String, id: String, bytes: ByteArray, quick: Boolean = true) {
        store.encryptFromBackup(trip, id, ByteArrayInputStream(bytes), bytes.size.toLong())
        db.attachmentDao().insertAttachment(AttachmentEntity(id, trip, "synthetic.pdf", "application/pdf",
            bytes.size.toLong(), System.currentTimeMillis(), quick))
    }
    private fun trip(id: String, name: String) = Trip(id, name, LocalDate.of(2026, 12, 20),
        ArrivalCard(accommodationName = "Synthetic hotel", address = "Synthetic address"),
        listOf(ArrivalItem(id = "$id-item", title = "Save confirmation", isComplete = true)),
        arrivalReminderEnabled = true)
    companion object {
        const val EXISTING = "fixture-existing"
        const val EXISTING_DOC = "fixture-existing-document"
        const val RESTORED = "fixture-restored"
        const val DOCUMENT = "fixture-restored-document"
        const val DOCUMENT_TWO = "fixture-second-document"
        const val TEMPLATE = "fixture-template"
        val DOCUMENT_BYTES = "%PDF-1.4\nSynthetic restore evidence\n%%EOF".toByteArray()
        val DOCUMENT_TWO_BYTES = "%PDF-1.4\nSecond synthetic confirmation\n%%EOF".toByteArray()
        val PASSWORD = "synthetic-restore-password".toCharArray()
    }
}
