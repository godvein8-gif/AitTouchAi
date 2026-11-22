package com.nexusai.assistant.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nexusai.assistant.ui.components.AiOrb
import com.nexusai.assistant.ui.components.OrbState
import com.nexusai.assistant.ui.theme.NexusWarning

@Composable
fun OtpVerificationScreen(
    authVm: AuthViewModel,
    onBack: () -> Unit
) {
    val state by authVm.ui.collectAsStateWithLifecycle()
    var code by remember { mutableStateOf("") }

    // A resend creates a fresh code; clear the old input.
    LaunchedEffect(state.otpSent) {
        if (state.otpSent) {
            authVm.consumeOtpSent()
            code = ""
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(16.dp))
        AiOrb(state = OrbState.Listening, orbSize = 110.dp)
        Spacer(Modifier.height(16.dp))
        Text("Verify your number", style = MaterialTheme.typography.headlineMedium)
        Text(
            "Enter the 6-digit code for ${state.countryCode} ${state.phone}",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 6.dp)
        )

        state.demoHint?.let {
            Text(
                "DEMO MODE: no SMS was sent. Use code $it",
                color = NexusWarning,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(top = 16.dp)
            )
        }

        Spacer(Modifier.height(20.dp))
        OutlinedTextField(
            value = code,
            onValueChange = { code = it.filter { c -> c.isDigit() }.take(6) },
            label = { Text("OTP") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )

        state.error?.let {
            Text(
                it,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 12.dp)
            )
        }

        Spacer(Modifier.height(16.dp))
        Button(
            onClick = { authVm.verifyOtp(code) },
            enabled = code.length == 6 && !state.isLoading,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (state.isLoading) {
                CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
            } else {
                Text("Verify")
            }
        }

        TextButton(
            onClick = authVm::requestOtp,
            enabled = state.cooldownSec == 0 && !state.isLoading
        ) {
            Text(
                if (state.cooldownSec > 0) "Resend code in ${state.cooldownSec}s"
                else "Resend code"
            )
        }
        TextButton(onClick = onBack) { Text("Change number") }
    }
}
