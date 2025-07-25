package de.hype.hypenotify.app.core;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
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
import de.hype.hypenotify.app.Config;
import de.hype.hypenotify.app.DebugThread;
import de.hype.hypenotify.app.ExecutionService;
import de.hype.hypenotify.app.NotificationUtils;
import de.hype.hypenotify.app.skyblockconstants.NeuRepoManager;
import de.hype.hypenotify.app.tools.bazaar.BazaarService;
import de.hype.hypenotify.app.tools.notification.*;
import de.hype.hypenotify.app.tools.timers.TimerService;

import java.util.Calendar;
import java.util.concurrent.ExecutionException;

import static android.content.Context.MODE_PRIVATE;
import static de.hype.hypenotify.app.core.Constants.*;

abstract class MiniCore implements de.hype.hypenotify.app.core.interfaces.MiniCore {
    protected String fireBaseToken;
    protected WakeLockManager wakeLock;
    protected String deviceName;
    protected int userId;
    protected String userAPIKey;
    public static final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    public BackgroundService context;
    protected SharedPreferences prefs;
    public final ExecutionService executionService = new ExecutionService(30);

    protected BazaarService bazaarService = new BazaarService(this);
    protected TimerService timerService;
    protected Config config;
    private DebugThread debugThread = new DebugThread(this);

    protected MiniCore(BackgroundService context) {
        this.context = context;
        prefs = context.getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        userAPIKey = prefs.getString(KEY_API, "");
        userId = prefs.getInt(KEY_USER_ID, -1);
        config = new Config(this);
        deviceName = prefs.getString(KEY_DEVICE, "");
        wakeLock = new WakeLockManager(this);
        timerService = new TimerService(this);
        scheduleDailyBatteryCheck();
        debugThread.setName("DebugThread");
        debugThread.start();
        NotificationUtils.synchronizeNotificationChannels(context);
        NotificationBuilder builder = new NotificationBuilder(context, "Title here", "message here", NotificationChannels.BAZAAR_TRACKER);
        builder.setAction(StaticIntents.LAUNCH_BAZAAR.getAsIntent(context).getAsPending());
        builder.setVisibility(NotificationVisibility.PUBLIC);
        builder.setAlertOnlyOnce(false);
        builder.setGroupAlertBehaviour(GroupBehaviour.GROUP_ALERT_ALL);
        builder.setPriority(Priority.HIGH);
        builder.send();
        executionService.execute(() -> {
            NeuRepoManager.INSTANCE.init(this);
        });
    }

    public WifiInfo getCurrentWifiNetwork() {
        ConnectivityManager manager = context.getSystemService(ConnectivityManager.class);
        ;
        Network currentNetwork = manager.getActiveNetwork();
        if (currentNetwork == null) return null;
        NetworkCapabilities networkCapabilities = manager.getNetworkCapabilities(currentNetwork);
        if (networkCapabilities.getTransportInfo() instanceof WifiInfo) {
            return ((WifiInfo) networkCapabilities.getTransportInfo());
        }
        return null;
    }

    public boolean isInFreeNetwork() {
        WifiInfo wifiInfo = getCurrentWifiNetwork();
        if (wifiInfo == null) return false;
        if (wifiInfo.isRestricted()) {
            return false;
        }
        return true;
    }

    public void scheduleDailyBatteryCheck() {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        IntentBuilder intent = DynamicIntents.BATTERY_REMINDER_CHECK.getAsIntent(context);

        PendingIntent pendingIntent = intent.getAsPending();

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 19); // 7 PM
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);

        if (alarmManager != null) {
            alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY, pendingIntent);
        }
    }

    public void fullInit() throws ExecutionException, InterruptedException {
        Task<String> tokenTask = FirebaseMessaging.getInstance().getToken();
        Tasks.await(tokenTask);
        fireBaseToken = tokenTask.getResult();
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

    public String getStringData(String key) {
        return prefs.getString(key, null);
    }

    @Override
    public Config config() {
        return config;
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

    @Override
    public Gson gson() {
        return gson;
    }

    @Override
    public void setUserData(int userId, String bbAPIKey, String deviceName) {
        this.userId = userId;
        this.userAPIKey = bbAPIKey;
        this.deviceName = deviceName;
        saveConfig();
    }

    public void onDestroy() {
        executionService.shutdown();
        debugThread.interrupt();
        wakeLock.onDestroy();
    }
}
