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
    public void onReceive(Context context, Intent receivedIntent) {
        Log.d("BroadcastIntentWatcher", "BroadcastIntentWatcher received intent: " + receivedIntent.getAction());
        if (StaticIntents.BASE_INTENT_NAME.equals(receivedIntent.getAction())) {
            Intent serviceIntent = new Intent(context, BackgroundService.class);
            context.startService(serviceIntent);
            BackgroundService.executeWithBackgroundService((s) -> StaticIntents.onIntent(s.getCore(), context, receivedIntent));
        }
    }
}