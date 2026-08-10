package com.shrikantbadwaik.landingkit.feature.kits.presentation.detail

import android.content.ActivityNotFoundException
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import com.shrikantbadwaik.landingkit.core.data.PreparedAttachment
import com.shrikantbadwaik.landingkit.core.designsystem.LandingKitColors
import com.shrikantbadwaik.landingkit.core.model.ArrivalItem
import com.shrikantbadwaik.landingkit.core.model.KitAttachment
import com.shrikantbadwaik.landingkit.core.model.Trip
import com.shrikantbadwaik.landingkit.feature.kits.AttachmentImportResult
import com.shrikantbadwaik.landingkit.feature.kits.PendingAttachmentAuthentication
import com.shrikantbadwaik.landingkit.feature.kits.ProtectedAttachmentAction
import com.shrikantbadwaik.landingkit.feature.kits.R
import com.shrikantbadwaik.landingkit.feature.kits.requiresAttachmentAuthentication
import com.shrikantbadwaik.landingkit.feature.kits.toUserMessage
import com.shrikantbadwaik.landingkit.feature.kits.presentation.component.ScreenHeader
import com.shrikantbadwaik.landingkit.feature.kits.presentation.component.TicketIllustration
import com.shrikantbadwaik.landingkit.feature.kits.presentation.detail.component.AnimatedChecklistItem
import com.shrikantbadwaik.landingkit.feature.kits.presentation.detail.component.AttachmentSection
import com.shrikantbadwaik.landingkit.feature.kits.presentation.detail.component.DeleteAttachmentDialog
import com.shrikantbadwaik.landingkit.feature.kits.presentation.detail.component.ClearAttachmentsDialog
import com.shrikantbadwaik.landingkit.feature.kits.presentation.detail.component.DeleteKitDialog
import com.shrikantbadwaik.landingkit.feature.kits.presentation.detail.component.DetailCard
import com.shrikantbadwaik.landingkit.feature.kits.presentation.detail.component.EditKitAction
import com.shrikantbadwaik.landingkit.feature.kits.presentation.detail.component.EmptyChecklistCard
import com.shrikantbadwaik.landingkit.feature.kits.presentation.detail.component.KitDangerZone
import com.shrikantbadwaik.landingkit.feature.kits.presentation.detail.component.PastArrivalNotice
import com.shrikantbadwaik.landingkit.feature.kits.presentation.model.ArrivalPhase
import com.shrikantbadwaik.landingkit.feature.kits.presentation.model.arrivalPhase
import com.shrikantbadwaik.landingkit.feature.kits.presentation.model.shouldOfferAttachmentCleanup
import com.shrikantbadwaik.landingkit.feature.kits.presentation.security.AttachmentAuthenticationAvailability
import com.shrikantbadwaik.landingkit.feature.kits.presentation.security.AttachmentAuthenticator
import com.shrikantbadwaik.landingkit.feature.kits.presentation.security.findFragmentActivity
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.delay

