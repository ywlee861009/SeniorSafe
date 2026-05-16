package com.seniorsafe.feature.mvp.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.seniorsafe.core.activity.service.ActivityMonitorService

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            ActivityMonitorService.start(context)
        }
    }
}
