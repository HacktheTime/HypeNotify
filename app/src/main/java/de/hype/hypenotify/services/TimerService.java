package de.hype.hypenotify.services;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import androidx.core.app.NotificationCompat;
import com.google.gson.JsonElement;
import com.google.gson.internal.LinkedTreeMap;
import com.google.gson.reflect.TypeToken;
import de.hype.hypenotify.*;
import de.hype.hypenotify.tools.notification.NotificationBuilder;
import de.hype.hypenotify.tools.notification.NotificationChannels;

import java.lang.reflect.Type;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

public class TimerService extends HypeNotifyService<TimerService> {
    public Map<Integer, SmartTimer> timers;

    @Override
    public void onCreate() {
        super.onCreate();
        NotificationBuilder notificationBuilder = new NotificationBuilder(this, "Background", "A HypeNotify Service is running in the background.", NotificationChannels.BACKGROUND_SERVICE);
        notificationBuilder.setSmallIcon(R.mipmap.icon);
        notificationBuilder.setLargeImage(R.mipmap.icon);
        notificationBuilder.getHiddenBuilder().setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_DEFERRED);
        startForeground(1, notificationBuilder.build());
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // Your background task code here
        return START_NOT_STICKY;
    }

    private void saveTimers() {
        core.saveData("timers", timers);
    }

    private void addTimer(SmartTimer smartTimer) {
        timers.put(smartTimer.id, smartTimer);
        saveTimers();
        if (smartTimer.active) {
            scheduleTimer(smartTimer);
        }
    }

    private void modifyTimer(SmartTimer smartTimer) {
        timers.put(smartTimer.id, smartTimer);
        saveTimers();
        if (smartTimer.active) {
            scheduleTimer(smartTimer);
        } else {
            cancelTimer(smartTimer);
        }
    }

    private void cancelTimer(TimerData timer) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(ALARM_SERVICE);
        PendingIntent pendingIntent = getSchedulingIntent(timer);
        if (alarmManager != null) {
            alarmManager.cancel(pendingIntent);
        }
    }

    @SuppressLint("MissingPermission")
    private void scheduleTimer(TimerData timer) {
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

    @Override
    public void setCore(MiniCore core) {
        super.setCore(core);
        loadTimers();
    }

    private void loadTimers() {
        TypeToken<HashMap<Integer, SmartTimer>> type = new TypeToken<>() {
        };
        timers = core.getData("timers", type);
        if (timers == null) timers = new HashMap<>();
        ServerUtils.getTimers(core);
    }

    public SmartTimer getTimerById(Integer timerId) {
        return timers.get(timerId);
    }

    public void addAlarm(Instant time, String test, Supplier<Boolean> check) {
        SmartTimer timer = new SmartTimer(new TimerData(1, time, true), this);
        addTimer(timer);
    }

    public static class SmartTimer extends TimerData {
        private transient TimerService service;

        public SmartTimer(TimerData data, TimerService service) {
            super(data);
            this.service = service;
        }

        public void replaceTimer(JsonElement replacementTimer) {
            service.cancelTimer(this);
            service.addReplacementTimer(replacementTimer);
        }

        public void cancel() {
            service.cancelTimer(this);
        }

        public void sleep(int delay, TimeUnit timeUnit) {
            time += (timeUnit.toSeconds(delay));
            service.scheduleTimer(this);
        }
    }

    private void addReplacementTimer(JsonElement replacementTimer) {
        //TODO implement replacement timers
    }
}