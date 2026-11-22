package com.nexusai.assistant.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nexusai.assistant.data.local.SettingsStore
import com.nexusai.assistant.data.repository.CallRepository
import com.nexusai.assistant.data.repository.ChatRepository
import com.nexusai.assistant.ui.components.ScreenHeader
import kotlinx.coroutines.launch

/** Deletes data stored on this device. Shared by Privacy and Profile screens. */
class DataViewModel(
    private val chats: ChatRepository,
    private val calls: CallRepository,
    private val settings: SettingsStore
) : ViewModel() {

    fun deleteChats() {
        viewModelScope.launch { chats.deleteAllConversations() }
    }

    fun deleteCalls() {
        viewModelScope.launch { calls.deleteAll() }
    }

    /** Chats, calls and settings. Returns after everything is wiped. */
    suspend fun wipeEverything() {
        chats.deleteAllConversations()
        calls.deleteAll()
        settings.clearAll()
    }

    fun wipeEverythingAsync(then: () -> Unit) {
        viewModelScope.launch {
            wipeEverything()
            then()
        }
    }
}

private enum class Confirm { Chats, Calls }

@Composable
fun PrivacyScreen(vm: DataViewModel, onBack: () -> Unit) {
    var confirm by remember { mutableStateOf<Confirm?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        ScreenHeader(title = "Privacy", onBack = onBack)
        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Call recording", style = MaterialTheme.typography.titleMedium)
                    Text(
                        "Always OFF. NEXUS AI does not record calls.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(checked = false, onCheckedChange = null, enabled = false)
            }

            Text("What is stored on this device", style = MaterialTheme.typography.titleMedium)
            Text(
                "Chat messages, call summaries, callback messages and your settings. " +
                    "Call transcripts are not kept. Nothing leaves this device in Phase 1.",
                style = MaterialTheme.typography.bodyLarge
            )

            OutlinedButton(
                onClick = { confirm = Confirm.Chats },
                modifier = Modifier.fillMaxWidth()
            ) { Text("Delete all chat history") }
            OutlinedButton(
                onClick = { confirm = Confirm.Calls },
                modifier = Modifier.fillMaxWidth()
            ) { Text("Delete all call history") }
            Text(
                "To remove your account and everything on this device, use Profile > Delete account.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }

    confirm?.let { which ->
        val label = if (which == Confirm.Chats) "chat history" else "call history"
        AlertDialog(
            onDismissRequest = { confirm = null },
            title = { Text("Delete all $label?") },
            text = { Text("This cannot be undone.") },
            confirmButton = {
                TextButton(onClick = {
                    if (which == Confirm.Chats) vm.deleteChats() else vm.deleteCalls()
                    confirm = null
                }) { Text("Delete") }
            },
            dismissButton = { TextButton(onClick = { confirm = null }) { Text("Cancel") } }
        )
    }
}
