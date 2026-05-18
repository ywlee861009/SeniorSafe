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
import com.seniorsafe.core.ui.component.SeniorPrimaryButton
import com.seniorsafe.core.ui.component.SeniorOutlinedButton
import com.seniorsafe.core.ui.theme.Neutral100
import com.seniorsafe.core.ui.theme.Neutral600
import com.seniorsafe.core.ui.theme.Neutral900

@Composable
fun PairingCodeScreen(
    onPairingComplete: () -> Unit,
    viewModel: PairingCodeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var timerSec by remember { mutableIntStateOf(600) }

    LaunchedEffect(uiState.code) {
        timerSec = 600
        while (timerSec > 0) {
            kotlinx.coroutines.delay(1000L)
            timerSec--
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "보호자에게 이 코드를\n알려주세요",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = Neutral900,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(16.dp))
        Text(
            text = "보호자 휴대폰에 아래 숫자를 입력하면 연결돼요.",
            fontSize = 18.sp,
            color = Neutral600,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(32.dp))

        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Neutral100,
            shape = RoundedCornerShape(8.dp)
        ) {
            Text(
                text = uiState.code.ifEmpty { "------" },
                fontSize = 48.sp,
                fontWeight = FontWeight.Bold,
                color = Neutral900,
                letterSpacing = 6.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(vertical = 32.dp)
            )
        }

        Spacer(Modifier.height(16.dp))
        val min = timerSec / 60
        val sec = timerSec % 60
        Text(
            text = "10분 동안 사용할 수 있어요  %02d:%02d".format(min, sec),
            fontSize = 18.sp,
            color = Neutral600,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(40.dp))
        SeniorOutlinedButton(text = "새 코드 받기", onClick = { viewModel.loadCode() })
        Spacer(Modifier.height(16.dp))
        SeniorPrimaryButton(
            text = "보호자와 연결됐어요",
            enabled = !uiState.isSaving,
            onClick = { viewModel.markPaired(onPairingComplete) }
        )
    }
}
