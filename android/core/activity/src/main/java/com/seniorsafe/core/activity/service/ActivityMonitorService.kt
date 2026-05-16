package com.seniorsafe.core.activity.service

import android.app.AlarmManager
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ServiceInfo
import android.os.IBinder
import android.os.PowerManager
import android.os.SystemClock
import androidx.core.app.NotificationCompat
import com.seniorsafe.core.activity.db.ActivityRepository
import com.seniorsafe.core.diagnostics.DiagnosticsLogStore
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
class ActivityMonitorService : Service() {

    companion object {
        const val CHANNEL_ID = "activity_monitor_channel"
        const val NOTIFICATION_ID = 3001
        private const val HEARTBEAT_INTERVAL_MS = 60_000L // 1분 주기

        fun start(context: Context) {
            context.startForegroundService(
                Intent(context, ActivityMonitorService::class.java)
            )
        }

        fun stop(context: Context) {
            context.stopService(
                Intent(context, ActivityMonitorService::class.java)
            )
        }
    }

    @Inject lateinit var activityRepository: ActivityRepository
    @Inject lateinit var diagnosticsLogStore: DiagnosticsLogStore
    @Inject lateinit var serviceStateStore: ActivityServiceStateStore

    private lateinit var wakeLock: PowerManager.WakeLock
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var heartbeatJob: Job? = null
    private var unlockReceiver: BroadcastReceiver? = null

    override fun onCreate() {
        super.onCreate()
        log("onCreate")
        recordEvent("started", "activity monitor service created")

        val powerManager = getSystemService(POWER_SERVICE) as PowerManager
        wakeLock = powerManager.newWakeLock(
            PowerManager.PARTIAL_WAKE_LOCK,
            "SeniorSafe:ActivityMonitor"
        ).apply { acquire() }
        log("partial wake lock acquired")

        createNotificationChannel()
        startForeground(
            NOTIFICATION_ID,
            buildNotification(),
            ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
        )
        log("foreground service started")

        registerUnlockReceiver()
        startHeartbeat()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        log("onStartCommand flags=$flags startId=$startId")
        serviceStateStore.recordHeartbeat("onStartCommand")
        return START_STICKY
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        log("task removed; scheduling restart")
        recordEvent("task_removed", "scheduling alarm restart")
        val restartIntent = Intent(this, ActivityMonitorService::class.java).apply {
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

    override fun onTrimMemory(level: Int) {
        log("onTrimMemory level=$level")
        super.onTrimMemory(level)
    }

    override fun onDestroy() {
        log("onDestroy")
        recordEvent("stopped", "activity monitor service destroyed")
        unregisterUnlockReceiver()
        heartbeatJob?.cancel()
        serviceStateStore.markStopped("onDestroy")
        if (wakeLock.isHeld) {
            wakeLock.release()
            log("partial wake lock released")
        }
        serviceScope.cancel()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun registerUnlockReceiver() {
        unlockReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                if (intent.action == Intent.ACTION_USER_PRESENT) {
                    val now = System.currentTimeMillis()
                    log("ACTION_USER_PRESENT received")
                    serviceScope.launch {
                        activityRepository.recordUnlock(now)
                        val count = activityRepository.unlockCount()
                        log("unlock event recorded (total=$count)")
                    }
                }
            }
        }
        val filter = IntentFilter(Intent.ACTION_USER_PRESENT)
        registerReceiver(unlockReceiver, filter)
        log("unlock receiver registered")
    }

    private fun unregisterUnlockReceiver() {
        unlockReceiver?.let {
            unregisterReceiver(it)
            unlockReceiver = null
            log("unlock receiver unregistered")
        }
    }

    private fun startHeartbeat() {
        heartbeatJob?.cancel()
        heartbeatJob = serviceScope.launch {
            while (true) {
                serviceStateStore.recordHeartbeat("heartbeat")
                delay(HEARTBEAT_INTERVAL_MS)
            }
        }
        log("heartbeat started (interval=${HEARTBEAT_INTERVAL_MS}ms)")
    }

    private fun recordEvent(type: String, detail: String) {
        serviceScope.launch {
            activityRepository.recordServiceEvent(type, detail)
        }
    }

    private fun log(message: String) {
        diagnosticsLogStore.add("ActivityMonitor", message)
    }

    private fun buildNotification(): Notification =
        NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("SeniorSafe")
            .setContentText("활동 모니터링 실행 중")
            .setSmallIcon(android.R.drawable.ic_menu_recent_history)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()

    private fun createNotificationChannel() {
        NotificationChannel(CHANNEL_ID, "활동 모니터링", NotificationManager.IMPORTANCE_LOW)
            .also {
                getSystemService(NotificationManager::class.java)
                    .createNotificationChannel(it)
            }
    }
}
