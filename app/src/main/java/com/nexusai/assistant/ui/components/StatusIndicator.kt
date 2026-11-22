package com.nexusai.assistant.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.nexusai.assistant.domain.model.ConnectionStatus
import com.nexusai.assistant.ui.theme.NexusError
import com.nexusai.assistant.ui.theme.NexusSuccess
import com.nexusai.assistant.ui.theme.NexusTextSecondary
import com.nexusai.assistant.ui.theme.NexusWarning

@Composable
fun StatusIndicator(
    label: String,
    status: ConnectionStatus,
    modifier: Modifier = Modifier
) {
    val color = when (status) {
        ConnectionStatus.Connected -> NexusSuccess
        ConnectionStatus.Demo -> NexusWarning
        ConnectionStatus.Disconnected -> NexusTextSecondary
        ConnectionStatus.Error -> NexusError
    }
    val statusText = when (status) {
        ConnectionStatus.Connected -> "Connected"
        ConnectionStatus.Demo -> "Demo"
        ConnectionStatus.Disconnected -> "Not connected"
        ConnectionStatus.Error -> "Error"
    }
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        androidx.compose.foundation.layout.Box(
            modifier = Modifier
                .size(10.dp)
                .background(color, CircleShape)
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text = "$label: $statusText",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
