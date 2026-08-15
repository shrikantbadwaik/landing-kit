package com.shrikantbadwaik.landingkit.feature.kits.presentation.detail

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.PointerEvent
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.node.ModifierNodeElement
import androidx.compose.ui.node.PointerInputModifierNode
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.InspectorInfo
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.shrikantbadwaik.landingkit.core.data.PreparedAttachment
import com.shrikantbadwaik.landingkit.core.designsystem.LandingKitColors
import com.shrikantbadwaik.landingkit.core.model.ArrivalItem
import com.shrikantbadwaik.landingkit.core.model.KitAttachment
import com.shrikantbadwaik.landingkit.core.model.OfflineArrivalContentFingerprint
import com.shrikantbadwaik.landingkit.core.model.Trip
import com.shrikantbadwaik.landingkit.core.model.effectiveOfflineCheckStatus
import com.shrikantbadwaik.landingkit.feature.kits.AttachmentImportResult
import com.shrikantbadwaik.landingkit.feature.kits.PendingAttachmentAuthentication
import com.shrikantbadwaik.landingkit.feature.kits.ProtectedAttachmentAction
import com.shrikantbadwaik.landingkit.feature.kits.R
import com.shrikantbadwaik.landingkit.feature.kits.presentation.component.ScreenHeader
import com.shrikantbadwaik.landingkit.feature.kits.presentation.component.TicketIllustration
import com.shrikantbadwaik.landingkit.feature.kits.presentation.detail.component.AnimatedChecklistItem
import com.shrikantbadwaik.landingkit.feature.kits.presentation.detail.component.ArrivalModeAction
import com.shrikantbadwaik.landingkit.feature.kits.presentation.detail.component.ArrivalReminderCard
import com.shrikantbadwaik.landingkit.feature.kits.presentation.detail.component.AttachmentSection
import com.shrikantbadwaik.landingkit.feature.kits.presentation.detail.component.ClearAttachmentsDialog
import com.shrikantbadwaik.landingkit.feature.kits.presentation.detail.component.DeleteAttachmentDialog
import com.shrikantbadwaik.landingkit.feature.kits.presentation.detail.component.DeleteKitDialog
import com.shrikantbadwaik.landingkit.feature.kits.presentation.detail.component.DetailCard
import com.shrikantbadwaik.landingkit.feature.kits.presentation.detail.component.EditKitAction
import com.shrikantbadwaik.landingkit.feature.kits.presentation.detail.component.EmptyChecklistCard
import com.shrikantbadwaik.landingkit.feature.kits.presentation.detail.component.KitDangerZone
import com.shrikantbadwaik.landingkit.feature.kits.presentation.detail.component.OfflineCheckStatusCard
import com.shrikantbadwaik.landingkit.feature.kits.presentation.detail.component.PastArrivalNotice
import com.shrikantbadwaik.landingkit.feature.kits.presentation.model.ArrivalPhase
import com.shrikantbadwaik.landingkit.feature.kits.presentation.model.arrivalPhase
import com.shrikantbadwaik.landingkit.feature.kits.presentation.model.shouldOfferAttachmentCleanup
import com.shrikantbadwaik.landingkit.feature.kits.presentation.security.rememberProtectedAttachmentOpener
import com.shrikantbadwaik.landingkit.feature.kits.toUserMessage
import kotlinx.coroutines.delay
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
internal fun KitDetailScreen(
    trip: Trip,
    attachments: List<KitAttachment>,
    onBack: () -> Unit,
    onArrivalMode: () -> Unit,
    onOfflineCheck: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onCheck: (ArrivalItem, Boolean) -> Unit,
    onImportAttachments: (List<android.net.Uri>, (AttachmentImportResult) -> Unit) -> Unit,
    onLoadAttachmentThumbnail: suspend (String) -> ByteArray?,
    onPrepareAttachment: (String, (Result<PreparedAttachment>) -> Unit) -> Unit,
    onDeleteAttachment: (String, (Result<Unit>) -> Unit) -> Unit,
    onSetQuickAccessAttachment: (String?, (Result<Unit>) -> Unit) -> Unit,
    onSetArrivalReminder: (Boolean, (Result<Unit>) -> Unit) -> Unit,
    onDeleteAttachments: ((Result<Int>) -> Unit) -> Unit,
    attachmentProtectionEnabled: Boolean,
    isAttachmentAccessUnlocked: Boolean,
    pendingAttachmentAuthentication: PendingAttachmentAuthentication?,
    onSetAttachmentProtection: (Boolean) -> Unit,
    onRequestAttachmentAuthentication: (ProtectedAttachmentAction) -> Unit,
    onFinishAttachmentAuthentication: (String) -> Unit,
    onUnlockAttachmentAccess: () -> Unit,
    onLockAttachmentAccess: () -> Unit,
) {
    val context = LocalContext.current
    var showDeleteConfirmation by remember { mutableStateOf(false) }
    var attachmentPendingDeletion by remember { mutableStateOf<KitAttachment?>(null) }
    var showClearAttachmentsConfirmation by remember { mutableStateOf(false) }
    var attachmentFeedback by remember { mutableStateOf<String?>(null) }
    var attachmentFeedbackVersion by remember { mutableIntStateOf(0) }
    var isImportingAttachment by remember { mutableStateOf(false) }
    var arrivalReminderPermissionDenied by remember(trip.id) { mutableStateOf(false) }
    var arrivalReminderChangeFailed by remember(trip.id) { mutableStateOf(false) }
    val dateFormatter = DateTimeFormatter.ofPattern(stringResource(R.string.date_format_detail))
    val phase = remember(trip.arrivalDate) { trip.arrivalPhase(LocalDate.now()) }
    val offlineCheckStatus = remember(trip, attachments) {
        trip.effectiveOfflineCheckStatus(
            OfflineArrivalContentFingerprint.derive(trip, attachments),
        )
    }
    val showAttachmentFeedback: (String) -> Unit = { message ->
        attachmentFeedback = message
        attachmentFeedbackVersion++
    }
    val attachmentPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenMultipleDocuments(),
    ) { sources ->
        if (sources.isNotEmpty()) {
            isImportingAttachment = true
            onImportAttachments(sources) { result ->
                isImportingAttachment = false
                showAttachmentFeedback(result.toUserMessage(context))
            }
        }
    }
    val reminderPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
    ) { granted ->
        arrivalReminderPermissionDenied = !granted
        if (granted) {
            onSetArrivalReminder(true) { result ->
                arrivalReminderChangeFailed = result.isFailure
            }
        }
    }
    val setArrivalReminder: (Boolean) -> Unit = { enabled ->
        arrivalReminderChangeFailed = false
        if (
            enabled &&
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS,
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            reminderPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            arrivalReminderPermissionDenied = false
            onSetArrivalReminder(enabled) { result ->
                arrivalReminderChangeFailed = result.isFailure
            }
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
        onFeedback = showAttachmentFeedback,
    )

    LaunchedEffect(attachmentFeedbackVersion) {
        if (attachmentFeedback != null) {
            delay(ATTACHMENT_FEEDBACK_DURATION_MILLIS)
            attachmentFeedback = null
        }
    }

    if (showDeleteConfirmation) {
        DeleteKitDialog(
            destination = trip.destination,
            onConfirm = {
                showDeleteConfirmation = false
                onDelete()
            },
            onDismiss = { showDeleteConfirmation = false },
        )
    }

    attachmentPendingDeletion?.let { attachment ->
        DeleteAttachmentDialog(
            attachment = attachment,
            onDismiss = { attachmentPendingDeletion = null },
            onDelete = {
                attachmentPendingDeletion = null
                onDeleteAttachment(attachment.id) { result ->
                    showAttachmentFeedback(
                        if (result.isSuccess) {
                            context.getString(R.string.attachment_removed, attachment.displayName)
                        } else {
                            context.getString(R.string.attachment_delete_failed)
                        },
                    )
                }
            },
        )
    }

    if (showClearAttachmentsConfirmation) {
        ClearAttachmentsDialog(
            attachmentCount = attachments.size,
            onDismiss = { showClearAttachmentsConfirmation = false },
            onDelete = {
                showClearAttachmentsConfirmation = false
                onDeleteAttachments { result ->
                    showAttachmentFeedback(
                        result.fold(
                            onSuccess = { deleted ->
                                context.resources.getQuantityString(
                                    R.plurals.attachment_cleanup_complete,
                                    deleted,
                                    deleted,
                                )
                            },
                            onFailure = {
                                context.getString(R.string.attachment_cleanup_failed)
                            },
                        ),
                    )
                }
            },
        )
    }

    val density = LocalDensity.current
    var heroHeightDp by remember { mutableStateOf(0.dp) }
    var heroHeightPx by remember { mutableStateOf(0f) }
    val listState = rememberLazyListState()

    val scrollProgress by remember {
        derivedStateOf {
            if (listState.firstVisibleItemIndex == 0) {
                (listState.firstVisibleItemScrollOffset / heroHeightPx.coerceAtLeast(1f)).coerceIn(
                    0f,
                    1f,
                )
            } else {
                1f
            }
        }
    }
    val heroScale by animateFloatAsState(
        targetValue = 1f - (scrollProgress * 0.04f),
        animationSpec = tween(durationMillis = 80, easing = LinearEasing),
        label = "heroScale",
    )

    Scaffold(containerColor = MaterialTheme.colorScheme.background) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 22.dp),
            ) {
                ScreenHeader(
                    onBack = onBack,
                    label = stringResource(R.string.arrival_card_label),
                    badge = stringResource(
                        when (phase) {
                            ArrivalPhase.UPCOMING -> R.string.detail_badge_offline
                            ArrivalPhase.TODAY -> R.string.detail_badge_today
                            ArrivalPhase.PAST -> R.string.detail_badge_past
                        },
                    ),
                )
                Spacer(Modifier.height(16.dp))
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .onGloballyPositioned { coordinates ->
                            with(density) {
                                heroHeightDp = coordinates.size.height.toDp()
                            }
                            heroHeightPx = coordinates.size.height.toFloat()
                        }
                        .graphicsLayer {
                            scaleX = heroScale
                            scaleY = heroScale
                        },
                    colors = CardDefaults.cardColors(containerColor = LandingKitColors.Forest),
                    shape = RoundedCornerShape(30.dp),
                ) {
                    Box {
                        TicketIllustration(
                            Modifier
                                .align(Alignment.TopEnd)
                                .offset(x = 42.dp, y = (-40).dp),
                        )
                        Column(
                            Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                        ) {
                            Text(
                                stringResource(
                                    when (phase) {
                                        ArrivalPhase.UPCOMING -> R.string.detail_arriving_in
                                        ArrivalPhase.TODAY -> R.string.detail_arriving_today
                                        ArrivalPhase.PAST -> R.string.detail_arrived_in
                                    },
                                ),
                                style = MaterialTheme.typography.labelSmall,
                                color = LandingKitColors.Seafoam,
                            )
                            Spacer(Modifier.height(24.dp))
                            Text(
                                trip.destination,
                                style = MaterialTheme.typography.headlineMedium,
                                color = Color.White,
                            )
                            Spacer(Modifier.height(10.dp))
                            Text(
                                trip.arrivalDate.format(dateFormatter),
                                color = Color.White.copy(alpha = .78f),
                            )
                            Spacer(Modifier.height(22.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                ArrivalModeAction(
                                    onClick = onArrivalMode,
                                    modifier = Modifier.weight(1f),
                                )
                                EditKitAction(onClick = onEdit)
                            }
                        }
                    }
                }
            }

            val headerOffset = 56.dp + 16.dp
            val panelTopPadding = headerOffset + heroHeightDp + 16.dp

            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .sharePointerInputWithSiblings()
                    .align(Alignment.TopStart),
                contentPadding = PaddingValues(
                    top = panelTopPadding,
                    bottom = 22.dp,
                ),
            ) {
                item(key = "detail_panel_handle") {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
                        color = MaterialTheme.colorScheme.background,
                        shadowElevation = 4.dp,
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 12.dp, bottom = 4.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            Surface(
                                modifier = Modifier
                                    .height(4.dp)
                                    .clip(RoundedCornerShape(2.dp)),
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = .28f),
                            ) {
                                Spacer(
                                    Modifier
                                        .height(4.dp)
                                        .padding(horizontal = 20.dp),
                                )
                            }
                        }
                    }
                }
                if (phase == ArrivalPhase.PAST) {
                    item(key = "detail_past_notice") {
                        PanelItem { PastArrivalNotice() }
                    }
                }
                if (phase != ArrivalPhase.PAST) {
                    item(key = "detail_arrival_reminder") {
                        PanelItem {
                            ArrivalReminderCard(
                                enabled = trip.arrivalReminderEnabled,
                                arrivalDate = trip.arrivalDate.format(dateFormatter),
                                permissionDenied = arrivalReminderPermissionDenied,
                                changeFailed = arrivalReminderChangeFailed,
                                onEnabledChange = setArrivalReminder,
                            )
                        }
                    }
                }
                item(key = "detail_offline_check") {
                    PanelItem {
                        OfflineCheckStatusCard(
                            status = offlineCheckStatus,
                            onClick = onOfflineCheck,
                        )
                    }
                }
                item(key = "detail_attachments") {
                    PanelItem {
                        AttachmentSection(
                            attachments = attachments,
                            isImporting = isImportingAttachment,
                            feedback = attachmentFeedback,
                            protectionEnabled = attachmentProtectionEnabled,
                            protectionControlEnabled = pendingAttachmentAuthentication == null,
                            showRetentionCleanup = shouldOfferAttachmentCleanup(
                                arrivalPhase = phase,
                                attachmentCount = attachments.size,
                            ),
                            onLoadThumbnail = onLoadAttachmentThumbnail,
                            onImport = {
                                attachmentPicker.launch(arrayOf(PDF_MIME_TYPE, IMAGE_MIME_TYPE))
                            },
                            onOpen = openAttachment,
                            onQuickAccess = { attachment ->
                                val attachmentId =
                                    attachment.takeUnless(KitAttachment::isQuickAccess)?.id
                                onSetQuickAccessAttachment(attachmentId) { result ->
                                    if (result.isFailure) {
                                        showAttachmentFeedback(
                                            context.getString(
                                                R.string.attachment_quick_access_failed,
                                            ),
                                        )
                                    }
                                }
                            },
                            onProtectionChange = { enabled ->
                                if (!enabled && isAttachmentAccessUnlocked) {
                                    onSetAttachmentProtection(false)
                                    onLockAttachmentAccess()
                                } else {
                                    onRequestAttachmentAuthentication(
                                        ProtectedAttachmentAction.SetProtection(enabled),
                                    )
                                }
                            },
                            onClearAttachments = { showClearAttachmentsConfirmation = true },
                            onDelete = { attachmentPendingDeletion = it },
                        )
                    }
                }
                item(key = "detail_driver_address") {
                    PanelItem {
                        DetailCard(
                            stringResource(R.string.detail_show_driver),
                            trip.arrivalCard.localAddress.ifBlank { trip.arrivalCard.address },
                            LandingKitColors.Sun,
                        )
                    }
                }
                item(key = "detail_stay") {
                    PanelItem {
                        DetailCard(
                            stringResource(R.string.detail_stay),
                            stringResource(
                                R.string.detail_stay_value,
                                trip.arrivalCard.accommodationName,
                                trip.arrivalCard.address,
                            ),
                            LandingKitColors.Seafoam,
                        )
                    }
                }
                if (trip.arrivalCard.bookingReference.isNotBlank()) {
                    item(key = "detail_booking") {
                        PanelItem {
                            DetailCard(
                                stringResource(R.string.detail_booking_reference),
                                trip.arrivalCard.bookingReference,
                                LandingKitColors.Sky,
                            )
                        }
                    }
                }
                if (trip.arrivalCard.transferInstructions.isNotBlank()) {
                    item(key = "detail_transfer") {
                        PanelItem {
                            DetailCard(
                                stringResource(R.string.detail_from_airport),
                                trip.arrivalCard.transferInstructions,
                                LandingKitColors.Coral.copy(alpha = .55f),
                            )
                        }
                    }
                }
                if (trip.arrivalCard.currencyNote.isNotBlank()) {
                    item(key = "detail_money") {
                        PanelItem {
                            DetailCard(
                                stringResource(R.string.detail_money),
                                trip.arrivalCard.currencyNote,
                                LandingKitColors.Sun,
                            )
                        }
                    }
                }
                if (trip.arrivalCard.emergencyContact.isNotBlank()) {
                    item(key = "detail_emergency") {
                        PanelItem {
                            DetailCard(
                                stringResource(R.string.detail_emergency),
                                trip.arrivalCard.emergencyContact,
                                LandingKitColors.Lavender,
                            )
                        }
                    }
                }
                if (trip.arrivalCard.dietaryPhrase.isNotBlank()) {
                    item(key = "detail_dietary") {
                        PanelItem {
                            DetailCard(
                                stringResource(R.string.detail_dietary_phrase),
                                trip.arrivalCard.dietaryPhrase,
                                LandingKitColors.Seafoam,
                            )
                        }
                    }
                }
                item(key = "detail_checklist_header") {
                    PanelItem {
                        Text(
                            stringResource(
                                if (phase == ArrivalPhase.PAST) {
                                    R.string.detail_arrival_checklist
                                } else {
                                    R.string.detail_ready_before_landing
                                },
                            ),
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onBackground,
                        )
                    }
                }
                if (trip.items.isEmpty()) {
                    item(key = "detail_checklist_empty") {
                        PanelItem { EmptyChecklistCard() }
                    }
                } else {
                    items(trip.items, key = { it.id }) { item ->
                        PanelItem {
                            AnimatedChecklistItem(item = item, onCheck = { onCheck(item, it) })
                        }
                    }
                }
                item(key = "detail_danger_zone") {
                    PanelItem {
                        KitDangerZone(onDelete = { showDeleteConfirmation = true })
                    }
                }
                item(key = "detail_privacy_footer") {
                    PanelItem {
                        Text(
                            stringResource(R.string.detail_privacy_footer),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PanelItem(content: @Composable () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 22.dp, vertical = 8.dp),
    ) {
        content()
    }
}

private fun Modifier.sharePointerInputWithSiblings(): Modifier =
    this then SharePointerInputElement

private data object SharePointerInputElement : ModifierNodeElement<SharePointerInputNode>() {
    override fun create() = SharePointerInputNode()

    override fun update(node: SharePointerInputNode) = Unit

    override fun InspectorInfo.inspectableProperties() {
        name = "sharePointerInputWithSiblings"
    }
}

private class SharePointerInputNode : Modifier.Node(), PointerInputModifierNode {
    override fun onPointerEvent(
        pointerEvent: PointerEvent,
        pass: PointerEventPass,
        bounds: IntSize,
    ) = Unit

    override fun onCancelPointerInput() = Unit

    override fun sharePointerInputWithSiblings() = true
}

private const val PDF_MIME_TYPE = "application/pdf"
private const val IMAGE_MIME_TYPE = "image/*"
private const val ATTACHMENT_FEEDBACK_DURATION_MILLIS = 4_000L
