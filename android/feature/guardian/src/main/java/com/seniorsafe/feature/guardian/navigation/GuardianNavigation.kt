package com.seniorsafe.feature.guardian.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.seniorsafe.feature.guardian.ConnectSeniorScreen
import com.seniorsafe.feature.guardian.GuardianHomeScreen

const val guardianHomeRoute  = "guardian_home"
const val connectSeniorRoute = "connect_senior"

fun NavGraphBuilder.guardianGraph(
    onNavigateToConnect: () -> Unit,
    onConnectSuccess: () -> Unit
) {
    composable(guardianHomeRoute) {
        GuardianHomeScreen(
            onNavigateToConnect = onNavigateToConnect
        )
    }
    composable(connectSeniorRoute) {
        ConnectSeniorScreen(
            onConnectSuccess = onConnectSuccess,
            onBack           = onConnectSuccess
        )
    }
}
