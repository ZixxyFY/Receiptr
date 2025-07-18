package com.receiptr.data.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.receiptr.domain.model.NotificationData
import com.receiptr.domain.model.NotificationTemplate
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationService @Inject constructor(
    @ApplicationContext private val context: Context
) {

    init {
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationTemplate.templates.values.forEach { template ->
                val channel = NotificationChannel(
                    template.type.channelId,
                    template.type.channelName,
                    template.type.importance
                ).apply {
                    description = template.bodyTemplate
                }
                val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                manager.createNotificationChannel(channel)
            }
        }
    }

    fun sendNotification(notificationData: NotificationData) {
        val template = NotificationTemplate.templates[notificationData.type] ?: return
        val notificationBuilder = NotificationCompat.Builder(context, notificationData.type.channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info) // Placeholder icon
            .setContentTitle(notificationData.title)
            .setContentText(notificationData.body)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)

        with(NotificationManagerCompat.from(context)) {
            notify(notificationData.id.hashCode(), notificationBuilder.build())
        }
    }
}

