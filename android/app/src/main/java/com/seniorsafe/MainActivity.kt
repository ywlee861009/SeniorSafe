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
                // MVP: 로그인 스킵, 바로 낙상감지 대시보드로
                val navController = rememberNavController()
                AppNavHost(
                    navController    = navController,
                    startDestination = Route.MVP_DASHBOARD
                )
            }
        }
    }
}
