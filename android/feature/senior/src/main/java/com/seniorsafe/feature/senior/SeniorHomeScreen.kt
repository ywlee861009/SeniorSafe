package com.seniorsafe.feature.senior

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.seniorsafe.core.ui.component.SeniorOutlinedButton
import com.seniorsafe.core.ui.component.SeniorPrimaryButton
import com.seniorsafe.core.ui.theme.Neutral100
import com.seniorsafe.core.ui.theme.Neutral400
import com.seniorsafe.core.ui.theme.Neutral600
import com.seniorsafe.core.ui.theme.Neutral900
import com.seniorsafe.core.ui.theme.Success500

@Composable
fun SeniorHomeScreen(
    onNavigateToPairingCode: () -> Unit,
    onNavigateToTodayMessage: () -> Unit,
    viewModel: SeniorHomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 28.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "보호자와 연결되어 있어요",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = Neutral900,
            textAlign = TextAlign.Center
        )
        Text(
            text = "휴대폰 사용 기록으로 안부를 확인해요.",
            fontSize = 18.sp,
            color = Neutral600,
            textAlign = TextAlign.Center
        )

        StatusPanel(uiState)

        SeniorPrimaryButton(
            text = if (uiState.isServiceRunning) "사용 기록 확인 끄기" else "사용 기록 확인 켜기",
            onClick = {
                if (uiState.isServiceRunning) {
                    viewModel.stopMonitoring()
                } else {
                    viewModel.startMonitoring()
                }
            }
        )

        SeniorOutlinedButton(
            text = "오늘의 글 보기",
            onClick = onNavigateToTodayMessage
        )

        Spacer(modifier = Modifier.weight(1f))

        Text(
            text = "개발용",
            fontSize = 14.sp,
            color = Neutral600,
            modifier = Modifier.fillMaxWidth()
        )
        SeniorOutlinedButton(
            text = "연결 전으로 돌아가기",
            onClick = {
                viewModel.resetPairingForDebug(onNavigateToPairingCode)
            }
        )
    }
}

@Composable
private fun StatusPanel(uiState: SeniorHomeUiState) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        color = Neutral100
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            StatusRow(
                label = "사용 기록 확인",
                value = if (uiState.isServiceRunning) "켜져 있어요" else "꺼져 있어요",
                valueColor = if (uiState.isServiceRunning) Success500 else Neutral400
            )
            StatusRow(
                label = "최근 휴대폰 사용",
                value = uiState.lastUnlockText
            )
            StatusRow(
                label = "오늘 사용 기록",
                value = "${uiState.todayUnlockCount}번"
            )
            StatusRow(
                label = "저장 상태",
                value = uiState.uploadStatusText
            )
        }
    }
}

@Composable
private fun StatusRow(
    label: String,
    value: String,
    valueColor: androidx.compose.ui.graphics.Color = Neutral900
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = label,
            fontSize = 16.sp,
            color = Neutral600
        )
        Text(
            text = value,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = valueColor
        )
    }
}
