package com.kero.anbu

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.kero.anbu.core.datastore.DeviceDataStore
import com.kero.anbu.core.model.DeviceRole
import com.kero.anbu.core.model.LocalDeviceState
import com.kero.anbu.core.model.PairingStatus
import com.kero.anbu.core.ui.theme.AnbuTheme
import com.kero.anbu.navigation.AppNavHost
import com.kero.anbu.navigation.Route
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject lateinit var deviceDataStore: DeviceDataStore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            AnbuTheme {
                val navController = rememberNavController()
                val startDestination by produceState<String?>(initialValue = null) {
                    value = intent.getStringExtra(EXTRA_START_ROUTE)
                        ?: deviceDataStore.getDeviceState().toStartDestination()
                }

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .statusBarsPadding()
                        .navigationBarsPadding(),
                    contentAlignment = Alignment.Center
                ) {
                    if (startDestination == null) {
                        CircularProgressIndicator()
                    } else {
                        AppNavHost(
                            navController = navController,
                            startDestination = startDestination ?: Route.ROLE_SELECT,
                            onRoleSelected = { destination ->
                                navController.navigate(destination) {
                                    popUpTo(Route.ROLE_SELECT) { inclusive = true }
                                }
                            }
                        )
                    }
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
    }

    companion object {
        const val EXTRA_START_ROUTE = "anbu_start_route"
    }
}

private fun LocalDeviceState.toStartDestination(): String =
    when (role) {
        null -> Route.ROLE_SELECT
        DeviceRole.SENIOR ->
            if (pairingStatus == PairingStatus.PAIRED) Route.SENIOR_HOME else Route.PAIRING_CODE
        DeviceRole.GUARDIAN ->
            if (pairingStatus == PairingStatus.PAIRED) Route.GUARDIAN_HOME else Route.CONNECT_SENIOR
    }
