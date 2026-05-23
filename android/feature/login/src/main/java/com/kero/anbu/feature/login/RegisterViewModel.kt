package com.kero.anbu.feature.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kero.anbu.core.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class RegisterUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSuccess: Boolean = false
)

@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(RegisterUiState())
    val uiState: StateFlow<RegisterUiState> = _uiState

    fun register(email: String, password: String, name: String, phone: String, userType: String) {
        viewModelScope.launch {
            _uiState.value = RegisterUiState(isLoading = true)
            try {
                authRepository.register(email, password, name, phone, userType)
                _uiState.value = RegisterUiState(isSuccess = true)
            } catch (e: Exception) {
                _uiState.value = RegisterUiState(error = "가입 실패: ${e.message}")
            }
        }
    }
}
