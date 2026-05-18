package com.seniorsafe.feature.senior

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.seniorsafe.core.activity.db.UnlockEventEntity
import com.seniorsafe.core.ui.component.SeniorOutlinedButton
import com.seniorsafe.core.ui.theme.Neutral100
import com.seniorsafe.core.ui.theme.Neutral400
import com.seniorsafe.core.ui.theme.Neutral600
import com.seniorsafe.core.ui.theme.Neutral900
import com.seniorsafe.core.ui.theme.Success500
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun SeniorHomeScreen(
    onNavigateToPairingCode: () -> Unit,
    viewModel: SeniorHomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 28.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        item {
            QuoteCard(message = uiState.message)
        }
        item {
            DebugSectionHeader()
        }
        item {
            ServiceStatusRow(
                isRunning = uiState.isServiceRunning,
                onToggle = {
                    if (uiState.isServiceRunning) viewModel.stopMonitoring()
                    else viewModel.startMonitoring()
                }
            )
        }
        if (uiState.recentEvents.isEmpty()) {
            item {
                Text(
                    text = "아직 기록이 없어요",
                    fontSize = 16.sp,
                    color = Neutral400
                )
            }
        } else {
            items(uiState.recentEvents) { event ->
                EventRow(event)
            }
        }
        item {
            Spacer(modifier = Modifier.height(8.dp))
            SeniorOutlinedButton(
                text = "연결 전으로 돌아가기",
                onClick = { viewModel.resetPairingForDebug(onNavigateToPairingCode) }
            )
        }
    }
}

@Composable
private fun QuoteCard(message: String) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = Neutral100
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 36.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "오늘의 명언",
                fontSize = 16.sp,
                color = Neutral600
            )
            Text(
                text = message,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Neutral900,
                textAlign = TextAlign.Center,
                lineHeight = 40.sp
            )
        }
    }
}

@Composable
private fun DebugSectionHeader() {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        HorizontalDivider(color = Neutral100, thickness = 1.dp)
        Text(
            text = "개발자 정보",
            fontSize = 13.sp,
            color = Neutral400
        )
    }
}

@Composable
private fun ServiceStatusRow(isRunning: Boolean, onToggle: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(text = "모니터링 서비스", fontSize = 13.sp, color = Neutral600)
            Text(
                text = if (isRunning) "켜짐" else "꺼짐",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = if (isRunning) Success500 else Neutral400
            )
        }
        TextButton(onClick = onToggle) {
            Text(
                text = if (isRunning) "끄기" else "켜기",
                color = if (isRunning) Neutral400 else Success500
            )
        }
    }
}

@Composable
private fun EventRow(event: UnlockEventEntity) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = event.toDebugTime(),
            fontSize = 14.sp,
            color = Neutral600,
            fontFamily = FontFamily.Monospace
        )
        Text(
            text = event.source.toSourceLabel(),
            fontSize = 14.sp,
            color = Neutral900
        )
    }
}

private fun UnlockEventEntity.toDebugTime(): String {
    val formatter = SimpleDateFormat("MM/dd HH:mm:ss", Locale.KOREA)
    return formatter.format(Date(unlockedAtMillis))
}

private fun String.toSourceLabel(): String = when (this) {
    "user_present" -> "잠금해제"
    "power_connected" -> "충전기 연결"
    "power_disconnected" -> "충전기 해제"
    else -> this
}
