package com.seniorsafe.feature.senior.service

import android.app.*
import android.content.Intent
import android.hardware.SensorManager
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.seniorsafe.core.data.repository.FallRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import javax.inject.Inject

@AndroidEntryPoint
class FallDetectionService : Service() {

    @Inject lateinit var fallRepository: FallRepository

    companion object {
        const val CHANNEL_ID      = "fall_detection_channel"
        const val NOTIFICATION_ID = 1001
    }

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private lateinit var sensorManager: SensorManager
    private lateinit var fallDetectionManager: FallDetectionManager

    override fun onCreate() {
        super.onCreate()
        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        fallDetectionManager = FallDetectionManager(sensorManager) {
            // FallRepository의 SharedFlow로 이벤트 발행 → AppNavHost가 수신해 화면 전환
            fallRepository.publishFallDetected()
        }
        createNotificationChannel()
        startForeground(NOTIFICATION_ID, buildNotification())
        fallDetectionManager.start()
    }

    override fun onDestroy() {
        super.onDestroy()
        fallDetectionManager.stop()
        scope.cancel()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun buildNotification(): Notification =
        NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("SeniorSafe 보호 중")
            .setContentText("낙상 감지 서비스가 실행 중입니다")
            .setSmallIcon(android.R.drawable.ic_menu_compass)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()

    private fun createNotificationChannel() {
        NotificationChannel(CHANNEL_ID, "낙상 감지 서비스", NotificationManager.IMPORTANCE_LOW)
            .also { getSystemService(NotificationManager::class.java).createNotificationChannel(it) }
    }
}
