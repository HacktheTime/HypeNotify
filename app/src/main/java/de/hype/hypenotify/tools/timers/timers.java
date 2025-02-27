package de.hype.hypenotify.tools.timers;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import de.hype.hypenotify.core.IntentBuilder;
import de.hype.hypenotify.core.StaticIntents;
import de.hype.hypenotify.core.interfaces.MiniCore;

import java.lang.reflect.Type;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static android.content.Context.ALARM_SERVICE;


/**
 * Gson Adapter for handling Timer polymorphic serialization & deserialization.
 */
class TimerAdapter implements JsonSerializer<BaseTimer>, JsonDeserializer<BaseTimer> {
    @Override
    public JsonElement serialize(BaseTimer src, Type typeOfSrc, JsonSerializationContext context) {
        JsonObject obj = context.serialize(src, src.getClass()).getAsJsonObject();
        obj.addProperty("className", src.getClass().getName());
        return obj;
    }

    @Override
    public BaseTimer deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        JsonObject obj = json.getAsJsonObject();
        String className = obj.get("className").getAsString();
        try {
            Class<?> clazz = Class.forName(className);
            return context.deserialize(obj, clazz);
        } catch (ClassNotFoundException e) {
            throw new JsonParseException("Unknown class: " + className, e);
        }
    }
}

/**
 * Abstract Base Timer
 */
abstract class BaseTimer {
    protected int id;
    protected Instant time;
    protected String message;
    protected boolean hasSleepButton;
    protected boolean isDeactivated;

    public BaseTimer(int id, Instant time, String message, boolean hasSleepButton, boolean isDeactivated) {
        this.id = id;
        this.time = time;
        this.message = message;
        this.hasSleepButton = hasSleepButton;
        this.isDeactivated = isDeactivated;
    }

    public BaseTimer(int id, Instant time, String message) {
        this(id, time, message, true, false);
    }

    public int getId() {
        return id;
    }

    public Instant getTime() {
        return time;
    }

    public void setTime(Instant newTime) {
        this.time = newTime;
    }

    public boolean isDeactivated() {
        return isDeactivated;
    }

    public final boolean shouldRing(MiniCore core) {
        return !isDeactivated() && wouldRing(core);
    }

    public abstract boolean wouldRing(MiniCore core);

    public void deactivate() {
        isDeactivated = true;
    }

    public void onInit(MiniCore core) {
    }
}

/**
 * AlwaysRingingTimer - Rings at a set time, no conditions.
 */
class AlwaysRingingTimer extends BaseTimer {
    public AlwaysRingingTimer(int id, Instant time, String message) {
        super(id, time, message);
    }

    @Override
    public boolean wouldRing(MiniCore core) {
        return true;
    }
}

/**
 * LocalTimer - Queries "test.de" and checks if "test" is true.
 */
class LocalTimer extends BaseTimer {
    public LocalTimer(int id, Instant time, String message) {
        super(id, time, message);
    }

    @Override
    public boolean wouldRing(MiniCore core) {
//        String response = core.getHttp("https://test.de");
        String response = "{\"test\":true}";
        return !isDeactivated && response.contains("\"test\":true");
    }
}

/**
 * ServerTimer - Checks with the server at "test.de/checkTimer?id="
 */
class ServerTimer extends BaseTimer {
    public ServerTimer(int id, Instant time, String message) {
        super(id, time, message);
    }

    @Override
    public boolean wouldRing(MiniCore core) {
//        String response = core.getHttp("https://test.de/checkTimer?id=" + id);
        String response = "{\"ring\":true}";
        return !isDeactivated && response.contains("\"ring\":true");
    }
}

/**
 * Timer Service - Handles scheduling and cancellation.
 */
class TimerService {
    private final MiniCore core;
    private final Map<Integer, BaseTimer> timers = new ConcurrentHashMap<>();
    private final AlarmManager alarmManager;
    private final Context context;

    public TimerService(MiniCore core) {
        this.core = core;
        this.context = core.context();
        this.alarmManager = (AlarmManager) context.getSystemService(ALARM_SERVICE);
        loadTimers();
    }

    public void addOrReplaceTimer(BaseTimer timer) {
        timers.put(timer.getId(), timer);
        saveTimers();
        scheduleTimer(timer);
    }

    public void cancelAndRemoveTimer(int id) {
        BaseTimer timer = timers.remove(id);
        if (timer != null) {
            cancelTimer(timer);
            saveTimers();
        }
    }

    private void cancelTimer(BaseTimer timer) {
        PendingIntent pendingIntent = getSchedulingIntent(timer);
        if (alarmManager != null) {
            alarmManager.cancel(pendingIntent);
        }
    }

    @SuppressLint("MissingPermission")
    private void scheduleTimer(BaseTimer timer) {
        if (alarmManager != null) {
            PendingIntent pendingIntent = getSchedulingIntent(timer);
            AlarmManager.AlarmClockInfo alarmClockInfo = new AlarmManager.AlarmClockInfo(timer.getTime().toEpochMilli(), pendingIntent); // Set alarm to go off in 1 minute
            alarmManager.setAlarmClock(alarmClockInfo, pendingIntent);
            AlarmManager.AlarmClockInfo next = alarmManager.getNextAlarmClock();
        }
    }

    private PendingIntent getSchedulingIntent(BaseTimer timer) {
        IntentBuilder intent = StaticIntents.TIMER_HIT.getAsIntent(context);
        intent.putExtra("timerId", timer.id);
        return intent.getAsPending();
    }

    private void saveTimers() {
        Gson gson = new GsonBuilder()
                .registerTypeAdapter(Instant.class, new InstantAdapter())
                .registerTypeAdapter(BaseTimer.class, new TimerAdapter())
                .create();
        core.saveData("timers", gson.toJson(timers));
    }

    private void loadTimers() {
        Gson gson = new GsonBuilder()
                .registerTypeAdapter(Instant.class, new InstantAdapter())
                .registerTypeAdapter(BaseTimer.class, new TimerAdapter())
                .create();
        Map<Integer, BaseTimer> loadedTimers = gson.fromJson(core.getStringData("timers"), new TypeToken<Map<Integer, BaseTimer>>() {
        }.getType());
        if (loadedTimers != null) {
            timers.putAll(loadedTimers);
            timers.values().forEach(timer -> timer.onInit(core));
        }
    }
}

/**
 * Timer Wrapper - Manages timers without needing direct TimerService calls.
 */
class TimerWrapper {
    private BaseTimer timer;
    private TimerService timerService;

    public TimerWrapper(BaseTimer timer, TimerService timerService) {
        this.timer = timer;
        this.timerService = timerService;
    }

    public void setTime(Instant newTime) {
        timerService.cancelAndRemoveTimer(timer.getId());
        timer.setTime(newTime);
        timerService.addOrReplaceTimer(timer);
    }

    public Instant getTime() {
        return timer.getTime();
    }

    public void cancel() {
        timerService.cancelAndRemoveTimer(timer.getId());
    }

    public boolean isScheduledToRing(MiniCore core) {
        return timer.shouldRing(core);
    }

    public boolean wouldRing(MiniCore core) {
        return timer.wouldRing(core);
    }

    public int getId() {
        return timer.getId();
    }
}
