package com.nexusai.assistant.service.auth

import com.nexusai.assistant.domain.repository.OtpRequestResult
import com.nexusai.assistant.domain.repository.OtpVerifyResult
import com.nexusai.assistant.domain.repository.SessionStorage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

private class FakeStorage : SessionStorage {
    private val state = MutableStateFlow<String?>(null)
    override val phone: Flow<String?> = state
    override suspend fun save(phone: String) { state.value = phone }
    override suspend fun clear() { state.value = null }
}

class DemoAuthRepositoryTest {

    private var clock = 1_000_000L
    private val storage = FakeStorage()
    private val repo = DemoAuthRepository(storage) { clock }
    private val validNumber = "9876543210"

    @Test
    fun rejectsShortIndianNumber() = runTest {
        val result = repo.requestOtp("+91", "12345")
        assertTrue(result is OtpRequestResult.Failed)
    }

    @Test
    fun rejectsIndianNumberStartingWithFive() = runTest {
        val result = repo.requestOtp("+91", "5876543210")
        assertTrue(result is OtpRequestResult.Failed)
    }

    @Test
    fun correctCodeCreatesSession() = runTest {
        repo.requestOtp("+91", validNumber)
        assertEquals(OtpVerifyResult.Success, repo.verifyOtp(DemoAuthRepository.DEMO_CODE))
        assertEquals("+91$validNumber", storage.phone.first())
    }

    @Test
    fun wrongCodeLocksOutAfterFiveAttempts() = runTest {
        repo.requestOtp("+91", validNumber)
        repeat(4) {
            assertTrue(repo.verifyOtp("000000") is OtpVerifyResult.Invalid)
        }
        assertEquals(OtpVerifyResult.LockedOut, repo.verifyOtp("000000"))
        assertEquals(OtpVerifyResult.LockedOut, repo.verifyOtp(DemoAuthRepository.DEMO_CODE))
        assertNull(storage.phone.first())
    }

    @Test
    fun codeExpiresAfterTtl() = runTest {
        repo.requestOtp("+91", validNumber)
        clock += DemoAuthRepository.OTP_TTL_MS + 1
        assertEquals(OtpVerifyResult.Expired, repo.verifyOtp(DemoAuthRepository.DEMO_CODE))
    }

    @Test
    fun resendIsBlockedDuringCooldown() = runTest {
        assertTrue(repo.requestOtp("+91", validNumber) is OtpRequestResult.Sent)
        assertTrue(repo.requestOtp("+91", validNumber) is OtpRequestResult.Failed)
        clock += DemoAuthRepository.RESEND_COOLDOWN_MS + 1
        assertTrue(repo.requestOtp("+91", validNumber) is OtpRequestResult.Sent)
    }

    @Test
    fun logoutClearsSession() = runTest {
        repo.requestOtp("+91", validNumber)
        repo.verifyOtp(DemoAuthRepository.DEMO_CODE)
        repo.logout()
        assertNull(storage.phone.first())
    }
}
