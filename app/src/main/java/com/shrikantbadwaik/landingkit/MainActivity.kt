package com.shrikantbadwaik.landingkit

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.shrikantbadwaik.landingkit.core.designsystem.LandingKitTheme
import com.shrikantbadwaik.landingkit.feature.kits.LandingKitApp
import com.shrikantbadwaik.landingkit.feature.kits.LandingKitViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val viewModel: LandingKitViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        receiveSharedAttachments(intent)
        setContent {
            LandingKitTheme {
                LandingKitApp(viewModel)
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        receiveSharedAttachments(intent)
    }

    private fun receiveSharedAttachments(incomingIntent: Intent?) {
        if (incomingIntent == null || !incomingIntent.isAttachmentShare) return
        val sources = incomingIntent.sharedAttachmentUris()
        if (sources.isNotEmpty()) viewModel.acceptSharedAttachments(sources)

        // Prevent configuration changes or task restoration from consuming the same share again.
        setIntent(Intent(this, MainActivity::class.java).setAction(Intent.ACTION_MAIN))
    }
}
