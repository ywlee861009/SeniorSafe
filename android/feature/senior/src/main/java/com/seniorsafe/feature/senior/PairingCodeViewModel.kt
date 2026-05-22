package com.seniorsafe.feature.senior

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.seniorsafe.core.data.repository.PairingRepository
import com.seniorsafe.core.datastore.DeviceDataStore
import com.seniorsafe.core.model.PairingStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PairingCodeUiState(
    val code: String = "",
    val expiresInSeconds: Int = 600,
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class PairingCodeViewModel @Inject constructor(
    private val deviceDataStore: DeviceDataStore,
    private val pairingRepository: PairingRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(PairingCodeUiState())
    val uiState: StateFlow<PairingCodeUiState> = _uiState

    init { loadCode() }

    fun loadCode() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                val response = pairingRepository.getPairingCode()
                _uiState.value = PairingCodeUiState(
                    code = response.code,
                    expiresInSeconds = response.expiresInSeconds ?: 600
                )
            } catch (e: Exception) {
                _uiState.value = PairingCodeUiState(error = "연결 코드를 가져오지 못했어요.")
            }
        }
    }

    fun markPaired(onComplete: () -> Unit) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSaving = true)
            try {
                val hasActivePairing = pairingRepository.getPairingList().any { it.active }
                if (hasActivePairing) {
                    deviceDataStore.savePairingStatus(PairingStatus.PAIRED)
                    onComplete()
                } else {
                    _uiState.value = _uiState.value.copy(
                        isSaving = false,
                        error = "아직 보호자 연결이 확인되지 않았어요."
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    error = "연결 상태 확인에 실패했어요."
                )
            }
        }
    }
}
