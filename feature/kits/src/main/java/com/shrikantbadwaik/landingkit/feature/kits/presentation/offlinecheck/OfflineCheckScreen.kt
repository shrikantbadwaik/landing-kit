package com.shrikantbadwaik.landingkit.feature.kits.presentation.offlinecheck

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
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
import com.shrikantbadwaik.landingkit.core.model.OfflineArrivalCheckIssue
import com.shrikantbadwaik.landingkit.core.model.OfflineArrivalCheckStatus
import com.shrikantbadwaik.landingkit.core.model.OfflineArrivalContentFingerprint
import com.shrikantbadwaik.landingkit.core.model.PlanBContentFingerprint
import com.shrikantbadwaik.landingkit.core.model.Trip
import com.shrikantbadwaik.landingkit.core.model.effectiveOfflineCheckStatus
import com.shrikantbadwaik.landingkit.core.model.effectivePlanBCheckStatus
import com.shrikantbadwaik.landingkit.core.model.hasPlanBData
import com.shrikantbadwaik.landingkit.core.model.isPlanBCheckStale
import com.shrikantbadwaik.landingkit.feature.kits.OfflineArrivalCheckUiState
import com.shrikantbadwaik.landingkit.feature.kits.PlanBCheckUiState
import com.shrikantbadwaik.landingkit.feature.kits.PendingAttachmentAuthentication
import com.shrikantbadwaik.landingkit.feature.kits.ProtectedAttachmentAction
import com.shrikantbadwaik.landingkit.feature.kits.R
import com.shrikantbadwaik.landingkit.feature.kits.presentation.component.ScreenHeader
import com.shrikantbadwaik.landingkit.feature.kits.presentation.component.JournalBackground
import com.shrikantbadwaik.landingkit.feature.kits.presentation.security.rememberProtectedAttachmentOpener
import com.shrikantbadwaik.landingkit.feature.kits.requiresAttachmentAuthentication
import java.text.DateFormat
import java.util.Date

private val JoinedStatusShape = RoundedCornerShape(
    topStart = 28.dp,
    topEnd = 28.dp,
    bottomEnd = 0.dp,
    bottomStart = 0.dp,
)
private val JoinedChecklistShape = RoundedCornerShape(
    topStart = 0.dp,
    topEnd = 0.dp,
    bottomEnd = 24.dp,
    bottomStart = 24.dp,
)

