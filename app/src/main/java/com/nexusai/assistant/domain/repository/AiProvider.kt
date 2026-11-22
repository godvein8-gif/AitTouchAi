package com.nexusai.assistant.domain.repository

import com.nexusai.assistant.domain.model.ChatMessage

/**
 * Replaceable AI backend. Phase 1 uses a mock; Phase 2 adds a provider that calls
 * the secure backend (API keys never live in the APK).
 */
interface AiProvider {
    /** True when replies are scripted demo text, not a real language model. */
    val isMock: Boolean

    /** [history] already contains the latest user message as its last entry. */
    suspend fun reply(history: List<ChatMessage>, userText: String): String
}
