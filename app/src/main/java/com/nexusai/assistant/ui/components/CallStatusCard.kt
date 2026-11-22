package com.nexusai.assistant.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.nexusai.assistant.domain.model.CallRecord
import com.nexusai.assistant.domain.model.CallStatus
import com.nexusai.assistant.ui.theme.NexusSurface
import com.nexusai.assistant.ui.theme.NexusWarning
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun CallStatusCard(call: CallRecord, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = NexusSurface),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(call.callerLabel, style = MaterialTheme.typography.titleMedium)
                if (call.isDemo) {
                    Text(
                        text = "DEMO",
                        style = MaterialTheme.typography.labelSmall,
                        color = NexusWarning,
                        modifier = Modifier
                            .background(NexusWarning.copy(alpha = 0.15f), RoundedCornerShape(6.dp))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }
            val time = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault())
                .format(Date(call.startedAt))
            val statusLabel = when (call.status) {
                CallStatus.Ringing -> "Ringing"
                CallStatus.Active -> "In progress"
                CallStatus.Completed -> "Completed - ${call.durationSec}s"
                CallStatus.Missed -> "Missed"
            }
            Text(
                text = "$time  |  $statusLabel",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp)
            )
            if (call.summary.isNotBlank()) {
                Text(
                    text = "Summary: ${call.summary}",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
            call.callbackMessage?.takeIf { it.isNotBlank() }?.let {
                Text(
                    text = "Callback message: $it",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = 6.dp)
                )
            }
        }
    }
}
