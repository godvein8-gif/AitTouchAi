package com.nexusai.assistant.domain.model

/** Preferred reply language: "auto", "hi" or "en". */
data class AppSettings(
    val receptionistEnabled: Boolean = false,
    val language: String = "auto"
)
