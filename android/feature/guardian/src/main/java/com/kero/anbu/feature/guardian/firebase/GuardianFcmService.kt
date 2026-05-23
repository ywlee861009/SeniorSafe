package com.kero.anbu.feature.guardian.firebase

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.kero.anbu.core.data.repository.DeviceRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class GuardianFcmService : FirebaseMessagingService() {

    @Inject lateinit var deviceRepository: DeviceRepository

    companion object {
        private const val CHANNEL_ID = "inactivity_alert_channel"
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        CoroutineScope(Dispatchers.IO).launch {
            try {
                deviceRepository.updateFcmToken(token)
            } catch (_: Exception) {}
        }
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        val title = message.notification?.title ?: "안부"
        val body  = message.notification?.body  ?: "휴대폰 사용 기록을 확인해주세요"
        showNotification(title, body)
    }

    private fun showNotification(title: String, body: String) {
        createChannel()
        val pendingIntent = PendingIntent.getActivity(
            this, 0,
            packageManager.getLaunchIntentForPackage(packageName),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(body)
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()
        getSystemService(NotificationManager::class.java)
            .notify(System.currentTimeMillis().toInt(), notification)
    }

    private fun createChannel() {
        NotificationChannel(CHANNEL_ID, "안부 확인 알림", NotificationManager.IMPORTANCE_HIGH)
            .also { getSystemService(NotificationManager::class.java).createNotificationChannel(it) }
    }
}
