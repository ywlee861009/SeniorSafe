package com.seniorsafe.feature.senior.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.seniorsafe.feature.senior.FallAlertScreen
import com.seniorsafe.feature.senior.PairingCodeScreen
import com.seniorsafe.feature.senior.SeniorHomeScreen
import com.seniorsafe.feature.senior.TodayMessageScreen

const val seniorHomeRoute  = "senior_home"
const val pairingCodeRoute = "pairing_code"
const val fallAlertRoute   = "fall_alert"
const val todayMessageRoute = "today_message"

fun NavGraphBuilder.seniorGraph(
    onNavigateToPairingCode: () -> Unit,
    onNavigateToTodayMessage: () -> Unit,
    onPairingComplete: () -> Unit,
    onFallDetected: () -> Unit,
    onFallAlertDismiss: () -> Unit,
    onTodayMessageBack: () -> Unit
) {
    composable(seniorHomeRoute) {
        SeniorHomeScreen(
            onNavigateToPairingCode = onNavigateToPairingCode,
            onNavigateToTodayMessage = onNavigateToTodayMessage
        )
    }
    composable(pairingCodeRoute) {
        PairingCodeScreen(onPairingComplete = onPairingComplete)
    }
    composable(fallAlertRoute) {
        FallAlertScreen(onDismiss = onFallAlertDismiss)
    }
    composable(todayMessageRoute) {
        TodayMessageScreen(onBack = onTodayMessageBack)
    }
}
