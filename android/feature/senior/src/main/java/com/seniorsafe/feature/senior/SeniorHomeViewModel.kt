package com.seniorsafe.feature.senior

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.seniorsafe.core.data.repository.FallRepository
import com.seniorsafe.core.datastore.TokenDataStore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SeniorHomeUiState(
    val userName: String = "",
    val isServiceRunning: Boolean = false
)

@HiltViewModel
class SeniorHomeViewModel @Inject constructor(
    private val tokenDataStore: TokenDataStore,
    private val fallRepository: FallRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SeniorHomeUiState())
    val uiState: StateFlow<SeniorHomeUiState> = _uiState

    // FallDetectionService → publishFallDetected() → 이 flow → UI 수신
    val fallDetectedEvent = fallRepository.fallDetectedEvent

    init {
        viewModelScope.launch {
            val name = tokenDataStore.getUserName() ?: ""
            _uiState.value = _uiState.value.copy(userName = name)
        }
    }

    fun setServiceRunning(running: Boolean) {
        _uiState.value = _uiState.value.copy(isServiceRunning = running)
    }
}
