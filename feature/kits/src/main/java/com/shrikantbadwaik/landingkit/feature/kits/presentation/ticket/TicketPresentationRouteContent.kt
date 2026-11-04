package com.shrikantbadwaik.landingkit.feature.kits.presentation.ticket

import androidx.compose.runtime.Composable
import com.shrikantbadwaik.landingkit.core.data.PreparedAttachment
import com.shrikantbadwaik.landingkit.core.model.KitAttachment
import com.shrikantbadwaik.landingkit.feature.kits.presentation.component.LoadingScreen
import com.shrikantbadwaik.landingkit.feature.kits.presentation.component.MissingKitScreen
import com.shrikantbadwaik.landingkit.feature.kits.presentation.component.MissingTicketScreen

@Composable
internal fun TicketPresentationRouteContent(
    tripExists: Boolean,
    attachments: List<KitAttachment>?,
    initialAttachmentId: String,
    attachmentProtectionEnabled: Boolean,
    isAttachmentAccessUnlocked: Boolean,
    onPrepareAttachment: (String, (Result<PreparedAttachment>) -> Unit) -> Unit,
    onExit: () -> Unit,
    onHome: () -> Unit,
) {
    // Null means the first repository emission is pending; empty is a completed result.
    when {
        !tripExists -> MissingKitScreen(onHome)
        attachments == null -> LoadingScreen()
        attachments.none { it.id == initialAttachmentId } -> MissingTicketScreen(onHome)
        else -> TicketPresentationScreen(
            attachments = attachments,
            initialAttachmentId = initialAttachmentId,
            attachmentProtectionEnabled = attachmentProtectionEnabled,
            isAttachmentAccessUnlocked = isAttachmentAccessUnlocked,
            onPrepareAttachment = onPrepareAttachment,
            onExit = onExit,
        )
    }
}
