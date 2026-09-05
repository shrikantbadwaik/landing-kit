package com.shrikantbadwaik.landingkit.feature.kits.presentation.detail.component

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.shrikantbadwaik.landingkit.core.designsystem.LandingKitColors
import com.shrikantbadwaik.landingkit.core.model.ArrivalItem
import com.shrikantbadwaik.landingkit.core.model.OfflineArrivalCheckStatus
import com.shrikantbadwaik.landingkit.feature.kits.R
import com.shrikantbadwaik.landingkit.feature.kits.presentation.component.journalDoodles
import com.shrikantbadwaik.landingkit.feature.kits.presentation.component.JournalArtwork
import com.shrikantbadwaik.landingkit.feature.kits.presentation.component.JournalScene

@Composable
internal fun EditKitAction(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.96f else 1f,
        animationSpec = tween(140),
        label = "Edit action scale",
    )
    val iconRotation by animateFloatAsState(
        targetValue = if (isPressed) -6f else 0f,
        animationSpec = tween(140),
        label = "Edit icon rotation",
    )

    Surface(
        onClick = onClick,
        modifier = modifier.graphicsLayer {
            scaleX = scale
            scaleY = scale
        },
        interactionSource = interactionSource,
        shape = MaterialTheme.shapes.small,
        color = MaterialTheme.colorScheme.primaryContainer,
        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_edit_kit),
            contentDescription = stringResource(R.string.kit_edit),
            modifier = Modifier
                .padding(12.dp)
                .size(18.dp)
                .graphicsLayer { rotationZ = iconRotation },
        )
    }
}

@Composable
internal fun ArrivalModeAction(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.97f else 1f,
        animationSpec = tween(140),
        label = "Arrival mode action scale",
    )

    Surface(
        onClick = onClick,
        modifier = modifier.graphicsLayer {
            scaleX = scale
            scaleY = scale
        },
        interactionSource = interactionSource,
        shape = MaterialTheme.shapes.medium,
        color = LandingKitColors.Coral,
        contentColor = LandingKitColors.Ink,
        shadowElevation = 1.dp,
    ) {
        Row(
            modifier = Modifier.padding(start = 20.dp, end = 8.dp, top = 9.dp, bottom = 9.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(R.string.arrival_mode_action),
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.titleMedium,
            )
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(LandingKitColors.Paper, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_journal_forward),
                    contentDescription = null,
                    modifier = Modifier.size(22.dp),
                    tint = LandingKitColors.Ink,
                )
            }
        }
    }
}

@Composable
internal fun ArrivalReminderCard(
    enabled: Boolean,
    arrivalDate: String,
    permissionDenied: Boolean,
    changeFailed: Boolean,
    onEnabledChange: (Boolean) -> Unit,
) {
    val backgroundColor by animateColorAsState(
        targetValue = if (enabled) {
            LandingKitColors.Seafoam.copy(alpha = .28f)
        } else {
            LandingKitColors.Sun.copy(alpha = .25f)
        },
        animationSpec = tween(240),
        label = "Arrival reminder background",
    )
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        color = backgroundColor,
        contentColor = MaterialTheme.colorScheme.onSurface,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().toggleable(
                    value = enabled,
                    role = Role.Switch,
                    onValueChange = onEnabledChange,
                )
                .padding(horizontal = 14.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(
                        if (enabled) MaterialTheme.colorScheme.primary else LandingKitColors.Sun,
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_notification_arrival),
                    contentDescription = null,
                    modifier = Modifier.size(19.dp),
                    tint = if (enabled) {
                        MaterialTheme.colorScheme.onPrimary
                    } else {
                        LandingKitColors.Forest
                    },
                )
            }
            Column(Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.arrival_reminder_title),
                    style = MaterialTheme.typography.titleSmall,
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = when {
                        changeFailed -> stringResource(R.string.arrival_reminder_failed)
                        permissionDenied -> stringResource(
                            R.string.arrival_reminder_permission_denied,
                        )
                        enabled -> stringResource(
                            R.string.arrival_reminder_description_on,
                            arrivalDate,
                        )
                        else -> stringResource(
                            R.string.arrival_reminder_description_off,
                            arrivalDate,
                        )
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = if (changeFailed || permissionDenied) {
                        MaterialTheme.colorScheme.error
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                )
            }
            Switch(
                checked = enabled,
                onCheckedChange = null,
            )
        }
    }
}

