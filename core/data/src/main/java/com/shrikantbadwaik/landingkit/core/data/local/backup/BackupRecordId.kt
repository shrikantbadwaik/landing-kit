package com.shrikantbadwaik.landingkit.core.data.local.backup

/** UUIDs and curated IDs fit this bound, including combined preview filenames. */
internal object BackupRecordId {
    const val MAX_LENGTH = 64
    fun isValid(id: String): Boolean = id.length in 1..MAX_LENGTH &&
        id != "." && id != ".." && id.all { character ->
            character in 'a'..'z' || character in 'A'..'Z' || character in '0'..'9' ||
                character == '_' || character == '-' || character == '.'
        }
}
