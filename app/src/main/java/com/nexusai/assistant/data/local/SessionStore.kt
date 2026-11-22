package com.nexusai.assistant.data.local

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.nexusai.assistant.domain.repository.SessionStorage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.sessionDataStore by preferencesDataStore(name = "session")

class SessionStore(private val context: Context) : SessionStorage {
    private val keyPhone = stringPreferencesKey("phone")

    override val phone: Flow<String?> =
        context.sessionDataStore.data.map { it[keyPhone] }

    override suspend fun save(phone: String) {
        context.sessionDataStore.edit { it[keyPhone] = phone }
    }

    override suspend fun clear() {
        context.sessionDataStore.edit { it.clear() }
    }
}
