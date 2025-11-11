package de.hype.hypenotify.app.core

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent) {
        if (Intent.ACTION_BOOT_COMPLETED == intent.getAction()) {
            DynamicIntents.Companion.startBackgroundService(context)
        }
    }

    companion object {
        private const val LAST_BOOT_TIME = "last_boot_time"
    }
}
