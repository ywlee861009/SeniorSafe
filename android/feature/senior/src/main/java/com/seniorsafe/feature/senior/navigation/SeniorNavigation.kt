package com.seniorsafe.feature.senior.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.seniorsafe.feature.senior.FallAlertScreen
import com.seniorsafe.feature.senior.PairingCodeScreen
import com.seniorsafe.feature.senior.SeniorHomeScreen

const val seniorHomeRoute  = "senior_home"
const val pairingCodeRoute = "pairing_code"
const val fallAlertRoute   = "fall_alert"

fun NavGraphBuilder.seniorGraph(
    onNavigateToPairingCode: () -> Unit,
    onFallDetected: () -> Unit,
    onFallAlertDismiss: () -> Unit
) {
    composable(seniorHomeRoute) {
        SeniorHomeScreen(
            onNavigateToPairingCode = onNavigateToPairingCode,
            onFallDetected          = onFallDetected
        )
    }
    composable(pairingCodeRoute) {
        PairingCodeScreen(onBack = onNavigateToPairingCode)
    }
    composable(fallAlertRoute) {
        FallAlertScreen(onDismiss = onFallAlertDismiss)
    }
}
