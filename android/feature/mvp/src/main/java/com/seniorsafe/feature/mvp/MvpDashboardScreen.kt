package com.seniorsafe.feature.mvp

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.seniorsafe.core.ui.component.SeniorOutlinedButton
import com.seniorsafe.core.ui.component.SeniorPrimaryButton
import com.seniorsafe.core.ui.theme.Neutral400
import com.seniorsafe.core.ui.theme.Neutral600
import com.seniorsafe.core.ui.theme.Success500
import com.seniorsafe.feature.mvp.service.MvpFallDetectionService

@Composable
fun MvpDashboardScreen(
    onFallDetected: () -> Unit,
    viewModel: MvpDashboardViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    // 낙상 이벤트 수신 → 알림 화면 전환
    LaunchedEffect(Unit) {
        viewModel.fallDetectedEvent.collect { onFallDetected() }
    }

    // 런타임 권한 요청
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { /* 결과 처리 불필요 — 거부해도 서비스 시작 가능 */ }

    LaunchedEffect(Unit) {
        val permissions = buildList {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                add(Manifest.permission.ACTIVITY_RECOGNITION)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                add(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
        if (permissions.isNotEmpty()) {
            permissionLauncher.launch(permissions.toTypedArray())
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "SeniorSafe MVP",
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
        Spacer(Modifier.height(8.dp))
        Text(
            text = if (uiState.isServiceRunning)
                "낙상 감지 서비스가 실행 중입니다"
            else
                "서비스를 켜면 낙상을 감지합니다",
            fontSize = 14.sp,
            color = Neutral600,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(48.dp))

        SeniorPrimaryButton(
            text = if (uiState.isServiceRunning) "서비스 끄기" else "서비스 켜기",
            onClick = {
                if (uiState.isServiceRunning) {
                    MvpFallDetectionService.stop(context)
                } else {
                    MvpFallDetectionService.start(context)
                }
                viewModel.setServiceRunning(!uiState.isServiceRunning)
            }
        )
        Spacer(Modifier.height(16.dp))

        // 배터리 최적화 예외 요청
        SeniorOutlinedButton(
            text = "배터리 최적화 해제",
            onClick = {
                val pm = context.getSystemService(PowerManager::class.java)
                if (!pm.isIgnoringBatteryOptimizations(context.packageName)) {
                    context.startActivity(
                        Intent(
                            Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS,
                            Uri.parse("package:${context.packageName}")
                        )
                    )
                }
            }
        )
    }
}
