package de.hype.hypenotify.core;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class BootReceiver extends BroadcastReceiver {

    private static final String LAST_BOOT_TIME = "last_boot_time";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            DynamicIntents.startBackgroundService(context);
        }
    }

}
