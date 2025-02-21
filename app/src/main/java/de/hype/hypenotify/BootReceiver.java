package de.hype.hypenotify;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import java.util.List;

public class BootReceiver extends BroadcastReceiver {
    private final Core core;

    public BootReceiver(Core core) {
        this.core = core;
    }

    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            // Retrieve timers from shared preferences or a database
            core.loadTimers();
        }
    }
}