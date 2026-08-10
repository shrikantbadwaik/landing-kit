package com.shrikantbadwaik.landingkit.feature.kits.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.shrikantbadwaik.landingkit.core.data.TripRepository
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ArrivalReminderReceiver : BroadcastReceiver() {
    @Inject lateinit var scheduler: ArrivalReminderScheduler
    @Inject lateinit var repository: TripRepository

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            ArrivalReminderScheduler.ACTION_SHOW_ARRIVAL_REMINDER -> {
                val tripId = intent.getStringExtra(ArrivalReminderIntent.EXTRA_TRIP_ID) ?: return
                val destination = intent.getStringExtra(
                    ArrivalReminderScheduler.DESTINATION_EXTRA,
                ) ?: return
                scheduler.show(tripId = tripId, destination = destination)
            }

            Intent.ACTION_BOOT_COMPLETED,
            Intent.ACTION_MY_PACKAGE_REPLACED,
            Intent.ACTION_TIMEZONE_CHANGED,
            -> rescheduleEnabledTrips()
        }
    }

    private fun rescheduleEnabledTrips() {
        val pendingResult = goAsync()
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            try {
                repository.observeTrips().first()
                    .filter { it.arrivalReminderEnabled }
                    .forEach(scheduler::sync)
            } finally {
                pendingResult.finish()
            }
        }
    }
}
