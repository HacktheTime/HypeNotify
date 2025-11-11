// File: src/main/java/de/hype/hypenotify/BroadcastIntentWatcher.java
package de.hype.hypenotify.app.core

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import java.util.function.Consumer

class BroadcastIntentWatcher : BroadcastReceiver() {
    init {
        Log.d("BroadcastIntentWatcher", "BroadcastIntentWatcher created")
    }

    override fun onReceive(context: Context?, receivedIntent: Intent) {
        Log.d("BroadcastIntentWatcher", "BroadcastIntentWatcher received intent: " + receivedIntent.getAction())
        if (StaticIntents.Companion.BASE_INTENT_NAME == receivedIntent.getAction()) {
            DynamicIntents.Companion.startBackgroundService(context)
            BackgroundService.Companion.executeWithBackgroundService(Consumer { s: BackgroundService? ->
                StaticIntents.Companion.onIntent(
                    s!!.getCore(),
                    context,
                    receivedIntent
                )
            })
        }
    }
}