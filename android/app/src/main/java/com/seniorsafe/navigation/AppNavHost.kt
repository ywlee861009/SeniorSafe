package com.seniorsafe.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import com.seniorsafe.feature.guardian.navigation.guardianGraph
import com.seniorsafe.feature.login.navigation.loginGraph
import com.seniorsafe.feature.mvp.navigation.mvpFallAlertRoute
import com.seniorsafe.feature.mvp.navigation.mvpGraph
import com.seniorsafe.feature.senior.navigation.seniorGraph

object Route {
    const val LOGIN        = "login"
    const val REGISTER     = "register"
    const val SENIOR_HOME  = "senior_home"
    const val PAIRING_CODE = "pairing_code"
    const val FALL_ALERT   = "fall_alert"
    const val GUARDIAN_HOME    = "guardian_home"
    const val CONNECT_SENIOR   = "connect_senior"
    const val FALL_HISTORY     = "fall_history/{seniorId}/{seniorName}"
    const val MVP_DASHBOARD    = "mvp_dashboard"
    const val MVP_FALL_ALERT   = "mvp_fall_alert"
}

@Composable
fun AppNavHost(
    navController: NavHostController,
    startDestination: String
) {
    NavHost(
        navController    = navController,
        startDestination = startDestination
    ) {
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
            onFallDetected          = { navController.navigate(Route.FALL_ALERT) },
            onFallAlertDismiss      = { navController.popBackStack() }
        )

        guardianGraph(
            onNavigateToConnect     = { navController.navigate(Route.CONNECT_SENIOR) },
            onNavigateToFallHistory = { id, name ->
                navController.navigate("fall_history/$id/$name")
            },
            onConnectSuccess        = { navController.popBackStack() }
        )

        mvpGraph(
            onFallDetected     = { navController.navigate(Route.MVP_FALL_ALERT) },
            onFallAlertDismiss = { navController.popBackStack() }
        )
    }
}
