package com.seniorsafe.core.activity.service

import android.content.Context
import com.seniorsafe.core.diagnostics.DiagnosticsLogStore
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
        stateStore.refresh(reason)
        if (stateStore.isManuallyStopped()) {
            diagnosticsLogStore.add("ActivityMonitorController", "ensure skipped; manually stopped; reason=$reason")
            return
        }

        if (stateStore.isHeartbeatFreshNow()) {
            diagnosticsLogStore.add("ActivityMonitorController", "ensure skipped; heartbeat fresh; reason=$reason")
            return
        }

        if (stateStore.isHeartbeatStale()) {
            restartIfStale(reason)
            return
        }

        diagnosticsLogStore.add("ActivityMonitorController", "ensure starting service; reason=$reason")
        startService(reason)
    }

    fun restartIfStale(reason: String) {
        if (stateStore.isManuallyStopped()) {
            diagnosticsLogStore.add("ActivityMonitorController", "stale restart skipped; manually stopped; reason=$reason")
            return
        }

        if (!stateStore.isHeartbeatStale()) {
            diagnosticsLogStore.add("ActivityMonitorController", "stale restart skipped; not stale; reason=$reason")
            return
        }

        diagnosticsLogStore.add("ActivityMonitorController", "restarting stale service; reason=$reason")
        stopService(reason)
        startService(reason)
    }

    fun startByUser(reason: String) {
        stateStore.markUserStarted(reason)
        diagnosticsLogStore.add("ActivityMonitorController", "user start requested; reason=$reason")
        startService(reason)
    }

    fun stopByUser(reason: String) {
        stateStore.markUserStopped(reason)
        diagnosticsLogStore.add("ActivityMonitorController", "user stop requested; reason=$reason")
        stopService(reason)
    }

    fun refresh(reason: String) {
        stateStore.refresh(reason)
        restartIfStale(reason)
    }

    private fun startService(reason: String) {
        try {
            ActivityMonitorService.start(context)
        } catch (error: RuntimeException) {
            diagnosticsLogStore.add(
                "ActivityMonitorController",
                "start failed; reason=$reason; error=${error.javaClass.simpleName}: ${error.message.orEmpty()}"
            )
        }
    }

    private fun stopService(reason: String) {
        try {
            ActivityMonitorService.stop(context)
        } catch (error: RuntimeException) {
            diagnosticsLogStore.add(
                "ActivityMonitorController",
                "stop failed; reason=$reason; error=${error.javaClass.simpleName}: ${error.message.orEmpty()}"
            )
        }
    }
}
