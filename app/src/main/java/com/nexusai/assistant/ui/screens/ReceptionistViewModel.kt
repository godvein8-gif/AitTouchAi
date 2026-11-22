package com.nexusai.assistant.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nexusai.assistant.data.local.SettingsStore
import com.nexusai.assistant.data.repository.CallRepository
import com.nexusai.assistant.domain.model.CallRecord
import com.nexusai.assistant.domain.model.CallStatus
import com.nexusai.assistant.service.voice.CallSimulator
import com.nexusai.assistant.service.voice.TranscriptLine
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface SimState {
    data object Idle : SimState
    data class Ringing(val callerLabel: String) : SimState
    data class Active(val callerLabel: String, val lines: List<TranscriptLine>) : SimState
}

class ReceptionistViewModel(
    private val settings: SettingsStore,
    private val calls: CallRepository
) : ViewModel() {

    val enabled: StateFlow<Boolean> = settings.settings
        .map { it.receptionistEnabled }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val recentCalls: StateFlow<List<CallRecord>> = calls.observeAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _sim = MutableStateFlow<SimState>(SimState.Idle)
    val sim: StateFlow<SimState> = _sim.asStateFlow()

    private var simJob: Job? = null
    private var simCounter = 0

    fun setEnabled(value: Boolean) {
        if (!value) {
            simJob?.cancel()
            _sim.value = SimState.Idle
        }
        viewModelScope.launch { settings.setReceptionistEnabled(value) }
    }

    /** DEMO only: plays a scripted fake call and saves its summary and callback message. */
    fun simulateCall() {
        if (!enabled.value || simJob?.isActive == true) return
        val scenario = CallSimulator.scenario(simCounter++)
        simJob = viewModelScope.launch {
            val startedAt = System.currentTimeMillis()
            _sim.value = SimState.Ringing(scenario.callerLabel)
            delay(1500)

            val shown = mutableListOf<TranscriptLine>()
            for (line in scenario.script) {
                shown += line
                _sim.value = SimState.Active(scenario.callerLabel, shown.toList())
                delay(1400)
            }

            calls.save(
                CallRecord(
                    callerLabel = scenario.callerLabel,
                    startedAt = startedAt,
                    durationSec = ((System.currentTimeMillis() - startedAt) / 1000).toInt(),
                    status = CallStatus.Completed,
                    summary = scenario.summary,
                    callbackMessage = scenario.callbackMessage,
                    isDemo = true
                )
            )
            _sim.value = SimState.Idle
        }
    }
}
