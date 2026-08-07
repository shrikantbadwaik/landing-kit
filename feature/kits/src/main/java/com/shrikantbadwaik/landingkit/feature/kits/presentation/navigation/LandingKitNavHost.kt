package com.shrikantbadwaik.landingkit.feature.kits.presentation.navigation

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.shrikantbadwaik.landingkit.core.designsystem.LandingKitColors
import com.shrikantbadwaik.landingkit.core.model.Trip
import com.shrikantbadwaik.landingkit.feature.kits.LandingKitViewModel
import com.shrikantbadwaik.landingkit.feature.kits.presentation.component.LoadingScreen
import com.shrikantbadwaik.landingkit.feature.kits.presentation.detail.KitDetailScreen
import com.shrikantbadwaik.landingkit.feature.kits.presentation.editor.KitEditorScreen
import com.shrikantbadwaik.landingkit.feature.kits.presentation.home.HomeScreen
import kotlinx.coroutines.launch

@Composable
internal fun LandingKitNavHost(
    trips: List<Trip>,
    viewModel: LandingKitViewModel,
    navController: NavHostController = rememberNavController(),
) {
    var hasPlayedHomeEntrance by rememberSaveable { mutableStateOf(false) }
    var hasPlayedEditorEntrance by rememberSaveable { mutableStateOf(false) }
    var animatedTripIds by remember { mutableStateOf(emptySet<String>()) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Box(Modifier.fillMaxSize()) {
        NavHost(
            navController = navController,
            startDestination = KitRoute.Home,
            modifier = Modifier.fillMaxSize(),
            enterTransition = {
                fadeIn(tween(durationMillis = 320, delayMillis = 70)) +
                    slideInHorizontally(
                        animationSpec = tween(420, easing = FastOutSlowInEasing),
                        initialOffsetX = { width -> width / 7 },
                    )
            },
            exitTransition = {
                fadeOut(tween(durationMillis = 180)) +
                    slideOutHorizontally(
                        animationSpec = tween(260, easing = FastOutSlowInEasing),
                        targetOffsetX = { width -> -width / 10 },
                    )
            },
            popEnterTransition = {
                fadeIn(tween(durationMillis = 320, delayMillis = 70)) +
                    slideInHorizontally(
                        animationSpec = tween(420, easing = FastOutSlowInEasing),
                        initialOffsetX = { width -> -width / 7 },
                    )
            },
            popExitTransition = {
                fadeOut(tween(durationMillis = 180)) +
                    slideOutHorizontally(
                        animationSpec = tween(260, easing = FastOutSlowInEasing),
                        targetOffsetX = { width -> width / 10 },
                    )
            },
        ) {
            composable<KitRoute.Home> {
                HomeScreen(
                    trips = trips,
                    animateEntrance = !hasPlayedHomeEntrance,
                    animatedTripIds = animatedTripIds,
                    onEntrancePlayed = {
                        hasPlayedHomeEntrance = true
                        animatedTripIds = animatedTripIds + trips.map { it.id }
                    },
                    onTripAnimated = { tripId -> animatedTripIds = animatedTripIds + tripId },
                    onCreate = { navController.navigate(KitRoute.Create) },
                    onSample = {
                        viewModel.addSample { tripId ->
                            navController.navigate(KitRoute.Detail(tripId))
                        }
                    },
                    onOpen = { tripId -> navController.navigate(KitRoute.Detail(tripId)) },
                )
            }

            composable<KitRoute.Create> {
                KitEditorScreen(
                    trip = null,
                    animateEntrance = !hasPlayedEditorEntrance,
                    onEntrancePlayed = { hasPlayedEditorEntrance = true },
                    onBack = { navController.popBackStack() },
                    onSave = { form ->
                        viewModel.saveKit(form) { tripId ->
                            navController.navigate(KitRoute.Detail(tripId)) {
                                popUpTo<KitRoute.Create> { inclusive = true }
                            }
                        }
                    },
                )
            }

            composable<KitRoute.Detail> { backStackEntry ->
                val route = backStackEntry.toRoute<KitRoute.Detail>()
                val trip = trips.firstOrNull { it.id == route.tripId }

                if (trip == null) {
                    LoadingScreen()
                } else {
                    KitDetailScreen(
                        trip = trip,
                        onBack = { navController.popBackStack() },
                        onEdit = { navController.navigate(KitRoute.Edit(trip.id)) },
                        onDelete = {
                            viewModel.deleteKit(trip.id) {
                                animatedTripIds = animatedTripIds - trip.id
                                navController.popBackStack()
                                scope.launch {
                                    val result = snackbarHostState.showSnackbar(
                                        message = "${trip.destination} kit deleted",
                                        actionLabel = "Undo",
                                        duration = SnackbarDuration.Long,
                                    )
                                    if (result == SnackbarResult.ActionPerformed) {
                                        viewModel.restoreKit(trip)
                                    }
                                }
                            }
                        },
                        onCheck = { item, checked ->
                            viewModel.setComplete(trip.id, item.id, checked)
                        },
                    )
                }
            }

            composable<KitRoute.Edit> { backStackEntry ->
                val route = backStackEntry.toRoute<KitRoute.Edit>()
                val trip = trips.firstOrNull { it.id == route.tripId }

                if (trip == null) {
                    LoadingScreen()
                } else {
                    KitEditorScreen(
                        trip = trip,
                        animateEntrance = !hasPlayedEditorEntrance,
                        onEntrancePlayed = { hasPlayedEditorEntrance = true },
                        onBack = { navController.popBackStack() },
                        onSave = { form ->
                            viewModel.saveKit(form, trip) { navController.popBackStack() }
                        },
                    )
                }
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(16.dp),
        ) { snackbarData ->
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                color = LandingKitColors.Ink,
                contentColor = LandingKitColors.Paper,
                shadowElevation = 8.dp,
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 64.dp)
                        .padding(start = 20.dp, end = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = snackbarData.visuals.message,
                        modifier = Modifier.weight(1f),
                    )
                    TextButton(
                        onClick = snackbarData::performAction,
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = LandingKitColors.Sun,
                        ),
                    ) {
                        Text("UNDO")
                    }
                }
            }
        }
    }
}
