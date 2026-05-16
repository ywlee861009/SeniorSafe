package com.seniorsafe.core.activity.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.seniorsafe.core.activity.service.ActivityMonitorController
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class BootReceiver : BroadcastReceiver() {

    @Inject lateinit var activityMonitorController: ActivityMonitorController

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            activityMonitorController.ensureServiceRunning("boot completed")
        }
    }
}
