package com.seniorsafe.feature.mvp.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.seniorsafe.feature.mvp.MvpDashboardScreen
import com.seniorsafe.feature.mvp.MvpFallAlertScreen

const val mvpDashboardRoute = "mvp_dashboard"
const val mvpFallAlertRoute = "mvp_fall_alert"

fun NavGraphBuilder.mvpGraph(
    onFallDetected: () -> Unit,
    onFallAlertDismiss: () -> Unit
) {
    composable(mvpDashboardRoute) {
        MvpDashboardScreen(onFallDetected = onFallDetected)
    }
    composable(mvpFallAlertRoute) {
        MvpFallAlertScreen(onDismiss = onFallAlertDismiss)
    }
}
