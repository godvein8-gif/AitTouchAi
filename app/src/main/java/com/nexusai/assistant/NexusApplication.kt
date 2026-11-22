package com.nexusai.assistant

import android.app.Application
import com.google.firebase.FirebaseApp
import com.nexusai.assistant.data.local.NexusDatabase
import com.nexusai.assistant.data.local.SessionStore
import com.nexusai.assistant.data.local.SettingsStore
import com.nexusai.assistant.data.remote.BackendAiProvider
import com.nexusai.assistant.data.remote.BackendApi
import com.nexusai.assistant.data.remote.MockAiProvider
import com.nexusai.assistant.data.repository.CallRepository
import com.nexusai.assistant.data.repository.ChatRepository
import com.nexusai.assistant.domain.repository.AiProvider
import com.nexusai.assistant.domain.repository.AuthRepository
import com.nexusai.assistant.service.auth.ActivityHolder
import com.nexusai.assistant.service.auth.DemoAuthRepository
import com.nexusai.assistant.service.auth.FirebaseAuthRepository

class NexusApplication : Application() {

    val activityHolder = ActivityHolder()
    val backendApi = BackendApi(BuildConfig.BACKEND_BASE_URL)

    lateinit var authRepository: AuthRepository
        private set
    lateinit var aiProvider: AiProvider
        private set

    val database: NexusDatabase by lazy { NexusDatabase.create(this) }
    val settingsStore: SettingsStore by lazy { SettingsStore(this) }
    val chatRepository: ChatRepository by lazy {
        ChatRepository(database.conversationDao(), database.messageDao(), aiProvider)
    }
    val callRepository: CallRepository by lazy { CallRepository(database.callDao()) }

    override fun onCreate() {
        super.onCreate()
        // Real login and real AI are used only when google-services.json was included in the build.
        // Otherwise the app falls back to the offline demo, so it never crashes without Firebase.
        val firebaseReady = FirebaseApp.getApps(this).isNotEmpty()
        if (firebaseReady) {
            authRepository = FirebaseAuthRepository(activityHolder)
            aiProvider = BackendAiProvider(backendApi, authRepository)
        } else {
            authRepository = DemoAuthRepository(SessionStore(this))
            aiProvider = MockAiProvider()
        }
    }
}
