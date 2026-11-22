package com.nexusai.assistant.domain.repository

import kotlinx.coroutines.flow.Flow

/** Seconds the user must wait before asking for another OTP. */
const val OTP_RESEND_COOLDOWN_SEC = 30

data class Session(val phoneE164: String, val isDemo: Boolean)

sealed interface OtpRequestResult {
    /** [demoHint] is only set in DEMO mode. A real provider never returns the code. */
    data class Sent(val demoHint: String?) : OtpRequestResult
    data class Failed(val message: String) : OtpRequestResult
}

sealed interface OtpVerifyResult {
    data object Success : OtpVerifyResult
    /** [attemptsLeft] is null when the provider does not report it. */
    data class Invalid(val attemptsLeft: Int?) : OtpVerifyResult
    data object Expired : OtpVerifyResult
    data object LockedOut : OtpVerifyResult
}

interface AuthRepository {
    /** True for the offline demo login, false for real Firebase phone login. */
    val isDemo: Boolean
    val session: Flow<Session?>
    suspend fun requestOtp(countryCode: String, number: String): OtpRequestResult
    suspend fun verifyOtp(code: String): OtpVerifyResult
    /** Token the backend can verify, or null when not signed in. */
    suspend fun idToken(): String?
    suspend fun logout()
    suspend fun deleteAccount()
}

/** Where the signed-in phone number is persisted. */
interface SessionStorage {
    val phone: Flow<String?>
    suspend fun save(phone: String)
    suspend fun clear()
}
