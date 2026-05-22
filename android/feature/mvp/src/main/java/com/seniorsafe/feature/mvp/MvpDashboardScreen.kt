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
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.seniorsafe.core.diagnostics.DiagnosticLogEntry
import com.seniorsafe.core.ui.theme.Neutral400
import com.seniorsafe.core.ui.theme.Neutral600
import com.seniorsafe.core.ui.theme.Success500

@Composable
fun MvpDashboardScreen(
    viewModel: MvpDashboardViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val logs by viewModel.logs.collectAsStateWithLifecycle()
    val unlocks by viewModel.recentUnlocks.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.logAction("MVP dashboard opened")
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        viewModel.logAction("permission result: $result")
    }

    LaunchedEffect(Unit) {
        val permissions = buildList {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                add(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
        if (permissions.isNotEmpty()) {
            viewModel.logAction("requesting runtime permissions: $permissions")
            permissionLauncher.launch(permissions.toTypedArray())
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Service control row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(84.dp)
                .border(1.dp, Neutral400, RoundedCornerShape(8.dp))
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(18.dp)
                    .clip(CircleShape)
                    .background(if (uiState.isServiceRunning) Success500 else Neutral400)
            )
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = if (uiState.isServiceRunning) "모니터링 중" else "모니터링 꺼짐",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "잠금해제 ${unlocks.size}건 기록됨",
                    fontSize = 12.sp,
                    color = Neutral600
                )
            }
            Button(
                onClick = {
                    if (uiState.isServiceRunning) {
                        viewModel.logAction("stop button clicked")
                        viewModel.stopMonitoring()
                    } else {
                        viewModel.logAction("start button clicked")
                        viewModel.startMonitoring()
                    }
                    viewModel.refreshServiceState()
                }
            ) {
                Text(
                    text = if (uiState.isServiceRunning) "끄기" else "켜기",
                    fontSize = 13.sp
                )
            }
            OutlinedButton(
                onClick = {
                    viewModel.logAction("battery optimization button clicked")
                    val pm = context.getSystemService(PowerManager::class.java)
                    if (!pm.isIgnoringBatteryOptimizations(context.packageName)) {
                        context.startActivity(
                            Intent(
                                Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS,
                                Uri.parse("package:${context.packageName}")
                            )
                        )
                    } else {
                        viewModel.logAction("battery optimization already ignored")
                    }
                }
            ) {
                Text(text = "배터리", fontSize = 13.sp)
            }
        }

        // Log header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Runtime Log",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
            OutlinedButton(onClick = { viewModel.clearLogs() }) {
                Text(text = "Clear", fontSize = 12.sp)
            }
        }

        LogConsole(
            logs = logs,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        )
    }
}

@Composable
private fun LogConsole(
    logs: List<DiagnosticLogEntry>,
    modifier: Modifier = Modifier
) {
    if (logs.isEmpty()) {
        Box(
            modifier = modifier
                .background(Color(0xFF111827), RoundedCornerShape(8.dp))
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "로그가 아직 없습니다",
                color = Color(0xFFCBD5E1),
                fontSize = 14.sp,
                textAlign = TextAlign.Center
            )
        }
        return
    }

    LazyColumn(
        modifier = modifier
            .background(Color(0xFF111827), RoundedCornerShape(8.dp))
            .padding(12.dp)
            .fillMaxHeight(),
        reverseLayout = true,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        items(logs.asReversed()) { entry ->
            Row(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = entry.timestamp,
                    color = Color(0xFF93C5FD),
                    fontSize = 11.sp,
                    modifier = Modifier.width(82.dp)
                )
                Text(
                    text = entry.source,
                    color = Color(0xFFFBBF24),
                    fontSize = 11.sp,
                    modifier = Modifier.width(96.dp)
                )
                Text(
                    text = entry.message,
                    color = Color(0xFFE5E7EB),
                    fontSize = 11.sp,
                    lineHeight = 15.sp,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}
