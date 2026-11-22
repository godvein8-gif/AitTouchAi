package com.nexusai.assistant.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nexusai.assistant.domain.model.ConnectionStatus
import com.nexusai.assistant.service.voice.Speaker
import com.nexusai.assistant.ui.components.ScreenHeader
import com.nexusai.assistant.ui.components.StatusIndicator
import com.nexusai.assistant.ui.theme.NexusSurface
import com.nexusai.assistant.ui.theme.NexusWarning
import com.nexusai.assistant.ui.util.maskedPhone

@Composable
private fun SectionCard(title: String, content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = NexusSurface),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            content()
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Column {
        Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
        Text(value, style = MaterialTheme.typography.bodyLarge)
    }
}

@Composable
fun IncomingCallScreen(
    vm: ReceptionistViewModel,
    phone: String,
    onBack: () -> Unit,
    onOpenHistory: () -> Unit
) {
    val enabled by vm.enabled.collectAsStateWithLifecycle()
    val sim by vm.sim.collectAsStateWithLifecycle()
    val calls by vm.recentCalls.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        ScreenHeader(title = "Call Receptionist", onBack = onBack)

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            SectionCard("AI receptionist") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(if (enabled) "ON (demo only)" else "OFF", style = MaterialTheme.typography.bodyLarge)
                        Text(
                            "Phase 1 answers no real calls. Only the simulator below runs.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(checked = enabled, onCheckedChange = vm::setEnabled)
                }
            }

            SectionCard("Number routing") {
                InfoRow("A. Account number (sign-in)", maskedPhone(phone))
                Text(
                    "Used only to sign in. It does not receive receptionist calls.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                InfoRow("B. Cloud receptionist number", "Not set up (arrives in Phase 4)")
                InfoRow("C. Forwarding from your personal number", "Not configured")
                Text(
                    "NEXUS never turns on call forwarding by itself. Forwarding needs carrier support, " +
                        "and Android apps cannot intercept normal SIM call audio.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            SectionCard("Connection") {
                StatusIndicator("Provider connection", ConnectionStatus.Disconnected)
                StatusIndicator("Cloud connection", ConnectionStatus.Disconnected)
            }

            SectionCard("Call simulator (DEMO)") {
                Text(
                    "Plays a fictional scripted call. No real caller, no audio.",
                    style = MaterialTheme.typography.bodySmall,
                    color = NexusWarning
                )
                Button(
                    onClick = vm::simulateCall,
                    enabled = enabled && sim is SimState.Idle,
                    modifier = Modifier.fillMaxWidth()
                ) { Text("Simulate incoming call") }
                if (!enabled) {
                    Text(
                        "Turn the receptionist ON to use the simulator.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                when (val s = sim) {
                    SimState.Idle -> {}
                    is SimState.Ringing ->
                        Text("DEMO: ${s.callerLabel} is calling...", style = MaterialTheme.typography.bodyLarge)
                    is SimState.Active -> {
                        Text(
                            "DEMO call in progress: ${s.callerLabel}",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                        s.lines.forEach { line ->
                            val who = if (line.speaker == Speaker.Ai) "NEXUS AI" else "Caller"
                            Text("$who: ${line.text}", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }

            SectionCard("Callback messages") {
                val withMessages = calls.filter { !it.callbackMessage.isNullOrBlank() }.take(5)
                if (withMessages.isEmpty()) {
                    Text(
                        "No callback messages yet.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    withMessages.forEach { call ->
                        val tag = if (call.isDemo) "[DEMO] " else ""
                        Text("$tag${call.callbackMessage}", style = MaterialTheme.typography.bodyLarge)
                    }
                }
            }

            TextButton(onClick = onOpenHistory) { Text("View call history") }
        }
    }
}
