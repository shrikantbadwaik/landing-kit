package com.shrikantbadwaik.landingkit.feature.kits.notification

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.shrikantbadwaik.landingkit.core.model.Trip
import com.shrikantbadwaik.landingkit.feature.kits.R
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.Instant
import java.time.ZoneId
import javax.inject.Inject
import javax.inject.Singleton
import androidx.core.net.toUri
import com.shrikantbadwaik.landingkit.core.data.TripRepository
import kotlinx.coroutines.flow.first
import com.shrikantbadwaik.landingkit.core.data.local.reminder.ReminderCancellationStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

@Singleton
class ArrivalReminderScheduler @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val repository: TripRepository,
    private val cancellationStore: ReminderCancellationStore,
) {
    private val alarmManager = context.getSystemService(AlarmManager::class.java)
    private val notificationManager = NotificationManagerCompat.from(context)
    private val mutex = Mutex()
    private val cancellationProcessor = ReminderCancellationProcessor(
        cancellationStore, ::cancelAlarm, ::cancelNotification,
    )

    suspend fun sync(trip: Trip) = withContext(Dispatchers.IO) {
        mutex.withLock {
            cancellationProcessor.drain()
            // A UI/startup snapshot can become stale during deletion or a reminder toggle.
            val current = repository.getTrip(trip.id)
            if (current?.arrivalReminderEnabled == true) scheduleLocked(current)
            else cancelLocked(trip.id)
        }
    }

    suspend fun reconcile(repository: TripRepository = this.repository) = withContext(Dispatchers.IO) {
        mutex.withLock {
            // Deleted IDs survive outside Room until both external effects are cancelled.
            cancellationProcessor.drain()
            repository.observeTrips().first().forEach { trip ->
                if (trip.arrivalReminderEnabled) scheduleLocked(trip) else cancelLocked(trip.id)
            }
        }
    }

    private suspend fun scheduleLocked(trip: Trip) {
        val triggerAt = arrivalReminderTriggerAt(
            arrivalDate = trip.arrivalDate,
            zoneId = ZoneId.systemDefault(),
            now = Instant.now(),
        ) ?: run {
            cancelLocked(trip.id)
            return
        }

        val intent = Intent(context, ArrivalReminderReceiver::class.java)
            .setAction(ACTION_SHOW_ARRIVAL_REMINDER)
            .setData(reminderUri(trip.id))
            .putExtra(ArrivalReminderIntent.EXTRA_TRIP_ID, trip.id)
        alarmManager.setAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            triggerAt.toEpochMilli(),
            PendingIntent.getBroadcast(
                context,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
            ),
        )
    }

    suspend fun cancel(tripId: String) = withContext(Dispatchers.IO) {
        mutex.withLock { cancelLocked(tripId) }
    }

    private suspend fun cancelLocked(tripId: String) {
        cancellationProcessor.drain(tripId)
        cancelAlarm(tripId)
        cancelNotification(tripId)
    }

    internal fun cancelAlarm(tripId: String) {
        val intent = Intent(context, ArrivalReminderReceiver::class.java)
            .setAction(ACTION_SHOW_ARRIVAL_REMINDER)
            .setData(reminderUri(tripId))
        PendingIntent.getBroadcast(
            context,
            0,
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE,
        )?.let {
            alarmManager.cancel(it)
            it.cancel()
        }
    }

    internal fun cancelNotification(tripId: String) {
        notificationManager.cancel(notificationId(tripId))
    }

    @SuppressLint("MissingPermission")
    internal suspend fun show(tripId: String) = withContext(Dispatchers.IO) {
        mutex.withLock {
            cancellationProcessor.drain()
            val trip = repository.getTrip(tripId)
            if (trip?.arrivalReminderEnabled == true) showNotification(tripId)
            else cancelLocked(tripId)
        }
    }

    @SuppressLint("MissingPermission")
    private fun showNotification(tripId: String) {
        if (!canPostNotifications()) return
        createChannel()

        val launchIntent = context.packageManager
            .getLaunchIntentForPackage(context.packageName)
            ?.setAction(ArrivalReminderIntent.ACTION_OPEN_ARRIVAL_MODE)
            ?.setData(arrivalModeUri(tripId))
            ?.putExtra(ArrivalReminderIntent.EXTRA_TRIP_ID, tripId)
            ?.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            ?: return
        val contentIntent = PendingIntent.getActivity(
            context,
            0,
            launchIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification_arrival)
            .setColor(Color.rgb(20, 82, 76))
            .setContentTitle(context.getString(R.string.arrival_reminder_notification_title))
            .setContentText(context.getString(R.string.arrival_reminder_notification_body))
            .setContentIntent(contentIntent)
            .setAutoCancel(true)
            .setOnlyAlertOnce(true)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setVisibility(NotificationCompat.VISIBILITY_PRIVATE)
            .build()
        notificationManager.notify(notificationId(tripId), notification)
    }

    private fun canPostNotifications(): Boolean =
        notificationManager.areNotificationsEnabled() &&
            (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS,
                ) == PackageManager.PERMISSION_GRANTED)

    private fun createChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            context.getString(R.string.arrival_reminder_channel_name),
            NotificationManager.IMPORTANCE_DEFAULT,
        ).apply {
            description = context.getString(R.string.arrival_reminder_channel_description)
            lockscreenVisibility = NotificationCompat.VISIBILITY_PRIVATE
        }
        context.getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
    }

    private fun reminderUri(tripId: String): Uri =
        "landingkit://arrival-reminder/${Uri.encode(tripId)}".toUri()

    private fun arrivalModeUri(tripId: String): Uri =
        "landingkit://arrival-mode/${Uri.encode(tripId)}".toUri()

    private fun notificationId(tripId: String): Int = tripId.hashCode() and Int.MAX_VALUE

    internal companion object {
        const val CHANNEL_ID = "arrival_day_shortcuts"
        const val ACTION_SHOW_ARRIVAL_REMINDER =
            "com.shrikantbadwaik.landingkit.action.SHOW_ARRIVAL_REMINDER"
    }
}
