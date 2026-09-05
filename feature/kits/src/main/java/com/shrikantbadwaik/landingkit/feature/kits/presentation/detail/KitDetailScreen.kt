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
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.foundation.layout.wrapContentHeight
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
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
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
import com.shrikantbadwaik.landingkit.feature.kits.presentation.component.journalDoodles
import com.shrikantbadwaik.landingkit.feature.kits.presentation.component.JournalArtwork
import com.shrikantbadwaik.landingkit.feature.kits.presentation.component.JournalScene
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
import com.shrikantbadwaik.landingkit.feature.kits.presentation.detail.component.TravelToolsCard
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
    onPrintableSummary: () -> Unit,
    onShareSummary: () -> Unit,
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
    var heroHeightPx by remember { mutableStateOf(0f) }
    var backdropHeightPx by remember { mutableStateOf(0f) }
    var viewportHeightPx by remember { mutableStateOf(0f) }
    val listState = rememberLazyListState()

    val scrollOffsetPx by remember {
        derivedStateOf {
            if (listState.firstVisibleItemIndex == 0) {
                listState.firstVisibleItemScrollOffset.toFloat()
            } else {
                backdropHeightPx
            }
        }
    }
    val scrollProgress by remember {
        derivedStateOf {
            (scrollOffsetPx / heroHeightPx.coerceAtLeast(1f)).coerceIn(0f, 1f)
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
                .padding(padding)
                .clipToBounds()
                .onSizeChanged { viewportHeightPx = it.height.toFloat() },
        ) {
            val backdropOverflowPx = (backdropHeightPx - viewportHeightPx).coerceAtLeast(0f)
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 22.dp),
            ) {
                item(key = "detail_hero") {
                    Column {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .graphicsLayer {
                                    // Cancel the hero item's scroll translation so the panel
                                    // rises over it. Short viewports first reveal hero overflow.
                                    translationY = scrollOffsetPx - scrollOffsetPx.coerceAtMost(
                                        (backdropHeightPx - viewportHeightPx).coerceAtLeast(0f),
                                    )
                                }
                                .wrapContentHeight(align = Alignment.Top, unbounded = true)
                                .onGloballyPositioned {
                                    backdropHeightPx = it.size.height.toFloat()
                                }
                                .padding(horizontal = 22.dp),
                        ) {
                            ScreenHeader(
                                onBack = onBack,
                                label = stringResource(R.string.arrival_card_label),
                            )
                            Spacer(Modifier.height(20.dp))
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(min = 340.dp)
                                    .onGloballyPositioned { coordinates ->
                                        heroHeightPx = coordinates.size.height.toFloat()
                                    }
                                    .graphicsLayer {
                                        scaleX = heroScale
                                        scaleY = heroScale
                                    },
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                shape = MaterialTheme.shapes.large,
                                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                            ) {
                                Box {
                                    Column(
                                        Modifier
                                            .fillMaxWidth()
                                            .padding(24.dp),
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                stringResource(
                                                    when (phase) {
                                                        ArrivalPhase.UPCOMING -> R.string.detail_arriving_in
                                                        ArrivalPhase.TODAY -> R.string.detail_arriving_today
                                                        ArrivalPhase.PAST -> R.string.detail_arrived_in
                                                    },
                                                ),
                                                modifier = Modifier.weight(1f),
                                                style = MaterialTheme.typography.labelMedium,
                                                color = MaterialTheme.colorScheme.secondary,
                                            )
                                            EditKitAction(onClick = onEdit)
                                        }
                                        JournalArtwork(
                                            scene = JournalScene.Mountains,
                                            modifier = Modifier.fillMaxWidth().height(96.dp).padding(vertical = 12.dp),
                                        )
                                        Text(
                                            trip.destination,
                                            style = MaterialTheme.typography.headlineLarge,
                                            color = MaterialTheme.colorScheme.onSurface,
                                        )
                                        Spacer(Modifier.height(10.dp))
                                        Text(
                                            trip.arrivalDate.format(dateFormatter),
                                            style = MaterialTheme.typography.bodyLarge,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        )
                                        Spacer(Modifier.height(24.dp))
                                        ArrivalModeAction(
                                            onClick = onArrivalMode,
                                            modifier = Modifier.fillMaxWidth(),
                                        )
                                    }
                                }
                            }
                        }
                        Spacer(Modifier.height(with(density) { backdropOverflowPx.toDp() } + 22.dp))
                    }
                }
                item(key = "detail_panel_handle") {
                    Surface(
                        modifier = Modifier.fillMaxWidth().blockBackdropTaps(),
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
                item(key = "detail_printable_summary") {
                    PanelItem {
                        TravelToolsCard(
                            onPrintableSummary = onPrintableSummary,
                            onShareSummary = onShareSummary,
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
                            doodleSeed = "detail-stay:${trip.id}",
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
                    PanelItem(doodleSeed = "detail-checklist_header:${trip.id}") {
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
            }
        }
    }
}

@Composable
private fun PanelItem(doodleSeed: String? = null, content: @Composable () -> Unit) {
    val decoration = if (doodleSeed != null) Modifier.journalDoodles(doodleSeed, count = 1) else Modifier
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
            .blockBackdropTaps()
            .then(decoration)
            .padding(horizontal = 22.dp, vertical = 8.dp),
    ) {
        content()
    }
}

/**
 * Opaque panel items must win hit testing over the translated hero behind them,
 * including their non-interactive content and padding. Child controls keep their
 * clicks; drags cancel this detector and remain available to the scrolling list.
 */
private fun Modifier.blockBackdropTaps(): Modifier = pointerInput(Unit) {
    detectTapGestures(onTap = {})
}

private const val PDF_MIME_TYPE = "application/pdf"
private const val IMAGE_MIME_TYPE = "image/*"
private const val ATTACHMENT_FEEDBACK_DURATION_MILLIS = 4_000L
