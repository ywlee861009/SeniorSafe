package com.kero.anbu.feature.login

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kero.anbu.core.ui.component.SeniorPrimaryButton

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    onRegisterSuccess: () -> Unit,
    onBack: () -> Unit,
    viewModel: RegisterViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var name     by remember { mutableStateOf("") }
    var phone    by remember { mutableStateOf("") }
    var email    by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isSenior by remember { mutableStateOf(true) }

    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) onRegisterSuccess()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("회원가입") },
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
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(Modifier.height(8.dp))

            listOf(
                Triple("이름", name, KeyboardType.Text) to { v: String -> name = v },
                Triple("전화번호", phone, KeyboardType.Phone) to { v: String -> phone = v },
                Triple("이메일", email, KeyboardType.Email) to { v: String -> email = v },
            ).forEach { (info, setter) ->
                val (label, value, kbType) = info
                OutlinedTextField(
                    value         = value,
                    onValueChange = setter,
                    label         = { Text(label, fontSize = 20.sp) },
                    modifier      = Modifier.fillMaxWidth().height(72.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = kbType),
                    textStyle     = LocalTextStyle.current.copy(fontSize = 20.sp),
                    singleLine    = true
                )
            }

            OutlinedTextField(
                value                = password,
                onValueChange        = { password = it },
                label                = { Text("비밀번호", fontSize = 20.sp) },
                modifier             = Modifier.fillMaxWidth().height(72.dp),
                visualTransformation = PasswordVisualTransformation(),
                textStyle            = LocalTextStyle.current.copy(fontSize = 20.sp),
                singleLine           = true
            )

            // 타입 선택
            Column {
                Text("가입 유형", fontSize = 18.sp, fontWeight = FontWeight.Medium)
                Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                    RadioButton(selected = isSenior, onClick = { isSenior = true })
                    Text("어르신으로 가입", fontSize = 20.sp, modifier = Modifier.padding(start = 8.dp))
                }
                Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                    RadioButton(selected = !isSenior, onClick = { isSenior = false })
                    Text("보호자로 가입", fontSize = 20.sp, modifier = Modifier.padding(start = 8.dp))
                }
            }

            Spacer(Modifier.height(8.dp))
            SeniorPrimaryButton(
                text    = "가입하기",
                onClick = {
                    viewModel.register(email, password, name, phone, if (isSenior) "senior" else "guardian")
                },
                enabled = !uiState.isLoading
            )

            uiState.error?.let {
                Text(it, color = MaterialTheme.colorScheme.error, fontSize = 17.sp)
            }
            Spacer(Modifier.height(16.dp))
        }
    }
}
