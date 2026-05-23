package com.kero.anbu.feature.guardian

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kero.anbu.core.data.repository.PairingRepository
import com.kero.anbu.core.model.PairingItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class GuardianHomeUiState(
    val pairings: List<PairingItem> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class GuardianHomeViewModel @Inject constructor(
    private val pairingRepository: PairingRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(GuardianHomeUiState())
    val uiState: StateFlow<GuardianHomeUiState> = _uiState

    init { loadPairings() }

    fun loadPairings() {
        viewModelScope.launch {
            _uiState.value = GuardianHomeUiState(isLoading = true)
            try {
                val list = pairingRepository.getPairingList()
                _uiState.value = GuardianHomeUiState(pairings = list)
            } catch (e: Exception) {
                _uiState.value = GuardianHomeUiState(error = e.message)
            }
        }
    }
}
