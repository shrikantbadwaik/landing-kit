package com.shrikantbadwaik.landingkit.feature.kits.presentation.editor.component

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import com.shrikantbadwaik.landingkit.core.designsystem.LandingKitColors
import com.shrikantbadwaik.landingkit.core.model.ArrivalItem
import com.shrikantbadwaik.landingkit.core.model.ChecklistTemplate
import com.shrikantbadwaik.landingkit.feature.kits.R

@Composable
internal fun ChecklistTemplatePanel(
    curatedTemplates: List<ChecklistTemplate>,
    personalTemplates: List<ChecklistTemplate>,
    currentChecklist: List<ArrivalItem>,
    feedback: String?,
    onApply: (ChecklistTemplate) -> Unit,
    onSave: (name: String) -> Unit,
    onDelete: (ChecklistTemplate) -> Unit,
) {
    var showSaveDialog by remember { mutableStateOf(false) }
    var pendingDelete by remember { mutableStateOf<ChecklistTemplate?>(null) }
    val hasAnyTasks = currentChecklist.isNotEmpty()
    val hasTasksToSave = hasAnyTasks && currentChecklist.all { it.title.isNotBlank() }

    if (showSaveDialog) {
        SaveTemplateDialog(
            onDismiss = { showSaveDialog = false },
            onSave = { name ->
                onSave(name)
                showSaveDialog = false
            },
        )
    }
    pendingDelete?.let { template ->
        DeleteTemplateDialog(
            template = template,
            onDismiss = { pendingDelete = null },
            onDelete = {
                onDelete(template)
                pendingDelete = null
            },
        )
    }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.template_section_title),
                    style = MaterialTheme.typography.titleSmall,
                )
                Spacer(Modifier.height(3.dp))
                Text(
                    text = stringResource(R.string.template_section_description),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Spacer(Modifier.width(12.dp))
            OutlinedButton(
                onClick = { showSaveDialog = true },
                enabled = hasTasksToSave,
                shape = RoundedCornerShape(14.dp),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(
                    horizontal = 12.dp,
                    vertical = 8.dp,
                ),
            ) {
                Text(stringResource(R.string.template_save_current))
            }
        }

        if (!hasTasksToSave) {
            Text(
                text = stringResource(
                    if (hasAnyTasks) {
                        R.string.template_invalid_checklist
                    } else {
                        R.string.template_empty_checklist
                    },
                ),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        AnimatedContent(
            targetState = feedback,
            transitionSpec = {
                val enter = fadeIn(tween(durationMillis = FEEDBACK_ENTER_DURATION_MILLIS)) +
                    slideInVertically(
                        animationSpec = tween(durationMillis = FEEDBACK_ENTER_DURATION_MILLIS),
                        initialOffsetY = { height -> -height / 3 },
                    )
                val exit = fadeOut(tween(durationMillis = FEEDBACK_EXIT_DURATION_MILLIS)) +
                    slideOutVertically(
                        animationSpec = tween(durationMillis = FEEDBACK_EXIT_DURATION_MILLIS),
                        targetOffsetY = { height -> -height / 4 },
                    )
                (enter togetherWith exit).using(SizeTransform(clip = false))
            },
            label = "templateFeedback",
        ) { message ->
            if (message != null) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    color = LandingKitColors.Seafoam.copy(alpha = .3f),
                ) {
                    Text(
                        text = message,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
            } else {
                Spacer(Modifier.fillMaxWidth())
            }
        }

        LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            items(personalTemplates, key = { it.id }) { template ->
                ChecklistTemplateCard(
                    template = template,
                    isPersonal = true,
                    onApply = { onApply(template) },
                    onDelete = { pendingDelete = template },
                )
            }
            items(curatedTemplates, key = { it.id }) { template ->
                ChecklistTemplateCard(
                    template = template,
                    isPersonal = false,
                    onApply = { onApply(template) },
                )
            }
        }
    }
}

private const val FEEDBACK_ENTER_DURATION_MILLIS = 240
private const val FEEDBACK_EXIT_DURATION_MILLIS = 180

@Composable
private fun ChecklistTemplateCard(
    template: ChecklistTemplate,
    isPersonal: Boolean,
    onApply: () -> Unit,
    onDelete: (() -> Unit)? = null,
) {
    val accent = if (isPersonal) LandingKitColors.Lavender else LandingKitColors.Sky

    Card(
        modifier = Modifier.width(252.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = accent.copy(alpha = .22f),
        ),
        border = BorderStroke(1.dp, accent.copy(alpha = .75f)),
    ) {
        Column(
            modifier = Modifier.padding(15.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = accent.copy(alpha = .7f),
                ) {
                    Text(
                        text = stringResource(
                            if (isPersonal) {
                                R.string.template_personal_label
                            } else {
                                R.string.template_curated_label
                            },
                        ),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                    )
                }
                Spacer(Modifier.weight(1f))
                if (onDelete != null) {
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(36.dp),
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_delete_kit),
                            contentDescription = stringResource(
                                R.string.template_delete_description,
                                template.name,
                            ),
                            modifier = Modifier.size(17.dp),
                            tint = MaterialTheme.colorScheme.error,
                        )
                    }
                }
            }
            Text(template.name, style = MaterialTheme.typography.titleMedium)
            Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                template.items.take(2).forEach { item ->
                    Row(verticalAlignment = Alignment.Top) {
                        Box(
                            modifier = Modifier
                                .padding(top = 7.dp)
                                .size(5.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Surface(
                                modifier = Modifier.size(5.dp),
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.primary,
                            ) {}
                        }
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = item.title,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
                val remainingCount = template.items.size - 2
                if (remainingCount > 0) {
                    Text(
                        text = pluralStringResource(
                            R.plurals.template_more_tasks,
                            remainingCount,
                            remainingCount,
                        ),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.secondary,
                    )
                }
            }
            Button(
                onClick = onApply,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(13.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                ),
            ) {
                Text(stringResource(R.string.template_add_missing))
            }
        }
    }
}

@Composable
private fun SaveTemplateDialog(
    onDismiss: () -> Unit,
    onSave: (String) -> Unit,
) {
    var name by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text(
                    text = stringResource(R.string.template_save_dialog_label),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.secondary,
                )
                Spacer(Modifier.height(7.dp))
                Text(stringResource(R.string.template_save_dialog_title))
            }
        },
        text = {
            Column {
                Text(
                    text = stringResource(R.string.template_save_dialog_description),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(16.dp))
                TextField(
                    value = name,
                    onValueChange = { name = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(stringResource(R.string.template_name_label)) },
                    placeholder = { Text(stringResource(R.string.template_name_placeholder)) },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Sentences,
                    ),
                    colors = TextFieldDefaults.colors(
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                    ),
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onSave(name.trim()) },
                enabled = name.isNotBlank(),
            ) {
                Text(stringResource(R.string.template_save_action))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.template_cancel))
            }
        },
        shape = RoundedCornerShape(26.dp),
        containerColor = MaterialTheme.colorScheme.surface,
    )
}

@Composable
private fun DeleteTemplateDialog(
    template: ChecklistTemplate,
    onDismiss: () -> Unit,
    onDelete: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.template_delete_dialog_title)) },
        text = {
            Text(
                text = stringResource(
                    R.string.template_delete_dialog_description,
                    template.name,
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        },
        confirmButton = {
            TextButton(
                onClick = onDelete,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.error,
                ),
            ) {
                Text(stringResource(R.string.template_delete_action))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.template_keep_action))
            }
        },
        shape = RoundedCornerShape(26.dp),
        containerColor = MaterialTheme.colorScheme.surface,
    )
}
