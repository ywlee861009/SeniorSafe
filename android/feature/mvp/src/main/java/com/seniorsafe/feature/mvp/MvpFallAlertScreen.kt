package com.seniorsafe.feature.mvp

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.seniorsafe.core.ui.component.FallCancelButton
import com.seniorsafe.core.ui.theme.Danger500
import com.seniorsafe.core.ui.theme.Neutral000
import kotlinx.coroutines.delay

@Composable
fun MvpFallAlertScreen(
    onDismiss: () -> Unit
) {
    var countdown by remember { mutableIntStateOf(30) }

    LaunchedEffect(Unit) {
        while (countdown > 0) {
            delay(1000L)
            countdown--
        }
        // MVP: API 호출 없이 로컬에서 카운트다운 종료 후 dismiss
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
                text = "낙상이 감지되었습니다",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Neutral000
            )
            Spacer(Modifier.height(48.dp))
            Text(
                text = "$countdown",
                fontSize = 60.sp,
                fontWeight = FontWeight.Bold,
                color = Neutral000
            )
            Spacer(Modifier.height(24.dp))
            Text(
                text = "${countdown}초 후 알림이 전송됩니다",
                fontSize = 20.sp,
                color = Neutral000
            )
            Spacer(Modifier.height(48.dp))
            FallCancelButton(
                onClick = onDismiss
            )
        }
    }
}
