package com.shrikantbadwaik.landingkit.feature.kits.presentation.offlinecheck

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.paneTitle
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.shrikantbadwaik.landingkit.core.data.PreparedAttachment
import com.shrikantbadwaik.landingkit.core.designsystem.LandingKitColors
import com.shrikantbadwaik.landingkit.core.model.KitAttachment
import com.shrikantbadwaik.landingkit.core.model.OfflineArrivalCheckIssue
import com.shrikantbadwaik.landingkit.core.model.OfflineArrivalCheckStatus
import com.shrikantbadwaik.landingkit.core.model.OfflineArrivalContentFingerprint
import com.shrikantbadwaik.landingkit.core.model.Trip
import com.shrikantbadwaik.landingkit.core.model.effectiveOfflineCheckStatus
import com.shrikantbadwaik.landingkit.feature.kits.OfflineArrivalCheckUiState
import com.shrikantbadwaik.landingkit.feature.kits.PendingAttachmentAuthentication
import com.shrikantbadwaik.landingkit.feature.kits.ProtectedAttachmentAction
import com.shrikantbadwaik.landingkit.feature.kits.R
import com.shrikantbadwaik.landingkit.feature.kits.presentation.component.ScreenHeader
import com.shrikantbadwaik.landingkit.feature.kits.presentation.security.rememberProtectedAttachmentOpener
import com.shrikantbadwaik.landingkit.feature.kits.requiresAttachmentAuthentication
import java.text.DateFormat
import java.util.Date

