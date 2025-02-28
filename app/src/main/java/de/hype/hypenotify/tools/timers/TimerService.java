package de.hype.hypenotify.tools.timers;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import de.hype.hypenotify.ServerUtils;
import de.hype.hypenotify.core.IntentBuilder;
import de.hype.hypenotify.core.StaticIntents;
import de.hype.hypenotify.core.interfaces.MiniCore;

import java.lang.reflect.Type;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static android.content.Context.ALARM_SERVICE;


/**
 * Gson Adapter for handling Timer polymorphic serialization & deserialization.
 */
class TimerAdapter implements JsonSerializer<TimerWrapper>, JsonDeserializer<TimerWrapper> {
    private final MiniCore core;

    public TimerAdapter(MiniCore core) {
        this.core = core;
    }

    @Override
    public JsonElement serialize(TimerWrapper wrapped, Type typeOfSrc, JsonSerializationContext context) {
        BaseTimer src = wrapped.getBaseTimer();
        JsonObject obj = context.serialize(src, src.getClass()).getAsJsonObject();
        obj.addProperty("className", src.getClass().getName());
        return obj;
    }

    @Override
    public TimerWrapper deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        JsonObject obj = json.getAsJsonObject();
        String className = obj.get("className").getAsString();
        try {
            Class<?> clazz = Class.forName(className);
            BaseTimer timer = context.deserialize(obj, clazz);
            timer.onInit(core);
            return new TimerWrapper(timer, core.timerService());
        } catch (ClassNotFoundException e) {
            throw new JsonParseException("Unknown class: " + className, e);
        }
    }
}

/**
 * LocalTimer - Queries "test.de" and checks if "test" is true.
 */
class LocalTimer extends BaseTimer {
    public LocalTimer(UUID clientId, Instant time, String message) {
        super(clientId, null, time, message);
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
    public ServerTimer(UUID clientId, UUID serverId, Instant time, String message) {
        super(clientId, serverId, time, message);
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
public class TimerService {
    private final MiniCore core;
    private final DualKeyMap<UUID, UUID, TimerWrapper> timers = new DualKeyMap<>();
    private final AlarmManager alarmManager;
    private final Gson gson;
    private final Context context;

    public TimerService(MiniCore core) {
        this.core = core;
        this.context = core.context();
        this.alarmManager = (AlarmManager) context.getSystemService(ALARM_SERVICE);
        gson = new GsonBuilder()
                .registerTypeAdapter(Instant.class, new InstantAdapter())
                .registerTypeAdapter(BaseTimer.class, new TimerAdapter(core))
                .create();
        loadTimers();
    }

    /**
     * Get a new client ID for a timer. Guaranteed to be unique.
     */
    public UUID getNewClientId() {
        UUID id;
        do {
            id = UUID.randomUUID();
        } while (timers.containsPrimaryKey(id));
        return id;
    }

    public void addOrReplaceTimer(TimerWrapper timer) {
        TimerWrapper oldTimer = timers.removeByPrimary(timer.getClientId());
        oldTimer.cancel();
        timers.put(timer.getClientId(), timer.getServerId(), timer);
        scheduleTimer(timer);
        saveTimers();
    }

    /**
     * Keep in mind that this will only account for the client id and not the server id!
     *
     * @param timer The timer to cancel and remove.
     */
    public void cancelAndRemoveTimer(TimerWrapper timer) {
        cancelAndRemoveTimer(timer.getClientId());
    }

    public void cancelAndRemoveTimer(UUID clientId) {
        TimerWrapper timer = timers.removeByPrimary(clientId);
        if (timer != null) {
            cancelTimer(timer);
            saveTimers();
        }
    }

    private void cancelTimer(TimerWrapper timer) {
        PendingIntent pendingIntent = getSchedulingIntent(timer);
        if (alarmManager != null) {
            alarmManager.cancel(pendingIntent);
        }
    }

    @SuppressLint("MissingPermission")
    private void scheduleTimer(TimerWrapper timer) {
        if (alarmManager != null) {
            PendingIntent pendingIntent = getSchedulingIntent(timer);
            AlarmManager.AlarmClockInfo alarmClockInfo = new AlarmManager.AlarmClockInfo(timer.getTime().toEpochMilli(), pendingIntent); // Set alarm to go off in 1 minute
            alarmManager.setAlarmClock(alarmClockInfo, pendingIntent);
            AlarmManager.AlarmClockInfo next = alarmManager.getNextAlarmClock();
        }
    }

    private PendingIntent getSchedulingIntent(TimerWrapper timer) {
        IntentBuilder intent = StaticIntents.TIMER_HIT.getAsIntent(context);
        intent.putExtra("timerId", timer.getClientId().toString());
        return intent.getAsPending();
    }

    private void saveTimers() {
        core.saveData("timers", gson.toJson(timers));
    }

    private void loadTimers() {
        DualKeyMap<UUID, UUID, TimerWrapper> loadedTimers = gson.fromJson(core.getStringData("timers"), new TypeToken<DualKeyMap<Integer, Integer, TimerWrapper>>() {
        }.getType());
        if (loadedTimers != null) {
            timers.putAll(loadedTimers);
        }
        List<BaseTimer> timer = ServerUtils.getServerTimers(core);
        for (BaseTimer t : timer) {
            t.onInit(core);
            timers.put(t.getClientId(), t.getServerId(), new TimerWrapper(t, this));
        }
    }

    public TimerWrapper getTimerByClientId(UUID clientId) {
        return timers.getPrimary(clientId);
    }

    public void addTimer(JsonElement replacementTimer) {
        BaseTimer timer = gson.fromJson(replacementTimer, BaseTimer.class);
        timer.onInit(core);
        timers.put(timer.getClientId(), timer.getServerId(), new TimerWrapper(timer, this));
        saveTimers();
    }

    public void createNewTimer(ICreateTimer timer) {
        UUID clientId = getNewClientId();
        TimerWrapper timerWrapper = new TimerWrapper(timer.createTimer(clientId), this);
        timers.put(timerWrapper.getClientId(), timerWrapper.getServerId(), timerWrapper);
        saveTimers();
    }

    @FunctionalInterface
    public interface ICreateTimer {
        BaseTimer createTimer(UUID clientId);
    }
}

