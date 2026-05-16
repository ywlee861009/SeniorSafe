package com.seniorsafe.feature.mvp.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.seniorsafe.core.falldetection.service.FallDetectionService

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            FallDetectionService.start(context)
        }
    }
}
