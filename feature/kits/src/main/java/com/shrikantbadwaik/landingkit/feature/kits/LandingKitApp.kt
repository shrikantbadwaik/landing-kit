package com.shrikantbadwaik.landingkit.feature.kits

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.shrikantbadwaik.landingkit.feature.kits.presentation.component.LoadingScreen
import com.shrikantbadwaik.landingkit.feature.kits.presentation.create.CreateKitScreen
import com.shrikantbadwaik.landingkit.feature.kits.presentation.detail.KitDetailScreen
import com.shrikantbadwaik.landingkit.feature.kits.presentation.home.HomeScreen
import com.shrikantbadwaik.landingkit.feature.kits.presentation.navigation.KitScreen
import com.shrikantbadwaik.landingkit.feature.kits.presentation.navigation.depth

@Composable
fun LandingKitApp(viewModel: LandingKitViewModel) {
    val trips by viewModel.trips.collectAsStateWithLifecycle()
    var screen: KitScreen by remember { mutableStateOf(KitScreen.Home) }
    var hasPlayedHomeEntrance by rememberSaveable { mutableStateOf(false) }
    var hasPlayedCreateEntrance by rememberSaveable { mutableStateOf(false) }
    var animatedTripIds by remember { mutableStateOf(emptySet<String>()) }

    AnimatedContent(
        targetState = screen,
        transitionSpec = {
            val direction = if (targetState.depth >= initialState.depth) 1 else -1
            val enter = fadeIn(tween(durationMillis = 320, delayMillis = 70)) +
                slideInHorizontally(
                    animationSpec = tween(420, easing = FastOutSlowInEasing),
                    initialOffsetX = { width -> direction * width / 7 },
                )
            val exit = fadeOut(tween(durationMillis = 180)) +
                slideOutHorizontally(
                    animationSpec = tween(260, easing = FastOutSlowInEasing),
                    targetOffsetX = { width -> -direction * width / 10 },
                )
            enter togetherWith exit
        },
        label = "Landing Kit navigation",
    ) { current ->
        when (current) {
            KitScreen.Home -> HomeScreen(
                trips = trips,
                animateEntrance = !hasPlayedHomeEntrance,
                animatedTripIds = animatedTripIds,
                onEntrancePlayed = {
                    hasPlayedHomeEntrance = true
                    animatedTripIds = animatedTripIds + trips.map { it.id }
                },
                onTripAnimated = { tripId -> animatedTripIds = animatedTripIds + tripId },
                onCreate = { screen = KitScreen.Create },
                onSample = { viewModel.addSample { screen = KitScreen.Detail(it) } },
                onOpen = { screen = KitScreen.Detail(it) },
            )

            KitScreen.Create -> CreateKitScreen(
                animateEntrance = !hasPlayedCreateEntrance,
                onEntrancePlayed = { hasPlayedCreateEntrance = true },
                onBack = { screen = KitScreen.Home },
                onSave = { viewModel.createKit(it) { id -> screen = KitScreen.Detail(id) } },
            )

            is KitScreen.Detail -> {
                val trip = trips.firstOrNull { it.id == current.tripId }
                if (trip == null) {
                    LoadingScreen()
                } else {
                    KitDetailScreen(
                        trip = trip,
                        onBack = { screen = KitScreen.Home },
                        onCheck = { item, checked ->
                            viewModel.setComplete(trip.id, item.id, checked)
                        },
                    )
                }
            }
        }
    }
}
