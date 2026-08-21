package com.shrikantbadwaik.landingkit.feature.kits.presentation.handoff

import android.app.Activity
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.paneTitle
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.core.view.WindowCompat
import com.shrikantbadwaik.landingkit.core.designsystem.LandingKitColors
import com.shrikantbadwaik.landingkit.core.model.Trip
import com.shrikantbadwaik.landingkit.feature.kits.R
import com.shrikantbadwaik.landingkit.feature.kits.presentation.component.ScreenHeader
import kotlinx.coroutines.delay

@Composable
internal fun HandoffScreen(
    trip: Trip,
    onBack: () -> Unit,
    onFinish: () -> Unit,
    handoffDurationMillis: Long = HANDOFF_DURATION_MILLIS,
) {
    var showStayName by rememberSaveable(trip.id) {
        mutableStateOf(trip.arrivalCard.accommodationName.isNotBlank())
    }
    var showAddress by rememberSaveable(trip.id) {
        mutableStateOf(
            trip.arrivalCard.address.isNotBlank() && trip.arrivalCard.localAddress.isBlank(),
        )
    }
    var showLocalAddress by rememberSaveable(trip.id) {
        mutableStateOf(trip.arrivalCard.localAddress.isNotBlank())
    }
    var showStayContact by rememberSaveable(trip.id) { mutableStateOf(false) }
    var preparedPhrase by rememberSaveable(trip.id) { mutableStateOf("") }
    var presenting by rememberSaveable(trip.id) { mutableStateOf(false) }

    if (presenting) {
        HandoffPresentation(
            stayName = trip.arrivalCard.accommodationName.takeIf { showStayName },
            address = trip.arrivalCard.address.takeIf { showAddress },
            localAddress = trip.arrivalCard.localAddress.takeIf { showLocalAddress },
            stayContact = trip.arrivalCard.accommodationContact.takeIf { showStayContact },
            preparedPhrase = preparedPhrase.trim().takeIf(String::isNotEmpty),
            onFinish = onFinish,
            handoffDurationMillis = handoffDurationMillis,
        )
    } else {
        HandoffSetup(
            trip = trip,
            showStayName = showStayName,
            onShowStayNameChanged = { showStayName = it },
            showAddress = showAddress,
            onShowAddressChanged = { showAddress = it },
            showLocalAddress = showLocalAddress,
            onShowLocalAddressChanged = { showLocalAddress = it },
            showStayContact = showStayContact,
            onShowStayContactChanged = { showStayContact = it },
            preparedPhrase = preparedPhrase,
            onPreparedPhraseChanged = { preparedPhrase = it },
            onBack = onBack,
            onStart = { presenting = true },
        )
    }
}

