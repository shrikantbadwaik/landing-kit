package com.shrikantbadwaik.landingkit.feature.kits

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.shrikantbadwaik.landingkit.feature.kits.presentation.component.LoadingScreen
import com.shrikantbadwaik.landingkit.feature.kits.presentation.navigation.LandingKitNavHost

@Composable
fun LandingKitApp(viewModel: LandingKitViewModel) {
    val tripCollection by viewModel.trips.collectAsStateWithLifecycle()
    val templates by viewModel.templates.collectAsStateWithLifecycle()
    val pendingSharedAttachments by viewModel.pendingSharedAttachments.collectAsStateWithLifecycle()
    when (val state = tripCollection) {
        TripCollectionState.Loading -> LoadingScreen()
        is TripCollectionState.Ready -> LandingKitNavHost(
            trips = state.trips,
            personalTemplates = templates,
            pendingSharedAttachments = pendingSharedAttachments,
            viewModel = viewModel,
        )
    }
}
