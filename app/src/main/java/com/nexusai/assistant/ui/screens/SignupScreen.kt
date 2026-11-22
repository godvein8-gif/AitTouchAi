package com.nexusai.assistant.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun SignupScreen(
    authVm: AuthViewModel,
    onOtpSent: () -> Unit,
    onGoLogin: () -> Unit
) {
    val state by authVm.ui.collectAsStateWithLifecycle()

    LaunchedEffect(state.otpSent) {
        if (state.otpSent) {
            authVm.consumeOtpSent()
            onOtpSent()
        }
    }

    PhoneEntryContent(
        title = "Create account",
        subtitle = "Register with your own mobile number",
        buttonLabel = "Send OTP",
        requireConsent = true,
        state = state,
        onCountryChange = authVm::onCountryChange,
        onPhoneChange = authVm::onPhoneChange,
        onSubmit = authVm::requestOtp,
        footerText = "Already have an account? Log in",
        onFooterClick = onGoLogin
    )
}
