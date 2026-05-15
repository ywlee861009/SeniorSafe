package com.seniorsafe.feature.mvp.service

import android.app.AlarmManager
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.hardware.SensorManager
import android.os.IBinder
import android.os.PowerManager
import android.os.SystemClock
import androidx.core.app.NotificationCompat
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MvpFallDetectionService : Service() {

    companion object {
        const val CHANNEL_ID = "mvp_fall_detection_channel"
        const val NOTIFICATION_ID = 2001

        fun start(context: Context) {
            context.startForegroundService(
                Intent(context, MvpFallDetectionService::class.java)
            )
        }

        fun stop(context: Context) {
            context.stopService(
                Intent(context, MvpFallDetectionService::class.java)
            )
        }
    }

    @Inject lateinit var fallEventBus: MvpFallEventBus

    private lateinit var fallDetectionManager: MvpFallDetectionManager
    private lateinit var wakeLock: PowerManager.WakeLock

    override fun onCreate() {
        super.onCreate()

        // WakeLock — CPU 슬립 방지
        val pm = getSystemService(POWER_SERVICE) as PowerManager
        wakeLock = pm.newWakeLock(
            PowerManager.PARTIAL_WAKE_LOCK,
            "SeniorSafe:MvpFallDetection"
        ).apply { acquire() }

        // 센서 매니저 + 낙상 감지
        val sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        fallDetectionManager = MvpFallDetectionManager(sensorManager) {
            fallEventBus.publishFallDetected()
        }

        createNotificationChannel()
        startForeground(
            NOTIFICATION_ID,
            buildNotification(),
            ServiceInfo.FOREGROUND_SERVICE_TYPE_HEALTH
        )
        fallDetectionManager.start()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        // 앱 스와이프 종료 시 1초 후 서비스 재시작
        val restartIntent = Intent(this, MvpFallDetectionService::class.java).apply {
            setPackage(packageName)
        }
        val pendingIntent = PendingIntent.getService(
            this, 1, restartIntent,
            PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
        )
        val alarmManager = getSystemService(ALARM_SERVICE) as AlarmManager
        alarmManager.set(
            AlarmManager.ELAPSED_REALTIME_WAKEUP,
            SystemClock.elapsedRealtime() + 1000,
            pendingIntent
        )
        super.onTaskRemoved(rootIntent)
    }

    override fun onDestroy() {
        fallDetectionManager.stop()
        if (wakeLock.isHeld) wakeLock.release()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun buildNotification(): Notification =
        NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("SeniorSafe MVP")
            .setContentText("낙상 감지 서비스 실행 중")
            .setSmallIcon(android.R.drawable.ic_menu_compass)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()

    private fun createNotificationChannel() {
        NotificationChannel(CHANNEL_ID, "MVP 낙상 감지", NotificationManager.IMPORTANCE_LOW)
            .also {
                getSystemService(NotificationManager::class.java)
                    .createNotificationChannel(it)
            }
    }
}
