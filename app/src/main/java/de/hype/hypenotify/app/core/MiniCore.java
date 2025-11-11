package de.hype.hypenotify.app.core;

import android.Manifest;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.wifi.WifiInfo;
import android.os.BatteryManager;
import androidx.annotation.RequiresPermission;
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
    public final ExecutionService executionService = new ExecutionService(5);

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
        StaticIntents.scheduleDailyBatteryCheck(this,true);
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

        // Periodic battery watcher: pause tracking while battery is low (<=15%) and resume when >15% or charging
        executionService.scheduleAtFixedRate(() -> {
            try {
                BatteryManager bm = (BatteryManager) context.getSystemService(Context.BATTERY_SERVICE);
                if (bm == null) return;
                int pct = bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY);
                boolean charging = bm.isCharging();

                if (!charging && pct <= 15) {
                    // If tracking is active, pause it to avoid draining the device further
                    try {
                        bazaarService.pauseTrackingForLowBattery();
                    } catch (Exception ignored) {
                    }
                } else {
                    // battery recovered or charging -> resume tracking
                    try {
                        bazaarService.resumeTrackingAfterBatteryOK();
                    } catch (Exception ignored) {
                    }
                }
            } catch (Throwable t) {
                // swallow errors from battery check
            }
        }, 10, 60, java.util.concurrent.TimeUnit.SECONDS);
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