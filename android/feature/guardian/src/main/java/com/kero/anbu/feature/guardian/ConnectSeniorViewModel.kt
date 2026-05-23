package com.kero.anbu.feature.guardian

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kero.anbu.core.data.repository.PairingRepository
import com.kero.anbu.core.datastore.DeviceDataStore
import com.kero.anbu.core.model.PairingStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ConnectUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSuccess: Boolean = false,
    val connectedName: String = ""
)

@HiltViewModel
class ConnectSeniorViewModel @Inject constructor(
    private val pairingRepository: PairingRepository,
    private val deviceDataStore: DeviceDataStore
) : ViewModel() {

    private val _uiState = MutableStateFlow(ConnectUiState())
    val uiState: StateFlow<ConnectUiState> = _uiState

    fun connect(code: String) {
        viewModelScope.launch {
            _uiState.value = ConnectUiState(isLoading = true)
            try {
                val res = pairingRepository.connectSenior(code)
                deviceDataStore.savePairingStatus(PairingStatus.PAIRED)
                _uiState.value = ConnectUiState(isSuccess = true, connectedName = res.seniorName)
            } catch (e: Exception) {
                _uiState.value = ConnectUiState(error = "코드를 다시 확인해주세요")
            }
        }
    }
}
