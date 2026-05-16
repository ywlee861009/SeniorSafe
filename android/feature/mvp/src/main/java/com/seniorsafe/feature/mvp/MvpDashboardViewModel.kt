package com.seniorsafe.feature.mvp

import androidx.lifecycle.ViewModel
import com.seniorsafe.core.diagnostics.DiagnosticsLogStore
import com.seniorsafe.core.falldetection.service.FallEventBus
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

data class MvpDashboardUiState(
    val isServiceRunning: Boolean = false
)

@HiltViewModel
class MvpDashboardViewModel @Inject constructor(
    private val fallEventBus: FallEventBus,
    private val diagnosticsLogStore: DiagnosticsLogStore
) : ViewModel() {

    private val _uiState = MutableStateFlow(MvpDashboardUiState())
    val uiState: StateFlow<MvpDashboardUiState> = _uiState

    val fallDetectedEvent = fallEventBus.fallDetectedEvent
    val logs = diagnosticsLogStore.entries

    fun setServiceRunning(running: Boolean) {
        _uiState.value = _uiState.value.copy(isServiceRunning = running)
        diagnosticsLogStore.add("MvpDashboard", "service running ui state changed: running=$running")
    }

    fun logAction(message: String) {
        diagnosticsLogStore.add("MvpDashboard", message)
    }

    fun clearLogs() {
        diagnosticsLogStore.clear()
    }
}
