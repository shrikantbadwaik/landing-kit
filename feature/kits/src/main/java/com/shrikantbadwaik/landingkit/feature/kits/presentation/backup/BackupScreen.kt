package com.shrikantbadwaik.landingkit.feature.kits.presentation.backup

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.animation.Crossfade
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.shrikantbadwaik.landingkit.core.data.BackupExportResult
import com.shrikantbadwaik.landingkit.core.data.BackupPreview
import com.shrikantbadwaik.landingkit.core.data.BackupRestoreResult
import com.shrikantbadwaik.landingkit.core.data.BackupUnlockException
import com.shrikantbadwaik.landingkit.core.data.UnsupportedBackupException
import com.shrikantbadwaik.landingkit.core.designsystem.LandingKitColors
import com.shrikantbadwaik.landingkit.feature.kits.R
import com.shrikantbadwaik.landingkit.feature.kits.presentation.component.ScreenHeader
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
internal fun BackupScreen(
    onBack: () -> Unit,
    onExport: (Uri, CharArray, (Result<BackupExportResult>) -> Unit) -> Unit,
    onInspect: (Uri, CharArray, (Result<BackupPreview>) -> Unit) -> Unit,
    onRestore: (Uri, CharArray, (Result<BackupRestoreResult>) -> Unit) -> Unit,
) {
    var exportPassword by remember { mutableStateOf("") }
    var exportConfirmation by remember { mutableStateOf("") }
    var exportPasswordVisible by remember { mutableStateOf(false) }
    var exportConfirmationVisible by remember { mutableStateOf(false) }
    var showExportWarning by remember { mutableStateOf(false) }
    var selectedBackup by remember { mutableStateOf<Uri?>(null) }
    var restorePassword by remember { mutableStateOf("") }
    var restorePasswordVisible by remember { mutableStateOf(false) }
    var preview by remember { mutableStateOf<BackupPreview?>(null) }
    var operation by remember { mutableStateOf(BackupOperation.Idle) }
    var feedback by remember { mutableStateOf<BackupFeedback?>(null) }

    val exportLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument(BACKUP_MIME_TYPE),
    ) { destination ->
        destination?.let { uri ->
            operation = BackupOperation.Exporting
            feedback = null
            onExport(uri, exportPassword.toCharArray()) { result ->
                operation = BackupOperation.Idle
                feedback = result.fold(
                    onSuccess = { BackupFeedback.Exported(it) },
                    onFailure = { BackupFeedback.ExportFailed },
                )
                if (result.isSuccess) {
                    exportPassword = ""
                    exportConfirmation = ""
                }
            }
        }
    }
    val importLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument(),
    ) { source ->
        selectedBackup = source
        restorePassword = ""
        preview = null
        feedback = null
    }

    if (showExportWarning) {
        AlertDialog(
            onDismissRequest = { showExportWarning = false },
            title = { Text(stringResource(R.string.backup_warning_title)) },
            text = { Text(stringResource(R.string.backup_warning_description)) },
            dismissButton = {
                TextButton(onClick = { showExportWarning = false }) {
                    Text(stringResource(R.string.backup_warning_cancel))
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showExportWarning = false
                        exportLauncher.launch(defaultBackupName())
                    },
                ) {
                    Text(stringResource(R.string.backup_warning_continue))
                }
            },
        )
    }

    Scaffold(containerColor = MaterialTheme.colorScheme.background) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(22.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            item {
                ScreenHeader(
                    onBack = onBack,
                    label = stringResource(R.string.backup_header),
                    badge = stringResource(R.string.backup_badge),
                )
            }
            item {
                Column {
                    Text(
                        stringResource(R.string.backup_title),
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onBackground,
                    )
                    Spacer(Modifier.height(7.dp))
                    Text(
                        stringResource(R.string.backup_intro),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            item {
                BackupPanel(
                    label = stringResource(R.string.backup_export_label),
                    title = stringResource(R.string.backup_export_title),
                    description = stringResource(R.string.backup_export_description),
                    accent = LandingKitColors.Seafoam,
                ) {
                    PasswordField(
                        value = exportPassword,
                        onValueChange = {
                            exportPassword = it
                            feedback = null
                        },
                        label = stringResource(R.string.backup_password_label),
                        visible = exportPasswordVisible,
                        onVisibilityChange = { exportPasswordVisible = !exportPasswordVisible },
                    )
                    Spacer(Modifier.height(10.dp))
                    PasswordField(
                        value = exportConfirmation,
                        onValueChange = {
                            exportConfirmation = it
                            feedback = null
                        },
                        label = stringResource(R.string.backup_password_confirm_label),
                        visible = exportConfirmationVisible,
                        onVisibilityChange = { exportConfirmationVisible = !exportConfirmationVisible },
                    )
                    Spacer(Modifier.height(8.dp))
                    PasswordGuidance(exportPassword, exportConfirmation)
                    Spacer(Modifier.height(14.dp))
                    Button(
                        onClick = { showExportWarning = true },
                        enabled = operation == BackupOperation.Idle &&
                            exportPassword.length >= MIN_PASSWORD_LENGTH &&
                            exportPassword == exportConfirmation,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        OperationButtonContent(
                            working = operation == BackupOperation.Exporting,
                            label = stringResource(
                                if (operation == BackupOperation.Exporting) {
                                    R.string.backup_exporting
                                } else {
                                    R.string.backup_export_action
                                },
                            ),
                        )
                    }
                    ExportFeedback(feedback)
                }
            }
            item {
                BackupPanel(
                    label = stringResource(R.string.backup_restore_label),
                    title = stringResource(R.string.backup_restore_title),
                    description = stringResource(R.string.backup_restore_description),
                    accent = LandingKitColors.Sun,
                ) {
                    if (selectedBackup == null) {
                        OutlinedButton(
                            onClick = { importLauncher.launch(arrayOf(BACKUP_MIME_TYPE, BINARY_MIME_TYPE)) },
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Text(stringResource(R.string.backup_choose_file))
                        }
                    } else {
                        SelectedBackupRow(
                            onChange = {
                                importLauncher.launch(arrayOf(BACKUP_MIME_TYPE, BINARY_MIME_TYPE))
                            },
                        )
                        Spacer(Modifier.height(12.dp))
                        PasswordField(
                            value = restorePassword,
                            onValueChange = {
                                restorePassword = it
                                preview = null
                                feedback = null
                            },
                            label = stringResource(R.string.backup_restore_password_label),
                            visible = restorePasswordVisible,
                            onVisibilityChange = { restorePasswordVisible = !restorePasswordVisible },
                        )
                        Spacer(Modifier.height(14.dp))
                        if (preview == null) {
                            OutlinedButton(
                                onClick = {
                                    operation = BackupOperation.Reviewing
                                    feedback = null
                                    onInspect(
                                        checkNotNull(selectedBackup),
                                        restorePassword.toCharArray(),
                                    ) { result ->
                                        operation = BackupOperation.Idle
                                        preview = result.getOrNull()
                                        feedback = result.exceptionOrNull()?.toReviewFeedback()
                                    }
                                },
                                enabled = operation == BackupOperation.Idle &&
                                    restorePassword.length >= MIN_PASSWORD_LENGTH,
                                modifier = Modifier.fillMaxWidth(),
                            ) {
                                OperationButtonContent(
                                    working = operation == BackupOperation.Reviewing,
                                    label = stringResource(
                                        if (operation == BackupOperation.Reviewing) {
                                            R.string.backup_reviewing
                                        } else {
                                            R.string.backup_review_action
                                        },
                                    ),
                                )
                            }
                        }
                        ReviewFeedback(feedback)
                        preview?.let { backupPreview ->
                            Spacer(Modifier.height(12.dp))
                            RestorePreview(backupPreview)
                            if (backupPreview.hasNewContent) {
                                Spacer(Modifier.height(14.dp))
                                Button(
                                    onClick = {
                                        operation = BackupOperation.Restoring
                                        feedback = null
                                        onRestore(
                                            checkNotNull(selectedBackup),
                                            restorePassword.toCharArray(),
                                        ) { result ->
                                            operation = BackupOperation.Idle
                                            feedback = result.fold(
                                                onSuccess = { BackupFeedback.Restored(it) },
                                                onFailure = { BackupFeedback.RestoreFailed },
                                            )
                                            if (result.isSuccess) {
                                                preview = null
                                                selectedBackup = null
                                                restorePassword = ""
                                            }
                                        }
                                    },
                                    enabled = operation == BackupOperation.Idle,
                                    modifier = Modifier.fillMaxWidth(),
                                ) {
                                    OperationButtonContent(
                                        working = operation == BackupOperation.Restoring,
                                        label = stringResource(
                                            if (operation == BackupOperation.Restoring) {
                                                R.string.backup_restoring
                                            } else {
                                                R.string.backup_restore_action
                                            },
                                        ),
                                    )
                                }
                            }
                        }
                        RestoreFeedback(feedback)
                    }
                }
            }
            item {
                Text(
                    stringResource(R.string.backup_private_footer),
                    modifier = Modifier.fillMaxWidth(),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
internal fun BackupEntryCard(onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(13.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .background(LandingKitColors.Seafoam, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    painterResource(R.drawable.ic_backup_restore),
                    contentDescription = null,
                    tint = LandingKitColors.Ink,
                )
            }
            Column(Modifier.weight(1f)) {
                Text(
                    stringResource(R.string.backup_entry_title),
                    style = MaterialTheme.typography.titleSmall,
                )
                Text(
                    stringResource(R.string.backup_entry_description),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Text("›", style = MaterialTheme.typography.titleLarge)
        }
    }
}

@Composable
private fun BackupPanel(
    label: String,
    title: String,
    description: String,
    accent: Color,
    content: @Composable () -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(26.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    ) {
        Column(Modifier.padding(18.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(9.dp).background(accent, CircleShape))
                Spacer(Modifier.size(8.dp))
                Text(label, style = MaterialTheme.typography.labelSmall, color = LandingKitColors.Forest)
            }
            Spacer(Modifier.height(9.dp))
            Text(title, style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(4.dp))
            Text(
                description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(16.dp))
            content()
        }
    }
}

@Composable
private fun PasswordField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    visible: Boolean,
    onVisibilityChange: () -> Unit,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth(),
        label = { Text(label) },
        singleLine = true,
        visualTransformation = if (visible) VisualTransformation.None else PasswordVisualTransformation(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
        trailingIcon = {
            IconButton(onClick = onVisibilityChange) {
                Crossfade(targetState = visible, label = "password visibility") { isVisible ->
                    Icon(
                        painter = painterResource(
                            if (isVisible) R.drawable.ic_visibility_off else R.drawable.ic_visibility,
                        ),
                        contentDescription = stringResource(
                            if (isVisible) {
                                R.string.backup_password_hide_content_description
                            } else {
                                R.string.backup_password_show_content_description
                            },
                        ),
                    )
                }
            }
        },
    )
}

@Composable
private fun PasswordGuidance(password: String, confirmation: String) {
    val message = when {
        password.isNotEmpty() && password.length < MIN_PASSWORD_LENGTH ->
            stringResource(R.string.backup_password_too_short)
        confirmation.isNotEmpty() && password != confirmation ->
            stringResource(R.string.backup_password_mismatch)
        else -> stringResource(R.string.backup_password_support)
    }
    Text(message, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
}

@Composable
private fun SelectedBackupRow(onChange: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = LandingKitColors.Seafoam.copy(alpha = .25f),
        shape = RoundedCornerShape(16.dp),
    ) {
        Row(
            modifier = Modifier.padding(start = 14.dp, end = 6.dp, top = 6.dp, bottom = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(stringResource(R.string.backup_file_selected), Modifier.weight(1f), style = MaterialTheme.typography.labelLarge)
            TextButton(onClick = onChange) { Text(stringResource(R.string.backup_file_change)) }
        }
    }
}

@Composable
private fun RestorePreview(preview: BackupPreview) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = LandingKitColors.Seafoam.copy(alpha = .22f),
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(stringResource(R.string.backup_preview_label), style = MaterialTheme.typography.labelSmall, color = LandingKitColors.Forest)
            Spacer(Modifier.height(5.dp))
            Text(stringResource(R.string.backup_preview_title), style = MaterialTheme.typography.titleSmall)
            Spacer(Modifier.height(10.dp))
            if (preview.hasNewContent) {
                Text(stringResource(R.string.backup_preview_arrivals, preview.newTripCount), style = MaterialTheme.typography.bodyMedium)
                Text(stringResource(R.string.backup_preview_templates, preview.newTemplateCount), style = MaterialTheme.typography.bodyMedium)
                Text(stringResource(R.string.backup_preview_attachments, preview.restorableAttachmentCount), style = MaterialTheme.typography.bodyMedium)
            } else {
                Text(stringResource(R.string.backup_preview_nothing_new), style = MaterialTheme.typography.bodyMedium)
            }
            if (preview.existingTripCount > 0 || preview.existingTemplateCount > 0) {
                Spacer(Modifier.height(8.dp))
                Text(
                    stringResource(
                        R.string.backup_preview_existing,
                        preview.existingTripCount,
                        preview.existingTemplateCount,
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun OperationButtonContent(working: Boolean, label: String) {
    if (working) {
        CircularProgressIndicator(
            modifier = Modifier.size(18.dp),
            strokeWidth = 2.dp,
            color = MaterialTheme.colorScheme.onPrimary,
        )
        Spacer(Modifier.size(9.dp))
    }
    Text(label)
}

@Composable
private fun ExportFeedback(feedback: BackupFeedback?) {
    when (feedback) {
        is BackupFeedback.Exported -> {
            Spacer(Modifier.height(10.dp))
            Text(
                text = androidx.compose.ui.platform.LocalContext.current.resources.getQuantityString(
                    R.plurals.backup_export_complete,
                    feedback.result.tripCount,
                    feedback.result.tripCount,
                    feedback.result.attachmentCount,
                ),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary,
            )
        }
        BackupFeedback.ExportFailed -> FeedbackText(stringResource(R.string.backup_export_failed))
        else -> Unit
    }
}

@Composable
private fun ReviewFeedback(feedback: BackupFeedback?) {
    when (feedback) {
        BackupFeedback.UnlockFailed -> FeedbackText(stringResource(R.string.backup_unlock_failed))
        BackupFeedback.Unsupported -> FeedbackText(stringResource(R.string.backup_unsupported))
        BackupFeedback.ReviewFailed -> FeedbackText(stringResource(R.string.backup_review_failed))
        else -> Unit
    }
}

@Composable
private fun RestoreFeedback(feedback: BackupFeedback?) {
    when (feedback) {
        is BackupFeedback.Restored -> {
            Spacer(Modifier.height(10.dp))
            Text(
                stringResource(
                    R.string.backup_restore_complete,
                    feedback.result.restoredTripIds.size,
                    feedback.result.restoredTemplateCount,
                    feedback.result.restoredAttachmentCount,
                ),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary,
            )
        }
        BackupFeedback.RestoreFailed -> FeedbackText(stringResource(R.string.backup_restore_failed))
        else -> Unit
    }
}

@Composable
private fun FeedbackText(message: String) {
    Spacer(Modifier.height(10.dp))
    Text(message, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
}

private fun Throwable.toReviewFeedback(): BackupFeedback = when (this) {
    is BackupUnlockException -> BackupFeedback.UnlockFailed
    is UnsupportedBackupException -> BackupFeedback.Unsupported
    else -> BackupFeedback.ReviewFailed
}

private fun defaultBackupName(): String =
    "landing-kit-backup-${LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)}.lkb"

private enum class BackupOperation { Idle, Exporting, Reviewing, Restoring }

private sealed interface BackupFeedback {
    data class Exported(val result: BackupExportResult) : BackupFeedback
    data object ExportFailed : BackupFeedback
    data object UnlockFailed : BackupFeedback
    data object Unsupported : BackupFeedback
    data object ReviewFailed : BackupFeedback
    data class Restored(val result: BackupRestoreResult) : BackupFeedback
    data object RestoreFailed : BackupFeedback
}

private const val MIN_PASSWORD_LENGTH = 12
private const val BACKUP_MIME_TYPE = "application/vnd.landing-kit.backup"
private const val BINARY_MIME_TYPE = "application/octet-stream"
