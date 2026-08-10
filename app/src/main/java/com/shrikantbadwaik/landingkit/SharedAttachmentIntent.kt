package com.shrikantbadwaik.landingkit

import android.content.Intent
import android.net.Uri
import androidx.core.content.IntentCompat

internal val Intent.isAttachmentShare: Boolean
    get() = action == Intent.ACTION_SEND || action == Intent.ACTION_SEND_MULTIPLE

internal fun Intent.sharedAttachmentUris(): List<Uri> {
    if (!isAttachmentShare) return emptyList()

    val streams = when (action) {
        Intent.ACTION_SEND -> listOfNotNull(
            IntentCompat.getParcelableExtra(this, Intent.EXTRA_STREAM, Uri::class.java),
        )
        Intent.ACTION_SEND_MULTIPLE -> IntentCompat.getParcelableArrayListExtra(
            this,
            Intent.EXTRA_STREAM,
            Uri::class.java,
        ).orEmpty()
        else -> emptyList()
    }
    val clipped = clipData?.let { data ->
        buildList {
            repeat(data.itemCount) { index ->
                data.getItemAt(index).uri?.let(::add)
            }
        }
    }.orEmpty()

    return (streams + clipped).distinct()
}
