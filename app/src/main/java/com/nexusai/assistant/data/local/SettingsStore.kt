package com.nexusai.assistant.data.local

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.nexusai.assistant.domain.model.AppSettings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.settingsDataStore by preferencesDataStore(name = "settings")

class SettingsStore(private val context: Context) {
    private val keyReceptionist = booleanPreferencesKey("receptionist_enabled")
    private val keyLanguage = stringPreferencesKey("language")

    val settings: Flow<AppSettings> = context.settingsDataStore.data.map {
        AppSettings(
            receptionistEnabled = it[keyReceptionist] ?: false,
            language = it[keyLanguage] ?: "auto"
        )
    }

    suspend fun setReceptionistEnabled(enabled: Boolean) {
        context.settingsDataStore.edit { it[keyReceptionist] = enabled }
    }

    suspend fun setLanguage(language: String) {
        context.settingsDataStore.edit { it[keyLanguage] = language }
    }

    suspend fun clearAll() {
        context.settingsDataStore.edit { it.clear() }
    }
}
