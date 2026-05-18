package com.seniorsafe.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.seniorsafe.feature.onboarding.RoleSelectScreen
import com.seniorsafe.feature.guardian.navigation.guardianGraph
import com.seniorsafe.feature.login.navigation.loginGraph
import com.seniorsafe.feature.mvp.navigation.mvpGraph
import com.seniorsafe.feature.senior.navigation.seniorGraph

object Route {
    const val ROLE_SELECT  = "role_select"
    const val LOGIN        = "login"
    const val REGISTER     = "register"
    const val SENIOR_HOME  = "senior_home"
    const val PAIRING_CODE = "pairing_code"
    const val FALL_ALERT   = "fall_alert"
    const val TODAY_MESSAGE = "today_message"
    const val GUARDIAN_HOME    = "guardian_home"
    const val CONNECT_SENIOR   = "connect_senior"
    const val FALL_HISTORY     = "fall_history/{seniorId}/{seniorName}"
    const val MVP_DASHBOARD    = "mvp_dashboard"
}

@Composable
fun AppNavHost(
    navController: NavHostController,
    startDestination: String,
    onRoleSelected: (String) -> Unit
) {
    NavHost(
        navController    = navController,
        startDestination = startDestination
    ) {
        composable(Route.ROLE_SELECT) {
            RoleSelectScreen(
                onSeniorSelected = { onRoleSelected(Route.PAIRING_CODE) },
                onGuardianSelected = { onRoleSelected(Route.CONNECT_SENIOR) }
            )
        }

        loginGraph(
            onLoginSuccess = { userType ->
                val dest = if (userType == "senior") Route.SENIOR_HOME else Route.GUARDIAN_HOME
                navController.navigate(dest) {
                    popUpTo(Route.LOGIN) { inclusive = true }
                }
            },
            onNavigateToRegister = { navController.navigate(Route.REGISTER) }
        )

        seniorGraph(
            onNavigateToPairingCode = { navController.navigate(Route.PAIRING_CODE) },
            onNavigateToTodayMessage = { navController.navigate(Route.TODAY_MESSAGE) },
            onPairingComplete       = {
                navController.navigate(Route.SENIOR_HOME) {
                    popUpTo(Route.PAIRING_CODE) { inclusive = true }
                }
            },
            onFallDetected          = { navController.navigate(Route.FALL_ALERT) },
            onFallAlertDismiss      = { navController.popBackStack() },
            onTodayMessageBack      = { navController.popBackStack() }
        )

        guardianGraph(
            onNavigateToConnect     = { navController.navigate(Route.CONNECT_SENIOR) },
            onNavigateToFallHistory = { id, name ->
                navController.navigate("fall_history/$id/$name")
            },
            onConnectSuccess        = { navController.popBackStack() }
        )

        mvpGraph()
    }
}
