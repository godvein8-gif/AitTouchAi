package com.nexusai.assistant.ui.screens

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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PhoneInTalk
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.nexusai.assistant.domain.model.ConnectionStatus
import com.nexusai.assistant.navigation.Routes
import com.nexusai.assistant.ui.components.AiOrb
import com.nexusai.assistant.ui.components.OrbState
import com.nexusai.assistant.ui.components.StatusIndicator
import com.nexusai.assistant.ui.theme.NexusSurface

private data class DashboardModule(
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val route: String
)

private val modules = listOf(
    DashboardModule("AI Chat", "Talk to NEXUS", Icons.Filled.Chat, Routes.CHAT),
    DashboardModule("Voice Assistant", "Speak and listen", Icons.Filled.Mic, Routes.VOICE),
    DashboardModule("Call Receptionist", "Incoming calls", Icons.Filled.PhoneInTalk, Routes.INCOMING_CALL),
    DashboardModule("Call History", "Summaries, messages", Icons.Filled.History, Routes.CALL_HISTORY),
    DashboardModule("Conversations", "Past chats", Icons.Filled.Forum, Routes.CONVERSATIONS),
    DashboardModule("Profile", "Account", Icons.Filled.Person, Routes.PROFILE),
    DashboardModule("AI Settings", "Language, voice", Icons.Filled.Settings, Routes.SETTINGS),
    DashboardModule("Privacy", "Your data", Icons.Filled.Lock, Routes.PRIVACY),
    DashboardModule("Backend", "Connection status", Icons.Filled.Cloud, Routes.BACKEND_STATUS)
)

private fun maskPhone(phone: String): String =
    if (phone.length > 6) phone.take(3) + "*****" + phone.takeLast(2) else phone

@Composable
fun DashboardScreen(
    phone: String,
    onOpen: (String) -> Unit,
    onLogout: () -> Unit
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item(span = { GridItemSpan(maxLineSpan) }) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("NEXUS AI", style = MaterialTheme.typography.headlineMedium)
                        Text(
                            "Signed in as ${maskPhone(phone)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(onClick = onLogout) {
                        Icon(Icons.Filled.PowerSettingsNew, contentDescription = "Log out")
                    }
                }
                Spacer(Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    AiOrb(state = OrbState.Idle, orbSize = 140.dp)
                }
                Spacer(Modifier.height(8.dp))
                StatusIndicator("Sign-in", ConnectionStatus.Demo)
                Spacer(Modifier.height(4.dp))
                StatusIndicator("AI provider", ConnectionStatus.Demo)
                Spacer(Modifier.height(4.dp))
                StatusIndicator("Call receptionist", ConnectionStatus.Demo)
            }
        }

        items(modules) { module ->
            Card(
                onClick = { onOpen(module.route) },
                colors = CardDefaults.cardColors(containerColor = NexusSurface),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Icon(
                        module.icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.height(10.dp))
                    Text(module.title, style = MaterialTheme.typography.titleMedium)
                    Text(
                        module.subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
