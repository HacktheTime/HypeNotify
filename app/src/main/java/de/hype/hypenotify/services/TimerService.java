package de.hype.hypenotify.services;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import androidx.core.app.NotificationCompat;
import de.hype.hypenotify.NotificationUtils;
import de.hype.hypenotify.R;
import de.hype.hypenotify.tools.notification.NotificationBuilder;
import de.hype.hypenotify.tools.notification.NotificationChannels;

public class TimerService extends HypeNotifyService<TimerService> {
    @Override
    public void onCreate() {
        super.onCreate();
        NotificationBuilder notificationBuilder = new NotificationBuilder(this,"Background","A HypeNotify Service is running in the background.", NotificationChannels.BACKGROUND_SERVICE);
        notificationBuilder.setSmallIcon(R.mipmap.icon);
        notificationBuilder.setLargeImage(R.mipmap.icon);
        notificationBuilder.getHiddenBuilder().setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_DEFERRED);
        startForeground(1, notificationBuilder.build());
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // Your background task code here
        return START_NOT_STICKY;
    }
}