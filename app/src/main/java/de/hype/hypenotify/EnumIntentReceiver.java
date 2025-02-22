package de.hype.hypenotify;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class EnumIntentReceiver extends BroadcastReceiver {
    private static final String TAG = "EnumIntentReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent != null) {
            String action = intent.getAction();
            if ("de.hype.hypenotify.ENUM_INTENT".equals(action)) {
                Log.d(TAG, "ENUM_INTENT broadcast received");
                Intents.handleIntent(intent, Core.getInstance(), context);
            }
        }
    }
}