@Composable
internal fun OfflineCheckStatusCard(
    status: OfflineArrivalCheckStatus,
    onClick: () -> Unit,
) {
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        color = when (status) {
            OfflineArrivalCheckStatus.ReadyOnThisPhone -> LandingKitColors.Seafoam
            OfflineArrivalCheckStatus.NeedsAttention -> LandingKitColors.Coral
            OfflineArrivalCheckStatus.NotChecked -> LandingKitColors.Lavender
        },
        contentColor = LandingKitColors.Ink,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 13.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(
                        when (status) {
                            OfflineArrivalCheckStatus.ReadyOnThisPhone -> LandingKitColors.Forest
                            OfflineArrivalCheckStatus.NeedsAttention -> LandingKitColors.Ink
                            OfflineArrivalCheckStatus.NotChecked -> LandingKitColors.Forest
                        },
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Text("✓", style = MaterialTheme.typography.titleMedium, color = Color.White)
            }
            Column(Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.offline_check_card_title),
                    style = MaterialTheme.typography.titleSmall,
                )
                Text(
                    text = stringResource(
                        when (status) {
                            OfflineArrivalCheckStatus.ReadyOnThisPhone -> R.string.offline_check_card_ready
                            OfflineArrivalCheckStatus.NeedsAttention -> R.string.offline_check_card_attention
                            OfflineArrivalCheckStatus.NotChecked -> R.string.offline_check_card_not_checked
                        },
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = LandingKitColors.Ink.copy(alpha = .65f),
                )
            }
            Text(
                text = stringResource(R.string.offline_check_card_action),
                style = MaterialTheme.typography.labelLarge,
                color = LandingKitColors.Ink,
            )
        }
    }
}

@Composable
internal fun TravelToolsCard(
    onPrintableSummary: () -> Unit,
    onShareSummary: () -> Unit,
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(R.string.detail_travel_tools),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.weight(1f),
            )
            JournalArtwork(JournalScene.Doodles, Modifier.size(width = 56.dp, height = 42.dp), monochrome = true)
        }
        Spacer(Modifier.height(8.dp))
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.medium,
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        ) {
            Column {
                TravelToolRow(
                    icon = R.drawable.ic_print_summary,
                    title = stringResource(R.string.print_summary_entry_title),
                    detail = stringResource(R.string.print_summary_entry_description),
                    accent = LandingKitColors.Coral,
                    onClick = onPrintableSummary,
                )
                Surface(
                    modifier = Modifier.padding(start = 64.dp).fillMaxWidth().height(1.dp),
                    color = MaterialTheme.colorScheme.outlineVariant,
                ) {}
                TravelToolRow(
                    icon = R.drawable.ic_share_summary,
                    title = stringResource(R.string.share_summary_entry_title),
                    detail = stringResource(R.string.share_summary_entry_description),
                    accent = LandingKitColors.Seafoam,
                    onClick = onShareSummary,
                )
            }
        }
    }
}

@Composable
private fun TravelToolRow(
    icon: Int,
    title: String,
    detail: String,
    accent: Color,
    onClick: () -> Unit,
) {
    Surface(onClick = onClick, color = Color.Transparent) {
        Row(
            modifier = Modifier.padding(horizontal = 15.dp, vertical = 15.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier.size(38.dp).background(accent, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    painter = painterResource(icon),
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = LandingKitColors.Ink,
                )
            }
            Column(Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleSmall)
                Spacer(Modifier.height(2.dp))
                Text(
                    detail,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Text("↗", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.secondary)
        }
    }
}

@Composable
internal fun KitDangerZone(onDelete: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        color = LandingKitColors.Coral.copy(alpha = 0.10f),
        contentColor = MaterialTheme.colorScheme.onSurface,
        border = BorderStroke(1.dp, LandingKitColors.Coral.copy(alpha = 0.28f)),
    ) {
        Column(Modifier.padding(18.dp)) {
            Text(
                text = stringResource(R.string.kit_manage_label),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.secondary,
            )
            Spacer(Modifier.height(14.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(LandingKitColors.Coral.copy(alpha = 0.18f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_delete_kit),
                        contentDescription = null,
                        modifier = Modifier.size(21.dp),
                        tint = MaterialTheme.colorScheme.error,
                    )
                }
                Column(Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.kit_remove_title),
                        style = MaterialTheme.typography.titleSmall,
                    )
                    Spacer(Modifier.height(3.dp))
                    Text(
                        text = stringResource(R.string.kit_remove_description),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            TextButton(
                onClick = onDelete,
                modifier = Modifier.align(Alignment.End),
                colors = ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.error,
                ),
            ) {
                Text(stringResource(R.string.kit_delete_action), style = MaterialTheme.typography.labelLarge)
            }
        }
    }
}

