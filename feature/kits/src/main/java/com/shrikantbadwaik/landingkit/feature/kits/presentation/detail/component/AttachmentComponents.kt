package com.shrikantbadwaik.landingkit.feature.kits.presentation.detail.component

import android.annotation.SuppressLint
import android.graphics.BitmapFactory
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.border
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.shrikantbadwaik.landingkit.core.designsystem.LandingKitColors
import com.shrikantbadwaik.landingkit.core.model.KitAttachment
import com.shrikantbadwaik.landingkit.feature.kits.R
import kotlinx.coroutines.CancellationException

@Composable
internal fun AttachmentSection(
    attachments: List<KitAttachment>,
    isImporting: Boolean,
    feedback: String?,
    protectionEnabled: Boolean,
    protectionControlEnabled: Boolean,
    showRetentionCleanup: Boolean,
    onLoadThumbnail: suspend (String) -> ByteArray?,
    onImport: () -> Unit,
    onOpen: (KitAttachment) -> Unit,
    onQuickAccess: (KitAttachment) -> Unit,
    onDelete: (KitAttachment) -> Unit,
    onProtectionChange: (Boolean) -> Unit,
    onClearAttachments: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(24.dp),
    ) {
        Column(
            modifier = Modifier
                .padding(18.dp)
                .animateContentSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = stringResource(R.string.attachment_section_label),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.secondary,
            )
            Text(
                text = stringResource(R.string.attachment_section_title),
                style = MaterialTheme.typography.titleMedium,
            )
            Text(
                text = stringResource(R.string.attachment_section_description),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            AttachmentProtectionControl(
                protectionEnabled = protectionEnabled,
                controlEnabled = protectionControlEnabled,
                onProtectionChange = onProtectionChange,
            )
            AnimatedVisibility(
                visible = feedback != null,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically(),
            ) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    color = LandingKitColors.Seafoam.copy(alpha = .28f),
                ) {
                    Text(
                        text = feedback.orEmpty(),
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
            }
            AttachmentGallery(
                attachments = attachments,
                isImporting = isImporting,
                onLoadThumbnail = onLoadThumbnail,
                onImport = onImport,
                onOpen = onOpen,
                onQuickAccess = onQuickAccess,
                onDelete = onDelete,
            )
            Text(
                text = stringResource(R.string.attachment_identity_warning),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            AnimatedVisibility(
                visible = showRetentionCleanup,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically(),
            ) {
                AttachmentRetentionCleanup(onClearAttachments = onClearAttachments)
            }
        }
    }
}

@Composable
private fun AttachmentRetentionCleanup(onClearAttachments: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = LandingKitColors.Coral.copy(alpha = .09f),
        contentColor = MaterialTheme.colorScheme.onSurface,
        border = BorderStroke(1.dp, LandingKitColors.Coral.copy(alpha = .24f)),
    ) {
        Column(Modifier.padding(14.dp)) {
            Text(
                text = stringResource(R.string.attachment_retention_label),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.secondary,
            )
            Text(
                text = stringResource(R.string.attachment_retention_title),
                modifier = Modifier.padding(top = 6.dp),
                style = MaterialTheme.typography.titleSmall,
            )
            Text(
                text = stringResource(R.string.attachment_retention_description),
                modifier = Modifier.padding(top = 3.dp),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            TextButton(
                onClick = onClearAttachments,
                modifier = Modifier.align(Alignment.End),
                colors = ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.error,
                ),
            ) {
                Text(stringResource(R.string.attachment_cleanup_action))
            }
        }
    }
}

