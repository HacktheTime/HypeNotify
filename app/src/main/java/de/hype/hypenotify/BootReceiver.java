package de.hype.hypenotify;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import de.hype.hypenotify.services.TimerService;
import de.hype.hypenotify.tools.notification.NotificationBuilder;
import de.hype.hypenotify.tools.notification.NotificationChannels;

public class BootReceiver extends BroadcastReceiver {

    private static final String LAST_BOOT_TIME = "last_boot_time";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_USER_UNLOCKED.equals(intent.getAction())) {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
            long storedBootTime = prefs.getLong(LAST_BOOT_TIME, 0);
            long currentBootTime = System.currentTimeMillis() - SystemClock.elapsedRealtime();

            if (currentBootTime != storedBootTime) {
                // Update the stored boot time so that this setup runs only once per boot.
                prefs.edit().putLong(LAST_BOOT_TIME, currentBootTime).apply();

                // Start your service and perform initialization.
                MiniCore core = new MiniCore(context);
                Intent serviceIntent = new Intent(context, TimerService.class);
                context.startForegroundService(serviceIntent);

                if (!PermissionUtils.checkPermissions(context)) {
                    NotificationBuilder notificationBuilder = new NotificationBuilder(
                            context,
                            "Permissions Missing",
                            "We noticed that HypeNotify is lacking Permissions. Please open the app and grant the permissions.",
                            NotificationChannels.ERROR
                    );
                    notificationBuilder.send();
                }

                try {
                    core.fullInit();
                } catch (Exception e) {
                    e.printStackTrace();
                }

                if (!core.areKeysSet()){
                    NotificationBuilder notificationBuilder = new NotificationBuilder(
                            context,
                            "Keys Missing",
                            "We noticed that HypeNotify is lacking Keys. Please open the app and enter your keys.",
                            NotificationChannels.ERROR
                    );
                    notificationBuilder.send();
                }
            }
        }
    }
}
