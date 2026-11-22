package com.nexusai.assistant.navigation

object Routes {
    const val LOGIN = "login"
    const val SIGNUP = "signup"
    const val OTP = "otp"
    const val DASHBOARD = "dashboard"

    /** Plain route used by the dashboard: opens a new chat. */
    const val CHAT = "chat"
    const val CHAT_PATTERN = "chat?conversationId={conversationId}"
    const val VOICE = "voice"
    const val INCOMING_CALL = "incoming_call"
    const val CALL_HISTORY = "call_history"
    const val CONVERSATIONS = "conversations"
    const val PROFILE = "profile"
    const val SETTINGS = "settings"
    const val PRIVACY = "privacy"
    const val BACKEND_STATUS = "backend_status"

    val authRoutes = setOf(LOGIN, SIGNUP, OTP)

    /** Opens an existing conversation, or a new one when [id] is -1. */
    fun chat(id: Long = -1L) = "chat?conversationId=$id"
}
