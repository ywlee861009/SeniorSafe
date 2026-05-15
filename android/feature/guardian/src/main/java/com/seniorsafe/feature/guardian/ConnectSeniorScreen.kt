package com.seniorsafe.feature.guardian

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.seniorsafe.core.ui.component.GuardianPrimaryButton
import com.seniorsafe.core.ui.theme.Neutral600

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConnectSeniorScreen(
    onConnectSuccess: () -> Unit,
    onBack: () -> Unit,
    viewModel: ConnectSeniorViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var code by remember { mutableStateOf("") }

    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) onConnectSuccess()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("어르신 연결하기") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "뒤로")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text      = "어르신 앱에서 발급한\n6자리 코드를 입력해주세요",
                fontSize  = 16.sp,
                color     = Neutral600,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(32.dp))

            OutlinedTextField(
                value         = code,
                onValueChange = { if (it.length <= 6) code = it.uppercase() },
                label         = { Text("6자리 코드") },
                modifier      = Modifier.fillMaxWidth(),
                singleLine    = true,
                textStyle     = LocalTextStyle.current.copy(fontSize = 24.sp, textAlign = TextAlign.Center)
            )
            Spacer(Modifier.height(24.dp))

            GuardianPrimaryButton(
                text    = "연결하기",
                onClick = { if (code.length == 6) viewModel.connect(code) },
                enabled = !uiState.isLoading && code.length == 6
            )

            uiState.error?.let {
                Spacer(Modifier.height(12.dp))
                Text(it, color = MaterialTheme.colorScheme.error)
            }
        }
    }
}
