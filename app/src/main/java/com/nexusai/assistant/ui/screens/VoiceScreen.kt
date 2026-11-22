package com.nexusai.assistant.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.nexusai.assistant.ui.components.AiOrb
import com.nexusai.assistant.ui.components.OrbState
import com.nexusai.assistant.ui.components.ScreenHeader
import com.nexusai.assistant.ui.theme.NexusWarning
import kotlinx.coroutines.delay
import androidx.compose.runtime.LaunchedEffect

private enum class VoiceMode { Idle, Listening, Speaking }

/**
 * Phase 1: voice UI only. The microphone is NOT recorded and no speech is recognised.
 * Real speech-to-text and text-to-speech arrive in Phase 3.
 * The user decides when "listening" starts and stops.
 */
@Composable
fun VoiceScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    var mode by remember { mutableStateOf(VoiceMode.Idle) }
    var permissionDenied by remember { mutableStateOf(false) }

    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        permissionDenied = !granted
        if (granted) mode = VoiceMode.Listening
    }

    // Speaking preview ends by itself after a few seconds.
    LaunchedEffect(mode) {
        if (mode == VoiceMode.Speaking) {
            delay(4000)
            mode = VoiceMode.Idle
        }
    }

    fun startListening() {
        val granted = ContextCompat.checkSelfPermission(
            context, Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED
        if (granted) {
            permissionDenied = false
            mode = VoiceMode.Listening
        } else {
            launcher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    val orbState = when (mode) {
        VoiceMode.Idle -> OrbState.Idle
        VoiceMode.Listening -> OrbState.Listening
        VoiceMode.Speaking -> OrbState.Speaking
    }
    val statusText = when (mode) {
        VoiceMode.Idle -> "Tap Start listening"
        VoiceMode.Listening -> "Listening animation (demo). Nothing is recorded."
        VoiceMode.Speaking -> "Speaking animation (demo)"
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        ScreenHeader(title = "Voice Assistant", onBack = onBack)

        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AiOrb(state = orbState, orbSize = 200.dp)
            Spacer(Modifier.height(24.dp))
            Text(statusText, style = MaterialTheme.typography.bodyLarge)
            Spacer(Modifier.height(12.dp))
            Text(
                "DEMO MODE: speech recognition and spoken replies are added in Phase 3.",
                style = MaterialTheme.typography.bodySmall,
                color = NexusWarning
            )
            if (permissionDenied) {
                Text(
                    "Microphone permission was denied. Allow it in Android Settings to use voice.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(top = 12.dp)
                )
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = ::startListening,
                enabled = mode == VoiceMode.Idle,
                modifier = Modifier.weight(1f)
            ) { Text("Start listening") }
            OutlinedButton(
                onClick = { mode = VoiceMode.Idle },
                enabled = mode != VoiceMode.Idle,
                modifier = Modifier.weight(1f)
            ) { Text(if (mode == VoiceMode.Speaking) "Interrupt" else "Stop") }
        }
        OutlinedButton(
            onClick = { mode = VoiceMode.Speaking },
            enabled = mode == VoiceMode.Idle,
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, bottom = 16.dp)
        ) { Text("Preview speaking animation") }
    }
}
