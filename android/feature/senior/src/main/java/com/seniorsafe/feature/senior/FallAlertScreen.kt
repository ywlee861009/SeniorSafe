package com.seniorsafe.feature.senior

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.seniorsafe.core.ui.component.FallCancelButton
import com.seniorsafe.core.ui.theme.Danger500
import com.seniorsafe.core.ui.theme.Neutral000
import kotlinx.coroutines.delay

@Composable
fun FallAlertScreen(
    onDismiss: () -> Unit,
    viewModel: FallAlertViewModel = hiltViewModel()
) {
    var countdown by remember { mutableIntStateOf(30) }

    LaunchedEffect(Unit) {
        while (countdown > 0) {
            delay(1000L)
            countdown--
        }
        viewModel.reportFall()
        onDismiss()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Danger500),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(horizontal = 24.dp)
        ) {
            Text(
                text       = "낙상이 감지되었습니다",
                fontSize   = 28.sp,
                fontWeight = FontWeight.Bold,
                color      = Neutral000
            )
            Spacer(Modifier.height(48.dp))
            Text(
                text       = "$countdown",
                fontSize   = 60.sp,
                fontWeight = FontWeight.Bold,
                color      = Neutral000
            )
            Spacer(Modifier.height(24.dp))
            Text(
                text     = "${countdown}초 후 보호자에게\n알림을 보냅니다",
                fontSize = 20.sp,
                color    = Neutral000
            )
            Spacer(Modifier.height(48.dp))
            FallCancelButton(
                onClick = {
                    viewModel.cancelFall()
                    onDismiss()
                }
            )
        }
    }
}
