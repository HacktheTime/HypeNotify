package de.hype.hypenotify.core;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

public class DynamicIntentWatcher {

    private static final String TAG = "DynamicIntentWatcher";
    private final Context context;
    private final BroadcastReceiver intentReceiver;

    public DynamicIntentWatcher(Context context) {
        this.context = context;
        this.intentReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.d(TAG, "Broadcast intent received: " + intent.getAction());
                handleIntent(intent);
            }
        };
    }

    public void startWatching() {
        IntentFilter filter = new IntentFilter();
        filter.addAction("de.hype.hypenotify.ENUM_INTENT");
        context.registerReceiver(intentReceiver, filter);
        Log.d(TAG, "Started watching for broadcast intents");
    }

    public void stopWatching() {
        context.unregisterReceiver(intentReceiver);
        Log.d(TAG, "Stopped watching for broadcast intents");
    }

    public void handlePendingIntent(PendingIntent pendingIntent) {
        try {
            pendingIntent.send();
            Log.d(TAG, "Pending intent sent");
        } catch (PendingIntent.CanceledException e) {
            Log.e(TAG, "Pending intent was canceled", e);
        }
    }

    private void handleIntent(Intent intent) {
        // Handle the intent here
        Log.d(TAG, "Handling intent: " + intent.getAction());
        // Add your intent handling logic here
    }
}