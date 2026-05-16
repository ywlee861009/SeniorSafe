package com.seniorsafe.feature.mvp.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.seniorsafe.feature.mvp.MvpDashboardScreen

const val mvpDashboardRoute = "mvp_dashboard"

fun NavGraphBuilder.mvpGraph() {
    composable(mvpDashboardRoute) {
        MvpDashboardScreen()
    }
}
