package com.seniorsafe.feature.guardian

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.seniorsafe.core.data.repository.FallRepository
import com.seniorsafe.core.model.FallHistoryItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class FallHistoryUiState(
    val events: List<FallHistoryItem> = emptyList(),
    val isLoading: Boolean = false
)

@HiltViewModel
class FallHistoryViewModel @Inject constructor(
    private val fallRepository: FallRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val seniorId: String = checkNotNull(savedStateHandle["seniorId"])

    private val _uiState = MutableStateFlow(FallHistoryUiState())
    val uiState: StateFlow<FallHistoryUiState> = _uiState

    init { loadHistory() }

    private fun loadHistory() {
        viewModelScope.launch {
            _uiState.value = FallHistoryUiState(isLoading = true)
            try {
                val events = fallRepository.getFallHistory(seniorId)
                _uiState.value = FallHistoryUiState(events = events)
            } catch (_: Exception) {
                _uiState.value = FallHistoryUiState()
            }
        }
    }
}
