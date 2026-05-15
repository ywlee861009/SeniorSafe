package com.seniorsafe.feature.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.seniorsafe.core.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LoginUiState(
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState

    fun login(email: String, password: String, onSuccess: (userType: String) -> Unit) {
        viewModelScope.launch {
            _uiState.value = LoginUiState(isLoading = true)
            try {
                val res = authRepository.login(email, password)
                onSuccess(res.userType)
            } catch (e: Exception) {
                _uiState.value = LoginUiState(error = "로그인 실패. 이메일과 비밀번호를 확인해주세요")
            }
        }
    }
}