@Composable
private fun AttachmentProtectionControl(
    protectionEnabled: Boolean,
    controlEnabled: Boolean,
    onProtectionChange: (Boolean) -> Unit,
) {
    val toggleDescription = stringResource(R.string.attachment_protection_toggle_description)
    val lockColor = if (protectionEnabled) {
        MaterialTheme.colorScheme.onPrimary
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = if (protectionEnabled) {
            LandingKitColors.Seafoam.copy(alpha = .3f)
        } else {
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = .38f)
        },
        contentColor = MaterialTheme.colorScheme.onSurface,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Surface(
                modifier = Modifier.size(38.dp),
                shape = CircleShape,
                color = if (protectionEnabled) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.outlineVariant
                },
                contentColor = lockColor,
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Canvas(Modifier.size(18.dp)) {
                        val strokeWidth = 1.7.dp.toPx()
                        drawRoundRect(
                            color = lockColor,
                            topLeft = Offset(size.width * .16f, size.height * .43f),
                            size = Size(size.width * .68f, size.height * .49f),
                            cornerRadius = CornerRadius(2.dp.toPx()),
                            style = Stroke(strokeWidth),
                        )
                        drawArc(
                            color = lockColor,
                            startAngle = 180f,
                            sweepAngle = 180f,
                            useCenter = false,
                            topLeft = Offset(size.width * .29f, size.height * .08f),
                            size = Size(size.width * .42f, size.height * .58f),
                            style = Stroke(strokeWidth),
                        )
                    }
                }
            }
            Column(Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.attachment_protection_title),
                    style = MaterialTheme.typography.labelLarge,
                )
                Text(
                    text = stringResource(R.string.attachment_protection_description),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Switch(
                checked = protectionEnabled,
                onCheckedChange = onProtectionChange,
                enabled = controlEnabled,
                modifier = Modifier.semantics {
                    contentDescription = toggleDescription
                },
            )
        }
    }
}

@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
private fun AttachmentGallery(
    attachments: List<KitAttachment>,
    isImporting: Boolean,
    onLoadThumbnail: suspend (String) -> ByteArray?,
    onImport: () -> Unit,
    onOpen: (KitAttachment) -> Unit,
    onQuickAccess: (KitAttachment) -> Unit,
    onDelete: (KitAttachment) -> Unit,
) {
    BoxWithConstraints(Modifier.fillMaxWidth()) {
        val spacing = 2.dp
        val deleteButtonOverflow = 12.dp
        val tileSize = ((maxWidth - (spacing * 2) - (deleteButtonOverflow * 3)) / 3)
            .coerceIn(72.dp, 104.dp)
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(spacing),
        ) {
            item(key = "attachment_picker") {
                AttachmentPickerTile(
                    tileSize = tileSize,
                    isImporting = isImporting,
                    onImport = onImport,
                )
            }
            items(
                items = attachments,
                key = KitAttachment::id,
            ) { attachment ->
                AttachmentPreview(
                    attachment = attachment,
                    previewSize = tileSize,
                    onLoadThumbnail = onLoadThumbnail,
                    onOpen = onOpen,
                    onQuickAccess = onQuickAccess,
                    onDelete = onDelete,
                )
            }
        }
    }
}

