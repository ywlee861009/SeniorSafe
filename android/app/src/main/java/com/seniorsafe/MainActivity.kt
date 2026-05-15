package com.seniorsafe

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.*
import androidx.navigation.compose.rememberNavController
import com.seniorsafe.core.datastore.TokenDataStore
import com.seniorsafe.core.ui.theme.SeniorSafeTheme
import com.seniorsafe.navigation.AppNavHost
import com.seniorsafe.navigation.Route
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject lateinit var tokenDataStore: TokenDataStore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            SeniorSafeTheme {
                // startDestination을 suspend 값으로 결정해야 하므로 produceState 사용
                val startDestination by produceState(initialValue = "") {
                    value = when (tokenDataStore.getUserType()) {
                        "senior"   -> Route.SENIOR_HOME
                        "guardian" -> Route.GUARDIAN_HOME
                        else       -> Route.LOGIN
                    }
                }

                if (startDestination.isNotEmpty()) {
                    val navController = rememberNavController()
                    AppNavHost(
                        navController    = navController,
                        startDestination = startDestination
                    )
                }
            }
        }
    }
}
