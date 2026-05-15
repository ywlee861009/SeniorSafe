package com.seniorsafe.feature.senior

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.seniorsafe.core.ui.component.SeniorOutlinedButton
import com.seniorsafe.core.ui.component.SeniorPrimaryButton
import com.seniorsafe.core.ui.theme.Neutral600
import com.seniorsafe.core.ui.theme.Success500
import com.seniorsafe.core.ui.theme.Neutral400
import com.seniorsafe.feature.senior.service.FallDetectionService

@Composable
fun SeniorHomeScreen(
    onNavigateToPairingCode: () -> Unit,
    onFallDetected: () -> Unit,
    viewModel: SeniorHomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    // FallDetectionService 이벤트 수신 → 화면 전환
    LaunchedEffect(Unit) {
        viewModel.fallDetectedEvent.collect { onFallDetected() }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "안녕하세요, ${uiState.userName}님",
            fontSize = 20.sp,
            color = Neutral600
        )
        Spacer(Modifier.height(48.dp))

        // 상태 인디케이터
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .background(if (uiState.isServiceRunning) Success500 else Neutral400)
        )
        Spacer(Modifier.height(16.dp))
        Text(
            text = if (uiState.isServiceRunning) "보호 중" else "보호 꺼짐",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(48.dp))

        SeniorPrimaryButton(
            text = if (uiState.isServiceRunning) "서비스 끄기" else "서비스 켜기",
            onClick = {
                if (uiState.isServiceRunning) {
                    context.stopService(Intent(context, FallDetectionService::class.java))
                } else {
                    context.startForegroundService(Intent(context, FallDetectionService::class.java))
                }
                viewModel.setServiceRunning(!uiState.isServiceRunning)
            }
        )
        Spacer(Modifier.height(16.dp))
        SeniorOutlinedButton(
            text    = "연결 코드 보기",
            onClick = onNavigateToPairingCode
        )
    }
}
