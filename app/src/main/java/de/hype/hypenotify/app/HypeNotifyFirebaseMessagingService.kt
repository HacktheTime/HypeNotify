package de.hype.hypenotify.app

import android.widget.Toast
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import de.hype.hypenotify.app.core.Constants
import de.hype.hypenotify.shared.notification.NotificationContent
import java.io.IOException

class HypeNotifyFirebaseMessagingService : FirebaseMessagingService() {
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        // Call your method to send the token to the server
        val prefs = getSharedPreferences(Constants.PREFS_NAME, MODE_PRIVATE)
        val apiKey: String = prefs.getString(Constants.KEY_API, "")!!
        val deviceName: String = prefs.getString(Constants.KEY_DEVICE, "")!!
        val userId = prefs.getInt(Constants.KEY_USER_ID, -1)
        try {
            ServerUtils.sendTokenToServer(apiKey, deviceName, token, userId, prefs)
        } catch (e: IOException) {
            Toast.makeText(this, "Error updating Firebase Token: %s".formatted(e.message), Toast.LENGTH_SHORT).show()
            e.printStackTrace()
        }
    }

    override fun onMessageReceived(message: RemoteMessage) {
        val string = message.getData().get("json")
        if (string == null) return
        val content: NotificationContent? = customGson.fromJson<NotificationContent?>(string, NotificationContent::class.java)
        super.onMessageReceived(message)
    }

    companion object {
        var customGson: Gson = GsonBuilder().create()
    }
}