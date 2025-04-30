package de.hype.hypenotify.app.core;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.SystemClock;
import androidx.core.app.NotificationCompat;
import de.hype.hypenotify.MainActivity;
import de.hype.hypenotify.R;
import de.hype.hypenotify.app.core.interfaces.Core;
import de.hype.hypenotify.app.services.HypeNotifyService;
import de.hype.hypenotify.app.tools.notification.NotificationBuilder;
import de.hype.hypenotify.app.tools.notification.NotificationChannels;
import org.jetbrains.annotations.Blocking;

import java.util.function.Consumer;

public class BackgroundService extends HypeNotifyService<BackgroundService> {
    @SuppressLint("StaticFieldLeak")
    public static BackgroundService instance;
    private static boolean isRunning = false;
    private MiniCore core;

    private static final String CLOSE_SERVICE_ACTION =
            "de.hype.hypenotify.CLOSE_SERVICE_ACTION";
    private static final String DISMISS_ACTION =
            "de.hype.hypenotify.NOTIF_DISMISSED";

    private final BroadcastReceiver closeReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context ctx, Intent i) {
            stopSelf();
        }
    };
    private final BroadcastReceiver dismissReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context ctx, Intent i) {
            showNotificationWithCloseButton();
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();

        // Register receivers
        registerReceiver(closeReceiver,
                new IntentFilter(CLOSE_SERVICE_ACTION),
                Context.RECEIVER_NOT_EXPORTED);
        registerReceiver(dismissReceiver,
                new IntentFilter(DISMISS_ACTION),
                Context.RECEIVER_NOT_EXPORTED);

        showNotificationWithCloseButton();
        instance = this;
        core = new ExpandedMiniCore(this);
    }

    private void showNotificationWithCloseButton() {
        Intent closeIntent = new Intent(CLOSE_SERVICE_ACTION)
                .setPackage(getPackageName());
        PendingIntent closePI = PendingIntent.getBroadcast(
                this, 0, closeIntent, PendingIntent.FLAG_IMMUTABLE);

        Intent deleteIntent = new Intent(DISMISS_ACTION)
                .setPackage(getPackageName());
        PendingIntent deletePI = PendingIntent.getBroadcast(
                this, 0, deleteIntent, PendingIntent.FLAG_IMMUTABLE);

        NotificationBuilder nb = new NotificationBuilder(
                this,
                "Background",
                "A HypeNotify Service is running in the background.",
                NotificationChannels.BACKGROUND_SERVICE);

        nb.setSmallIcon(R.mipmap.icon);
        nb.setLargeImage(R.mipmap.icon);
        nb.getHiddenBuilder()
                .setOngoing(true)
                .setForegroundServiceBehavior(
                        NotificationCompat.FOREGROUND_SERVICE_DEFERRED)
                .addAction(
                        android.R.drawable.ic_menu_close_clear_cancel,
                        "Stop Service",
                        closePI)
                .setDeleteIntent(deletePI);

        startForeground(nb.build().getID(), nb.build().get());
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (isRunning) return START_NOT_STICKY;
        isRunning = true;
        return START_STICKY;
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        Intent restart = new Intent(getApplicationContext(),
                BackgroundService.class)
                .setPackage(getPackageName());
        PendingIntent pi = PendingIntent.getService(
                getApplicationContext(), 1, restart,
                PendingIntent.FLAG_IMMUTABLE);
        AlarmManager am = (AlarmManager) getSystemService(
                Context.ALARM_SERVICE);
        am.set(AlarmManager.ELAPSED_REALTIME,
                SystemClock.elapsedRealtime() + 1000, pi);
        super.onTaskRemoved(rootIntent);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(closeReceiver);
        unregisterReceiver(dismissReceiver);
        instance = null;
        isRunning = false;
    }

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
    public de.hype.hypenotify.app.core.interfaces.MiniCore getCore() {
        return core;
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

    @Blocking
    public Core getCore(MainActivity context) {
        while (core == null) {
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {

            }
        }
        return new CoreImpl(context, core);
    }
}
