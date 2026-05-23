package com.kero.anbu.feature.mvp

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kero.anbu.core.activity.service.ActivityMonitorController
import com.kero.anbu.core.activity.db.ActivityRepository
import com.kero.anbu.core.activity.db.UnlockEventEntity
import com.kero.anbu.core.activity.service.ActivityServiceStateStore
import com.kero.anbu.core.diagnostics.DiagnosticsLogStore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class MvpDashboardUiState(
    val isServiceRunning: Boolean = false
)

@HiltViewModel
class MvpDashboardViewModel @Inject constructor(
    private val activityMonitorController: ActivityMonitorController,
    private val activityServiceStateStore: ActivityServiceStateStore,
    private val activityRepository: ActivityRepository,
    private val diagnosticsLogStore: DiagnosticsLogStore
) : ViewModel() {

    private val _uiState = MutableStateFlow(MvpDashboardUiState())
    val uiState: StateFlow<MvpDashboardUiState> = _uiState

    val logs = diagnosticsLogStore.entries

    val recentUnlocks: StateFlow<List<UnlockEventEntity>> =
        activityRepository.observeRecentUnlocks(50)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        viewModelScope.launch {
            activityServiceStateStore.isRunning.collect { running ->
                _uiState.value = _uiState.value.copy(isServiceRunning = running)
                diagnosticsLogStore.add("MvpDashboard", "activity service running=$running")
            }
        }
    }

    fun startMonitoring() {
        activityMonitorController.startByUser("dashboard start button")
    }

    fun stopMonitoring() {
        activityMonitorController.stopByUser("dashboard stop button")
    }

    fun refreshServiceState() {
        activityMonitorController.refresh("dashboard requested refresh")
    }

    fun logAction(message: String) {
        diagnosticsLogStore.add("MvpDashboard", message)
    }

    fun clearLogs() {
        diagnosticsLogStore.clear()
    }
}
