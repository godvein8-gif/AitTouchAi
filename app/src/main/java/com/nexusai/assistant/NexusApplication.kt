package com.nexusai.assistant

import android.app.Application
import com.nexusai.assistant.data.local.NexusDatabase
import com.nexusai.assistant.data.local.SessionStore
import com.nexusai.assistant.data.local.SettingsStore
import com.nexusai.assistant.data.remote.MockAiProvider
import com.nexusai.assistant.data.repository.CallRepository
import com.nexusai.assistant.data.repository.ChatRepository
import com.nexusai.assistant.domain.repository.AuthRepository
import com.nexusai.assistant.service.auth.DemoAuthRepository

class NexusApplication : Application() {

    lateinit var authRepository: AuthRepository
        private set

    val database: NexusDatabase by lazy { NexusDatabase.create(this) }
    val settingsStore: SettingsStore by lazy { SettingsStore(this) }

    // Phase 1: mock AI. Phase 2 swaps MockAiProvider for a backend-backed provider.
    val chatRepository: ChatRepository by lazy {
        ChatRepository(database.conversationDao(), database.messageDao(), MockAiProvider())
    }
    val callRepository: CallRepository by lazy { CallRepository(database.callDao()) }

    override fun onCreate() {
        super.onCreate()
        // Phase 1: demo auth. Phase 2 swaps this for a Firebase-backed repository.
        authRepository = DemoAuthRepository(SessionStore(this))
    }
}