@Composable
private fun HandoffSetup(
    trip: Trip,
    showStayName: Boolean,
    onShowStayNameChanged: (Boolean) -> Unit,
    showAddress: Boolean,
    onShowAddressChanged: (Boolean) -> Unit,
    showLocalAddress: Boolean,
    onShowLocalAddressChanged: (Boolean) -> Unit,
    showStayContact: Boolean,
    onShowStayContactChanged: (Boolean) -> Unit,
    preparedPhrase: String,
    onPreparedPhraseChanged: (String) -> Unit,
    onBack: () -> Unit,
    onStart: () -> Unit,
) {
    val title = stringResource(R.string.handoff_setup_header)
    val hasSelectedContent =
        (showStayName && trip.arrivalCard.accommodationName.isNotBlank()) ||
            (showAddress && trip.arrivalCard.address.isNotBlank()) ||
            (showLocalAddress && trip.arrivalCard.localAddress.isNotBlank()) ||
            (showStayContact && trip.arrivalCard.accommodationContact.isNotBlank()) ||
            preparedPhrase.isNotBlank()

    Scaffold(
        modifier = Modifier.semantics { paneTitle = title },
        containerColor = MaterialTheme.colorScheme.background,
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(22.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item {
                ScreenHeader(
                    onBack = onBack,
                    label = title,
                    badge = stringResource(R.string.handoff_setup_badge),
                )
            }
            item {
                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = LandingKitColors.Seafoam.copy(alpha = 0.22f),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Text(
                            text = stringResource(R.string.handoff_setup_title),
                            modifier = Modifier.semantics { heading() },
                            style = MaterialTheme.typography.headlineSmall,
                        )
                        Text(
                            text = stringResource(R.string.handoff_setup_description),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
            if (trip.arrivalCard.accommodationName.isNotBlank()) {
                item {
                    HandoffOption(
                        label = stringResource(R.string.handoff_option_stay_name),
                        value = trip.arrivalCard.accommodationName,
                        selected = showStayName,
                        onSelectedChanged = onShowStayNameChanged,
                    )
                }
            }
            if (trip.arrivalCard.address.isNotBlank()) {
                item {
                    HandoffOption(
                        label = stringResource(R.string.handoff_option_address),
                        value = trip.arrivalCard.address,
                        selected = showAddress,
                        onSelectedChanged = onShowAddressChanged,
                    )
                }
            }
            if (trip.arrivalCard.localAddress.isNotBlank()) {
                item {
                    HandoffOption(
                        label = stringResource(R.string.handoff_option_local_address),
                        value = trip.arrivalCard.localAddress,
                        selected = showLocalAddress,
                        onSelectedChanged = onShowLocalAddressChanged,
                    )
                }
            }
            if (trip.arrivalCard.accommodationContact.isNotBlank()) {
                item {
                    HandoffOption(
                        label = stringResource(R.string.handoff_option_stay_contact),
                        value = trip.arrivalCard.accommodationContact,
                        selected = showStayContact,
                        onSelectedChanged = onShowStayContactChanged,
                    )
                }
            }
            item {
                OutlinedTextField(
                    value = preparedPhrase,
                    onValueChange = onPreparedPhraseChanged,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(stringResource(R.string.handoff_phrase_label)) },
                    supportingText = { Text(stringResource(R.string.handoff_phrase_support)) },
                    minLines = 3,
                    maxLines = 6,
                    shape = RoundedCornerShape(18.dp),
                )
            }
            item {
                Button(
                    onClick = onStart,
                    enabled = hasSelectedContent,
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(18.dp),
                ) {
                    Text(stringResource(R.string.handoff_start_action))
                }
            }
        }
    }
}

@Composable
private fun HandoffOption(
    label: String,
    value: String,
    selected: Boolean,
    onSelectedChanged: (Boolean) -> Unit,
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .toggleable(
                value = selected,
                onValueChange = onSelectedChanged,
            )
            .semantics(mergeDescendants = true) {},
        shape = RoundedCornerShape(22.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        color = MaterialTheme.colorScheme.surface,
    ) {
        Row(
            modifier = Modifier.padding(18.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(Modifier.weight(1f)) {
                Text(text = label, style = MaterialTheme.typography.titleSmall)
                Spacer(Modifier.height(4.dp))
                Text(
                    text = value,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Switch(checked = selected, onCheckedChange = null)
        }
    }
}

@Composable
private fun HandoffPresentation(
    stayName: String?,
    address: String?,
    localAddress: String?,
    stayContact: String?,
    preparedPhrase: String?,
    onFinish: () -> Unit,
    handoffDurationMillis: Long,
) {
    val title = stringResource(R.string.handoff_active_pane)
    val lifecycleOwner = LocalLifecycleOwner.current
    val activity = LocalContext.current as? Activity
    val view = LocalView.current
    var finishRequested by rememberSaveable { mutableStateOf(false) }
    val requestFinish = {
        if (!finishRequested) {
            finishRequested = true
            onFinish()
        }
    }

    BackHandler(onBack = requestFinish)
    LaunchedEffect(handoffDurationMillis) {
        delay(handoffDurationMillis)
        requestFinish()
    }
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_STOP) requestFinish()
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }
    DisposableEffect(activity, view) {
        val controller = activity?.window?.let { WindowCompat.getInsetsController(it, view) }
        val previousLightStatusBars = controller?.isAppearanceLightStatusBars
        controller?.isAppearanceLightStatusBars = false
        onDispose {
            if (previousLightStatusBars != null) {
                controller.isAppearanceLightStatusBars = previousLightStatusBars
            }
        }
    }

    Scaffold(
        modifier = Modifier.semantics { paneTitle = title },
        containerColor = LandingKitColors.Forest,
        contentColor = Color.White,
        bottomBar = {
            Surface(
                modifier = Modifier.navigationBarsPadding(),
                color = LandingKitColors.Forest,
            ) {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    val endDescription = stringResource(R.string.handoff_end_action)
                    Surface(
                        onClick = requestFinish,
                        modifier = Modifier
                            .size(72.dp)
                            .semantics { contentDescription = endDescription },
                        shape = CircleShape,
                        color = Color.White,
                        contentColor = LandingKitColors.Forest,
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = stringResource(R.string.handoff_close_symbol),
                                style = MaterialTheme.typography.titleLarge,
                            )
                        }
                    }
                }
            }
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(28.dp),
            verticalArrangement = Arrangement.spacedBy(26.dp),
        ) {
            item {
                Text(
                    text = stringResource(R.string.handoff_active_label),
                    modifier = Modifier.semantics { heading() },
                    style = MaterialTheme.typography.labelLarge,
                    color = LandingKitColors.Seafoam,
                )
            }
            stayName?.let { value ->
                item { HandoffDisplayItem(R.string.handoff_display_stay, value) }
            }
            address?.let { value ->
                item { HandoffDisplayItem(R.string.handoff_display_address, value) }
            }
            localAddress?.let { value ->
                item { HandoffDisplayItem(R.string.handoff_display_local_address, value) }
            }
            stayContact?.let { value ->
                item { HandoffDisplayItem(R.string.handoff_display_phone, value) }
            }
            preparedPhrase?.let { value ->
                item { HandoffDisplayItem(R.string.handoff_display_message, value) }
            }
            item {
                Text(
                    text = stringResource(R.string.handoff_timeout_note),
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.72f),
                )
            }
        }
    }
}

@Composable
private fun HandoffDisplayItem(labelRes: Int, value: String) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = stringResource(labelRes),
            style = MaterialTheme.typography.labelLarge,
            color = LandingKitColors.Sun,
        )
        Text(
            text = value,
            modifier = Modifier.semantics { heading() },
            style = MaterialTheme.typography.headlineLarge,
            color = Color.White,
        )
    }
}

private const val HANDOFF_DURATION_MILLIS = 2 * 60 * 1_000L
