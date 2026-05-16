package com.seniorsafe.core.falldetection.service

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
import com.seniorsafe.core.diagnostics.DiagnosticsLogStore
import com.seniorsafe.core.diagnostics.diagnosticsLogStore
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@AndroidEntryPoint
class FallDetectionService : Service() {

    companion object {
        const val CHANNEL_ID = "fall_detection_channel"
        const val NOTIFICATION_ID = 2001

        fun start(context: Context) {
            context.diagnosticsLogStore().add("FallService", "start requested")
            context.startForegroundService(
                Intent(context, FallDetectionService::class.java)
            )
        }

        fun stop(context: Context) {
            context.diagnosticsLogStore().add("FallService", "stop requested")
            context.stopService(
                Intent(context, FallDetectionService::class.java)
            )
        }
    }

    @Inject lateinit var fallEventBus: FallEventBus
    @Inject lateinit var diagnosticsLogStore: DiagnosticsLogStore
    @Inject lateinit var serviceStateStore: FallDetectionServiceStateStore

    private lateinit var fallDetectionManager: FallDetectionManager
    private lateinit var wakeLock: PowerManager.WakeLock
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private var heartbeatJob: Job? = null

    override fun onCreate() {
        super.onCreate()
        diagnosticsLogStore.add("FallService", "onCreate")
        startHeartbeat()

        val powerManager = getSystemService(POWER_SERVICE) as PowerManager
        wakeLock = powerManager.newWakeLock(
            PowerManager.PARTIAL_WAKE_LOCK,
            "SeniorSafe:FallDetection"
        ).apply { acquire() }
        diagnosticsLogStore.add("FallService", "partial wake lock acquired")

        val sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        fallDetectionManager = FallDetectionManager(sensorManager, diagnosticsLogStore) {
            fallEventBus.publishFallDetected()
        }

        createNotificationChannel()
        diagnosticsLogStore.add("FallService", "notification channel ready")
        startForeground(
            NOTIFICATION_ID,
            buildNotification(),
            ServiceInfo.FOREGROUND_SERVICE_TYPE_HEALTH
        )
        diagnosticsLogStore.add("FallService", "foreground service started")
        fallDetectionManager.start()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        diagnosticsLogStore.add("FallService", "onStartCommand flags=$flags startId=$startId")
        serviceStateStore.recordHeartbeat("service onStartCommand")
        return START_STICKY
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        diagnosticsLogStore.add("FallService", "task removed; scheduling restart")
        val restartIntent = Intent(this, FallDetectionService::class.java).apply {
            setPackage(packageName)
        }
        val pendingIntent = PendingIntent.getService(
            this,
            1,
            restartIntent,
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

    override fun onTrimMemory(level: Int) {
        diagnosticsLogStore.add("FallService", "onTrimMemory level=$level")
        super.onTrimMemory(level)
    }

    override fun onDestroy() {
        diagnosticsLogStore.add("FallService", "onDestroy")
        heartbeatJob?.cancel()
        serviceStateStore.markStopped("service onDestroy")
        fallDetectionManager.stop()
        if (wakeLock.isHeld) {
            wakeLock.release()
            diagnosticsLogStore.add("FallService", "partial wake lock released")
        }
        serviceScope.cancel()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun buildNotification(): Notification =
        NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("SeniorSafe")
            .setContentText("낙상 감지 서비스 실행 중")
            .setSmallIcon(android.R.drawable.ic_menu_compass)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()

    private fun createNotificationChannel() {
        NotificationChannel(CHANNEL_ID, "낙상 감지", NotificationManager.IMPORTANCE_LOW)
            .also {
                getSystemService(NotificationManager::class.java)
                    .createNotificationChannel(it)
            }
    }

    private fun startHeartbeat() {
        heartbeatJob?.cancel()
        heartbeatJob = serviceScope.launch {
            while (true) {
                serviceStateStore.recordHeartbeat("service heartbeat")
                delay(FallDetectionServiceStateStore.HEARTBEAT_INTERVAL_MS)
            }
        }
    }
}
