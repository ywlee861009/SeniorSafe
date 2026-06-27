package com.kero.anbu.feature.guardian

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kero.anbu.core.data.repository.PairingRepository
import com.kero.anbu.core.model.ActivityEventItem
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

/** 보호자 홈에 표시할 어르신 1명 — 페어링 정보 + 가장 최근 활동 + 가장 최근 미사용 알림. */
data class SeniorRow(
    val pairing: PairingItem,
    val lastActivity: ActivityEventItem? = null,
    val latestAlert: InactivityAlertItem? = null
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
                val rows = loadRows(pairings)
                _uiState.value = GuardianHomeUiState(rows = rows)
            } catch (e: Exception) {
                _uiState.value = GuardianHomeUiState(error = e.message)
            }
        }
    }

    /** 각 어르신의 최근 활동·최근 미사용 알림을 병렬 로드한다. 개별 실패는 null로 흡수. */
    private suspend fun loadRows(
        pairings: List<PairingItem>
    ): List<SeniorRow> = coroutineScope {
        pairings.map { pairing ->
            async {
                val seniorId = pairing.seniorDeviceId
                val lastActivity = seniorId?.let { id ->
                    runCatching { pairingRepository.getLatestActivity(id) }.getOrNull()
                }
                val latestAlert = seniorId?.let { id ->
                    runCatching {
                        pairingRepository.getInactivityAlerts(seniorDeviceId = id, limit = 1)
                            .firstOrNull()
                    }.getOrNull()
                }
                SeniorRow(
                    pairing = pairing,
                    lastActivity = lastActivity,
                    latestAlert = latestAlert
                )
            }
        }.awaitAll()
    }
}
