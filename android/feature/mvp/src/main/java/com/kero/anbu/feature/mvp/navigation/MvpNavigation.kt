package com.kero.anbu.feature.mvp.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.kero.anbu.feature.mvp.MvpDashboardScreen

const val mvpDashboardRoute = "mvp_dashboard"

fun NavGraphBuilder.mvpGraph() {
    composable(mvpDashboardRoute) {
        MvpDashboardScreen()
    }
}
