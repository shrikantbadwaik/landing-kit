package com.shrikantbadwaik.landingkit.feature.kits.presentation.security

import android.content.ActivityNotFoundException
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.core.content.FileProvider
import com.shrikantbadwaik.landingkit.core.data.PreparedAttachment
import com.shrikantbadwaik.landingkit.core.model.KitAttachment
import com.shrikantbadwaik.landingkit.feature.kits.PendingAttachmentAuthentication
import com.shrikantbadwaik.landingkit.feature.kits.ProtectedAttachmentAction
import com.shrikantbadwaik.landingkit.feature.kits.R
import com.shrikantbadwaik.landingkit.feature.kits.requiresAttachmentAuthentication

@Composable
internal fun rememberProtectedAttachmentOpener(
    attachments: List<KitAttachment>,
    protectionEnabled: Boolean,
    accessUnlocked: Boolean,
    pendingAuthentication: PendingAttachmentAuthentication?,
    onPrepareAttachment: (String, (Result<PreparedAttachment>) -> Unit) -> Unit,
    onSetProtection: (Boolean) -> Unit,
    onRequestAuthentication: (ProtectedAttachmentAction) -> Unit,
    onFinishAuthentication: (String) -> Unit,
    onUnlockAccess: () -> Unit,
    onLockAccess: () -> Unit,
    onFeedback: (String) -> Unit,
): (KitAttachment) -> Unit {
    val context = LocalContext.current
    val activity = remember(context) { context.findFragmentActivity() }
    val authenticator = remember(activity) { activity?.let(::AttachmentAuthenticator) }
    val authenticationTitle = stringResource(R.string.attachment_auth_prompt_title)
    val authenticationSubtitle = stringResource(R.string.attachment_auth_prompt_subtitle)
    val latestAttachments = rememberUpdatedState(attachments)
    val latestFeedback = rememberUpdatedState(onFeedback)

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
                    latestFeedback.value(context.getString(R.string.attachment_no_viewer))
                } catch (_: RuntimeException) {
                    latestFeedback.value(context.getString(R.string.attachment_open_failed))
                }
            }.onFailure {
                latestFeedback.value(context.getString(R.string.attachment_open_failed))
            }
        }
    }
    val latestOpenAttachment = rememberUpdatedState(openAttachment)

    LaunchedEffect(pendingAuthentication?.id, authenticator) {
        val request = pendingAuthentication ?: return@LaunchedEffect
        val activeAuthenticator = authenticator
        val isDisablingProtection =
            (request.action as? ProtectedAttachmentAction.SetProtection)?.enabled == false
        when (activeAuthenticator?.availability()) {
            AttachmentAuthenticationAvailability.Available -> activeAuthenticator.authenticate(
                title = authenticationTitle,
                subtitle = authenticationSubtitle,
                onSuccess = {
                    onFinishAuthentication(request.id)
                    when (val action = request.action) {
                        is ProtectedAttachmentAction.Open -> {
                            onUnlockAccess()
                            latestAttachments.value.firstOrNull { it.id == action.attachmentId }
                                ?.let(latestOpenAttachment.value)
                        }
                        is ProtectedAttachmentAction.SetProtection -> {
                            onSetProtection(action.enabled)
                            if (action.enabled) onUnlockAccess() else onLockAccess()
                        }
                    }
                },
                onCancelled = { onFinishAuthentication(request.id) },
                onError = {
                    onFinishAuthentication(request.id)
                    latestFeedback.value(
                        context.getString(R.string.attachment_authentication_failed),
                    )
                },
            )
            AttachmentAuthenticationAvailability.NotEnrolled -> {
                onFinishAuthentication(request.id)
                if (isDisablingProtection) {
                    onSetProtection(false)
                    onLockAccess()
                    latestFeedback.value(
                        context.getString(R.string.attachment_protection_disabled_unavailable),
                    )
                } else {
                    latestFeedback.value(
                        context.getString(R.string.attachment_authentication_not_enrolled),
                    )
                }
            }
            AttachmentAuthenticationAvailability.Unavailable, null -> {
                onFinishAuthentication(request.id)
                if (isDisablingProtection) {
                    onSetProtection(false)
                    onLockAccess()
                    latestFeedback.value(
                        context.getString(R.string.attachment_protection_disabled_unavailable),
                    )
                } else {
                    latestFeedback.value(
                        context.getString(R.string.attachment_authentication_unavailable),
                    )
                }
            }
        }
    }

    return { attachment ->
        if (requiresAttachmentAuthentication(protectionEnabled, accessUnlocked)) {
            onRequestAuthentication(ProtectedAttachmentAction.Open(attachment.id))
        } else {
            openAttachment(attachment)
        }
    }
}
