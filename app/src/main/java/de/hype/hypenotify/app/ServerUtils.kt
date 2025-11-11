package de.hype.hypenotify.app

import android.content.SharedPreferences
import android.util.Log
import com.google.gson.JsonObject
import de.hype.hypenotify.app.core.Constants
import de.hype.hypenotify.app.core.interfaces.Core
import de.hype.hypenotify.app.core.interfaces.MiniCore
import de.hype.hypenotify.app.tools.timers.BaseTimer
import de.hype.hypenotify.app.tools.timers.TimerWrapper
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.nio.charset.StandardCharsets

object ServerUtils {
    /**
     * @param apiKey      The Users API Key
     * @param deviceName  The Name of the Device (the device shall appear as)
     * @param firebaseKey The Firebase Key
     * @param userId      The Users ID (where key needs to match)
     * @param prefs       The SharedPreferences to save the data to
     *
     *
     * The method sends the token to the server and if valid saves it to the SharedPreferences
     */
    @Throws(IOException::class)
    fun sendTokenToServer(apiKey: String, deviceName: String?, firebaseKey: String?, userId: Int, prefs: SharedPreferences) {
        if (apiKey.isEmpty() || userId == 0) return
        // Your existing code to send the token to the server
        val postData: String = "apiKey=%s&deviceName=%s&firebaseKey=%s&userId=%d".formatted(apiKey, deviceName, firebaseKey, userId)
        val url = URL(Config.Companion.INSTANCE.serverURL + "addDevice")
        val conn = url.openConnection() as HttpURLConnection
        conn.setRequestMethod("POST")
        conn.setDoOutput(true)
        val os = conn.getOutputStream()
        os.write(postData.toByteArray(StandardCharsets.UTF_8))
        os.flush()
        os.close()
        val responseCode = conn.getResponseCode()
        // Save to SharedPreferences
        if (responseCode == 200) {
            prefs.edit()
                .putString(Constants.KEY_API, apiKey)
                .putString(Constants.KEY_DEVICE, deviceName)
                .putInt(Constants.KEY_USER_ID, userId)
                .apply()
        }
    }

    fun checkTimersValidity(timer: TimerWrapper, core: Core) {
        try {
            val finalURL: String =
                "%scheckTimer?id=%s&apiKey=%s&userId=%d".formatted(
                    Config.Companion.INSTANCE.serverURL,
                    timer.getServerId(),
                    core.userAPIKey(),
                    core.userId()
                )
            val url = URL(finalURL)
            val conn = url.openConnection() as HttpURLConnection
            conn.setRequestMethod("GET")
            conn.setConnectTimeout(5000)
            conn.setReadTimeout(5000)
            val responseCode = conn.getResponseCode()
            if (responseCode == 200) {
                val `in` = BufferedReader(InputStreamReader(conn.getInputStream()))
                val json = core.gson().fromJson<JsonObject>(`in`, JsonObject::class.java)
                `in`.close()
                val valid = json.get("valid").getAsBoolean()
                if (!valid) {
                    timer.deactivate()
                    if (json.has("replacementTimer")) {
                        timer.replaceTimer(json.get("replacementTimer"))
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("ServerUtils", "Error checking smartTimer validity: ", e)
        }
    }


    fun getServerTimers(core: MiniCore?): MutableList<BaseTimer?> {
        //TODO implement
        return mutableListOf<BaseTimer?>()
    }

    fun uploadTimer(core: MiniCore?, baseTimer: BaseTimer?) {
    }
}
