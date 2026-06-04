package com.kero.anbu.feature.guardian

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kero.anbu.core.data.repository.PairingRepository
import com.kero.anbu.core.model.ActivityEventItem
import com.kero.anbu.core.model.PairingItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/** 보호자 홈에 표시할 어르신 1명 — 페어링 정보 + 가장 최근 활동. */
data class SeniorRow(
    val pairing: PairingItem,
    val lastActivity: ActivityEventItem? = null
)

data class GuardianHomeUiState(
    val rows: List<SeniorRow> = emptyList(),
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
                val pairings = pairingRepository.getPairingList()
                val rows = pairings.map { pairing ->
                    async {
                        val latest = pairing.seniorDeviceId?.let { seniorId ->
                            runCatching { pairingRepository.getLatestActivity(seniorId) }
                                .getOrNull()
                        }
                        SeniorRow(pairing = pairing, lastActivity = latest)
                    }
                }.awaitAll()
                _uiState.value = GuardianHomeUiState(rows = rows)
            } catch (e: Exception) {
                _uiState.value = GuardianHomeUiState(error = e.message)
            }
        }
    }
}
