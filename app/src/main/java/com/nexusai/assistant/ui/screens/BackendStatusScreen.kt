package com.nexusai.assistant.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.nexusai.assistant.BuildConfig
import com.nexusai.assistant.domain.model.ConnectionStatus
import com.nexusai.assistant.ui.components.ScreenHeader
import com.nexusai.assistant.ui.components.StatusIndicator

/** Phase 1 makes no network calls. This screen only reports that honestly. */
@Composable
fun BackendStatusScreen(onBack: () -> Unit) {
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
            StatusIndicator("Backend", ConnectionStatus.Disconnected)
            StatusIndicator("Authentication", ConnectionStatus.Demo)
            StatusIndicator("AI provider", ConnectionStatus.Demo)
            StatusIndicator("Voice provider", ConnectionStatus.Disconnected)
            Text(
                "Configured address: ${BuildConfig.BACKEND_BASE_URL}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                "Phase 1 does not contact any server. A live health check is added in Phase 2.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