@Composable
private fun AttachmentPickerTile(
    tileSize: Dp,
    isImporting: Boolean,
    onImport: () -> Unit,
) {
    val shape = RoundedCornerShape(20.dp)
    val outlineColor = if (isImporting) {
        MaterialTheme.colorScheme.secondary
    } else {
        MaterialTheme.colorScheme.primary
    }
    Column(
        modifier = Modifier
            .padding(top = 12.dp, end = 12.dp)
            .size(tileSize)
            .clip(shape)
            .background(LandingKitColors.Sky.copy(alpha = .16f))
            .drawBehind {
                drawRoundRect(
                    color = outlineColor,
                    cornerRadius = CornerRadius(20.dp.toPx()),
                    style = Stroke(
                        width = 1.5.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(
                            floatArrayOf(9.dp.toPx(), 7.dp.toPx()),
                        ),
                    ),
                )
            }
            .clickable(enabled = !isImporting, onClick = onImport)
            .padding(8.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        if (isImporting) {
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                color = MaterialTheme.colorScheme.primary,
                strokeWidth = 2.dp,
            )
        } else {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(MaterialTheme.colorScheme.primary, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "+",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onPrimary,
                )
            }
        }
        Spacer(Modifier.height(6.dp))
        Text(
            text = if (isImporting) {
                stringResource(R.string.attachment_importing)
            } else {
                stringResource(R.string.attachment_picker_title_more)
            },
            style = MaterialTheme.typography.labelSmall,
            color = outlineColor,
            maxLines = 2,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun AttachmentPreview(
    attachment: KitAttachment,
    previewSize: Dp,
    onLoadThumbnail: suspend (String) -> ByteArray?,
    onOpen: (KitAttachment) -> Unit,
    onQuickAccess: (KitAttachment) -> Unit,
    onDelete: (KitAttachment) -> Unit,
) {
    val openDescription = stringResource(R.string.attachment_open_description, attachment.displayName)
    val removeDescription = stringResource(R.string.attachment_remove_description, attachment.displayName)
    val quickAccessDescription = stringResource(
        if (attachment.isQuickAccess) {
            R.string.attachment_quick_access_remove_description
        } else {
            R.string.attachment_quick_access_set_description
        },
        attachment.displayName,
    )
    val thumbnailBytes by produceState<ByteArray?>(
        initialValue = null,
        key1 = attachment.id,
    ) {
        value = try {
            onLoadThumbnail(attachment.id)
        } catch (cancellation: CancellationException) {
            throw cancellation
        } catch (_: Exception) {
            null
        }
    }
    val imageBitmap = remember(thumbnailBytes) {
        thumbnailBytes?.let { bytes ->
            BitmapFactory.decodeByteArray(bytes, 0, bytes.size)?.asImageBitmap()
        }
    }

    Box(Modifier.size(previewSize + 12.dp)) {
        Surface(
            modifier = Modifier
                .size(previewSize)
                .align(Alignment.BottomStart)
                .clip(RoundedCornerShape(20.dp))
                .semantics { contentDescription = openDescription }
                .clickable { onOpen(attachment) },
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = .38f),
            border = BorderStroke(
                width = if (attachment.isQuickAccess) 2.dp else 1.dp,
                color = if (attachment.isQuickAccess) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.outlineVariant
                },
            ),
        ) {
            Crossfade(
                targetState = imageBitmap,
                label = "attachment_thumbnail",
            ) { thumbnail ->
                if (thumbnail != null) {
                    Image(
                        bitmap = thumbnail,
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(20.dp)),
                        contentScale = ContentScale.Crop,
                    )
                } else {
                    FilePreviewPlaceholder(
                        isPdf = attachment.mimeType == PDF_MIME_TYPE,
                    )
                }
            }
        }
        IconButton(
            onClick = { onDelete(attachment) },
            modifier = Modifier
                .align(Alignment.TopEnd)
                .size(48.dp)
                .semantics { contentDescription = removeDescription },
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(LandingKitColors.Coral, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                TrashGlyph()
            }
        }
        IconButton(
            onClick = { onQuickAccess(attachment) },
            modifier = Modifier
                .align(Alignment.BottomStart)
                .size(48.dp)
                .semantics { contentDescription = quickAccessDescription },
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(
                        color = if (attachment.isQuickAccess) {
                            LandingKitColors.Sun
                        } else {
                            MaterialTheme.colorScheme.surface.copy(alpha = .94f)
                        },
                        shape = CircleShape,
                    )
                    .border(
                        width = 1.dp,
                        color = if (attachment.isQuickAccess) {
                            LandingKitColors.Forest.copy(alpha = .18f)
                        } else {
                            MaterialTheme.colorScheme.primary.copy(alpha = .62f)
                        },
                        shape = CircleShape,
                    ),
                contentAlignment = Alignment.Center,
            ) {
                QuickAccessGlyph(
                    selected = attachment.isQuickAccess,
                    color = if (attachment.isQuickAccess) {
                        LandingKitColors.Forest
                    } else {
                        MaterialTheme.colorScheme.primary
                    },
                )
            }
        }
    }
}

