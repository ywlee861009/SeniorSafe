package com.kero.anbu.feature.login

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kero.anbu.core.ui.component.SeniorPrimaryButton
import com.kero.anbu.core.ui.theme.Neutral600
import com.kero.anbu.core.ui.theme.Primary500

@Composable
fun LoginScreen(
    onLoginSuccess: (userType: String) -> Unit,
    onNavigateToRegister: () -> Unit,
    viewModel: LoginViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("안부", fontSize = 28.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        Text("어르신 안전 서비스", fontSize = 20.sp, color = Neutral600)
        Spacer(Modifier.height(48.dp))

        OutlinedTextField(
            value         = email,
            onValueChange = { email = it },
            label         = { Text("이메일", fontSize = 20.sp) },
            modifier      = Modifier.fillMaxWidth().height(72.dp),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            textStyle     = LocalTextStyle.current.copy(fontSize = 20.sp),
            singleLine    = true
        )
        Spacer(Modifier.height(16.dp))

        OutlinedTextField(
            value                  = password,
            onValueChange          = { password = it },
            label                  = { Text("비밀번호", fontSize = 20.sp) },
            modifier               = Modifier.fillMaxWidth().height(72.dp),
            visualTransformation   = PasswordVisualTransformation(),
            keyboardOptions        = KeyboardOptions(keyboardType = KeyboardType.Password),
            textStyle              = LocalTextStyle.current.copy(fontSize = 20.sp),
            singleLine             = true
        )
        Spacer(Modifier.height(32.dp))

        SeniorPrimaryButton(
            text    = "로그인",
            onClick = { viewModel.login(email, password, onLoginSuccess) },
            enabled = !uiState.isLoading
        )

        if (uiState.error != null) {
            Spacer(Modifier.height(12.dp))
            Text(uiState.error!!, color = MaterialTheme.colorScheme.error, fontSize = 17.sp)
        }

        Spacer(Modifier.height(24.dp))
        TextButton(onClick = onNavigateToRegister) {
            Text("처음이신가요? 회원가입", fontSize = 20.sp, color = Primary500)
        }
    }
}
