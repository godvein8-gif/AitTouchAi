package com.nexusai.assistant.service.auth

import com.nexusai.assistant.domain.repository.AuthRepository
import com.nexusai.assistant.domain.repository.OtpRequestResult
import com.nexusai.assistant.domain.repository.OtpVerifyResult
import com.nexusai.assistant.domain.repository.Session
import com.nexusai.assistant.domain.repository.SessionStorage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.security.MessageDigest

/**
 * DEMO authentication. No SMS is sent and no server is contacted.
 * Real OTP arrives in Phase 2 (Firebase Phone Auth + backend token check).
 *
 * Even in demo mode the code is kept only as a SHA-256 hash in memory.
 */
class DemoAuthRepository(
    private val storage: SessionStorage,
    private val now: () -> Long = { System.currentTimeMillis() }
) : AuthRepository {

    companion object {
        const val DEMO_CODE = "123456"
        const val OTP_TTL_MS = 5 * 60_000L
        const val RESEND_COOLDOWN_MS = 30_000L
        const val MAX_ATTEMPTS = 5
    }

    private var pendingPhone: String? = null
    private var codeHash: String? = null
    private var issuedAt = 0L
    private var lastSentAt = 0L
    private var attemptsLeft = MAX_ATTEMPTS

    override val session: Flow<Session?> =
        storage.phone.map { phone -> phone?.let { Session(it, isDemo = true) } }

    override suspend fun requestOtp(countryCode: String, number: String): OtpRequestResult {
        val digits = number.filter { it.isDigit() }
        validate(countryCode, digits)?.let { return OtpRequestResult.Failed(it) }

        val t = now()
        val since = t - lastSentAt
        if (lastSentAt != 0L && since < RESEND_COOLDOWN_MS) {
            val wait = (RESEND_COOLDOWN_MS - since + 999) / 1000
            return OtpRequestResult.Failed("Please wait ${wait}s before requesting a new code.")
        }

        pendingPhone = countryCode + digits
        codeHash = sha256(DEMO_CODE)
        issuedAt = t
        lastSentAt = t
        attemptsLeft = MAX_ATTEMPTS
        return OtpRequestResult.Sent(demoHint = DEMO_CODE)
    }

    override suspend fun verifyOtp(code: String): OtpVerifyResult {
        val expected = codeHash
        val phone = pendingPhone
        if (expected == null || phone == null) return OtpVerifyResult.Expired
        if (now() - issuedAt > OTP_TTL_MS) {
            clearPending()
            return OtpVerifyResult.Expired
        }
        if (attemptsLeft <= 0) return OtpVerifyResult.LockedOut

        if (sha256(code) == expected) {
            storage.save(phone)
            clearPending()
            return OtpVerifyResult.Success
        }
        attemptsLeft--
        return if (attemptsLeft <= 0) OtpVerifyResult.LockedOut
        else OtpVerifyResult.Invalid(attemptsLeft)
    }

    override suspend fun logout() {
        clearPending()
        storage.clear()
    }

    /** Demo: removes the local session only. Real server-side deletion comes in Phase 2. */
    override suspend fun deleteAccount() {
        clearPending()
        storage.clear()
    }

    private fun clearPending() {
        pendingPhone = null
        codeHash = null
    }

    private fun validate(countryCode: String, digits: String): String? {
        if (!countryCode.startsWith("+")) return "Select a country code."
        if (countryCode == "+91") {
            if (digits.length != 10) return "Enter a 10-digit Indian mobile number."
            if (digits.first() !in '6'..'9') return "Indian mobile numbers start with 6, 7, 8 or 9."
        } else if (digits.length !in 6..14) {
            return "Enter a valid mobile number."
        }
        return null
    }

    private fun sha256(value: String): String =
        MessageDigest.getInstance("SHA-256")
            .digest(value.toByteArray())
            .joinToString("") { "%02x".format(it) }
}
