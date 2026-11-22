package com.nexusai.assistant.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.nexusai.assistant.domain.repository.AuthRepository
import com.nexusai.assistant.domain.repository.OTP_RESEND_COOLDOWN_SEC
import com.nexusai.assistant.domain.repository.OtpRequestResult
import com.nexusai.assistant.domain.repository.OtpVerifyResult
import com.nexusai.assistant.domain.repository.Session
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

sealed interface SessionState {
    data object Loading : SessionState
    data object LoggedOut : SessionState
    data class LoggedIn(val session: Session) : SessionState
}

data class AuthUiState(
    val countryCode: String = "+91",
    val phone: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val demoHint: String? = null,
    val cooldownSec: Int = 0,
    val otpSent: Boolean = false
)

class AuthViewModel(private val repo: AuthRepository) : ViewModel() {

    private val _ui = MutableStateFlow(AuthUiState())
    val ui: StateFlow<AuthUiState> = _ui.asStateFlow()

    val sessionState: StateFlow<SessionState> = repo.session
        .map { s -> if (s == null) SessionState.LoggedOut else SessionState.LoggedIn(s) }
        .stateIn(viewModelScope, SharingStarted.Eagerly, SessionState.Loading)

    private var cooldownJob: Job? = null

    fun onCountryChange(code: String) = _ui.update { it.copy(countryCode = code, error = null) }

    fun onPhoneChange(value: String) =
        _ui.update { it.copy(phone = value.filter { c -> c.isDigit() }.take(15), error = null) }

    fun requestOtp() {
        val s = _ui.value
        if (s.isLoading) return
        _ui.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            when (val r = repo.requestOtp(s.countryCode, s.phone)) {
                is OtpRequestResult.Sent -> {
                    _ui.update { it.copy(isLoading = false, otpSent = true, demoHint = r.demoHint) }
                    startCooldown()
                }
                is OtpRequestResult.Failed ->
                    _ui.update { it.copy(isLoading = false, error = r.message) }
            }
        }
    }

    fun consumeOtpSent() = _ui.update { it.copy(otpSent = false) }

    fun verifyOtp(code: String) {
        if (_ui.value.isLoading) return
        _ui.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            val message: String? = when (val r = repo.verifyOtp(code)) {
                OtpVerifyResult.Success -> null
                is OtpVerifyResult.Invalid -> "Wrong code. ${r.attemptsLeft} attempts left."
                OtpVerifyResult.Expired -> "Code expired. Request a new one."
                OtpVerifyResult.LockedOut -> "Too many wrong attempts. Request a new code."
            }
            // On success the session flow changes and navigation moves to the dashboard.
            _ui.update { it.copy(isLoading = false, error = message) }
        }
    }

    fun logout() {
        viewModelScope.launch {
            repo.logout()
            resetForm()
        }
    }

    fun deleteAccount() {
        viewModelScope.launch {
            repo.deleteAccount()
            resetForm()
        }
    }

    private fun resetForm() {
        cooldownJob?.cancel()
        _ui.value = AuthUiState()
    }

    private fun startCooldown() {
        cooldownJob?.cancel()
        cooldownJob = viewModelScope.launch {
            for (s in OTP_RESEND_COOLDOWN_SEC downTo 1) {
                _ui.update { it.copy(cooldownSec = s) }
                delay(1000)
            }
            _ui.update { it.copy(cooldownSec = 0) }
        }
    }

    companion object {
        fun factory(repo: AuthRepository): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T =
                    AuthViewModel(repo) as T
            }
    }
}
