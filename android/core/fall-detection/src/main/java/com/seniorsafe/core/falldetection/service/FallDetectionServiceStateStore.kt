package com.seniorsafe.core.falldetection.service

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
class FallDetectionServiceStateStore @Inject constructor(
    @ApplicationContext context: Context,
    private val diagnosticsLogStore: DiagnosticsLogStore
) {

    companion object {
        private const val PREFS_NAME = "fall_detection_service_state"
        private const val KEY_LAST_HEARTBEAT_AT = "last_heartbeat_at"
        private const val HEARTBEAT_STALE_MS = 5_000L
        const val HEARTBEAT_INTERVAL_MS = 1_000L
    }

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val _isRunning = MutableStateFlow(isHeartbeatFresh(System.currentTimeMillis()))
    val isRunning: StateFlow<Boolean> = _isRunning.asStateFlow()

    init {
        scope.launch {
            while (true) {
                refresh("heartbeat monitor")
                delay(HEARTBEAT_INTERVAL_MS)
            }
        }
    }

    fun recordHeartbeat(reason: String) {
        prefs.edit().putLong(KEY_LAST_HEARTBEAT_AT, System.currentTimeMillis()).apply()
        refresh(reason)
    }

    fun markStopped(reason: String) {
        prefs.edit().remove(KEY_LAST_HEARTBEAT_AT).apply()
        refresh(reason)
    }

    fun refresh(reason: String) {
        val running = isHeartbeatFresh(System.currentTimeMillis())
        if (_isRunning.value != running) {
            diagnosticsLogStore.add("FallServiceState", "running=$running; reason=$reason")
        }
        _isRunning.value = running
    }

    private fun isHeartbeatFresh(now: Long): Boolean {
        val lastHeartbeatAt = prefs.getLong(KEY_LAST_HEARTBEAT_AT, 0L)
        return lastHeartbeatAt > 0L && now - lastHeartbeatAt <= HEARTBEAT_STALE_MS
    }
}
