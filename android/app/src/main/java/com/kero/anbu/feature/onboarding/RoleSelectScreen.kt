package com.kero.anbu.feature.onboarding

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.kero.anbu.core.data.repository.DeviceRepository
import com.kero.anbu.core.model.DeviceRole
import com.kero.anbu.core.ui.component.SeniorOutlinedButton
import com.kero.anbu.core.ui.component.SeniorPrimaryButton
import com.kero.anbu.core.ui.theme.Neutral600
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class RoleSelectUiState(
    val isSaving: Boolean = false,
    val error: String? = null
)

@Composable
fun RoleSelectScreen(
    onSeniorSelected: () -> Unit,
    onGuardianSelected: () -> Unit,
    viewModel: RoleSelectViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "누가 이 휴대폰을 사용하나요?",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "선택한 뒤 바로 연결을 시작할 수 있어요.",
            color = Neutral600,
            fontSize = 18.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(48.dp))
        SeniorPrimaryButton(
            text = "어르신이에요",
            enabled = !uiState.isSaving,
            isLoading = uiState.isSaving,
            onClick = {
                viewModel.selectRole(DeviceRole.SENIOR, onSeniorSelected)
            }
        )
        Spacer(modifier = Modifier.height(16.dp))
        SeniorOutlinedButton(
            text = "보호자예요",
            enabled = !uiState.isSaving,
            onClick = {
                viewModel.selectRole(DeviceRole.GUARDIAN, onGuardianSelected)
            }
        )
        uiState.error?.let {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = it,
                color = MaterialTheme.colorScheme.error,
                textAlign = TextAlign.Center
            )
        }
    }
}

@HiltViewModel
class RoleSelectViewModel @Inject constructor(
    private val deviceRepository: DeviceRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(RoleSelectUiState())
    val uiState: StateFlow<RoleSelectUiState> = _uiState

    fun selectRole(role: DeviceRole, onComplete: () -> Unit) {
        viewModelScope.launch {
            _uiState.value = RoleSelectUiState(isSaving = true)
            try {
                deviceRepository.registerCurrentDevice(role)
                onComplete()
            } catch (e: Exception) {
                _uiState.value = RoleSelectUiState(error = "기기 등록에 실패했어요. 서버 연결을 확인해주세요.")
            }
        }
    }
}
