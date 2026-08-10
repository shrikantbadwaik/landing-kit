package com.shrikantbadwaik.landingkit.core.data.preferences

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore("landing_kit_preferences")

class UserPreferences(private val context: Context) {
    val hasSeenPrivacyNote: Flow<Boolean> = context.dataStore.data.map {
        it[HAS_SEEN_PRIVACY_NOTE] ?: false
    }
    val attachmentProtectionEnabled: Flow<Boolean> = context.dataStore.data.map {
        it[ATTACHMENT_PROTECTION_ENABLED] ?: false
    }

    suspend fun acknowledgePrivacyNote() {
        context.dataStore.edit { it[HAS_SEEN_PRIVACY_NOTE] = true }
    }

    suspend fun setAttachmentProtectionEnabled(enabled: Boolean) {
        context.dataStore.edit { it[ATTACHMENT_PROTECTION_ENABLED] = enabled }
    }

    private companion object {
        val HAS_SEEN_PRIVACY_NOTE = booleanPreferencesKey("has_seen_privacy_note")
        val ATTACHMENT_PROTECTION_ENABLED = booleanPreferencesKey(
            "attachment_protection_enabled",
        )
    }
}
