package com.seniorsafe.feature.login.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.seniorsafe.feature.login.LoginScreen
import com.seniorsafe.feature.login.RegisterScreen

const val loginRoute    = "login"
const val registerRoute = "register"

fun NavGraphBuilder.loginGraph(
    onLoginSuccess: (userType: String) -> Unit,
    onNavigateToRegister: () -> Unit,
    onBack: () -> Unit = {}
) {
    composable(loginRoute) {
        LoginScreen(
            onLoginSuccess       = onLoginSuccess,
            onNavigateToRegister = onNavigateToRegister
        )
    }
    composable(registerRoute) {
        RegisterScreen(
            onRegisterSuccess = onBack,
            onBack            = onBack
        )
    }
}
