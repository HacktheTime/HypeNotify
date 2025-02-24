// File: src/main/java/de/hype/hypenotify/BroadcastIntentWatcher.java
package de.hype.hypenotify.core;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class BroadcastIntentWatcher extends BroadcastReceiver {

    public BroadcastIntentWatcher() {
        Log.d("BroadcastIntentWatcher", "BroadcastIntentWatcher created");
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("BroadcastIntentWatcher", "BroadcastIntentWatcher received intent: " + intent.getAction());
        if ("de.hype.hypenotify.ENUM_INTENT".equals(intent.getAction())) {
            // Send the intent to the service
            Intent dynamicIntent = new Intent(context, DynamicIntentService.class);
            dynamicIntent.setAction(intent.getAction());
            dynamicIntent.putExtras(intent.getExtras());
            context.startService(dynamicIntent);
        }
    }
}