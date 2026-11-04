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

@Singleton
class ArrivalReminderScheduler @Inject constructor(
    @param:ApplicationContext private val context: Context,
) {
    private val alarmManager = context.getSystemService(AlarmManager::class.java)
    private val notificationManager = NotificationManagerCompat.from(context)

    fun sync(trip: Trip) {
        if (trip.arrivalReminderEnabled) schedule(trip) else cancel(trip.id)
    }

    suspend fun reconcile(repository: TripRepository) {
        repository.observeTrips().first().forEach(::sync)
    }

    fun schedule(trip: Trip) {
        val triggerAt = arrivalReminderTriggerAt(
            arrivalDate = trip.arrivalDate,
            zoneId = ZoneId.systemDefault(),
            now = Instant.now(),
        ) ?: run {
            cancel(trip.id)
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

    fun cancel(tripId: String) {
        val intent = Intent(context, ArrivalReminderReceiver::class.java)
            .setAction(ACTION_SHOW_ARRIVAL_REMINDER)
            .setData(reminderUri(tripId))
        PendingIntent.getBroadcast(
            context,
            0,
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE,
        )?.let(alarmManager::cancel)
        notificationManager.cancel(notificationId(tripId))
    }

    @SuppressLint("MissingPermission")
    internal fun show(tripId: String) {
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
