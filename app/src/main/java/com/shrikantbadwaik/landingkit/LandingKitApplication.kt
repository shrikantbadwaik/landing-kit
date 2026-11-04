package com.shrikantbadwaik.landingkit

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import com.shrikantbadwaik.landingkit.core.data.local.attachment.EncryptedAttachmentStore
import com.shrikantbadwaik.landingkit.core.data.local.backup.BackupRestoreRecovery
import com.shrikantbadwaik.landingkit.core.data.TripRepository
import com.shrikantbadwaik.landingkit.feature.kits.notification.ArrivalReminderScheduler
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import android.util.Log

@HiltAndroidApp
class LandingKitApplication : Application() {
    private val recoveryScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var recoveryJob: Job? = null
    @Inject lateinit var attachmentStore: EncryptedAttachmentStore
    @Inject lateinit var backupRecovery: BackupRestoreRecovery
    @Inject lateinit var trips: TripRepository
    @Inject lateinit var reminders: ArrivalReminderScheduler

    override fun onCreate() {
        super.onCreate()
        // The access gate protects consumers while recovery runs without blocking startup.
        retrySavedTravel()
        recoveryScope.launch { attachmentStore.cleanInactiveViewingPreviews() }
    }

    fun retrySavedTravel() {
        if (recoveryJob?.isActive == true) return
        recoveryJob = recoveryScope.launch { recoverSavedTravel() }
    }

    private suspend fun recoverSavedTravel(): Boolean = withContext(Dispatchers.IO) {
        try {
            backupRecovery.recover()
        } catch (cancellation: CancellationException) {
            throw cancellation
        } catch (error: Exception) {
            Log.e("BackupRecovery", "Saved travel recovery will be retried", error)
            return@withContext false
        }
        try {
            // Covers termination after restore committed but before its ViewModel scheduled alarms.
            reminders.reconcile(trips)
        } catch (cancellation: CancellationException) {
            throw cancellation
        } catch (error: Exception) {
            // The restore itself is safe. Alarm reconciliation can retry on the next startup.
            Log.w("BackupRecovery", "Could not reconcile arrival reminders", error)
        }
        true
    }
}
