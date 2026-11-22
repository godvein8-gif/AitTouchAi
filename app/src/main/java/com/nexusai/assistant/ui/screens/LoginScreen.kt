package com.nexusai.assistant.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun LoginScreen(
    authVm: AuthViewModel,
    onOtpSent: () -> Unit,
    onGoSignup: () -> Unit
) {
    val state by authVm.ui.collectAsStateWithLifecycle()

    LaunchedEffect(state.otpSent) {
        if (state.otpSent) {
            authVm.consumeOtpSent()
            onOtpSent()
        }
    }

    PhoneEntryContent(
        title = "NEXUS AI",
        subtitle = "Log in with your mobile number",
        buttonLabel = "Send OTP",
        requireConsent = false,
        state = state,
        onCountryChange = authVm::onCountryChange,
        onPhoneChange = authVm::onPhoneChange,
        onSubmit = authVm::requestOtp,
        footerText = "New here? Create an account",
        onFooterClick = onGoSignup
    )
}
