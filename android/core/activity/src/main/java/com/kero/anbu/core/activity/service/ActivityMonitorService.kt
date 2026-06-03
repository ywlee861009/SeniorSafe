package com.kero.anbu.core.activity.service

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
import com.kero.anbu.core.activity.db.ActivityRepository
import com.kero.anbu.core.diagnostics.DiagnosticsLogStore
import com.kero.anbu.core.util.KeroLog
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
        private const val HEARTBEAT_INTERVAL_MS = 60_000L
        private const val ACTION_REATTACH = "com.kero.anbu.REATTACH_NOTIFICATION"

        fun start(context: Context) {
            KeroLog.d("[ActivityMonitorService] start() called — dispatching startForegroundService")
            context.startForegroundService(
                Intent(context, ActivityMonitorService::class.java)
            )
        }

        fun stop(context: Context) {
            KeroLog.d("[ActivityMonitorService] stop() called — dispatching stopService")
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
    private var powerReceiver: BroadcastReceiver? = null

    override fun onCreate() {
        // KeroLog는 주입 없이 동작하므로 super.onCreate()(Hilt 주입) 전에 찍힌다
        KeroLog.d("[ActivityMonitorService] onCreate — before Hilt injection")
        super.onCreate()
        KeroLog.d("[ActivityMonitorService] onCreate — Hilt injection complete")
        log("onCreate")
        recordEvent("started", "activity monitor service created")

        val powerManager = getSystemService(POWER_SERVICE) as PowerManager
        wakeLock = powerManager.newWakeLock(
            PowerManager.PARTIAL_WAKE_LOCK,
            "Anbu:ActivityMonitor"
        ).apply { acquire() }
        log("partial wake lock acquired")

        createNotificationChannel()
        try {
            startForeground(
                NOTIFICATION_ID,
                buildNotification(),
                ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
            )
            log("startForeground OK")
            KeroLog.d("[ActivityMonitorService] startForeground success — notification should be visible")
        } catch (e: Exception) {
            KeroLog.e("[ActivityMonitorService] startForeground FAILED", e)
            log("startForeground FAILED: ${e.javaClass.simpleName}: ${e.message}")
        }

        registerUnlockReceiver()
        registerPowerReceiver()
        startHeartbeat()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        KeroLog.d("[ActivityMonitorService] onStartCommand flags=$flags startId=$startId action=${intent?.action}")
        log("onStartCommand flags=$flags startId=$startId")
        serviceStateStore.recordHeartbeat("onStartCommand")
        if (intent?.action == ACTION_REATTACH) {
            KeroLog.d("[ActivityMonitorService] notification dismissed — reattaching")
            log("notification dismissed — reattaching")
            startForeground(NOTIFICATION_ID, buildNotification(), ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE)
        }
        return START_STICKY
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        KeroLog.w("[ActivityMonitorService] onTaskRemoved — scheduling AlarmManager restart in 1s")
        log("task removed; scheduling restart")
        recordEvent("error", "task removed; scheduling alarm restart")
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
        KeroLog.w("[ActivityMonitorService] onTrimMemory level=$level")
        log("onTrimMemory level=$level")
        super.onTrimMemory(level)
    }

    override fun onDestroy() {
        KeroLog.d("[ActivityMonitorService] onDestroy")
        log("onDestroy")
        recordEvent("stopped", "activity monitor service destroyed")
        unregisterUnlockReceiver()
        unregisterPowerReceiver()
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
                    KeroLog.d("[ActivityMonitorService] ACTION_USER_PRESENT received")
                    log("ACTION_USER_PRESENT received")
                    serviceScope.launch {
                        activityRepository.recordUnlock(now)
                        val count = activityRepository.unlockCount()
                        log("unlock event recorded (total=$count)")
                        uploadPendingEvents("unlock")
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

    private fun registerPowerReceiver() {
        powerReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                val source = when (intent.action) {
                    Intent.ACTION_POWER_CONNECTED    -> "power_connected"
                    Intent.ACTION_POWER_DISCONNECTED -> "power_disconnected"
                    else -> return
                }
                val now = System.currentTimeMillis()
                KeroLog.d("[ActivityMonitorService] $source received")
                log("$source received")
                serviceScope.launch {
                    activityRepository.recordUnlock(now, source)
                    val count = activityRepository.unlockCount()
                    log("activity event recorded: $source (total=$count)")
                    uploadPendingEvents(source)
                }
            }
        }
        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_POWER_CONNECTED)
            addAction(Intent.ACTION_POWER_DISCONNECTED)
        }
        registerReceiver(powerReceiver, filter)
        log("power receiver registered")
    }

    private fun unregisterPowerReceiver() {
        powerReceiver?.let {
            unregisterReceiver(it)
            powerReceiver = null
            log("power receiver unregistered")
        }
    }

    private fun startHeartbeat() {
        heartbeatJob?.cancel()
        heartbeatJob = serviceScope.launch {
            while (true) {
                serviceStateStore.recordHeartbeat("heartbeat")
                activityRepository.recordServiceEvent("heartbeat", "activity monitor heartbeat")
                uploadPendingEvents("heartbeat")
                delay(HEARTBEAT_INTERVAL_MS)
            }
        }
        log("heartbeat started (interval=${HEARTBEAT_INTERVAL_MS}ms)")
    }

    private fun recordEvent(type: String, detail: String) {
        serviceScope.launch {
            activityRepository.recordServiceEvent(type, detail)
            uploadPendingEvents("service:$type")
        }
    }

    private suspend fun uploadPendingEvents(reason: String) {
        try {
            val result = activityRepository.uploadPendingEvents()
            if (result.activityUploaded > 0 || result.serviceUploaded > 0) {
                log(
                    "uploaded pending events ($reason): " +
                        "activity=${result.activityUploaded}, service=${result.serviceUploaded}"
                )
            }
        } catch (e: Exception) {
            log("upload pending events failed ($reason): ${e.javaClass.simpleName}: ${e.message}")
            ActivityUploadWorker.enqueueImmediate(applicationContext)
        }
    }

    private fun log(message: String) {
        diagnosticsLogStore.add("ActivityMonitor", message)
    }

    private fun buildNotification(): Notification {
        val reattachIntent = PendingIntent.getService(
            this, 2,
            Intent(this, ActivityMonitorService::class.java).apply { action = ACTION_REATTACH },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("안부")
            .setContentText("활동 모니터링 실행 중")
            .setSmallIcon(android.R.drawable.ic_menu_recent_history)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setDeleteIntent(reattachIntent)
            .build()
    }

    private fun createNotificationChannel() {
        NotificationChannel(CHANNEL_ID, "활동 모니터링", NotificationManager.IMPORTANCE_LOW)
            .also {
                getSystemService(NotificationManager::class.java)
                    .createNotificationChannel(it)
            }
    }
}
