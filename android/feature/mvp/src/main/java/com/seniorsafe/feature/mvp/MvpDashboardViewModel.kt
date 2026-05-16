package com.seniorsafe.feature.mvp

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.seniorsafe.core.diagnostics.DiagnosticsLogStore
import com.seniorsafe.core.falldetection.service.FallEventBus
import com.seniorsafe.core.falldetection.service.FallDetectionServiceStateStore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class MvpDashboardUiState(
    val isServiceRunning: Boolean = false
)

@HiltViewModel
class MvpDashboardViewModel @Inject constructor(
    private val fallEventBus: FallEventBus,
    private val serviceStateStore: FallDetectionServiceStateStore,
    private val diagnosticsLogStore: DiagnosticsLogStore
) : ViewModel() {

    private val _uiState = MutableStateFlow(MvpDashboardUiState())
    val uiState: StateFlow<MvpDashboardUiState> = _uiState

    val fallDetectedEvent = fallEventBus.fallDetectedEvent
    val logs = diagnosticsLogStore.entries

    init {
        viewModelScope.launch {
            serviceStateStore.isRunning.collect { running ->
                _uiState.value = _uiState.value.copy(isServiceRunning = running)
                diagnosticsLogStore.add("MvpDashboard", "service running state synced: running=$running")
            }
        }
    }

    fun setServiceRunning(running: Boolean) {
        serviceStateStore.refresh("dashboard requested refresh")
    }

    fun logAction(message: String) {
        diagnosticsLogStore.add("MvpDashboard", message)
    }

    fun clearLogs() {
        diagnosticsLogStore.clear()
    }
}
