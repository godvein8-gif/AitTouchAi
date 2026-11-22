package com.nexusai.assistant.data.repository

import com.nexusai.assistant.data.local.ChatMessageEntity
import com.nexusai.assistant.data.local.ConversationDao
import com.nexusai.assistant.data.local.ConversationEntity
import com.nexusai.assistant.data.local.MessageDao
import com.nexusai.assistant.domain.model.ChatMessage
import com.nexusai.assistant.domain.model.Conversation
import com.nexusai.assistant.domain.repository.AiProvider
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ChatRepository(
    private val convDao: ConversationDao,
    private val msgDao: MessageDao,
    private val ai: AiProvider
) {
    fun observeConversations(): Flow<List<Conversation>> =
        convDao.observeAll().map { list ->
            list.map { Conversation(id = it.id, title = it.title, updatedAt = it.updatedAt) }
        }

    fun observeMessages(conversationId: Long): Flow<List<ChatMessage>> =
        msgDao.observe(conversationId).map { list -> list.map { it.toModel() } }

    suspend fun createConversation(title: String): Long {
        val now = System.currentTimeMillis()
        return convDao.insert(ConversationEntity(title = title, createdAt = now, updatedAt = now))
    }

    suspend fun addUserMessage(conversationId: Long, text: String) {
        val now = System.currentTimeMillis()
        msgDao.insert(
            ChatMessageEntity(
                conversationId = conversationId,
                text = text,
                isUser = true,
                timestamp = now,
                isError = false,
                isMock = false
            )
        )
        convDao.touch(conversationId, now)
    }

    /**
     * Asks the AI to answer the latest user message. Throws if the provider fails,
     * so the caller can offer Retry. Does nothing if the last message is already a reply.
     */
    suspend fun generateReply(conversationId: Long) {
        val history = msgDao.list(conversationId).map { it.toModel() }
        val last = history.lastOrNull() ?: return
        if (!last.isUser) return

        val reply = ai.reply(history, last.text)
        val now = System.currentTimeMillis()
        msgDao.insert(
            ChatMessageEntity(
                conversationId = conversationId,
                text = reply,
                isUser = false,
                timestamp = now,
                isError = false,
                isMock = ai.isMock
            )
        )
        convDao.touch(conversationId, now)
    }

    suspend fun clearMessages(conversationId: Long) = msgDao.clear(conversationId)

    suspend fun deleteConversation(conversationId: Long) = convDao.delete(conversationId)

    suspend fun deleteAllConversations() = convDao.deleteAll()

    private fun ChatMessageEntity.toModel() = ChatMessage(
        id = id,
        text = text,
        isUser = isUser,
        timestamp = timestamp,
        isError = isError,
        isMock = isMock
    )
}
