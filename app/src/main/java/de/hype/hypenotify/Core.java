package de.hype.hypenotify;

import android.annotation.SuppressLint;
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
import de.hype.hypenotify.services.HypeNotifyServiceConnection;
import de.hype.hypenotify.services.TimerService;
import de.hype.hypenotify.tools.bazaar.BazaarService;

import java.util.Calendar;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import static android.content.Context.ALARM_SERVICE;
import static android.content.Context.MODE_PRIVATE;
import static de.hype.hypenotify.Constants.*;

public class Core {
    private static Core INSTANCE;
    public MainActivity context;
    public Map<Integer, TimerData> timers;
    public WakeLockManager wakeLock;
    private String TAG = "Core";
    public String fireBaseToken;
    String deviceName;
    public int userId;
    public String userAPIKey;
    public Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private SharedPreferences prefs;
    public ExecutionService executionService = new ExecutionService(30);
    private DebugThread debugThread = new DebugThread(this);
    public TimerService timerService;
    private BazaarService bazaarService = new BazaarService(this);
    private NotificationService notificationService;


    public Core(MainActivity context) throws ExecutionException, InterruptedException {
        this.context = context;
        INSTANCE = this;
        // Load stored values
        prefs = context.getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        userAPIKey = prefs.getString(KEY_API, "");
        userId = prefs.getInt(KEY_USER_ID, -1);
        deviceName = prefs.getString(KEY_DEVICE, "");
        wakeLock = new WakeLockManager(this);
        notificationService = new NotificationService(this);
        debugThread.setDaemon(true);
        debugThread.setName("DebugThread");
        debugThread.start();
        scheduleDailyBatteryCheck();
    }

    public static Core getInstance() {
        return INSTANCE;
    }

    public boolean areKeysSet() {
        if (userAPIKey.isEmpty() || userId == -1 || deviceName.isEmpty()) {
            return false;
        }
        return true;
    }

    void loadTimers() {
        String json = prefs.getString("timers", "{}");
        Gson gson = new Gson();
        timers = gson.fromJson(json, Map.class);
        for (TimerData timer : timers.values()) {
            if (timer.active) {
                scheduleTimer(timer);
            }
        }

        ServerUtils.getTimers(this);
    }


    private void saveTimers() {
        SharedPreferences.Editor editor = prefs.edit();
        Gson gson = new Gson();
        String json = gson.toJson(timers);
        editor.putString("timers", json);
        editor.apply();
    }

    private void addTimer(TimerData timer) {
        timers.put(timer.id, timer);
        saveTimers();
        if (timer.active) {
            scheduleTimer(timer);
        }
    }

    public void modifyTimer(TimerData timer) {
        timers.put(timer.id, timer);
        saveTimers();
        if (timer.active) {
            scheduleTimer(timer);
        } else {
            cancelTimer(timer);
        }
    }

    public void cancelTimer(TimerData timer) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(ALARM_SERVICE);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context, timer.id, Intents.TIMER_HIT.getAsIntent(context),
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
        if (alarmManager != null) {
            alarmManager.cancel(pendingIntent);
        }
    }

    @SuppressLint("MissingPermission")
    public void scheduleTimer(TimerData timer) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager != null) {
            PendingIntent pendingIntent = getSchedulingIntent(timer);
            AlarmManager.AlarmClockInfo alarmClockInfo = new AlarmManager.AlarmClockInfo(timer.getTime().toEpochMilli(), pendingIntent); // Set alarm to go off in 1 minute
            alarmManager.setAlarmClock(alarmClockInfo, pendingIntent);
        }
    }

    private PendingIntent getSchedulingIntent(TimerData timer) {
        Intent intent = Intents.TIMER_HIT.getAsIntent(context);
        intent.putExtra("timerId", timer.id);
        return PendingIntent.getActivity(
                context, timer.id, intent,
                PendingIntent.FLAG_MUTABLE
        );
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
        Intent intent = new Intent(context, DailyChargeCheckReceiver.class);
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
}
