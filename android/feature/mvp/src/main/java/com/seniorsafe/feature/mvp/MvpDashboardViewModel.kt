package com.seniorsafe.feature.mvp

import androidx.lifecycle.ViewModel
import com.seniorsafe.feature.mvp.service.MvpFallEventBus
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

data class MvpDashboardUiState(
    val isServiceRunning: Boolean = false
)

@HiltViewModel
class MvpDashboardViewModel @Inject constructor(
    private val fallEventBus: MvpFallEventBus
) : ViewModel() {

    private val _uiState = MutableStateFlow(MvpDashboardUiState())
    val uiState: StateFlow<MvpDashboardUiState> = _uiState

    val fallDetectedEvent = fallEventBus.fallDetectedEvent

    fun setServiceRunning(running: Boolean) {
        _uiState.value = _uiState.value.copy(isServiceRunning = running)
    }
}
