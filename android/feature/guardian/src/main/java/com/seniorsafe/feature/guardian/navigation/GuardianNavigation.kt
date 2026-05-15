package com.seniorsafe.feature.guardian.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.seniorsafe.feature.guardian.ConnectSeniorScreen
import com.seniorsafe.feature.guardian.FallHistoryScreen
import com.seniorsafe.feature.guardian.GuardianHomeScreen

const val guardianHomeRoute  = "guardian_home"
const val connectSeniorRoute = "connect_senior"
const val fallHistoryRoute   = "fall_history/{seniorId}/{seniorName}"

fun NavGraphBuilder.guardianGraph(
    onNavigateToConnect: () -> Unit,
    onNavigateToFallHistory: (seniorId: String, seniorName: String) -> Unit,
    onConnectSuccess: () -> Unit
) {
    composable(guardianHomeRoute) {
        GuardianHomeScreen(
            onNavigateToConnect     = onNavigateToConnect,
            onNavigateToFallHistory = onNavigateToFallHistory
        )
    }
    composable(connectSeniorRoute) {
        ConnectSeniorScreen(
            onConnectSuccess = onConnectSuccess,
            onBack           = onConnectSuccess
        )
    }
    composable(
        route     = fallHistoryRoute,
        arguments = listOf(
            navArgument("seniorId")   { type = NavType.StringType },
            navArgument("seniorName") { type = NavType.StringType }
        )
    ) { backStack ->
        FallHistoryScreen(
            seniorName = backStack.arguments?.getString("seniorName") ?: "",
            onBack     = onConnectSuccess
        )
    }
}