@Composable
internal fun KitDetailScreen(
    trip: Trip,
    attachments: List<KitAttachment>,
    onBack: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onCheck: (ArrivalItem, Boolean) -> Unit,
    onImportAttachments: (List<android.net.Uri>, (AttachmentImportResult) -> Unit) -> Unit,
    onLoadAttachmentThumbnail: suspend (String) -> ByteArray?,
    onPrepareAttachment: (String, (Result<PreparedAttachment>) -> Unit) -> Unit,
    onDeleteAttachment: (String, (Result<Unit>) -> Unit) -> Unit,
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
    val activity = remember(context) { context.findFragmentActivity() }
    val authenticator = remember(activity) { activity?.let(::AttachmentAuthenticator) }
    var showDeleteConfirmation by remember { mutableStateOf(false) }
    var attachmentPendingDeletion by remember { mutableStateOf<KitAttachment?>(null) }
    var showClearAttachmentsConfirmation by remember { mutableStateOf(false) }
    var attachmentFeedback by remember { mutableStateOf<String?>(null) }
    var attachmentFeedbackVersion by remember { mutableIntStateOf(0) }
    var isImportingAttachment by remember { mutableStateOf(false) }
    val dateFormatter = DateTimeFormatter.ofPattern(stringResource(R.string.date_format_detail))
    val phase = remember(trip.arrivalDate) { trip.arrivalPhase(LocalDate.now()) }
    val authenticationTitle = stringResource(R.string.attachment_auth_prompt_title)
    val authenticationSubtitle = stringResource(R.string.attachment_auth_prompt_subtitle)
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
    val openAttachment: (KitAttachment) -> Unit = { attachment ->
        onPrepareAttachment(attachment.id) { result ->
            result.onSuccess { prepared ->
                val contentUri = FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    prepared.file,
                )
                val intent = Intent(Intent.ACTION_VIEW)
                    .setDataAndType(contentUri, prepared.mimeType)
                    .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                try {
                    context.startActivity(intent)
                } catch (_: ActivityNotFoundException) {
                    showAttachmentFeedback(context.getString(R.string.attachment_no_viewer))
                } catch (_: RuntimeException) {
                    showAttachmentFeedback(context.getString(R.string.attachment_open_failed))
                }
            }.onFailure {
                showAttachmentFeedback(context.getString(R.string.attachment_open_failed))
            }
        }
    }
    val latestAttachments by rememberUpdatedState(attachments)
    val latestOpenAttachment by rememberUpdatedState(openAttachment)

    LaunchedEffect(pendingAttachmentAuthentication?.id, authenticator) {
        val request = pendingAttachmentAuthentication ?: return@LaunchedEffect
        val activeAuthenticator = authenticator
        val isDisablingProtection =
            (request.action as? ProtectedAttachmentAction.SetProtection)?.enabled == false
        when (activeAuthenticator?.availability()) {
            AttachmentAuthenticationAvailability.Available -> activeAuthenticator.authenticate(
                title = authenticationTitle,
                subtitle = authenticationSubtitle,
                onSuccess = {
                    onFinishAttachmentAuthentication(request.id)
                    when (val action = request.action) {
                        is ProtectedAttachmentAction.Open -> {
                            onUnlockAttachmentAccess()
                            latestAttachments.firstOrNull { it.id == action.attachmentId }
                                ?.let(latestOpenAttachment)
                        }
                        is ProtectedAttachmentAction.SetProtection -> {
                            onSetAttachmentProtection(action.enabled)
                            if (action.enabled) {
                                onUnlockAttachmentAccess()
                            } else {
                                onLockAttachmentAccess()
                            }
                        }
                    }
                },
                onCancelled = { onFinishAttachmentAuthentication(request.id) },
                onError = {
                    onFinishAttachmentAuthentication(request.id)
                    showAttachmentFeedback(
                        context.getString(R.string.attachment_authentication_failed),
                    )
                },
            )
            AttachmentAuthenticationAvailability.NotEnrolled -> {
                onFinishAttachmentAuthentication(request.id)
                if (isDisablingProtection) {
                    onSetAttachmentProtection(false)
                    onLockAttachmentAccess()
                    showAttachmentFeedback(
                        context.getString(R.string.attachment_protection_disabled_unavailable),
                    )
                } else {
                    showAttachmentFeedback(
                        context.getString(R.string.attachment_authentication_not_enrolled),
                    )
                }
            }
            AttachmentAuthenticationAvailability.Unavailable, null -> {
                onFinishAttachmentAuthentication(request.id)
                if (isDisablingProtection) {
                    onSetAttachmentProtection(false)
                    onLockAttachmentAccess()
                    showAttachmentFeedback(
                        context.getString(R.string.attachment_protection_disabled_unavailable),
                    )
                } else {
                    showAttachmentFeedback(
                        context.getString(R.string.attachment_authentication_unavailable),
                    )
                }
            }
        }
    }

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

    Scaffold(containerColor = MaterialTheme.colorScheme.background) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(22.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item(key = "detail_header") {
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
            }
            item(key = "detail_hero") {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = LandingKitColors.Forest),
                    shape = RoundedCornerShape(30.dp),
                ) {
                    Box {
                        TicketIllustration(
                            Modifier.align(Alignment.TopEnd).offset(x = 42.dp, y = (-40).dp),
                        )
                        Column(Modifier.fillMaxWidth().padding(24.dp)) {
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
                            EditKitAction(
                                onClick = onEdit,
                                modifier = Modifier.align(Alignment.End),
                            )
                        }
                    }
                }
            }
            if (phase == ArrivalPhase.PAST) {
                item(key = "detail_past_notice") {
                    PastArrivalNotice()
                }
            }
            item(key = "detail_attachments") {
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
                    onOpen = { attachment ->
                        if (requiresAttachmentAuthentication(
                                protectionEnabled = attachmentProtectionEnabled,
                                accessUnlocked = isAttachmentAccessUnlocked,
                            )
                        ) {
                            onRequestAttachmentAuthentication(
                                ProtectedAttachmentAction.Open(attachment.id),
                            )
                        } else {
                            openAttachment(attachment)
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
            item(key = "detail_driver_address") {
                DetailCard(
                    stringResource(R.string.detail_show_driver),
                    trip.arrivalCard.localAddress.ifBlank { trip.arrivalCard.address },
                    LandingKitColors.Sun,
                )
            }
            item(key = "detail_stay") {
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
            if (trip.arrivalCard.bookingReference.isNotBlank()) {
                item(key = "detail_booking") {
                    DetailCard(
                        stringResource(R.string.detail_booking_reference),
                        trip.arrivalCard.bookingReference,
                        LandingKitColors.Sky,
                    )
                }
            }
            if (trip.arrivalCard.transferInstructions.isNotBlank()) {
                item(key = "detail_transfer") {
                    DetailCard(
                        stringResource(R.string.detail_from_airport),
                        trip.arrivalCard.transferInstructions,
                        LandingKitColors.Coral.copy(alpha = .55f),
                    )
                }
            }
            if (trip.arrivalCard.currencyNote.isNotBlank()) {
                item(key = "detail_money") {
                    DetailCard(
                        stringResource(R.string.detail_money),
                        trip.arrivalCard.currencyNote,
                        LandingKitColors.Sun,
                    )
                }
            }
            if (trip.arrivalCard.emergencyContact.isNotBlank()) {
                item(key = "detail_emergency") {
                    DetailCard(
                        stringResource(R.string.detail_emergency),
                        trip.arrivalCard.emergencyContact,
                        LandingKitColors.Lavender,
                    )
                }
            }
            if (trip.arrivalCard.dietaryPhrase.isNotBlank()) {
                item(key = "detail_dietary") {
                    DetailCard(
                        stringResource(R.string.detail_dietary_phrase),
                        trip.arrivalCard.dietaryPhrase,
                        LandingKitColors.Seafoam,
                    )
                }
            }
            item(key = "detail_checklist_header") {
                Text(
                    stringResource(
                        if (phase == ArrivalPhase.PAST) {
                            R.string.detail_arrival_checklist
                        } else {
                            R.string.detail_ready_before_landing
                        },
                    ),
                    style = MaterialTheme.typography.titleLarge,
                )
            }
            if (trip.items.isEmpty()) {
                item(key = "detail_checklist_empty") {
                    EmptyChecklistCard()
                }
            } else {
                items(trip.items, key = { it.id }) { item ->
                    AnimatedChecklistItem(item = item, onCheck = { onCheck(item, it) })
                }
            }
            item(key = "detail_danger_zone") {
                KitDangerZone(onDelete = { showDeleteConfirmation = true })
            }
            item(key = "detail_privacy_footer") {
                Text(
                    stringResource(R.string.detail_privacy_footer),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

private const val PDF_MIME_TYPE = "application/pdf"
private const val IMAGE_MIME_TYPE = "image/*"
private const val ATTACHMENT_FEEDBACK_DURATION_MILLIS = 4_000L
