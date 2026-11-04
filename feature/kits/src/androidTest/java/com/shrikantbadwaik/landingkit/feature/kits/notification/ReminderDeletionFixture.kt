package com.shrikantbadwaik.landingkit.feature.kits.notification

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.room.Room
import com.shrikantbadwaik.landingkit.core.data.local.attachment.AttachmentMutationRecovery
import com.shrikantbadwaik.landingkit.core.data.local.attachment.EncryptedAttachmentStore
import com.shrikantbadwaik.landingkit.core.data.local.backup.BackupRestoreRecovery
import com.shrikantbadwaik.landingkit.core.data.local.database.LandingKitDatabase
import com.shrikantbadwaik.landingkit.core.data.local.reminder.ReminderCancellationStore
import com.shrikantbadwaik.landingkit.core.data.local.repository.LocalTripRepository
import com.shrikantbadwaik.landingkit.core.data.preferences.UserPreferences
import com.shrikantbadwaik.landingkit.core.model.ArrivalCard
import com.shrikantbadwaik.landingkit.core.model.Trip
import java.io.File
import java.time.LocalDate
import java.util.UUID
import kotlinx.coroutines.delay
import kotlinx.coroutines.withTimeout

internal class ReminderDeletionFixture(val context: Context, val root: File) {
    private val identityFile = File(root, "identity")
    val tripId: String
    val otherId: String
    init {
        check(root.isDirectory || root.mkdirs())
        val identity = if (identityFile.exists()) identityFile.readText()
            else UUID.randomUUID().toString().also(identityFile::writeText)
        tripId = "reminder-$identity"
        otherId = "other-$identity"
    }
    val queue = ReminderCancellationStore(File(root, "cancellations.json"))
    private val store = EncryptedAttachmentStore(context, File(root, "documents"))
    var database = open()
    fun recovery(afterTripDeleted: () -> Unit = {}): BackupRestoreRecovery =
        BackupRestoreRecovery(context, database, store, UserPreferences(context),
            File(root, "restore.json"), File(root, "work"), reminderCancellations = queue,
            mutations = AttachmentMutationRecovery(database, store, File(root, "deletion.json"),
                reminderCancellations = queue, afterTripMetadataDeleted = afterTripDeleted))
    fun repository(recovery: BackupRestoreRecovery = recovery()) =
        LocalTripRepository(database.tripDao(), store, recovery = recovery)
    fun scheduler() = ArrivalReminderScheduler(context, repository(), queue)
    val trip get() = Trip(tripId, "Synthetic reminder", LocalDate.now().plusDays(1),
        ArrivalCard(accommodationName = "Synthetic stay", address = "Synthetic address"),
        emptyList(), arrivalReminderEnabled = true)
    val other get() = trip.copy(id = otherId, destination = "Unrelated synthetic reminder")

    suspend fun seed() {
        repository().saveTrip(trip)
        repository().saveTrip(other)
        val scheduler = scheduler()
        scheduler.sync(trip); scheduler.sync(other)
        scheduler.show(tripId); scheduler.show(otherId)
        withTimeout(5_000) {
            while (!posted(tripId) || !posted(otherId)) delay(50)
        }
        check(alarmExists(tripId) && alarmExists(otherId))
    }

    fun alarmExists(id: String): Boolean = PendingIntent.getBroadcast(context, 0,
        Intent(context, ArrivalReminderReceiver::class.java)
            .setAction(ArrivalReminderScheduler.ACTION_SHOW_ARRIVAL_REMINDER)
            .setData(Uri.parse("landingkit://arrival-reminder/${Uri.encode(id)}")),
        PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE) != null

    fun posted(id: String) = context.getSystemService(NotificationManager::class.java)
        .activeNotifications.any { it.id == (id.hashCode() and Int.MAX_VALUE) }

    suspend fun verifyRecovered() {
        recovery().recover()
        scheduler().reconcile()
        scheduler().reconcile()
        withTimeout(5_000) { while (posted(tripId)) delay(25) }
        check(database.tripDao().getTrip(tripId) == null)
        check(queue.pending().isEmpty())
        check(!alarmExists(tripId) && !posted(tripId))
        check(alarmExists(otherId) && posted(otherId))
        check(database.tripDao().getTrip(otherId) != null)
    }

    fun reopen() { database.close(); database = open() }
    fun close() { database.close() }
    suspend fun cleanupEffects() {
        scheduler().cancel(tripId); scheduler().cancel(otherId)
    }
    private fun open() = Room.databaseBuilder(context, LandingKitDatabase::class.java,
        File(root, "reminders.db").path).build()
}
