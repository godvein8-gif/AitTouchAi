package com.nexusai.assistant.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nexusai.assistant.data.repository.ChatRepository
import com.nexusai.assistant.domain.model.ChatMessage
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class ChatUiState(
    val messages: List<ChatMessage> = emptyList(),
    val isTyping: Boolean = false,
    val canRetry: Boolean = false,
    val error: String? = null
)

@OptIn(ExperimentalCoroutinesApi::class)
class ChatViewModel(
    private val repo: ChatRepository,
    initialConversationId: Long?
) : ViewModel() {

    private data class Status(
        val isTyping: Boolean = false,
        val canRetry: Boolean = false,
        val error: String? = null
    )

    private val conversationId = MutableStateFlow(initialConversationId)
    private val status = MutableStateFlow(Status())

    private val messages: StateFlow<List<ChatMessage>> = conversationId
        .flatMapLatest { id -> if (id == null) flowOf(emptyList()) else repo.observeMessages(id) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val ui: StateFlow<ChatUiState> = combine(messages, status) { m, s ->
        ChatUiState(messages = m, isTyping = s.isTyping, canRetry = s.canRetry, error = s.error)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ChatUiState())

    fun send(text: String) {
        val clean = text.trim()
        if (clean.isEmpty() || status.value.isTyping) return
        viewModelScope.launch {
            val id = conversationId.value
                ?: repo.createConversation(clean.take(40)).also { conversationId.value = it }
            repo.addUserMessage(id, clean)
            generate(id)
        }
    }

    fun retry() {
        val id = conversationId.value ?: return
        if (!status.value.canRetry) return
        viewModelScope.launch { generate(id) }
    }

    fun clearConversation() {
        val id = conversationId.value ?: return
        viewModelScope.launch {
            repo.clearMessages(id)
            status.value = Status()
        }
    }

    private suspend fun generate(id: Long) {
        status.value = Status(isTyping = true)
        try {
            repo.generateReply(id)
            status.value = Status()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            status.value = Status(canRetry = true, error = e.message ?: "Something went wrong.")
        }
    }
}
