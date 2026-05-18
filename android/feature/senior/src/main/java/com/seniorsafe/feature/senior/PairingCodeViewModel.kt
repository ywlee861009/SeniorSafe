package com.seniorsafe.feature.senior

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.seniorsafe.core.datastore.DeviceDataStore
import com.seniorsafe.core.model.PairingStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.Locale
import kotlin.random.Random
import javax.inject.Inject

data class PairingCodeUiState(
    val code: String = "",
    val isSaving: Boolean = false
)

@HiltViewModel
class PairingCodeViewModel @Inject constructor(
    private val deviceDataStore: DeviceDataStore
) : ViewModel() {

    private val _uiState = MutableStateFlow(PairingCodeUiState())
    val uiState: StateFlow<PairingCodeUiState> = _uiState

    init { loadCode() }

    fun loadCode() {
        val code = String.format(Locale.US, "%06d", Random.nextInt(0, 1_000_000))
        _uiState.value = PairingCodeUiState(code = code)
    }

    fun markPaired(onComplete: () -> Unit) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSaving = true)
            deviceDataStore.savePairingStatus(PairingStatus.PAIRED)
            onComplete()
        }
    }
}
