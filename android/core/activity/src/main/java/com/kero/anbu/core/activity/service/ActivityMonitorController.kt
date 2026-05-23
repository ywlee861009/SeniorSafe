package com.kero.anbu.core.activity.service

import android.content.Context
import com.kero.anbu.core.diagnostics.DiagnosticsLogStore
import com.kero.anbu.core.util.KeroLog
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ActivityMonitorController @Inject constructor(
    @ApplicationContext private val context: Context,
    private val stateStore: ActivityServiceStateStore,
    private val diagnosticsLogStore: DiagnosticsLogStore
) {

    fun ensureServiceRunning(reason: String) {
        val manuallyStopped = stateStore.isManuallyStopped()
        val heartbeatFresh  = stateStore.isHeartbeatFreshNow()
        val heartbeatStale  = stateStore.isHeartbeatStale()
        log("ensureServiceRunning; reason=$reason manuallyStopped=$manuallyStopped heartbeatFresh=$heartbeatFresh heartbeatStale=$heartbeatStale")

        stateStore.refresh(reason)

        if (manuallyStopped) {
            log("ensure skipped — manually stopped; reason=$reason")
            return
        }
        if (heartbeatFresh) {
            log("ensure skipped — heartbeat fresh; reason=$reason")
            return
        }
        if (heartbeatStale) {
            restartIfStale(reason)
            return
        }
        log("ensure → starting service; reason=$reason")
        startService(reason)
    }

    fun restartIfStale(reason: String) {
        if (stateStore.isManuallyStopped()) {
            log("stale restart skipped — manually stopped; reason=$reason")
            return
        }
        if (!stateStore.isHeartbeatStale()) {
            log("stale restart skipped — not stale; reason=$reason")
            return
        }
        log("restarting stale service; reason=$reason")
        stopService(reason)
        startService(reason)
    }

    fun startByUser(reason: String) {
        stateStore.markUserStarted(reason)
        log("user start requested; reason=$reason")
        startService(reason)
    }

    fun stopByUser(reason: String) {
        stateStore.markUserStopped(reason)
        log("user stop requested; reason=$reason")
        stopService(reason)
    }

    fun refresh(reason: String) {
        stateStore.refresh(reason)
        restartIfStale(reason)
    }

    private fun startService(reason: String) {
        log("→ calling startForegroundService; reason=$reason")
        try {
            ActivityMonitorService.start(context)
            log("→ startForegroundService dispatched OK; reason=$reason")
        } catch (e: Exception) {
            log("→ startForegroundService FAILED; reason=$reason; ${e.javaClass.name}: ${e.message.orEmpty()}")
            KeroLog.e("[ActivityMonitorController] startForegroundService FAILED reason=$reason", e)
        }
    }

    private fun stopService(reason: String) {
        log("→ calling stopService; reason=$reason")
        try {
            ActivityMonitorService.stop(context)
            log("→ stopService dispatched OK; reason=$reason")
        } catch (e: Exception) {
            log("→ stopService FAILED; reason=$reason; ${e.javaClass.name}: ${e.message.orEmpty()}")
            KeroLog.e("[ActivityMonitorController] stopService FAILED reason=$reason", e)
        }
    }

    private fun log(message: String) {
        diagnosticsLogStore.add("ActivityMonitorController", message)
    }
}
