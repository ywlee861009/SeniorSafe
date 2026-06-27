package com.kero.anbu.feature.senior

import android.Manifest
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kero.anbu.core.activity.db.UnlockEventEntity
import com.kero.anbu.core.ui.component.SeniorOutlinedButton
import com.kero.anbu.core.ui.theme.Neutral100
import com.kero.anbu.core.ui.theme.Neutral400
import com.kero.anbu.core.ui.theme.Neutral600
import com.kero.anbu.core.ui.theme.Neutral900
import com.kero.anbu.core.ui.theme.Success500
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SeniorHomeScreen(
    onNavigateToPairingCode: () -> Unit,
    viewModel: SeniorHomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var backPressedTime by remember { mutableLongStateOf(0L) }
    var menuExpanded by remember { mutableStateOf(false) }
    var showDebugSheet by remember { mutableStateOf(false) }

    BackHandler {
        val now = System.currentTimeMillis()
        if (now - backPressedTime < 2000) {
            (context as? android.app.Activity)?.finish()
        } else {
            backPressedTime = now
            Toast.makeText(context, "한 번 더 누르면 종료됩니다", Toast.LENGTH_SHORT).show()
        }
    }

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { /* 결과 무관 — 허용 여부는 시스템이 처리 */ }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("안부") },
                navigationIcon = {
                    Box {
                        IconButton(onClick = { menuExpanded = true }) {
                            Icon(Icons.Default.Menu, contentDescription = "메뉴")
                        }
                        DropdownMenu(
                            expanded = menuExpanded,
                            onDismissRequest = { menuExpanded = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("개발자 정보") },
                                onClick = {
                                    menuExpanded = false
                                    showDebugSheet = true
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("다시 페어링") },
                                onClick = {
                                    menuExpanded = false
                                    viewModel.resetPairingForDebug(onNavigateToPairingCode)
                                }
                            )
                        }
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp, vertical = 28.dp),
            contentAlignment = Alignment.Center
        ) {
            QuoteCard(message = uiState.message)
        }
    }

    if (showDebugSheet) {
        ModalBottomSheet(onDismissRequest = { showDebugSheet = false }) {
            DebugSheetContent(
                isServiceRunning = uiState.isServiceRunning,
                recentEvents = uiState.recentEvents,
                onToggleService = {
                    if (uiState.isServiceRunning) viewModel.stopMonitoring()
                    else viewModel.startMonitoring()
                }
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
                text = "오늘의 이야기",
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
private fun DebugSheetContent(
    isServiceRunning: Boolean,
    recentEvents: List<UnlockEventEntity>,
    onToggleService: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .padding(bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            DebugSectionHeader()
        }
        item {
            ServiceStatusRow(
                isRunning = isServiceRunning,
                onToggle = onToggleService
            )
        }
        if (recentEvents.isEmpty()) {
            item {
                Text(
                    text = "아직 기록이 없어요",
                    fontSize = 16.sp,
                    color = Neutral400
                )
            }
        } else {
            items(recentEvents) { event ->
                EventRow(event)
            }
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
