package com.kero.anbu.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.kero.anbu.feature.onboarding.RoleSelectScreen
import com.kero.anbu.feature.guardian.navigation.guardianGraph
import com.kero.anbu.feature.guardian.navigation.inactivityAlertsRoute
import com.kero.anbu.feature.login.navigation.loginGraph
import com.kero.anbu.feature.mvp.navigation.mvpGraph
import com.kero.anbu.feature.senior.navigation.seniorGraph

object Route {
    const val ROLE_SELECT  = "role_select"
    const val LOGIN        = "login"
    const val REGISTER     = "register"
    const val SENIOR_HOME  = "senior_home"
    const val PAIRING_CODE = "pairing_code"
    const val TODAY_MESSAGE = "today_message"
    const val GUARDIAN_HOME    = "guardian_home"
    const val CONNECT_SENIOR   = "connect_senior"
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
            onTodayMessageBack      = { navController.popBackStack() }
        )

        guardianGraph(
            onNavigateToConnect     = { navController.navigate(Route.CONNECT_SENIOR) },
            onNavigateToAlerts      = { item ->
                item.seniorDeviceId?.let { seniorDeviceId ->
                    navController.navigate(
                        inactivityAlertsRoute(
                            seniorDeviceId = seniorDeviceId,
                            seniorName = item.seniorName
                        )
                    )
                }
            },
            onConnectSuccess        = { navController.popBackStack() }
        )

        mvpGraph()
    }
}
