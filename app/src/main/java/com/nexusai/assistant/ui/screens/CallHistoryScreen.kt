package com.nexusai.assistant.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.nexusai.assistant.data.repository.CallRepository
import com.nexusai.assistant.domain.model.CallRecord
import com.nexusai.assistant.ui.components.CallStatusCard
import com.nexusai.assistant.ui.components.ScreenHeader
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class CallHistoryViewModel(private val repo: CallRepository) : ViewModel() {
    val calls: StateFlow<List<CallRecord>> = repo.observeAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun delete(id: Long) {
        viewModelScope.launch { repo.delete(id) }
    }

    fun deleteAll() {
        viewModelScope.launch { repo.deleteAll() }
    }
}

@Composable
fun CallHistoryScreen(vm: CallHistoryViewModel, onBack: () -> Unit) {
    val calls by vm.calls.collectAsStateWithLifecycle()
    var confirmDeleteAll by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        ScreenHeader(title = "Call History", onBack = onBack) {
            if (calls.isNotEmpty()) {
                IconButton(onClick = { confirmDeleteAll = true }) {
                    Icon(Icons.Filled.Delete, contentDescription = "Delete all calls")
                }
            }
        }

        if (calls.isEmpty()) {
            Text(
                "No calls yet. Use the simulator on the Call Receptionist screen to create a DEMO call.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(24.dp)
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(calls, key = { it.id }) { call ->
                    Column(horizontalAlignment = Alignment.End) {
                        CallStatusCard(call)
                        TextButton(onClick = { vm.delete(call.id) }) { Text("Delete") }
                    }
                }
            }
        }
    }

    if (confirmDeleteAll) {
        AlertDialog(
            onDismissRequest = { confirmDeleteAll = false },
            title = { Text("Delete all call history?") },
            text = { Text("Summaries and callback messages will be removed from this device.") },
            confirmButton = {
                TextButton(onClick = {
                    vm.deleteAll()
                    confirmDeleteAll = false
                }) { Text("Delete all") }
            },
            dismissButton = {
                TextButton(onClick = { confirmDeleteAll = false }) { Text("Cancel") }
            }
        )
    }
}
