package com.kero.anbu.feature.senior.today

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.kero.anbu.core.activity.db.ActivityRepository
import com.kero.anbu.feature.senior.navigation.todayMessageRoute
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@AndroidEntryPoint
class TodayMessageNotificationReceiver : BroadcastReceiver() {

    @Inject lateinit var activityRepository: ActivityRepository

    override fun onReceive(context: Context, intent: Intent?) {
        createNotificationChannel(context)

        val launchIntent = context.packageManager.getLaunchIntentForPackage(context.packageName)
            ?.putExtra("anbu_start_route", todayMessageRoute)
            ?: return

        val pendingIntent = PendingIntent.getActivity(
            context,
            NOTIFICATION_ID,
            launchIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("오늘의 글이 도착했어요")
            .setContentText("잠시 쉬면서 오늘의 글을 읽어보세요.")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        context.getSystemService(NotificationManager::class.java)
            .notify(NOTIFICATION_ID, notification)

        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                activityRepository.recordServiceEvent(
                    eventType = "today_message_notification_sent",
                    detail = "daily evening reminder"
                )
            } finally {
                pendingResult.finish()
            }
        }
    }

    private fun createNotificationChannel(context: Context) {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "오늘의 글",
            NotificationManager.IMPORTANCE_DEFAULT
        )
        context.getSystemService(NotificationManager::class.java)
            .createNotificationChannel(channel)
    }

    companion object {
        private const val CHANNEL_ID = "today_message_channel"
        private const val NOTIFICATION_ID = 20260518
    }
}
