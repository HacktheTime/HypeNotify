package de.hype.hypenotify.app.services

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.wifi.WifiManager

class WifiConnectionReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent) {
        if (WifiManager.NETWORK_STATE_CHANGED_ACTION == intent.getAction()) {
            onWifiConnected(context)
        }
    }

    private fun onWifiConnected(context: Context?) {
        // Your code to handle Wi-Fi connection
    }
}