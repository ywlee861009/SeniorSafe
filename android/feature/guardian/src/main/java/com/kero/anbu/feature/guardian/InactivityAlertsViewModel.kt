package com.kero.anbu.feature.guardian

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kero.anbu.core.data.repository.PairingRepository
import com.kero.anbu.core.model.InactivityAlertItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class InactivityAlertsUiState(
    val seniorName: String = "어르신",
    val alerts: List<InactivityAlertItem> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class InactivityAlertsViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val pairingRepository: PairingRepository
) : ViewModel() {

    private val seniorDeviceId: String = checkNotNull(savedStateHandle["seniorDeviceId"])
    private val seniorName: String = savedStateHandle["seniorName"] ?: "어르신"

    private val _uiState = MutableStateFlow(InactivityAlertsUiState(seniorName = seniorName))
    val uiState: StateFlow<InactivityAlertsUiState> = _uiState

    init {
        loadAlerts()
    }

    fun loadAlerts() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                _uiState.value = InactivityAlertsUiState(
                    seniorName = seniorName,
                    alerts = pairingRepository.getInactivityAlerts(seniorDeviceId = seniorDeviceId)
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "알림 이력을 불러오지 못했습니다"
                )
            }
        }
    }
}
