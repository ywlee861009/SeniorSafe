package com.kero.anbu.feature.senior.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.kero.anbu.feature.senior.PairingCodeScreen
import com.kero.anbu.feature.senior.SeniorHomeScreen
import com.kero.anbu.feature.senior.TodayMessageScreen

const val seniorHomeRoute  = "senior_home"
const val pairingCodeRoute = "pairing_code"
const val todayMessageRoute = "today_message"

fun NavGraphBuilder.seniorGraph(
    onNavigateToPairingCode: () -> Unit,
    onNavigateToTodayMessage: () -> Unit,
    onPairingComplete: () -> Unit,
    onTodayMessageBack: () -> Unit
) {
    composable(seniorHomeRoute) {
        SeniorHomeScreen(
            onNavigateToPairingCode = onNavigateToPairingCode
        )
    }
    composable(pairingCodeRoute) {
        PairingCodeScreen(onPairingComplete = onPairingComplete)
    }
    composable(todayMessageRoute) {
        TodayMessageScreen(onBack = onTodayMessageBack)
    }
}
