package com.kero.anbu.feature.guardian.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.kero.anbu.feature.guardian.ConnectSeniorScreen
import com.kero.anbu.feature.guardian.GuardianHomeScreen

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
