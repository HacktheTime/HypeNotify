package de.hype.hypenotify.app;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import androidx.core.app.NotificationCompat;
import de.hype.hypenotify.app.tools.notification.NotificationChannels;
import de.hype.hypenotify.app.tools.notification.NotificationImportance;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class NotificationUtils {

    public static void createNotificationChannel(Context context, String channelId, String name, String description, int importance) {
        NotificationChannel channel = new NotificationChannel(channelId, name, importance);
        channel.setDescription(description);
        NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
        if (notificationManager != null)
            notificationManager.createNotificationChannel(channel);
    }

    public static void createNotification(Context context, String title, String content, NotificationChannels channel, NotificationImportance importance) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, channel.channelId)
                .setSmallIcon(R.mipmap.icon) // Replace with your app's notification icon
                .setContentTitle(title)
                .setContentText(content)
                .setPriority(importance.getInt())
                .setAutoCancel(true);

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager != null) {
            notificationManager.notify(1, builder.build());
        }
    }

    public static void synchronizeNotificationChannels(Context context) {
        NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
        if (notificationManager == null) return;

        List<NotificationChannel> existingChannels = notificationManager.getNotificationChannels();
        Set<String> existingChannelIds = new HashSet<>();
        for (NotificationChannel channel : existingChannels) {
            existingChannelIds.add(channel.getId());
        }

        Set<String> enumChannelIds = new HashSet<>();
        for (NotificationChannels channel : NotificationChannels.values()) {
            enumChannelIds.add(channel.channelId);
            if (!existingChannelIds.contains(channel.channelId)) {
                createNotificationChannel(context, channel.channelId, channel.displayName, channel.description, channel.getImportance().getInt());
            }
        }

        for (String existingChannelId : existingChannelIds) {
            if (!enumChannelIds.contains(existingChannelId)) {
                notificationManager.deleteNotificationChannel(existingChannelId);
            }
        }
    }
}

