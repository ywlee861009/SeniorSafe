package com.seniorsafe.core.activity.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.seniorsafe.core.activity.service.ActivityMonitorService
import com.seniorsafe.core.diagnostics.diagnosticsLogStore

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            context.diagnosticsLogStore().add("BootReceiver", "boot completed; starting activity monitor")
            ActivityMonitorService.start(context)
        }
    }
}
