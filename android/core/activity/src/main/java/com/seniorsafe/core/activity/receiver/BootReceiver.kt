package com.seniorsafe.core.activity.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.seniorsafe.core.activity.service.ActivityMonitorController
import com.seniorsafe.core.util.KeroLog
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class BootReceiver : BroadcastReceiver() {

    @Inject lateinit var activityMonitorController: ActivityMonitorController

    override fun onReceive(context: Context, intent: Intent) {
        KeroLog.d("BootReceiver", "onReceive action=${intent.action}")
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            KeroLog.d("BootReceiver", "BOOT_COMPLETED → ensureServiceRunning")
            activityMonitorController.ensureServiceRunning("boot completed")
        } else {
            KeroLog.w("BootReceiver", "unexpected action=${intent.action}")
        }
    }
}
