package de.hype.hypenotify.app

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import de.hype.hypenotify.R
import de.hype.hypenotify.app.tools.notification.NotificationChannels
import de.hype.hypenotify.app.tools.notification.NotificationImportance

object NotificationUtils {
    fun createNotificationChannel(context: Context, channelId: String?, name: String?, description: String?, importance: Int) {
        val channel = NotificationChannel(channelId, name, importance)
        channel.setDescription(description)
        val notificationManager = context.getSystemService<NotificationManager?>(NotificationManager::class.java)
        if (notificationManager != null) notificationManager.createNotificationChannel(channel)
    }

    fun createNotification(
        context: Context,
        title: String?,
        content: String?,
        channel: NotificationChannels,
        importance: NotificationImportance
    ) {
        val builder = NotificationCompat.Builder(context, channel.channelId)
            .setSmallIcon(R.mipmap.icon) // Replace with your app's notification icon
            .setContentTitle(title)
            .setContentText(content)
            .setPriority(importance.getInt())
            .setAutoCancel(true)

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager?
        if (notificationManager != null) {
            notificationManager.notify(1, builder.build())
        }
    }

    fun synchronizeNotificationChannels(context: Context) {
        val notificationManager = context.getSystemService<NotificationManager?>(NotificationManager::class.java)
        if (notificationManager == null) return

        val existingChannels = notificationManager.getNotificationChannels()
        val existingChannelIds: MutableSet<String?> = HashSet<String?>()
        for (channel in existingChannels) {
            existingChannelIds.add(channel.getId())
        }

        val enumChannelIds: MutableSet<String?> = HashSet<String?>()
        for (channel in NotificationChannels.entries) {
            enumChannelIds.add(channel.channelId)
            if (!existingChannelIds.contains(channel.channelId)) {
                createNotificationChannel(
                    context,
                    channel.channelId,
                    channel.displayName,
                    channel.description,
                    channel.getImportance().getInt()
                )
            }
        }

        for (existingChannelId in existingChannelIds) {
            if (!enumChannelIds.contains(existingChannelId)) {
                notificationManager.deleteNotificationChannel(existingChannelId)
            }
        }
    }
}

