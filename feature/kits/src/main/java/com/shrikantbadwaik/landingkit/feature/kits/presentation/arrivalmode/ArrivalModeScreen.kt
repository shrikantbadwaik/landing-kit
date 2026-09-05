package com.shrikantbadwaik.landingkit.feature.kits.presentation.arrivalmode

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.paneTitle
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.shrikantbadwaik.landingkit.core.data.PreparedAttachment
import com.shrikantbadwaik.landingkit.core.designsystem.LandingKitColors
import com.shrikantbadwaik.landingkit.core.model.KitAttachment
import com.shrikantbadwaik.landingkit.core.model.Trip
import com.shrikantbadwaik.landingkit.feature.kits.PendingAttachmentAuthentication
import com.shrikantbadwaik.landingkit.feature.kits.ProtectedAttachmentAction
import com.shrikantbadwaik.landingkit.feature.kits.R
import com.shrikantbadwaik.landingkit.feature.kits.presentation.component.ScreenHeader
import com.shrikantbadwaik.landingkit.feature.kits.presentation.component.JournalBackground
import com.shrikantbadwaik.landingkit.feature.kits.presentation.model.IfDelayedContent
import com.shrikantbadwaik.landingkit.feature.kits.presentation.model.toArrivalModeContent
import com.shrikantbadwaik.landingkit.feature.kits.presentation.security.rememberProtectedAttachmentOpener
import kotlinx.coroutines.launch

