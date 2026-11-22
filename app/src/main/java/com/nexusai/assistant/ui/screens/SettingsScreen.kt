package com.nexusai.assistant.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.nexusai.assistant.data.local.SettingsStore
import com.nexusai.assistant.ui.components.ScreenHeader
import com.nexusai.assistant.ui.theme.NexusWarning
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(private val store: SettingsStore) : ViewModel() {
    val language: StateFlow<String> = store.settings
        .map { it.language }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "auto")

    fun setLanguage(value: String) {
        viewModelScope.launch { store.setLanguage(value) }
    }
}

private val languageOptions = listOf(
    "auto" to "Automatic (match what I write)",
    "hi" to "Hindi",
    "en" to "English"
)

@Composable
fun SettingsScreen(vm: SettingsViewModel, onBack: () -> Unit) {
    val language by vm.language.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        ScreenHeader(title = "AI Settings", onBack = onBack)
        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            Text("Preferred language", style = MaterialTheme.typography.titleMedium)
            Text(
                "Saved on this device. It will guide replies once a real AI is connected (Phase 2).",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            languageOptions.forEach { (code, label) ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(selected = language == code, onClick = { vm.setLanguage(code) })
                    Text(label, style = MaterialTheme.typography.bodyLarge)
                }
            }

            Text(
                "AI provider",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(top = 24.dp)
            )
            Text(
                "Mock provider (DEMO). Real AI is connected through the secure backend in Phase 2. " +
                    "No API key is ever stored in this app.",
                style = MaterialTheme.typography.bodySmall,
                color = NexusWarning
            )

            Text(
                "Voice settings",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(top = 24.dp)
            )
            Text(
                "Voice language and speed options arrive in Phase 3.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
