package com.shrikantbadwaik.landingkit.feature.kits.presentation.editor.component

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import com.shrikantbadwaik.landingkit.core.designsystem.LandingKitColors
import com.shrikantbadwaik.landingkit.core.model.ArrivalItem
import com.shrikantbadwaik.landingkit.feature.kits.R

@Composable
internal fun ChecklistEditor(
    items: List<ArrivalItem>,
    onTitleChange: (itemId: String, title: String) -> Unit,
    onCheckedChange: (itemId: String, checked: Boolean) -> Unit,
    onMoveUp: (itemId: String) -> Unit,
    onMoveDown: (itemId: String) -> Unit,
    onRemove: (itemId: String) -> Unit,
    onAdd: (title: String) -> Unit,
) {
    var draftTitle by rememberSaveable { mutableStateOf("") }
    val focusManager = LocalFocusManager.current

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(R.string.editor_checklist_description),
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.width(12.dp))
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            ) {
                Text(
                    text = pluralStringResource(
                        R.plurals.editor_checklist_task_count,
                        items.size,
                        items.size,
                    ),
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    style = MaterialTheme.typography.labelMedium,
                )
            }
        }

        if (items.isEmpty()) {
            ChecklistEmptyState()
        } else {
            Column(
                modifier = Modifier.animateContentSize(),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                items.forEachIndexed { index, item ->
                    androidx.compose.runtime.key(item.id) {
                        ChecklistEditorItem(
                            item = item,
                            canMoveUp = index > 0,
                            canMoveDown = index < items.lastIndex,
                            onTitleChange = { onTitleChange(item.id, it) },
                            onCheckedChange = { onCheckedChange(item.id, it) },
                            onMoveUp = { onMoveUp(item.id) },
                            onMoveDown = { onMoveDown(item.id) },
                            onRemove = { onRemove(item.id) },
                        )
                    }
                }
            }
        }

        AddChecklistItem(
            value = draftTitle,
            onValueChange = { draftTitle = it },
            onAdd = {
                addDraftTask(draftTitle, focusManager, onAdd) {
                    draftTitle = ""
                }
            },
        )
    }
}

@Composable
private fun ChecklistEditorItem(
    item: ArrivalItem,
    canMoveUp: Boolean,
    canMoveDown: Boolean,
    onTitleChange: (String) -> Unit,
    onCheckedChange: (Boolean) -> Unit,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit,
    onRemove: () -> Unit,
) {
    val moveUpDescription = stringResource(R.string.editor_checklist_move_up, item.title)
    val moveDownDescription = stringResource(R.string.editor_checklist_move_down, item.title)
    val toggleDescription = stringResource(
        if (item.isComplete) {
            R.string.editor_checklist_mark_incomplete
        } else {
            R.string.editor_checklist_mark_complete
        },
        item.title,
    )
    val removeDescription = stringResource(R.string.editor_checklist_remove, item.title)
    val itemDescription = stringResource(R.string.editor_checklist_item_label)

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = if (item.isComplete) {
            LandingKitColors.Seafoam.copy(alpha = .22f)
        } else {
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = .42f)
        },
        contentColor = MaterialTheme.colorScheme.onSurface,
        border = BorderStroke(
            1.dp,
            if (item.title.isBlank()) {
                MaterialTheme.colorScheme.error.copy(alpha = .65f)
            } else {
                MaterialTheme.colorScheme.outlineVariant.copy(alpha = .65f)
            },
        ),
    ) {
        Row(
            modifier = Modifier.padding(start = 4.dp, top = 6.dp, end = 6.dp, bottom = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                ReorderButton(
                    symbol = stringResource(R.string.symbol_move_up),
                    contentDescription = moveUpDescription,
                    enabled = canMoveUp,
                    onClick = onMoveUp,
                )
                ReorderButton(
                    symbol = stringResource(R.string.symbol_move_down),
                    contentDescription = moveDownDescription,
                    enabled = canMoveDown,
                    onClick = onMoveDown,
                )
            }
            Checkbox(
                checked = item.isComplete,
                onCheckedChange = onCheckedChange,
                modifier = Modifier.semantics { contentDescription = toggleDescription },
            )
            TextField(
                value = item.title,
                onValueChange = onTitleChange,
                modifier = Modifier
                    .weight(1f)
                    .semantics { contentDescription = itemDescription },
                textStyle = MaterialTheme.typography.bodyMedium,
                singleLine = false,
                minLines = 1,
                maxLines = 2,
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Sentences,
                    imeAction = ImeAction.Done,
                ),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                ),
            )
            IconButton(
                onClick = onRemove,
                modifier = Modifier.size(42.dp),
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_delete_kit),
                    contentDescription = removeDescription,
                    modifier = Modifier.size(19.dp),
                    tint = MaterialTheme.colorScheme.error,
                )
            }
        }
    }
}

@Composable
private fun ReorderButton(
    symbol: String,
    contentDescription: String,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    IconButton(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier
            .size(34.dp)
            .semantics { this.contentDescription = contentDescription },
    ) {
        Text(
            text = symbol,
            style = MaterialTheme.typography.titleSmall,
            color = if (enabled) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.onSurface.copy(alpha = .24f)
            },
        )
    }
}

@Composable
private fun AddChecklistItem(
    value: String,
    onValueChange: (String) -> Unit,
    onAdd: () -> Unit,
) {
    Column {
        TextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text(stringResource(R.string.editor_checklist_add_placeholder)) },
            singleLine = true,
            shape = RoundedCornerShape(16.dp),
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Sentences,
                imeAction = ImeAction.Done,
            ),
            keyboardActions = KeyboardActions(onDone = { if (value.isNotBlank()) onAdd() }),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = .58f),
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = .58f),
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
            ),
        )
        Spacer(Modifier.height(8.dp))
        Button(
            onClick = onAdd,
            enabled = value.isNotBlank(),
            modifier = Modifier.align(Alignment.End),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = LandingKitColors.Sun,
                contentColor = LandingKitColors.Ink,
            ),
        ) {
            Text(stringResource(R.string.editor_checklist_add_action))
        }
    }
}

@Composable
private fun ChecklistEmptyState() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = LandingKitColors.Sky.copy(alpha = .25f),
        contentColor = MaterialTheme.colorScheme.onSurface,
        border = BorderStroke(1.dp, LandingKitColors.Sky.copy(alpha = .6f)),
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(
                text = stringResource(R.string.editor_checklist_empty_title),
                style = MaterialTheme.typography.titleSmall,
            )
            Spacer(Modifier.height(3.dp))
            Text(
                text = stringResource(R.string.editor_checklist_empty_description),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

private fun addDraftTask(
    title: String,
    focusManager: FocusManager,
    onAdd: (String) -> Unit,
    onAdded: () -> Unit,
) {
    if (title.isBlank()) return
    onAdd(title)
    onAdded()
    focusManager.clearFocus()
}
