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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.shrikantbadwaik.landingkit.core.designsystem.LandingKitColors
import com.shrikantbadwaik.landingkit.core.model.ChecklistTemplate
import com.shrikantbadwaik.landingkit.core.model.Trip
import com.shrikantbadwaik.landingkit.feature.kits.LandingKitViewModel
import com.shrikantbadwaik.landingkit.feature.kits.PendingSharedAttachments
import com.shrikantbadwaik.landingkit.feature.kits.PendingAttachmentAuthentication
import com.shrikantbadwaik.landingkit.feature.kits.R
import com.shrikantbadwaik.landingkit.feature.kits.toUserMessage
import com.shrikantbadwaik.landingkit.feature.kits.presentation.component.LoadingScreen
import com.shrikantbadwaik.landingkit.feature.kits.presentation.component.MissingKitScreen
import com.shrikantbadwaik.landingkit.feature.kits.presentation.detail.KitDetailScreen
import com.shrikantbadwaik.landingkit.feature.kits.presentation.editor.KitEditorScreen
import com.shrikantbadwaik.landingkit.feature.kits.presentation.home.HomeScreen
import com.shrikantbadwaik.landingkit.feature.kits.presentation.resource.curatedChecklistTemplates
import com.shrikantbadwaik.landingkit.feature.kits.presentation.resource.defaultChecklistContent
import com.shrikantbadwaik.landingkit.feature.kits.presentation.resource.sampleTripContent
import com.shrikantbadwaik.landingkit.feature.kits.presentation.share.SharedAttachmentSheet
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
internal fun LandingKitNavHost(
    trips: List<Trip>,
    personalTemplates: List<ChecklistTemplate>,
    pendingSharedAttachments: PendingSharedAttachments?,
    attachmentProtectionEnabled: Boolean,
    isAttachmentAccessUnlocked: Boolean,
    pendingAttachmentAuthentication: PendingAttachmentAuthentication?,
    viewModel: LandingKitViewModel,
    navController: NavHostController = rememberNavController(),
) {
    var hasPlayedHomeEntrance by rememberSaveable { mutableStateOf(false) }
    var hasPlayedEditorEntrance by rememberSaveable { mutableStateOf(false) }
    var animatedTripIds by remember { mutableStateOf(emptySet<String>()) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    var importingSharedAttachmentsInto by remember(pendingSharedAttachments?.id) {
        mutableStateOf<String?>(null)
    }
    val checklistContent = defaultChecklistContent()
    val curatedTemplates = curatedChecklistTemplates()
    val sampleContent = sampleTripContent(checklistContent)
    val undoLabel = stringResource(R.string.kit_undo_action)
    val returnHome = {
        if (!navController.popBackStack(KitRoute.Home, inclusive = false)) {
            navController.navigate(KitRoute.Home) {
                popUpTo(navController.graph.startDestinationId) { inclusive = true }
                launchSingleTop = true
            }
        }
    }

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
                        viewModel.addSample(sampleContent) { tripId ->
                            navController.navigate(KitRoute.Detail(tripId))
                        }
                    },
                    onOpen = { tripId -> navController.navigate(KitRoute.Detail(tripId)) },
                )
            }

            composable<KitRoute.Create> {
                KitEditorScreen(
                    trip = null,
                    defaultChecklistContent = checklistContent,
                    curatedTemplates = curatedTemplates,
                    personalTemplates = personalTemplates,
                    animateEntrance = !hasPlayedEditorEntrance,
                    onEntrancePlayed = { hasPlayedEditorEntrance = true },
                    onBack = { navController.popBackStack() },
                    onSaveTemplate = viewModel::saveTemplate,
                    onDeleteTemplate = { template -> viewModel.deleteTemplate(template.id) },
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
                val attachments by remember(route.tripId) {
                    viewModel.attachments(route.tripId)
                }.collectAsStateWithLifecycle(initialValue = emptyList())
                var missingRouteConfirmed by remember(route.tripId) { mutableStateOf(false) }
                val deletedMessage = trip?.let {
                    stringResource(R.string.kit_deleted_message, it.destination)
                }

                LaunchedEffect(route.tripId, trip) {
                    if (trip == null) {
                        delay(MISSING_ROUTE_GRACE_MILLIS)
                        missingRouteConfirmed = true
                    } else {
                        missingRouteConfirmed = false
                    }
                }

                if (trip == null) {
                    if (missingRouteConfirmed) {
                        MissingKitScreen(onHome = returnHome)
                    } else {
                        LoadingScreen()
                    }
                } else {
                    KitDetailScreen(
                        trip = trip,
                        attachments = attachments,
                        onBack = { navController.popBackStack() },
                        onEdit = { navController.navigate(KitRoute.Edit(trip.id)) },
                        onDelete = {
                            viewModel.deleteKit(trip.id) {
                                animatedTripIds = animatedTripIds - trip.id
                                navController.popBackStack()
                                scope.launch {
                                    val result = snackbarHostState.showSnackbar(
                                        message = checkNotNull(deletedMessage),
                                        actionLabel = undoLabel,
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
                        onImportAttachments = { sources, onComplete ->
                            viewModel.importAttachments(trip.id, sources, onComplete)
                        },
                        onLoadAttachmentThumbnail = viewModel::attachmentThumbnail,
                        onPrepareAttachment = viewModel::prepareAttachment,
                        onDeleteAttachment = viewModel::deleteAttachment,
                        onDeleteAttachments = { onDeleted ->
                            viewModel.deleteAttachments(trip.id, onDeleted)
                        },
                        attachmentProtectionEnabled = attachmentProtectionEnabled,
                        isAttachmentAccessUnlocked = isAttachmentAccessUnlocked,
                        pendingAttachmentAuthentication = pendingAttachmentAuthentication,
                        onSetAttachmentProtection = viewModel::setAttachmentProtectionEnabled,
                        onRequestAttachmentAuthentication =
                            viewModel::requestAttachmentAuthentication,
                        onFinishAttachmentAuthentication =
                            viewModel::finishAttachmentAuthentication,
                        onUnlockAttachmentAccess = viewModel::unlockAttachmentAccess,
                        onLockAttachmentAccess = viewModel::lockAttachmentAccess,
                    )
                }
            }

            composable<KitRoute.Edit> { backStackEntry ->
                val route = backStackEntry.toRoute<KitRoute.Edit>()
                val trip = trips.firstOrNull { it.id == route.tripId }
                var missingRouteConfirmed by remember(route.tripId) { mutableStateOf(false) }

                LaunchedEffect(route.tripId, trip) {
                    if (trip == null) {
                        delay(MISSING_ROUTE_GRACE_MILLIS)
                        missingRouteConfirmed = true
                    } else {
                        missingRouteConfirmed = false
                    }
                }

                if (trip == null) {
                    if (missingRouteConfirmed) {
                        MissingKitScreen(onHome = returnHome)
                    } else {
                        LoadingScreen()
                    }
                } else {
                    KitEditorScreen(
                        trip = trip,
                        defaultChecklistContent = checklistContent,
                        curatedTemplates = curatedTemplates,
                        personalTemplates = personalTemplates,
                        animateEntrance = !hasPlayedEditorEntrance,
                        onEntrancePlayed = { hasPlayedEditorEntrance = true },
                        onBack = { navController.popBackStack() },
                        onSaveTemplate = viewModel::saveTemplate,
                        onDeleteTemplate = { template -> viewModel.deleteTemplate(template.id) },
                        onSave = { form ->
                            viewModel.saveKit(
                                form = form,
                                existingTrip = trip,
                            ) { navController.popBackStack() }
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
                    snackbarData.visuals.actionLabel?.let { actionLabel ->
                        TextButton(
                            onClick = snackbarData::performAction,
                            colors = ButtonDefaults.textButtonColors(
                                contentColor = LandingKitColors.Sun,
                            ),
                        ) {
                            Text(actionLabel)
                        }
                    }
                }
            }
        }

        pendingSharedAttachments?.let { pending ->
            SharedAttachmentSheet(
                pending = pending,
                trips = trips,
                importingTripId = importingSharedAttachmentsInto,
                onDismiss = { viewModel.dismissSharedAttachments(pending.id) },
                onSelectTrip = { trip ->
                    importingSharedAttachmentsInto = trip.id
                    viewModel.importSharedAttachments(
                        pendingId = pending.id,
                        tripId = trip.id,
                    ) { result ->
                        importingSharedAttachmentsInto = null
                        if (result.imported > 0) {
                            navController.navigate(KitRoute.Detail(trip.id)) {
                                launchSingleTop = true
                            }
                        }
                        scope.launch {
                            snackbarHostState.showSnackbar(result.toUserMessage(context))
                        }
                    }
                },
            )
        }
    }
}

private const val MISSING_ROUTE_GRACE_MILLIS = 450L
