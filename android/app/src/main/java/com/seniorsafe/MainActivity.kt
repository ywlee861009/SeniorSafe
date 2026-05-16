package com.seniorsafe

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.rememberNavController
import com.seniorsafe.core.activity.service.ActivityMonitorController
import com.seniorsafe.core.datastore.TokenDataStore
import com.seniorsafe.core.ui.theme.SeniorSafeTheme
import com.seniorsafe.navigation.AppNavHost
import com.seniorsafe.navigation.Route
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject lateinit var tokenDataStore: TokenDataStore
    @Inject lateinit var activityMonitorController: ActivityMonitorController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        lifecycleScope.launch {
            activityMonitorController.ensureServiceRunning("app opened")
        }

        setContent {
            SeniorSafeTheme {
                // MVP: 로그인 스킵, 바로 낙상감지 대시보드로
                val navController = rememberNavController()
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .statusBarsPadding()
                        .navigationBarsPadding()
                ) {
                    AppNavHost(
                        navController = navController,
                        startDestination = Route.MVP_DASHBOARD
                    )
                }
            }
        }
    }
}
