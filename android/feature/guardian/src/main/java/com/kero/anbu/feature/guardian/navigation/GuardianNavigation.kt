package com.kero.anbu.feature.guardian.navigation

import android.net.Uri
import androidx.navigation.NavType
import androidx.navigation.NavGraphBuilder
import androidx.navigation.navArgument
import androidx.navigation.compose.composable
import com.kero.anbu.feature.guardian.ConnectSeniorScreen
import com.kero.anbu.feature.guardian.GuardianHomeScreen
import com.kero.anbu.feature.guardian.InactivityAlertsScreen
import com.kero.anbu.core.model.PairingItem

const val guardianHomeRoute  = "guardian_home"
const val connectSeniorRoute = "connect_senior"
const val inactivityAlertsRoute = "inactivity_alerts"

fun inactivityAlertsRoute(seniorDeviceId: String, seniorName: String): String =
    "$inactivityAlertsRoute/$seniorDeviceId/${Uri.encode(seniorName)}"

fun NavGraphBuilder.guardianGraph(
    onNavigateToConnect: () -> Unit,
    onNavigateToAlerts: (PairingItem) -> Unit,
    onConnectSuccess: () -> Unit
) {
    composable(guardianHomeRoute) {
        GuardianHomeScreen(
            onNavigateToConnect = onNavigateToConnect,
            onNavigateToAlerts = onNavigateToAlerts
        )
    }
    composable(connectSeniorRoute) {
        ConnectSeniorScreen(
            onConnectSuccess = onConnectSuccess,
            onBack           = onConnectSuccess
        )
    }
    composable(
        route = "$inactivityAlertsRoute/{seniorDeviceId}/{seniorName}",
        arguments = listOf(
            navArgument("seniorDeviceId") { type = NavType.StringType },
            navArgument("seniorName") { type = NavType.StringType }
        )
    ) {
        InactivityAlertsScreen(onBack = onConnectSuccess)
    }
}
