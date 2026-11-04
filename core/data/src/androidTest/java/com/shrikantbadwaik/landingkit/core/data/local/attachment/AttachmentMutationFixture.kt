package com.shrikantbadwaik.landingkit.core.data.local.attachment

import android.content.Context
import android.net.Uri
import androidx.room.Room
import com.shrikantbadwaik.landingkit.core.data.local.backup.BackupRecoveryAccess
import com.shrikantbadwaik.landingkit.core.data.local.backup.BackupRestoreRecovery
import com.shrikantbadwaik.landingkit.core.data.local.database.LandingKitDatabase
import com.shrikantbadwaik.landingkit.core.data.local.entity.AttachmentEntity
import com.shrikantbadwaik.landingkit.core.data.local.mapper.toEntity
import com.shrikantbadwaik.landingkit.core.data.local.repository.LocalAttachmentRepository
import com.shrikantbadwaik.landingkit.core.data.local.repository.LocalTripRepository
import com.shrikantbadwaik.landingkit.core.data.preferences.UserPreferences
import com.shrikantbadwaik.landingkit.core.data.local.reminder.ReminderCancellationStore
import com.shrikantbadwaik.landingkit.core.model.ArrivalCard
import com.shrikantbadwaik.landingkit.core.model.ArrivalItem
import com.shrikantbadwaik.landingkit.core.model.Trip
import java.io.ByteArrayInputStream
import java.io.File
import java.time.LocalDate

/** Isolated disk DB/files, never production traveller records. Also used by the process probe. */
internal class AttachmentMutationFixture(val context: Context, val root: File) {
    val store = EncryptedAttachmentStore(context, File(root, "documents"))
    val access = BackupRecoveryAccess()
    val reminderCancellations = ReminderCancellationStore(File(root, "reminder-cancellations.json"))
    var database = open()
    val journal = File(root, "restore.json.documents")
    val source = File(root, "source.pdf")
    val sourceUri: Uri get() = Uri.Builder().scheme("content")
        .authority("com.shrikantbadwaik.landingkit.core.data.test.mutation-evidence")
        .appendQueryParameter("file", source.path).build()

    fun recovery(hook: (AttachmentMutationCheckpoint) -> Unit = {}) = BackupRestoreRecovery(
        context, database, store, UserPreferences(context), File(root, "restore.json"),
        File(root, "work"), access, reminderCancellations,
        AttachmentMutationRecovery(database, store, journal, hook, reminderCancellations),
    )
    fun attachments(recovery: BackupRestoreRecovery) =
        LocalAttachmentRepository(context.contentResolver, database.attachmentDao(), store, access, recovery)
    fun trips(recovery: BackupRestoreRecovery) =
        LocalTripRepository(database.tripDao(), store, access, recovery)

    suspend fun seed() {
        source.writeBytes(BYTES)
        for (id in listOf(TRIP, OTHER)) {
            val trip = Trip(id, "Synthetic $id", LocalDate.of(2026, 12, 20),
                ArrivalCard(accommodationName = "Synthetic hotel", address = "Synthetic address"),
                listOf(ArrivalItem(id = "$id-item", title = "Save booking")))
            database.tripDao().replaceTrip(trip.toEntity(),
                trip.items.mapIndexed { index, item -> item.toEntity(id, index) })
        }
        document(TRIP, DOCUMENT, quick = false)
        document(TRIP, KEEP, quick = true)
        document(OTHER, OTHER_DOC, quick = true)
    }

    suspend fun document(trip: String, id: String, quick: Boolean = false) {
        store.encrypt(trip, id, ByteArrayInputStream(BYTES))
        store.saveThumbnail(trip, id, byteArrayOf(1, 2, 3))
        database.attachmentDao().insertAttachment(AttachmentEntity(id, trip, "$id.pdf",
            "application/pdf", BYTES.size.toLong(), System.currentTimeMillis(), quick))
    }

    fun reopen() { database.close(); database = open() }
    fun close() { database.close() }
    private fun open(): LandingKitDatabase {
        check(root.isDirectory || root.mkdirs())
        return Room.databaseBuilder(context, LandingKitDatabase::class.java,
            File(root, "mutation.db").path).build()
    }

    suspend fun verifyOther() {
        check(database.tripDao().getTrip(OTHER) != null)
        check(database.attachmentDao().getAttachment(OTHER_DOC)?.isQuickAccess == true)
        check(store.verifyDecryptable(OTHER, OTHER_DOC))
    }

    companion object {
        const val TRIP = "mutation-trip"
        const val OTHER = "mutation-other"
        const val DOCUMENT = "mutation-document"
        const val KEEP = "mutation-keep"
        const val OTHER_DOC = "mutation-other-document"
        val BYTES = "%PDF-1.4\nSynthetic staged mutation\n%%EOF".toByteArray()
    }
}
