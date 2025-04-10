package de.hype.hypenotify.core;

import android.annotation.SuppressLint;
import android.content.Intent;
import androidx.core.app.NotificationCompat;
import de.hype.hypenotify.MainActivity;
import de.hype.hypenotify.NotificationUtils;
import de.hype.hypenotify.PermissionUtils;
import de.hype.hypenotify.R;
import de.hype.hypenotify.core.interfaces.Core;
import de.hype.hypenotify.services.HypeNotifyService;
import de.hype.hypenotify.tools.notification.Notification;
import de.hype.hypenotify.tools.notification.NotificationBuilder;
import de.hype.hypenotify.tools.notification.NotificationChannels;
import org.jetbrains.annotations.Blocking;

import java.util.function.Consumer;

public class BackgroundService extends HypeNotifyService<BackgroundService> {
    @SuppressLint("StaticFieldLeak")
    public static BackgroundService instance;
    private static boolean isRunning = false;
    private MiniCore core;

    /**
     * Make sure you start the Service before calling this method!
     */
    @Blocking
    public static BackgroundService getInstanceBlocking() {
        while (instance == null) {
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        return instance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        NotificationUtils.synchronizeNotificationChannels(this);
        NotificationBuilder notificationBuilder = new NotificationBuilder(this, "Background", "A HypeNotify Service is running in the background.", NotificationChannels.BACKGROUND_SERVICE);
        notificationBuilder.setSmallIcon(R.mipmap.icon);
        notificationBuilder.setLargeImage(R.mipmap.icon);
        notificationBuilder.getHiddenBuilder().setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_DEFERRED);
        Notification notification = notificationBuilder.build();
        startForeground(notification.getID(), notification.get());
        startCore();
        instance = this;
    }

    private void startCore() {
        // Start your service and perform initialization.
        core = new ExpandedMiniCore(this);

        if (!PermissionUtils.checkPermissions(this)) {
            NotificationBuilder notificationBuilder = new NotificationBuilder(
                    this,
                    "Permissions Missing",
                    "We noticed that HypeNotify is lacking Permissions. Please open the app and grant the permissions.",
                    NotificationChannels.ERROR
            );
            notificationBuilder.send();
        }

        core.executionService.execute(() -> {
            try {
                core.fullInit();
            } catch (Exception e) {
                e.printStackTrace();
            }

            if (!core.areKeysSet()) {
                NotificationBuilder notificationBuilder = new NotificationBuilder(
                        this,
                        "Keys Missing",
                        "We noticed that HypeNotify is lacking Keys. Please open the app and enter your keys.",
                        NotificationChannels.ERROR
                );
                notificationBuilder.send();
            }
        });
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (isRunning) {
            return START_NOT_STICKY;
        }
        isRunning = true;
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        core.onDestroy();
        isRunning = false;
        instance = null;
    }

    /**
     * Will wait until
     *
     * @param consumer consumer to be run.
     */
    public static void executeWithBackgroundService(Consumer<BackgroundService> consumer) {
        Thread thread = new Thread(() -> {
            BackgroundService instance = getInstanceBlocking();
            consumer.accept(instance);
        });
        thread.start();
    }

    public Core getCore(MainActivity context) {
        return new CoreImpl(context, core);
    }

    @Override
    public de.hype.hypenotify.core.interfaces.MiniCore getCore() {
        return core;
    }

}