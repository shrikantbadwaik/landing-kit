package com.shrikantbadwaik.landingkit.feature.kits.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.shrikantbadwaik.landingkit.core.data.TripRepository
import com.shrikantbadwaik.landingkit.core.data.local.backup.BackupRecoveryAccess
import com.shrikantbadwaik.landingkit.core.data.local.backup.BackupRecoveryStatus
import android.util.Log
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull

@AndroidEntryPoint
class ArrivalReminderReceiver : BroadcastReceiver() {
    @Inject lateinit var scheduler: ArrivalReminderScheduler
    @Inject lateinit var repository: TripRepository
    @Inject lateinit var recoveryAccess: BackupRecoveryAccess

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action ?: return
        if (action !in setOf(ArrivalReminderScheduler.ACTION_SHOW_ARRIVAL_REMINDER,
                Intent.ACTION_BOOT_COMPLETED, Intent.ACTION_MY_PACKAGE_REPLACED,
                Intent.ACTION_TIMEZONE_CHANGED)) return
        val tripId = intent.getStringExtra(ArrivalReminderIntent.EXTRA_TRIP_ID)
        if (action == ArrivalReminderScheduler.ACTION_SHOW_ARRIVAL_REMINDER && tripId == null) return
        val pendingResult = goAsync()
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            try {
                // Cold-start recovery is asynchronous. Wait for a safe state rather than
                // dropping every alarm arriving before Application recovery completes.
                val status = withTimeoutOrNull(8_000) {
                    recoveryAccess.status.first { it != BackupRecoveryStatus.Recovering }
                }
                if (status != BackupRecoveryStatus.Ready) return@launch
                if (action == ArrivalReminderScheduler.ACTION_SHOW_ARRIVAL_REMINDER) {
                    repository.getTrip(checkNotNull(tripId))
                        ?.takeIf { it.arrivalReminderEnabled }
                        ?.let { scheduler.show(tripId = it.id) }
                } else {
                    scheduler.reconcile(repository)
                }
            } catch (error: Exception) {
                Log.w("ArrivalReminders", "Could not handle an arrival reminder", error)
            } finally {
                pendingResult.finish()
            }
        }
    }
}
