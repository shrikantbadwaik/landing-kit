package com.shrikantbadwaik.landingkit

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel
import com.shrikantbadwaik.landingkit.core.designsystem.LandingKitTheme
import com.shrikantbadwaik.landingkit.feature.kits.LandingKitApp
import com.shrikantbadwaik.landingkit.feature.kits.LandingKitViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            LandingKitTheme {
                val viewModel: LandingKitViewModel = viewModel()
                LandingKitApp(viewModel)
            }
        }
    }
}