@Composable
private fun QuickAccessGlyph(selected: Boolean, color: Color) {
    Canvas(Modifier.size(18.dp)) {
        val stroke = 1.8.dp.toPx()
        val centerX = size.width * .5f
        val headCenter = Offset(centerX, size.height * .34f)
        drawCircle(
            color = color,
            radius = size.width * .19f,
            center = headCenter,
            style = if (selected) androidx.compose.ui.graphics.drawscope.Fill else Stroke(stroke),
        )
        drawLine(
            color = color,
            start = Offset(centerX, size.height * .53f),
            end = Offset(centerX, size.height * .86f),
            strokeWidth = stroke,
        )
        drawLine(
            color = color,
            start = Offset(size.width * .34f, size.height * .86f),
            end = Offset(size.width * .66f, size.height * .86f),
            strokeWidth = stroke,
        )
    }
}

@Composable
private fun FilePreviewPlaceholder(isPdf: Boolean) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(14.dp))
            .background(if (isPdf) LandingKitColors.Sun else LandingKitColors.Seafoam),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = stringResource(
                    if (isPdf) R.string.attachment_type_pdf else R.string.attachment_type_image,
                ),
                style = MaterialTheme.typography.labelSmall,
                color = LandingKitColors.Forest,
            )
            Spacer(Modifier.height(7.dp))
            repeat(3) { line ->
                Box(
                    Modifier
                        .padding(vertical = 2.dp)
                        .width(if (line == 2) 30.dp else 44.dp)
                        .height(2.dp)
                        .background(LandingKitColors.Forest.copy(alpha = .46f), CircleShape),
                )
            }
        }
    }
}

@Composable
private fun TrashGlyph() {
    val color = LandingKitColors.Ink
    Canvas(Modifier.size(18.dp)) {
        val stroke = 1.8.dp.toPx()
        drawLine(color, Offset(size.width * .28f, size.height * .32f), Offset(size.width * .72f, size.height * .32f), stroke)
        drawLine(color, Offset(size.width * .39f, size.height * .22f), Offset(size.width * .61f, size.height * .22f), stroke)
        drawLine(color, Offset(size.width * .34f, size.height * .38f), Offset(size.width * .39f, size.height * .79f), stroke)
        drawLine(color, Offset(size.width * .66f, size.height * .38f), Offset(size.width * .61f, size.height * .79f), stroke)
        drawLine(color, Offset(size.width * .39f, size.height * .79f), Offset(size.width * .61f, size.height * .79f), stroke)
    }
}

@Composable
internal fun DeleteAttachmentDialog(
    attachment: KitAttachment,
    onDismiss: () -> Unit,
    onDelete: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.attachment_delete_title)) },
        text = {
            Text(
                text = stringResource(R.string.attachment_delete_description, attachment.displayName),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        },
        confirmButton = {
            TextButton(
                onClick = onDelete,
                colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error),
            ) { Text(stringResource(R.string.attachment_delete_action)) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.attachment_keep_action)) }
        },
        shape = RoundedCornerShape(24.dp),
    )
}

@Composable
internal fun ClearAttachmentsDialog(
    attachmentCount: Int,
    onDismiss: () -> Unit,
    onDelete: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.attachment_cleanup_dialog_title)) },
        text = {
            Text(
                text = androidx.compose.ui.res.pluralStringResource(
                    R.plurals.attachment_cleanup_dialog_description,
                    attachmentCount,
                    attachmentCount,
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
                Text(stringResource(R.string.attachment_cleanup_confirm))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.attachment_cleanup_keep))
            }
        },
        shape = RoundedCornerShape(24.dp),
    )
}

private const val PDF_MIME_TYPE = "application/pdf"
