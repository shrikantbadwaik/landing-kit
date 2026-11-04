package com.shrikantbadwaik.landingkit

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.fragment.app.FragmentActivity
import com.shrikantbadwaik.landingkit.core.designsystem.LandingKitTheme
import com.shrikantbadwaik.landingkit.feature.kits.LandingKitApp
import com.shrikantbadwaik.landingkit.feature.kits.LandingKitViewModel
import com.shrikantbadwaik.landingkit.feature.kits.notification.ArrivalReminderIntent
import dagger.hilt.android.AndroidEntryPoint
import com.shrikantbadwaik.travel.with.thikana.R
import com.shrikantbadwaik.landingkit.core.data.local.backup.BackupRecoveryAccess
import com.shrikantbadwaik.landingkit.core.data.local.backup.BackupRecoveryStatus
import javax.inject.Inject
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle

@AndroidEntryPoint
class MainActivity : FragmentActivity() {
    private val viewModelLazy = viewModels<LandingKitViewModel>()
    private val viewModel: LandingKitViewModel by viewModelLazy
    @Inject lateinit var recoveryAccess: BackupRecoveryAccess

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                recoveryAccess.status.collect { status ->
                    if (status == BackupRecoveryStatus.Ready) receivePendingIntent()
                }
            }
        }
        setContent {
            LandingKitTheme {
                val status by recoveryAccess.status.collectAsState()
                if (status == BackupRecoveryStatus.Ready) {
                    LandingKitApp(viewModel)
                } else {
                    val recovering = status == BackupRecoveryStatus.Recovering
                    Surface(Modifier.fillMaxSize()) {
                        Column(Modifier.padding(32.dp), verticalArrangement = Arrangement.Center) {
                            Text(stringResource(if (recovering) R.string.restore_recovery_loading_title
                                else R.string.restore_recovery_title),
                                style = MaterialTheme.typography.headlineMedium)
                            Text(stringResource(if (recovering) R.string.restore_recovery_loading_message
                                else R.string.restore_recovery_message),
                                modifier = Modifier.padding(vertical = 24.dp))
                            if (recovering) {
                                CircularProgressIndicator()
                            } else {
                                Button(onClick = {
                                    (application as LandingKitApplication).retrySavedTravel()
                                }) {
                                    Text(stringResource(R.string.restore_recovery_retry))
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        if (recoveryAccess.isReady) receivePendingIntent()
    }

    override fun onStop() {
        super.onStop()
        if (viewModelLazy.isInitialized() && !isChangingConfigurations) viewModel.lockAttachmentAccess()
    }

    override fun onStart() {
        super.onStart()
        if (recoveryAccess.isReady) {
            val activeViewModel = viewModel
            lifecycleScope.launch(Dispatchers.IO) { activeViewModel.cleanInactiveViewingPreviews() }
        }
    }

    private fun receivePendingIntent() {
        receiveSharedAttachments(intent)
        receiveArrivalModeShortcut(intent)
    }

    private fun receiveSharedAttachments(incomingIntent: Intent?) {
        if (incomingIntent == null || !incomingIntent.isAttachmentShare) return
        val sources = incomingIntent.sharedAttachmentUris()
        if (sources.isNotEmpty()) viewModel.acceptSharedAttachments(sources)

        // Prevent configuration changes or task restoration from consuming the same share again.
        setIntent(Intent(this, MainActivity::class.java).setAction(Intent.ACTION_MAIN))
    }

    private fun receiveArrivalModeShortcut(incomingIntent: Intent?) {
        val tripId = ArrivalReminderIntent.tripId(incomingIntent)
        when {
            tripId != null -> viewModel.acceptArrivalModeShortcut(tripId)
            ArrivalReminderIntent.opensNextArrivalMode(incomingIntent) ->
                viewModel.acceptNextArrivalModeShortcut()
            else -> return
        }

        // Prevent task restoration from reopening Arrival Mode after the shortcut is consumed.
        setIntent(Intent(this, MainActivity::class.java).setAction(Intent.ACTION_MAIN))
    }
}
