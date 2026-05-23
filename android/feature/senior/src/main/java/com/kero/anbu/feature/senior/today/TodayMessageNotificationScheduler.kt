package com.kero.anbu.feature.senior.today

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.Calendar
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TodayMessageNotificationScheduler @Inject constructor(
    @ApplicationContext private val context: Context
) {
    fun scheduleDailyEveningReminder() {
        val alarmManager = context.getSystemService(AlarmManager::class.java)
        val triggerAt = nextEightPmMillis()
        alarmManager.setInexactRepeating(
            AlarmManager.RTC_WAKEUP,
            triggerAt,
            AlarmManager.INTERVAL_DAY,
            reminderPendingIntent()
        )
        // 오늘 20시가 이미 지났으면 즉시 노티를 한 번 발송
        if (triggerAt > todayEightPmMillis()) {
            reminderPendingIntent().send()
        }
    }

    private fun reminderPendingIntent(): PendingIntent =
        PendingIntent.getBroadcast(
            context,
            REQUEST_CODE,
            Intent(context, TodayMessageNotificationReceiver::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

    private fun todayEightPmMillis(): Long =
        Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 20)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

    private fun nextEightPmMillis(): Long {
        val today = todayEightPmMillis()
        return if (today > System.currentTimeMillis()) today
        else today + AlarmManager.INTERVAL_DAY
    }

    companion object {
        private const val REQUEST_CODE = 20260518
    }
}
