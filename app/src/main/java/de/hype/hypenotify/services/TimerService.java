package de.hype.hypenotify.services;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;
import de.hype.hypenotify.ServerUtils;
import de.hype.hypenotify.TimerData;
import de.hype.hypenotify.core.IntentBuilder;
import de.hype.hypenotify.core.StaticIntents;
import de.hype.hypenotify.core.interfaces.MiniCore;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import static android.content.Context.ALARM_SERVICE;

public class TimerService {
    public Map<Integer, SmartTimer> timers;
    private final MiniCore core;
    private AlarmManager alarmManager;
    private Context context;
    public TimerService(MiniCore core){
        this.core = core;
        this.context = core.context();
        this.alarmManager = (AlarmManager) context.getSystemService(ALARM_SERVICE);
        loadTimers();
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
        PendingIntent pendingIntent = getSchedulingIntent(timer);
        if (alarmManager != null) {
            alarmManager.cancel(pendingIntent);
        }
    }

    @SuppressLint("MissingPermission")
    private void scheduleTimer(TimerData timer) {
        if (alarmManager != null) {
            PendingIntent pendingIntent = getSchedulingIntent(timer);
            AlarmManager.AlarmClockInfo alarmClockInfo = new AlarmManager.AlarmClockInfo(timer.getTime().toEpochMilli(), pendingIntent); // Set alarm to go off in 1 minute
            alarmManager.setAlarmClock(alarmClockInfo, pendingIntent);
            AlarmManager.AlarmClockInfo next = alarmManager.getNextAlarmClock();
        }
    }

    private PendingIntent getSchedulingIntent(TimerData timer) {
        IntentBuilder intent = StaticIntents.TIMER_HIT.getAsIntent(context);
        intent.putExtra("timerId", timer.id);
        return intent.getAsPending();
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

        public boolean shallRing() {
            return active;
        }
    }

    private void addReplacementTimer(JsonElement replacementTimer) {
        //TODO implement replacement timers
    }
}