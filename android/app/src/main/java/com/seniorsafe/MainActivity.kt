package com.seniorsafe

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.navigation.compose.rememberNavController
import com.seniorsafe.core.ui.theme.SeniorSafeTheme
import com.seniorsafe.navigation.AppNavHost
import com.seniorsafe.navigation.Route
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import com.seniorsafe.core.datastore.TokenDataStore
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject lateinit var tokenDataStore: TokenDataStore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        lifecycleScope.launch {
            val userType = tokenDataStore.getUserType()
            val startDestination = when (userType) {
                "senior"   -> Route.SENIOR_HOME
                "guardian" -> Route.GUARDIAN_HOME
                else       -> Route.LOGIN
            }

            setContent {
                SeniorSafeTheme {
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
