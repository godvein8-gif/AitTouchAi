package com.nexusai.assistant.domain.model

data class ChatMessage(
    val id: Long = 0L,
    val text: String,
    val isUser: Boolean,
    val timestamp: Long = System.currentTimeMillis(),
    val isError: Boolean = false,
    /** True when the reply came from the Phase 1 mock provider, not a real LLM. */
    val isMock: Boolean = false
)

enum class CallStatus { Ringing, Active, Completed, Missed }

data class CallRecord(
    val id: Long = 0L,
    val callerLabel: String,
    val startedAt: Long = System.currentTimeMillis(),
    val durationSec: Int = 0,
    val status: CallStatus = CallStatus.Completed,
    val summary: String = "",
    val callbackMessage: String? = null,
    /** Phase 1: every call is a simulated DEMO call. */
    val isDemo: Boolean = true
)

enum class ConnectionStatus { Connected, Disconnected, Demo, Error }