@Composable
internal fun ArrivalModeScreen(
    trip: Trip,
    attachments: List<KitAttachment>,
    onBack: () -> Unit,
    onPrivateHandoff: () -> Unit,
    onTravelDayAction: (TravelDayAction) -> TravelDayActionResult,
    onPresentAttachment: (KitAttachment) -> Unit,
    onPrepareAttachment: (String, (Result<PreparedAttachment>) -> Unit) -> Unit,
    attachmentProtectionEnabled: Boolean,
    isAttachmentAccessUnlocked: Boolean,
    pendingAttachmentAuthentication: PendingAttachmentAuthentication?,
    onSetAttachmentProtection: (Boolean) -> Unit,
    onRequestAttachmentAuthentication: (ProtectedAttachmentAction) -> Unit,
    onFinishAttachmentAuthentication: (String) -> Unit,
    onUnlockAttachmentAccess: () -> Unit,
    onLockAttachmentAccess: () -> Unit,
) {
    val content = remember(trip, attachments) { trip.toArrivalModeContent(attachments) }
    val screenTitle = stringResource(R.string.arrival_mode_header)
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val addressCopiedMessage = stringResource(R.string.arrival_mode_address_copied)
    val mapUnavailableMessage = stringResource(R.string.arrival_mode_map_unavailable)
    val callingUnavailableMessage = stringResource(R.string.arrival_mode_call_unavailable)
    val messagingUnavailableMessage = stringResource(R.string.arrival_mode_message_unavailable)
    val performTravelDayAction: (TravelDayAction) -> Unit = { action ->
        val feedback = when (onTravelDayAction(action)) {
            TravelDayActionResult.Started -> null
            TravelDayActionResult.AddressCopied -> addressCopiedMessage
            TravelDayActionResult.MapUnavailable -> mapUnavailableMessage
            TravelDayActionResult.CallingUnavailable -> callingUnavailableMessage
            TravelDayActionResult.MessagingUnavailable -> messagingUnavailableMessage
        }
        feedback?.let { message ->
            scope.launch { snackbarHostState.showSnackbar(message) }
        }
    }
    val openAttachment = rememberProtectedAttachmentOpener(
        attachments = attachments,
        protectionEnabled = attachmentProtectionEnabled,
        accessUnlocked = isAttachmentAccessUnlocked,
        pendingAuthentication = pendingAttachmentAuthentication,
        onPrepareAttachment = onPrepareAttachment,
        onSetProtection = onSetAttachmentProtection,
        onRequestAuthentication = onRequestAttachmentAuthentication,
        onFinishAuthentication = onFinishAttachmentAuthentication,
        onUnlockAccess = onUnlockAttachmentAccess,
        onLockAccess = onLockAttachmentAccess,
        onFeedback = { message -> scope.launch { snackbarHostState.showSnackbar(message) } },
        onPresentAttachment = onPresentAttachment,
    )

    Scaffold(
        modifier = Modifier.semantics { paneTitle = screenTitle },
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        val listState = rememberLazyListState()
        JournalBackground(
            seed = "arrival:${trip.id}",
            listState = listState,
            count = 3,
            modifier = Modifier.fillMaxSize().padding(padding),
        ) {
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 22.dp, vertical = 20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                item(key = "arrival_mode_header") {
                    ScreenHeader(
                        onBack = onBack,
                        label = stringResource(R.string.arrival_mode_header),
                    )
                }
                item(key = "arrival_mode_address") {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = LandingKitColors.Forest,
                        contentColor = Color.White,
                        shape = MaterialTheme.shapes.large,
                        shadowElevation = 2.dp,
                    ) {
                        Column(Modifier.padding(horizontal = 24.dp, vertical = 28.dp)) {
                            Text(
                                text = stringResource(R.string.arrival_mode_show_address),
                                style = MaterialTheme.typography.labelSmall,
                                color = LandingKitColors.Seafoam,
                            )
                            Spacer(Modifier.height(12.dp))
                            Text(
                                text = trip.destination,
                                style = MaterialTheme.typography.titleMedium,
                                color = Color.White.copy(alpha = 0.76f),
                            )
                            Spacer(Modifier.height(20.dp))
                            Text(
                                text = content.address,
                                modifier = Modifier.semantics { heading() },
                                style = MaterialTheme.typography.headlineLarge,
                                color = Color.White,
                            )
                            content.supportingAddress?.let { supportingAddress ->
                                Spacer(Modifier.height(18.dp))
                                Text(
                                    text = supportingAddress,
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = Color.White.copy(alpha = 0.78f),
                                )
                            }
                            Spacer(Modifier.height(26.dp))
                            Surface(
                                color = Color.White.copy(alpha = 0.10f),
                                contentColor = Color.White,
                                shape = MaterialTheme.shapes.medium,
                            ) {
                                Column(Modifier.fillMaxWidth().padding(18.dp)) {
                                    Text(
                                        text = stringResource(R.string.arrival_mode_staying_at),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = LandingKitColors.Sun,
                                    )
                                    Spacer(Modifier.height(6.dp))
                                    Text(
                                        text = trip.arrivalCard.accommodationName,
                                        style = MaterialTheme.typography.titleMedium,
                                    )
                                }
                            }
                            Spacer(Modifier.height(12.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(20.dp),
                            ) {
                                TravelDayActionControl(
                                    modifier = Modifier.weight(1f),
                                    icon = R.drawable.ic_open_map,
                                    label = stringResource(R.string.arrival_mode_open_map),
                                    description = stringResource(
                                        R.string.arrival_mode_open_map_description,
                                    ),
                                    containerColor = Color.White.copy(alpha = 0.12f),
                                    contentColor = Color.White,
                                    borderColor = Color.White.copy(alpha = 0.18f),
                                    onClick = {
                                        performTravelDayAction(
                                            TravelDayAction.OpenMap(content.mapQuery),
                                        )
                                    },
                                )
                                TravelDayActionControl(
                                    modifier = Modifier.weight(1f),
                                    icon = R.drawable.ic_copy_address,
                                    label = stringResource(R.string.arrival_mode_copy_address),
                                    description = stringResource(
                                        R.string.arrival_mode_copy_address_description,
                                    ),
                                    containerColor = Color.White.copy(alpha = 0.12f),
                                    contentColor = Color.White,
                                    borderColor = Color.White.copy(alpha = 0.18f),
                                    onClick = {
                                        performTravelDayAction(
                                            TravelDayAction.CopyAddress(content.copyableAddress),
                                        )
                                    },
                                )
                            }
                        }
                    }
                }
                item(key = "arrival_mode_handoff") {
                    Surface(
                        onClick = onPrivateHandoff,
                        modifier = Modifier.fillMaxWidth().semantics(mergeDescendants = true) {},
                        shape = MaterialTheme.shapes.large,
                        color = LandingKitColors.Seafoam.copy(alpha = 0.34f),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            Text(
                                text = stringResource(R.string.handoff_entry_label),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.secondary,
                            )
                            Text(
                                text = stringResource(R.string.handoff_entry_title),
                                style = MaterialTheme.typography.titleMedium,
                            )
                            Text(
                                text = stringResource(R.string.handoff_entry_description),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
                content.bookingReference?.let { bookingReference ->
                    item(key = "arrival_mode_booking") {
                        ArrivalModeInfoCard(
                            label = stringResource(R.string.arrival_mode_booking),
                            value = bookingReference,
                            accent = LandingKitColors.Sky,
                        )
                    }
                }
                content.transferInstructions?.let { transferInstructions ->
                    item(key = "arrival_mode_transfer") {
                        ArrivalModeInfoCard(
                            label = stringResource(R.string.arrival_mode_transfer),
                            value = transferInstructions,
                            accent = LandingKitColors.Sun,
                        )
                    }
                }
                content.ifDelayed?.let { delayed ->
                    item(key = "arrival_mode_if_delayed") {
                        ArrivalModeIfDelayedCard(
                            content = delayed,
                            onTravelDayAction = performTravelDayAction,
                        )
                    }
                }
                content.primaryAttachment?.let { attachment ->
                    item(key = "arrival_mode_attachment") {
                        ArrivalEvidenceAction(
                            attachment = attachment,
                            onOpen = { openAttachment(attachment) },
                        )
                    }
                } ?: item(key = "arrival_mode_no_attachment") {
                    ArrivalModeInfoCard(
                        label = stringResource(R.string.arrival_mode_evidence_label),
                        value = stringResource(R.string.arrival_mode_no_evidence),
                        accent = LandingKitColors.Seafoam,
                    )
                }
                content.emergencyContact?.let { emergencyContact ->
                    item(key = "arrival_mode_emergency") {
                        ArrivalModeInfoCard(
                            label = stringResource(R.string.arrival_mode_emergency),
                            value = emergencyContact,
                            accent = LandingKitColors.Coral,
                            emphasized = true,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ArrivalModeInfoCard(
    label: String,
    accent: Color,
    emphasized: Boolean = false,
    value: String? = null,
    content: (@Composable () -> Unit)? = null,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        color = if (emphasized) {
            accent.copy(alpha = 0.12f)
        } else {
            MaterialTheme.colorScheme.surface
        },
        border = BorderStroke(
            width = 1.dp,
            color = if (emphasized) {
                accent.copy(alpha = 0.46f)
            } else {
                MaterialTheme.colorScheme.outlineVariant
            },
        ),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(20.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.Top,
        ) {
            Surface(
                modifier = Modifier.size(width = 7.dp, height = 68.dp),
                shape = CircleShape,
                color = accent,
            ) {}
            Column(Modifier.weight(1f)) {
                Text(
                    text = label,
                    modifier = Modifier.semantics { heading() },
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.secondary,
                )
                Spacer(Modifier.height(8.dp))
                if (content != null) {
                    content()
                } else if (value != null) {
                    Text(
                        text = value,
                        style = if (emphasized) {
                            MaterialTheme.typography.titleMedium
                        } else {
                            MaterialTheme.typography.bodyLarge
                        },
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
            }
        }
    }
}

@Composable
private fun ArrivalEvidenceAction(
    attachment: KitAttachment,
    onOpen: () -> Unit,
) {
    val openDescription = stringResource(
        R.string.attachment_open_description,
        attachment.displayName,
    )
    Surface(
        onClick = onOpen,
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 152.dp)
            .semantics(mergeDescendants = true) {
                heading()
                contentDescription = openDescription
            },
        shape = MaterialTheme.shapes.large,
        color = LandingKitColors.Sun,
        contentColor = LandingKitColors.Ink,
        border = BorderStroke(1.dp, LandingKitColors.Ink.copy(alpha = 0.12f)),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(20.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier.size(56.dp),
                contentAlignment = Alignment.Center,
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    shape = CircleShape,
                    color = LandingKitColors.Forest,
                    contentColor = Color.White,
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = stringResource(
                                if (attachment.mimeType == PDF_MIME_TYPE) {
                                    R.string.attachment_type_pdf
                                } else {
                                    R.string.symbol_open
                                },
                            ),
                            style = MaterialTheme.typography.labelSmall,
                        )
                    }
                }
            }
            Column(Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.arrival_mode_evidence_label),
                    style = MaterialTheme.typography.labelSmall,
                    color = LandingKitColors.Ink,
                )
                Spacer(Modifier.height(5.dp))
                Text(
                    text = stringResource(R.string.arrival_mode_evidence_title),
                    style = MaterialTheme.typography.titleMedium,
                )
                Spacer(Modifier.height(4.dp))
                Spacer(Modifier.height(12.dp))
                Text(
                    text = stringResource(R.string.arrival_mode_evidence_open),
                    style = MaterialTheme.typography.labelLarge,
                    color = LandingKitColors.Ink,
                )
            }
        }
    }
}

@Composable
private fun ArrivalModeIfDelayedCard(
    content: IfDelayedContent,
    onTravelDayAction: (TravelDayAction) -> Unit,
) {
    ArrivalModeInfoCard(
        label = stringResource(R.string.arrival_mode_if_delayed),
        accent = LandingKitColors.Lavender,
    ) {
        content.fallbackTransport?.let {
            IfDelayedRow(
                label = stringResource(R.string.plan_b_fallback_transport_label),
                value = it,
            )
        }
        content.lastServiceInfo?.let {
            IfDelayedRow(
                label = stringResource(R.string.plan_b_last_service_label),
                value = it,
            )
        }
        content.checkInCutoff?.let {
            IfDelayedRow(
                label = stringResource(R.string.plan_b_check_in_cutoff_label),
                value = it,
            )
        }
        content.lateArrivalInstructions?.let {
            IfDelayedRow(
                label = stringResource(R.string.plan_b_late_arrival_label),
                value = it,
            )
        }
        content.accommodationContact?.let {
            IfDelayedContactRow(
                label = stringResource(R.string.plan_b_accommodation_contact_label),
                value = it,
                onCall = { onTravelDayAction(TravelDayAction.CallStay(it)) },
                onMessage = { onTravelDayAction(TravelDayAction.MessageStay(it)) },
            )
        }
        content.expectedCostNote?.let {
            IfDelayedRow(
                label = stringResource(R.string.plan_b_expected_cost_label),
                value = it,
            )
        }
        content.alternateStayNote?.let {
            IfDelayedRow(
                label = stringResource(R.string.plan_b_alternate_stay_label),
                value = it,
            )
        }
    }
}

@Composable
private fun IfDelayedContactRow(
    label: String,
    value: String,
    onCall: () -> Unit,
    onMessage: () -> Unit,
) {
    Column(Modifier.fillMaxWidth().padding(bottom = 10.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(3.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Spacer(Modifier.height(10.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            TravelDayActionControl(
                modifier = Modifier.weight(1f),
                icon = R.drawable.ic_call_stay,
                label = stringResource(R.string.arrival_mode_call_stay),
                description = stringResource(R.string.arrival_mode_call_stay_description),
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.onSurface,
                borderColor = MaterialTheme.colorScheme.outlineVariant,
                onClick = onCall,
            )
            TravelDayActionControl(
                modifier = Modifier.weight(1f),
                icon = R.drawable.ic_message_stay,
                label = stringResource(R.string.arrival_mode_message_stay),
                description = stringResource(R.string.arrival_mode_message_stay_description),
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.onSurface,
                borderColor = MaterialTheme.colorScheme.outlineVariant,
                onClick = onMessage,
            )
        }
    }
}

@Composable
private fun TravelDayActionControl(
    modifier: Modifier = Modifier,
    icon: Int,
    label: String,
    description: String,
    containerColor: Color,
    contentColor: Color,
    borderColor: Color,
    onClick: () -> Unit,
) {
    Column(
        modifier = modifier
            .clickable(role = Role.Button, onClick = onClick)
            .semantics(mergeDescendants = true) { contentDescription = description },
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Surface(
            modifier = Modifier.size(56.dp),
            shape = CircleShape,
            color = containerColor,
            contentColor = contentColor,
            border = BorderStroke(1.dp, borderColor),
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    painter = painterResource(icon),
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = contentColor,
                )
            }
        }
        Spacer(Modifier.height(8.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = contentColor,
            maxLines = 2,
        )
    }
}

@Composable
private fun IfDelayedRow(label: String, value: String) {
    Column(Modifier.fillMaxWidth().padding(bottom = 10.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(3.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

private const val PDF_MIME_TYPE = "application/pdf"
