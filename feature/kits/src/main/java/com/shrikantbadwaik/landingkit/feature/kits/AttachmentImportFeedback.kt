package com.shrikantbadwaik.landingkit.feature.kits

import android.content.Context

internal fun AttachmentImportResult.toUserMessage(context: Context): String {
    val rejected = unsupported + tooLarge + failed
    return when {
        imported > 0 && rejected > 0 -> context.getString(
            R.string.attachment_import_partial,
            imported,
            rejected,
        )
        imported > 0 -> context.resources.getQuantityString(
            R.plurals.attachment_imported,
            imported,
            imported,
        )
        tooLarge > 0 -> context.getString(R.string.attachment_import_too_large)
        unsupported > 0 -> context.getString(R.string.attachment_import_unsupported)
        else -> context.getString(R.string.attachment_import_failed)
    }
}
