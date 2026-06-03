package com.kero.anbu.feature.guardian

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kero.anbu.core.data.repository.PairingRepository
import com.kero.anbu.core.model.InactivityAlertItem
import com.kero.anbu.core.model.PairingItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class GuardianHomeUiState(
    val pairings: List<PairingItem> = emptyList(),
    val latestAlertsBySeniorId: Map<String, InactivityAlertItem> = emptyMap(),
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
                val alerts = loadLatestAlerts(list)
                _uiState.value = GuardianHomeUiState(
                    pairings = list,
                    latestAlertsBySeniorId = alerts
                )
            } catch (e: Exception) {
                _uiState.value = GuardianHomeUiState(error = e.message)
            }
        }
    }

    private suspend fun loadLatestAlerts(
        pairings: List<PairingItem>
    ): Map<String, InactivityAlertItem> = coroutineScope {
        pairings
            .mapNotNull { it.seniorDeviceId }
            .distinct()
            .map { seniorDeviceId ->
                async {
                    runCatching {
                        pairingRepository.getInactivityAlerts(
                            seniorDeviceId = seniorDeviceId,
                            limit = 1
                        ).firstOrNull()
                    }.getOrNull()?.let { seniorDeviceId to it }
                }
            }
            .awaitAll()
            .filterNotNull()
            .toMap()
    }
}