@Composable
internal fun OfflineCheckScreen(
    trip: Trip,
    attachments: List<KitAttachment>,
    state: OfflineArrivalCheckUiState,
    onBack: () -> Unit,
    onRunCheck: () -> Unit,
    onEdit: () -> Unit,
    onManageEvidence: () -> Unit,
    onOpenArrivalMode: () -> Unit,
    attachmentProtectionEnabled: Boolean,
    isAttachmentAccessUnlocked: Boolean,
    pendingAttachmentAuthentication: PendingAttachmentAuthentication?,
    onPrepareAttachment: (String, (Result<PreparedAttachment>) -> Unit) -> Unit,
    onSetAttachmentProtection: (Boolean) -> Unit,
    onRequestAttachmentAuthentication: (ProtectedAttachmentAction) -> Unit,
    onFinishAttachmentAuthentication: (String) -> Unit,
    onUnlockAttachmentAccess: () -> Unit,
    onLockAttachmentAccess: () -> Unit,
) {
    val title = stringResource(R.string.offline_check_header)
    var feedback by remember(trip.id) { mutableStateOf<String?>(null) }
    val cancelledMessage = stringResource(R.string.offline_check_unlock_cancelled)
    rememberProtectedAttachmentOpener(
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
        onFeedback = { feedback = it },
        onOfflineCheckAuthenticated = { tripId ->
            if (tripId == trip.id) onRunCheck()
        },
        onAuthenticationCancelled = { action ->
            if (action is ProtectedAttachmentAction.VerifyOfflineCheck) {
                feedback = cancelledMessage
            }
        },
    )
    val currentFingerprint = remember(trip, attachments) {
        OfflineArrivalContentFingerprint.derive(trip, attachments)
    }
    val completed = state as? OfflineArrivalCheckUiState.Complete
    val runtimeResult = completed?.takeIf {
        it.tripId == trip.id && it.contentFingerprint == currentFingerprint
    }?.result
    val effectiveState = if (completed != null && runtimeResult == null) {
        OfflineArrivalCheckUiState.Idle
    } else {
        state
    }
    val status = when {
        state is OfflineArrivalCheckUiState.Checking && state.tripId == trip.id ->
            OfflineArrivalCheckStatus.NotChecked
        runtimeResult != null -> runtimeResult.status
        else -> trip.effectiveOfflineCheckStatus(currentFingerprint)
    }
    val issues = runtimeResult?.issues.orEmpty()
    val quickAccess = attachments.firstOrNull(KitAttachment::isQuickAccess)
    val supportedEvidence = quickAccess?.mimeType == "application/pdf" ||
        quickAccess?.mimeType?.startsWith("image/") == true
    val checkAction = {
        feedback = null
        if (
            requiresAttachmentAuthentication(
                attachmentProtectionEnabled,
                isAttachmentAccessUnlocked,
            )
        ) {
            onRequestAttachmentAuthentication(
                ProtectedAttachmentAction.VerifyOfflineCheck(trip.id),
            )
        } else {
            onRunCheck()
        }
    }

    Scaffold(
        modifier = Modifier.semantics { paneTitle = title },
        containerColor = MaterialTheme.colorScheme.background,
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(22.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item(key = "offline_check_header") {
                ScreenHeader(
                    onBack = onBack,
                    label = title,
                    badge = stringResource(R.string.offline_check_badge),
                )
            }
            item(key = "offline_check_status") {
                OfflineCheckStatusPanel(
                    status = status,
                    state = effectiveState,
                    tripId = trip.id,
                    checkedAtMillis = trip.offlineCheckedAtMillis,
                )
            }
            feedback?.let { message ->
                item(key = "offline_check_feedback") {
                    Text(
                        text = message,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }
            item(key = "offline_check_rules") {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    color = MaterialTheme.colorScheme.surface,
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp),
                    ) {
                        RuleRow(
                            stringResource(R.string.offline_check_rule_stay),
                            trip.arrivalCard.accommodationName.isNotBlank(),
                        )
                        RuleRow(
                            stringResource(R.string.offline_check_rule_address),
                            trip.arrivalCard.localAddress.isNotBlank() ||
                                trip.arrivalCard.address.isNotBlank(),
                        )
                        RuleRow(
                            stringResource(R.string.offline_check_rule_evidence),
                            supportedEvidence,
                        )
                        RuleRow(
                            stringResource(R.string.offline_check_rule_readable),
                            status == OfflineArrivalCheckStatus.ReadyOnThisPhone &&
                                OfflineArrivalCheckIssue.UnreadableEvidence !in issues,
                        )
                    }
                }
            }
            item(key = "offline_check_action") {
                Button(
                    onClick = checkAction,
                    enabled = state !is OfflineArrivalCheckUiState.Checking,
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(18.dp),
                ) {
                    Text(
                        stringResource(
                            if (status == OfflineArrivalCheckStatus.NotChecked) {
                                R.string.offline_check_run_action
                            } else {
                                R.string.offline_check_run_again_action
                            },
                        ),
                    )
                }
            }
            if (status == OfflineArrivalCheckStatus.NeedsAttention) {
                item(key = "offline_check_fixes") {
                    Column {
                        TextButton(onClick = onEdit) {
                            Text(stringResource(R.string.offline_check_fix_details))
                        }
                        TextButton(onClick = onManageEvidence) {
                            Text(stringResource(R.string.offline_check_fix_evidence))
                        }
                    }
                }
            }
            if (status == OfflineArrivalCheckStatus.ReadyOnThisPhone) {
                item(key = "offline_check_rehearse") {
                    Button(
                        onClick = onOpenArrivalMode,
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        shape = RoundedCornerShape(18.dp),
                    ) {
                        Text(stringResource(R.string.offline_check_rehearse_action))
                    }
                }
            }
            item(key = "offline_check_honesty") {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    color = LandingKitColors.Sun.copy(alpha = .18f),
                ) {
                    Column(Modifier.padding(18.dp)) {
                        Text(
                            text = stringResource(R.string.offline_check_honesty_title),
                            style = MaterialTheme.typography.titleSmall,
                            modifier = Modifier.semantics { heading() },
                        )
                        Spacer(Modifier.height(6.dp))
                        Text(
                            text = stringResource(R.string.offline_check_honesty_copy),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun OfflineCheckStatusPanel(
    status: OfflineArrivalCheckStatus,
    state: OfflineArrivalCheckUiState,
    tripId: String,
    checkedAtMillis: Long?,
) {
    val isChecking = state is OfflineArrivalCheckUiState.Checking && state.tripId == tripId
    val isFailed = state is OfflineArrivalCheckUiState.Failed && state.tripId == tripId
    val statusText = stringResource(
        when {
            isChecking -> R.string.offline_check_status_checking
            isFailed -> R.string.offline_check_status_failed
            status == OfflineArrivalCheckStatus.ReadyOnThisPhone ->
                R.string.offline_check_status_ready
            status == OfflineArrivalCheckStatus.NeedsAttention ->
                R.string.offline_check_status_attention
            else -> R.string.offline_check_status_not_checked
        },
    )
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        color = when (status) {
            OfflineArrivalCheckStatus.ReadyOnThisPhone ->
                LandingKitColors.Seafoam.copy(alpha = .32f)
            OfflineArrivalCheckStatus.NeedsAttention ->
                LandingKitColors.Coral.copy(alpha = .18f)
            OfflineArrivalCheckStatus.NotChecked -> MaterialTheme.colorScheme.surface
        },
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    ) {
        Column(Modifier.padding(22.dp)) {
            Text(
                text = statusText,
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.semantics { heading() },
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = when {
                    state is OfflineArrivalCheckUiState.Complete && state.tripId == tripId ->
                        stringResource(R.string.offline_check_checked_now)
                    checkedAtMillis != null && status != OfflineArrivalCheckStatus.NotChecked ->
                        stringResource(
                            R.string.offline_check_checked_at,
                            DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT)
                                .format(Date(checkedAtMillis)),
                        )
                    else -> stringResource(R.string.offline_check_intro)
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun RuleRow(label: String, passed: Boolean) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Surface(
            modifier = Modifier.size(28.dp),
            shape = CircleShape,
            color = if (passed) {
                LandingKitColors.Seafoam
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            },
            contentColor = LandingKitColors.Ink,
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Text(if (passed) "✓" else "—")
            }
        }
        Text(
            text = label,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}