@Composable
internal fun OfflineCheckScreen(
    trip: Trip,
    attachments: List<KitAttachment>,
    state: OfflineArrivalCheckUiState,
    planBState: PlanBCheckUiState,
    onBack: () -> Unit,
    onRunCheck: () -> Unit,
    onRunPlanBCheck: () -> Unit,
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
    val hasPlanBData = trip.hasPlanBData()
    val planBFingerprint = remember(trip) { PlanBContentFingerprint.derive(trip) }
    val completedPlanB = planBState as? PlanBCheckUiState.Complete
    val runtimePlanBResult = completedPlanB?.takeIf {
        it.tripId == trip.id && it.contentFingerprint == planBFingerprint
    }?.result
    val effectivePlanBState = if (completedPlanB != null && runtimePlanBResult == null) {
        PlanBCheckUiState.Idle
    } else {
        planBState
    }
    val planBStatus = when {
        planBState is PlanBCheckUiState.Checking && planBState.tripId == trip.id ->
            OfflineArrivalCheckStatus.NotChecked
        runtimePlanBResult != null -> runtimePlanBResult.status
        else -> trip.effectivePlanBCheckStatus(planBFingerprint)
    }
    val planBStale = trip.isPlanBCheckStale(planBFingerprint)
    val hasCheckInRecovery = trip.arrivalCard.checkInCutoff.isNotBlank() ||
        trip.arrivalCard.lateArrivalInstructions.isNotBlank()

    Scaffold(
        modifier = Modifier.semantics { paneTitle = title },
        containerColor = MaterialTheme.colorScheme.background,
    ) { padding ->
        val listState = rememberLazyListState()
        JournalBackground(
            seed = "offline:${trip.id}",
            listState = listState,
            count = if (hasPlanBData) 3 else 2,
            modifier = Modifier.fillMaxSize().padding(padding),
        ) {
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(22.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                item(key = "offline_check_header") {
                    ScreenHeader(
                        onBack = onBack,
                        label = title,
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
                item(key = "offline_check_group") {
                    Column {
                        OfflineCheckStatusPanel(
                            status = status,
                            state = effectiveState,
                            tripId = trip.id,
                            checkedAtMillis = trip.offlineCheckedAtMillis,
                            shape = JoinedStatusShape,
                        )
                        EditableChecklistCard(
                            editDescription = stringResource(
                                R.string.offline_check_edit_arrival_details_description,
                            ),
                            onEdit = onEdit,
                            checkLabel = if (status != OfflineArrivalCheckStatus.ReadyOnThisPhone) {
                                stringResource(R.string.offline_check_run_again_action)
                            } else {
                                null
                            },
                            checkEnabled = state !is OfflineArrivalCheckUiState.Checking,
                            onCheck = checkAction,
                            shape = JoinedChecklistShape,
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
                                actionDescription = stringResource(
                                    R.string.offline_check_fix_evidence,
                                ),
                                onClick = onManageEvidence,
                            )
                            RuleRow(
                                stringResource(R.string.offline_check_rule_readable),
                                status == OfflineArrivalCheckStatus.ReadyOnThisPhone &&
                                    OfflineArrivalCheckIssue.UnreadableEvidence !in issues,
                            )
                        }
                    }
                }
                if (hasPlanBData) {
                    item(key = "offline_check_plan_b_group") {
                        Column(Modifier.testTag("plan_b_checks")) {
                            PlanBCheckStatusPanel(
                                status = planBStatus,
                                state = effectivePlanBState,
                                tripId = trip.id,
                                checkedAtMillis = trip.planBCheckedAtMillis,
                                stale = planBStale,
                                shape = JoinedStatusShape,
                            )
                            EditableChecklistCard(
                                editDescription = stringResource(
                                    R.string.offline_check_edit_plan_b_description,
                                ),
                                onEdit = onEdit,
                                checkLabel = if (
                                    planBStatus != OfflineArrivalCheckStatus.ReadyOnThisPhone
                                ) {
                                    stringResource(R.string.offline_check_run_again_action)
                                } else {
                                    null
                                },
                                checkEnabled = planBState !is PlanBCheckUiState.Checking,
                                onCheck = onRunPlanBCheck,
                                shape = JoinedChecklistShape,
                            ) {
                                RuleRow(
                                    stringResource(R.string.plan_b_check_rule_fallback),
                                    trip.arrivalCard.fallbackTransport.isNotBlank(),
                                )
                                RuleRow(
                                    stringResource(R.string.plan_b_check_rule_contact),
                                    trip.arrivalCard.accommodationContact.isNotBlank(),
                                )
                                RuleRow(
                                    stringResource(R.string.plan_b_check_rule_recovery),
                                    hasCheckInRecovery,
                                )
                            }
                        }
                    }
                }
                if (status == OfflineArrivalCheckStatus.ReadyOnThisPhone) {
                    item(key = "offline_check_rehearse") {
                        Button(
                            onClick = onOpenArrivalMode,
                            modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp),
                            shape = MaterialTheme.shapes.medium,
                        ) {
                            Text(stringResource(R.string.offline_check_rehearse_action))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EditableChecklistCard(
    editDescription: String,
    onEdit: () -> Unit,
    checkLabel: String?,
    checkEnabled: Boolean,
    onCheck: () -> Unit,
    shape: RoundedCornerShape,
    content: @Composable ColumnScope.() -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = shape,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        color = MaterialTheme.colorScheme.surface,
    ) {
        Box {
            Column(
                modifier = Modifier.fillMaxWidth().padding(18.dp),
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(end = 54.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    content = content,
                )
                checkLabel?.let { label ->
                    Spacer(Modifier.height(16.dp))
                    OutlinedButton(
                        onClick = onCheck,
                        enabled = checkEnabled,
                        modifier = Modifier
                            .align(Alignment.End)
                            .heightIn(min = 48.dp),
                        shape = MaterialTheme.shapes.medium,
                        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
                    ) {
                        Text(label)
                    }
                }
            }
            Box(Modifier.align(Alignment.TopEnd).padding(10.dp)) {
                Surface(
                    onClick = onEdit,
                    modifier = Modifier
                        .size(48.dp)
                        .semantics { contentDescription = editDescription },
                    shape = CircleShape,
                    color = LandingKitColors.Sun.copy(alpha = .22f),
                    contentColor = MaterialTheme.colorScheme.onSurface,
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            painter = painterResource(R.drawable.ic_edit_kit),
                            contentDescription = null,
                            modifier = Modifier.size(20.dp),
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
    shape: RoundedCornerShape,
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
        shape = shape,
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
private fun PlanBCheckStatusPanel(
    status: OfflineArrivalCheckStatus,
    state: PlanBCheckUiState,
    tripId: String,
    checkedAtMillis: Long?,
    stale: Boolean,
    shape: RoundedCornerShape,
) {
    val isChecking = state is PlanBCheckUiState.Checking && state.tripId == tripId
    val isFailed = state is PlanBCheckUiState.Failed && state.tripId == tripId
    val statusText = stringResource(
        when {
            isChecking -> R.string.plan_b_status_checking
            isFailed -> R.string.plan_b_status_failed
            stale -> R.string.plan_b_status_stale
            status == OfflineArrivalCheckStatus.ReadyOnThisPhone ->
                R.string.plan_b_status_ready
            status == OfflineArrivalCheckStatus.NeedsAttention ->
                R.string.plan_b_status_attention
            else -> R.string.plan_b_status_not_checked
        },
    )
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = shape,
        color = when {
            stale -> LandingKitColors.Sun.copy(alpha = .18f)
            status == OfflineArrivalCheckStatus.ReadyOnThisPhone ->
                LandingKitColors.Seafoam.copy(alpha = .32f)
            status == OfflineArrivalCheckStatus.NeedsAttention ->
                LandingKitColors.Coral.copy(alpha = .18f)
            else -> MaterialTheme.colorScheme.surface
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
                    state is PlanBCheckUiState.Complete && state.tripId == tripId ->
                        stringResource(R.string.plan_b_checked_now)
                    checkedAtMillis != null && !stale &&
                        status != OfflineArrivalCheckStatus.NotChecked ->
                        stringResource(
                            R.string.plan_b_checked_at,
                            DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT)
                                .format(Date(checkedAtMillis)),
                        )
                    else -> stringResource(R.string.plan_b_intro)
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun RuleRow(
    label: String,
    passed: Boolean,
    actionDescription: String? = null,
    onClick: (() -> Unit)? = null,
) {
    val modifier = if (onClick != null && actionDescription != null) {
        Modifier
            .fillMaxWidth()
            .heightIn(min = 48.dp)
            .clickable(role = Role.Button, onClick = onClick)
            .semantics(mergeDescendants = true) {
                contentDescription = actionDescription
            }
    } else {
        Modifier.fillMaxWidth()
    }
    Row(
        modifier = modifier,
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
        if (onClick != null) {
            Surface(
                modifier = Modifier.size(40.dp),
                shape = CircleShape,
                color = LandingKitColors.Sun.copy(alpha = .18f),
                contentColor = MaterialTheme.colorScheme.onSurface,
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = stringResource(R.string.offline_check_evidence_action_symbol),
                        style = MaterialTheme.typography.titleMedium,
                    )
                }
            }
        }
    }
}