@Composable
internal fun DeleteKitDialog(
    destination: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    var hasEntered by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { hasEntered = true }
    val dialogScale by animateFloatAsState(
        targetValue = if (hasEntered) 1f else 0.92f,
        animationSpec = tween(240, easing = FastOutSlowInEasing),
        label = "Delete dialog scale",
    )
    val dialogAlpha by animateFloatAsState(
        targetValue = if (hasEntered) 1f else 0f,
        animationSpec = tween(190),
        label = "Delete dialog alpha",
    )

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth().graphicsLayer {
                scaleX = dialogScale
                scaleY = dialogScale
                alpha = dialogAlpha
            },
            shape = MaterialTheme.shapes.large,
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(LandingKitColors.Coral.copy(alpha = 0.18f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_delete_kit),
                        contentDescription = null,
                        modifier = Modifier.size(29.dp),
                        tint = MaterialTheme.colorScheme.error,
                    )
                }
                Spacer(Modifier.height(20.dp))
                Text(
                    text = stringResource(R.string.kit_delete_dialog_label),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.secondary,
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = stringResource(R.string.kit_delete_dialog_title),
                    style = MaterialTheme.typography.titleLarge,
                )
                Spacer(Modifier.height(10.dp))
                Text(
                    text = stringResource(R.string.kit_delete_dialog_description, destination),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(22.dp))
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp),
                    shape = MaterialTheme.shapes.medium,
                ) {
                    Text(stringResource(R.string.kit_keep_action))
                }
                Spacer(Modifier.height(4.dp))
                TextButton(
                    onClick = onConfirm,
                    modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error,
                    ),
                ) {
                    Text(stringResource(R.string.kit_delete_permanently))
                }
            }
        }
    }
}

@Composable
internal fun AnimatedChecklistItem(item: ArrivalItem, onCheck: (Boolean) -> Unit) {
    val containerColor by animateColorAsState(
        targetValue = if (item.isComplete) {
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = .42f)
        } else {
            MaterialTheme.colorScheme.surface
        },
        animationSpec = tween(360, easing = FastOutSlowInEasing),
        label = "Checklist completion color",
    )
    val textColor by animateColorAsState(
        targetValue = if (item.isComplete) {
            MaterialTheme.colorScheme.onSurfaceVariant
        } else {
            MaterialTheme.colorScheme.onSurface
        },
        animationSpec = tween(300),
        label = "Checklist text color",
    )

    Surface(
        color = containerColor,
        shape = MaterialTheme.shapes.medium,
        modifier = Modifier.animateContentSize(tween(320, easing = FastOutSlowInEasing)),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .toggleable(
                    value = item.isComplete,
                    role = Role.Checkbox,
                    onValueChange = onCheck,
                )
                .padding(horizontal = 8.dp, vertical = 6.dp),
        ) {
            Checkbox(
                checked = item.isComplete,
                onCheckedChange = null,
                modifier = Modifier.size(48.dp),
            )
            Column(Modifier.padding(vertical = 7.dp)) {
                Text(item.title, style = MaterialTheme.typography.titleSmall, color = textColor)
                if (item.detail.isNotBlank()) {
                    Text(
                        item.detail,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

@Composable
internal fun EmptyChecklistCard() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        color = LandingKitColors.Sky.copy(alpha = .24f),
        border = BorderStroke(1.dp, LandingKitColors.Sky.copy(alpha = .58f)),
    ) {
        Text(
            text = stringResource(R.string.detail_checklist_empty),
            modifier = Modifier.padding(18.dp),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
internal fun PastArrivalNotice() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        color = LandingKitColors.Lavender.copy(alpha = .42f),
        contentColor = MaterialTheme.colorScheme.onSurface,
        border = BorderStroke(1.dp, LandingKitColors.Lavender.copy(alpha = .82f)),
    ) {
        Column(Modifier.padding(18.dp)) {
            Text(
                text = stringResource(R.string.detail_past_notice_label),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.secondary,
            )
            Spacer(Modifier.height(5.dp))
            Text(
                text = stringResource(R.string.detail_past_notice_title),
                style = MaterialTheme.typography.titleSmall,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = stringResource(R.string.detail_past_notice_description),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
internal fun DetailCard(label: String, value: String, accent: Color, doodleSeed: String? = null) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = MaterialTheme.shapes.large,
    ) {
        Row(
            Modifier.journalDoodles(doodleSeed.orEmpty(), count = if (doodleSeed != null) 1 else 0)
                .padding(18.dp),
        ) {
            Box(Modifier.width(5.dp).height(44.dp).clip(CircleShape).background(accent))
            Spacer(Modifier.width(14.dp))
            Column {
                Text(
                    label.uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.secondary,
                )
                Spacer(Modifier.height(6.dp))
                Text(value, style = MaterialTheme.typography.bodyLarge)
            }
        }
    }
}
