package com.nexusai.assistant.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.nexusai.assistant.BuildConfig
import com.nexusai.assistant.data.remote.BackendApi
import com.nexusai.assistant.domain.model.ConnectionStatus
import com.nexusai.assistant.ui.components.ScreenHeader
import com.nexusai.assistant.ui.components.StatusIndicator

@Composable
fun BackendStatusScreen(api: BackendApi, isDemo: Boolean, onBack: () -> Unit) {
    var checking by remember { mutableStateOf(true) }
    var reachable by remember { mutableStateOf(false) }
    var attempt by remember { mutableIntStateOf(0) }

    // Live check. The free server can take up to a minute to wake up.
    LaunchedEffect(attempt) {
        checking = true
        reachable = api.health()
        checking = false
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        ScreenHeader(title = "Backend Connection", onBack = onBack)
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (checking) {
                Text("Checking server... (can take up to a minute if it was asleep)",
                    style = MaterialTheme.typography.bodySmall)
            } else {
                StatusIndicator(
                    "Backend",
                    if (reachable) ConnectionStatus.Connected else ConnectionStatus.Error
                )
            }
            StatusIndicator("Authentication", if (isDemo) ConnectionStatus.Demo else ConnectionStatus.Connected)
            StatusIndicator("AI provider", if (isDemo) ConnectionStatus.Demo else ConnectionStatus.Connected)
            StatusIndicator("Voice provider", ConnectionStatus.Disconnected)
            Text(
                "Server address: ${BuildConfig.BACKEND_BASE_URL}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (isDemo) {
                Text(
                    "Offline demo: the app is not using the server for chat. " +
                        "Real login and AI need google-services.json in the build.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            OutlinedButton(onClick = { attempt++ }, enabled = !checking) { Text("Check again") }
        }
    }
}
