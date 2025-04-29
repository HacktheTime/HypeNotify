package de.hype.hypenotify;

import android.content.Context;
import android.content.SharedPreferences;
import android.widget.Toast;
import androidx.annotation.NonNull;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import de.hype.hypenotify.shared.notification.NotificationContent;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

import static de.hype.hypenotify.ServerUtils.sendTokenToServer;
import static de.hype.hypenotify.core.Constants.*;

public class HypeNotifyFirebaseMessagingService extends FirebaseMessagingService {
    public static Gson customGson = new GsonBuilder().create();

    @Override
    public void onNewToken(@NotNull String token) {
        super.onNewToken(token);
        // Call your method to send the token to the server
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String apiKey = prefs.getString(KEY_API, "");
        String deviceName = prefs.getString(KEY_DEVICE, "");
        int userId = prefs.getInt(KEY_USER_ID, -1);
        try {
            sendTokenToServer(apiKey, deviceName, token, userId, prefs);
        } catch (IOException e) {
            Toast.makeText(this, "Error updating Firebase Token: %s".formatted(e.getMessage()), Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    @Override
    public void onMessageReceived(@NonNull RemoteMessage message) {
        var string = message.getData().get("json");
        if (string == null) return;
        NotificationContent content = customGson.fromJson(string, NotificationContent.class);
        super.onMessageReceived(message);
    }
}