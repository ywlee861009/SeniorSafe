package com.seniorsafe.core.activity.service

import android.content.Context
import com.seniorsafe.core.diagnostics.DiagnosticsLogStore
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@Singleton
class ActivityServiceStateStore @Inject constructor(
    @ApplicationContext context: Context,
    private val diagnosticsLogStore: DiagnosticsLogStore
) {

    companion object {
        private const val PREFS_NAME = "activity_monitor_service_state"
        private const val KEY_LAST_HEARTBEAT_AT = "last_heartbeat_at"
        private const val KEY_MANUALLY_STOPPED = "manually_stopped"
        private const val HEARTBEAT_STALE_MS = 180_000L // 3분 (heartbeat 간격 1분 기준)
        const val MONITOR_INTERVAL_MS = 5_000L
    }

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val _isRunning = MutableStateFlow(isHeartbeatFresh(System.currentTimeMillis()))
    val isRunning: StateFlow<Boolean> = _isRunning.asStateFlow()

    init {
        scope.launch {
            while (true) {
                refresh("monitor")
                delay(MONITOR_INTERVAL_MS)
            }
        }
    }

    fun recordHeartbeat(reason: String) {
        prefs.edit().putLong(KEY_LAST_HEARTBEAT_AT, System.currentTimeMillis()).apply()
        refresh(reason)
    }

    fun markUserStarted(reason: String) {
        prefs.edit().putBoolean(KEY_MANUALLY_STOPPED, false).apply()
        diagnosticsLogStore.add("ActivityServiceState", "manual stop cleared; reason=$reason")
    }

    fun markUserStopped(reason: String) {
        prefs.edit()
            .putBoolean(KEY_MANUALLY_STOPPED, true)
            .remove(KEY_LAST_HEARTBEAT_AT)
            .apply()
        diagnosticsLogStore.add("ActivityServiceState", "manual stop set; reason=$reason")
        refresh(reason)
    }

    fun markStopped(reason: String) {
        prefs.edit().remove(KEY_LAST_HEARTBEAT_AT).apply()
        refresh(reason)
    }

    fun refresh(reason: String) {
        val running = isHeartbeatFresh(System.currentTimeMillis())
        if (_isRunning.value != running) {
            diagnosticsLogStore.add("ActivityServiceState", "running=$running; reason=$reason")
        }
        _isRunning.value = running
    }

    fun isManuallyStopped(): Boolean =
        prefs.getBoolean(KEY_MANUALLY_STOPPED, false)

    fun isHeartbeatStale(now: Long = System.currentTimeMillis()): Boolean {
        val lastHeartbeatAt = prefs.getLong(KEY_LAST_HEARTBEAT_AT, 0L)
        return lastHeartbeatAt > 0L && now - lastHeartbeatAt > HEARTBEAT_STALE_MS
    }

    fun isHeartbeatFreshNow(now: Long = System.currentTimeMillis()): Boolean =
        isHeartbeatFresh(now)

    private fun isHeartbeatFresh(now: Long): Boolean {
        val lastHeartbeatAt = prefs.getLong(KEY_LAST_HEARTBEAT_AT, 0L)
        return lastHeartbeatAt > 0L && now - lastHeartbeatAt <= HEARTBEAT_STALE_MS
    }
}
