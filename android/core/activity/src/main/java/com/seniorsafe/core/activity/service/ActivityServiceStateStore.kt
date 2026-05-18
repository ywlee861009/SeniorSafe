package com.seniorsafe.core.activity.service

import android.content.Context
import com.seniorsafe.core.diagnostics.DiagnosticsLogStore
import com.seniorsafe.core.util.KeroLog
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
        private const val HEARTBEAT_STALE_MS = 180_000L
        const val MONITOR_INTERVAL_MS = 5_000L
    }

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val _isRunning = MutableStateFlow(isHeartbeatFresh(System.currentTimeMillis()))
    val isRunning: StateFlow<Boolean> = _isRunning.asStateFlow()

    init {
        KeroLog.d("[ActivityServiceState] init — isRunning=${_isRunning.value} manuallyStopped=${isManuallyStopped()}")
        scope.launch {
            while (true) {
                refresh("monitor")
                delay(MONITOR_INTERVAL_MS)
            }
        }
    }

    fun recordHeartbeat(reason: String) {
        val now = System.currentTimeMillis()
        val prev = prefs.getLong(KEY_LAST_HEARTBEAT_AT, 0L)
        val deltaMs = if (prev > 0L) now - prev else -1L
        prefs.edit().putLong(KEY_LAST_HEARTBEAT_AT, now).apply()
        KeroLog.d("[ActivityServiceState] heartbeat recorded; reason=$reason deltaMs=$deltaMs")
        refresh(reason)
    }

    fun markUserStarted(reason: String) {
        prefs.edit().putBoolean(KEY_MANUALLY_STOPPED, false).apply()
        KeroLog.d("[ActivityServiceState] manual stop cleared; reason=$reason")
        diagnosticsLogStore.add("ActivityServiceState", "manual stop cleared; reason=$reason")
    }

    fun markUserStopped(reason: String) {
        prefs.edit()
            .putBoolean(KEY_MANUALLY_STOPPED, true)
            .remove(KEY_LAST_HEARTBEAT_AT)
            .apply()
        KeroLog.d("[ActivityServiceState] manual stop set; reason=$reason")
        diagnosticsLogStore.add("ActivityServiceState", "manual stop set; reason=$reason")
        refresh(reason)
    }

    fun markStopped(reason: String) {
        prefs.edit().remove(KEY_LAST_HEARTBEAT_AT).apply()
        KeroLog.d("[ActivityServiceState] heartbeat cleared (service stopped); reason=$reason")
        refresh(reason)
    }

    fun refresh(reason: String) {
        val running = isHeartbeatFresh(System.currentTimeMillis())
        if (_isRunning.value != running) {
            KeroLog.d("[ActivityServiceState] isRunning changed ${ _isRunning.value}→$running; reason=$reason")
            diagnosticsLogStore.add("ActivityServiceState", "running=$running; reason=$reason")
        }
        _isRunning.value = running
    }

    fun isManuallyStopped(): Boolean =
        prefs.getBoolean(KEY_MANUALLY_STOPPED, false)

    fun isHeartbeatStale(now: Long = System.currentTimeMillis()): Boolean {
        val last = prefs.getLong(KEY_LAST_HEARTBEAT_AT, 0L)
        val stale = last > 0L && now - last > HEARTBEAT_STALE_MS
        if (stale) KeroLog.w("[ActivityServiceState] heartbeat STALE — last=$last now=$now delta=${now - last}ms")
        return stale
    }

    fun isHeartbeatFreshNow(now: Long = System.currentTimeMillis()): Boolean =
        isHeartbeatFresh(now)

    private fun isHeartbeatFresh(now: Long): Boolean {
        val last = prefs.getLong(KEY_LAST_HEARTBEAT_AT, 0L)
        return last > 0L && now - last <= HEARTBEAT_STALE_MS
    }
}
