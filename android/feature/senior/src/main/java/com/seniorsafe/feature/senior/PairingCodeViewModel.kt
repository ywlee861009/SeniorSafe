package com.seniorsafe.feature.senior

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.seniorsafe.core.data.repository.PairingRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PairingCodeUiState(
    val code: String = "",
    val expiresAt: String = "",
    val isLoading: Boolean = false
)

@HiltViewModel
class PairingCodeViewModel @Inject constructor(
    private val pairingRepository: PairingRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(PairingCodeUiState())
    val uiState: StateFlow<PairingCodeUiState> = _uiState

    init { loadCode() }

    fun loadCode() {
        viewModelScope.launch {
            _uiState.value = PairingCodeUiState(isLoading = true)
            try {
                val res = pairingRepository.getPairingCode()
                _uiState.value = PairingCodeUiState(code = res.code, expiresAt = res.expiresAt)
            } catch (_: Exception) {
                _uiState.value = PairingCodeUiState(code = "오류")
            }
        }
    }
}
