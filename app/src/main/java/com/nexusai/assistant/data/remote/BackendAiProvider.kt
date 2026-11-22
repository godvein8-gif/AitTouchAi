package com.nexusai.assistant.data.remote

import com.nexusai.assistant.domain.model.ChatMessage
import com.nexusai.assistant.domain.repository.AiProvider
import com.nexusai.assistant.domain.repository.AuthRepository
import java.io.IOException

/** Real AI replies, fetched through the secure backend. No AI key exists in this app. */
class BackendAiProvider(
    private val api: BackendApi,
    private val auth: AuthRepository
) : AiProvider {

    override val isMock: Boolean = false

    override suspend fun reply(history: List<ChatMessage>, userText: String): String {
        val token = auth.idToken() ?: throw IOException("Please log in again.")
        val earlier = history.dropLast(1).filter { !it.isError }.takeLast(12)
        return api.chat(token, earlier, userText)
    }
}
