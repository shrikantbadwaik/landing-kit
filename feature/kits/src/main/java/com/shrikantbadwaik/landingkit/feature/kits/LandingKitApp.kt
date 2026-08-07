package com.shrikantbadwaik.landingkit.feature.kits

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.shrikantbadwaik.landingkit.feature.kits.presentation.navigation.LandingKitNavHost

@Composable
fun LandingKitApp(viewModel: LandingKitViewModel) {
    val trips by viewModel.trips.collectAsStateWithLifecycle()
    LandingKitNavHost(trips = trips, viewModel = viewModel)
}
