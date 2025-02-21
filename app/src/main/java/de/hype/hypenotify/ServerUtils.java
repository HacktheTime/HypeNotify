package de.hype.hypenotify;

import android.content.SharedPreferences;
import android.util.Log;
import com.google.gson.JsonObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import static de.hype.hypenotify.Constants.*;

public class ServerUtils {
    /**
     * @param apiKey      The Users API Key
     * @param deviceName  The Name of the Device (the device shall appear as)
     * @param firebaseKey The Firebase Key
     * @param userId      The Users ID (where key needs to match)
     * @param prefs       The SharedPreferences to save the data to
     *                    <p>
     *                    The method sends the token to the server and if valid saves it to the SharedPreferences
     */
    public static void sendTokenToServer(String apiKey, String deviceName, String firebaseKey, int userId, SharedPreferences prefs) throws IOException {
        if (apiKey.isEmpty() || userId == 0) return;
        // Your existing code to send the token to the server
        String postData = "apiKey=%s&deviceName=%s&firebaseKey=%s&userId=%d".formatted(apiKey, deviceName, firebaseKey, userId);
        URL url = new URL("http://hackthetime.de:8085/hypenotify/addDevice");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setDoOutput(true);
        OutputStream os = conn.getOutputStream();
        os.write(postData.getBytes("UTF-8"));
        os.flush();
        os.close();
        int responseCode = conn.getResponseCode();
        // Save to SharedPreferences
        if (responseCode == 200) {
            prefs.edit()
                    .putString(KEY_API, apiKey)
                    .putString(KEY_DEVICE, deviceName)
                    .putInt(KEY_USER_ID, userId)
                    .apply();
        }
    }

    public static void checkTimersValidity(Core core) {
        for (TimerData timer : core.timers.values()) {
            try {
                URL url = new URL("http://hackthetime.de:8085/hypenotify/checkTimer?id="
                        + timer.id + "&apiKey=" + core.userAPIKey
                        + "&userId=" + core.userId);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setConnectTimeout(5000);
                conn.setReadTimeout(5000);
                int responseCode = conn.getResponseCode();
                if (responseCode == 200) {
                    BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    JsonObject json = core.gson.fromJson(in, JsonObject.class);
                    in.close();
                    boolean valid = json.get("valid").getAsBoolean();
                    if (!valid) {
                        timer.active = false;
                        core.cancelTimer(timer);
                        if (json.has("replacementTimer")) {
                            timer.active = true;
                            core.scheduleTimer(timer);
                        }
                    }
                }
            } catch (Exception e) {
                Log.e("ServerUtils", "Error checking timer validity: ", e);
            }
        }
    }

    public static void getTimers(Core core) {

    }
}
