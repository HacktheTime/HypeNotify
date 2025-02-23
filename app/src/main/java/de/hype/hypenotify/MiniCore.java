package de.hype.hypenotify;

import android.accessibilityservice.AccessibilityService;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.wifi.WifiInfo;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import de.hype.hypenotify.services.HypeNotifyServiceConnection;
import de.hype.hypenotify.services.TimerService;
import de.hype.hypenotify.tools.bazaar.BazaarService;

import java.util.Calendar;
import java.util.concurrent.ExecutionException;

import static android.content.Context.MODE_PRIVATE;
import static de.hype.hypenotify.Constants.*;
import static de.hype.hypenotify.Constants.KEY_DEVICE;

public class MiniCore {
    public String fireBaseToken;
    public WakeLockManager wakeLock;
    public String deviceName;
    public int userId;
    public String userAPIKey;
    public Gson gson = new GsonBuilder().setPrettyPrinting().create();
    public Context context;
    private SharedPreferences prefs;
    public ExecutionService executionService = new ExecutionService(30);

    private BazaarService bazaarService = new BazaarService(this);
    private NotificationService notificationService;
    public TimerService timerService;

    public MiniCore(Context context) {
        this.context = context;
        prefs = context.getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        userAPIKey = prefs.getString(KEY_API, "");
        userId = prefs.getInt(KEY_USER_ID, -1);
        deviceName = prefs.getString(KEY_DEVICE, "");
        wakeLock = new WakeLockManager(this);
        notificationService = new NotificationService(this);
        scheduleDailyBatteryCheck();
    }
    public WifiInfo getCurrentWifiNetwork() {
        ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        Network currentNetwork = manager.getActiveNetwork();
        if (currentNetwork == null) return null;
        NetworkCapabilities networkCapabilities = manager.getNetworkCapabilities(currentNetwork);
        if (networkCapabilities.getTransportInfo() instanceof WifiInfo) {
            return ((WifiInfo) networkCapabilities.getTransportInfo());
        }
        return null;
    }

    public boolean isInHomeNetwork() {
        return PrivateConfig.isHomeNetwork(getCurrentWifiNetwork());
    }

    public void scheduleDailyBatteryCheck() {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = Intents.BATTERY_REMINDER_CHECK.getAsIntent(context);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 19); // 7 PM
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);

        if (alarmManager != null) {
            alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY, pendingIntent);
        }
    }

    public ServiceConnection getServiceConnection() {
        return new HypeNotifyServiceConnection(this);
    }

    public void fullInit() throws ExecutionException, InterruptedException {
        Task<String> tokenTask = FirebaseMessaging.getInstance().getToken();
        Tasks.await(tokenTask);
        fireBaseToken = tokenTask.getResult();
    }

    public BazaarService getBazaarService() {
        return bazaarService;
    }

    public void saveData(String key, Object data) {
        SharedPreferences.Editor editor = prefs.edit();
        String json = gson.toJson(data);
        editor.putString(key, json);
        editor.apply();
    }

    public <T> T getData(String key, Class<T> clazz) {
        String json = prefs.getString(key, "");
        if (json.isEmpty()) {
            return null;
        }
        return gson.fromJson(json, clazz);
    }
    public <T> T getData(String key, TypeToken<T> clazz) {
        String json = prefs.getString(key, "");
        if (json.isEmpty()) {
            return null;
        }
        return gson.fromJson(json, clazz.getType());
    }

    public boolean areKeysSet() {
        if (userAPIKey.isEmpty() || userId == -1 || deviceName.isEmpty()) {
            return false;
        }
        return true;
    }

    public void saveConfig() {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(KEY_API, userAPIKey);
        editor.putInt(KEY_USER_ID, userId);
        editor.putString(KEY_DEVICE, deviceName);
        editor.apply();
    }
}
