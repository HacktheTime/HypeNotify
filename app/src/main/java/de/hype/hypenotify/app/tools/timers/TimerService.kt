package de.hype.hypenotify.app.tools.timers

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.AlarmManager.AlarmClockInfo
import android.app.PendingIntent
import android.content.Context
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonParseException
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import com.google.gson.reflect.TypeToken
import de.hype.hypenotify.app.ServerUtils
import de.hype.hypenotify.app.core.StaticIntents
import de.hype.hypenotify.app.core.interfaces.MiniCore
import java.lang.reflect.Type
import java.time.Instant
import java.util.*

/**
 * Gson Adapter for handling Timer polymorphic serialization & deserialization.
 */
internal class TimerAdapter(core: MiniCore) : JsonSerializer<TimerWrapper?>, JsonDeserializer<TimerWrapper?> {
    private val core: MiniCore

    init {
        this.core = core
    }

    override fun serialize(wrapped: TimerWrapper, typeOfSrc: Type?, context: JsonSerializationContext): JsonElement {
        val src = wrapped.getBaseTimer()
        val obj = context.serialize(src, src.javaClass).getAsJsonObject()
        obj.addProperty("className", src.javaClass.getName())
        return obj
    }

    @Throws(JsonParseException::class)
    override fun deserialize(json: JsonElement, typeOfT: Type?, context: JsonDeserializationContext): TimerWrapper {
        val obj = json.getAsJsonObject()
        val className = obj.get("className").getAsString()
        try {
            val clazz = Class.forName(className)
            val timer = context.deserialize<BaseTimer>(obj, clazz)
            timer.onInit(core)
            return TimerWrapper(timer, core.timerService())
        } catch (e: ClassNotFoundException) {
            throw JsonParseException("Unknown class: " + className, e)
        }
    }
}

/**
 * LocalTimer - Queries "test.de" and checks if "test" is true.
 */
internal class LocalTimer(clientId: UUID?, time: Instant?, message: String?) : BaseTimer(clientId, null, time, message) {
    override fun wouldRing(core: MiniCore?): Boolean {
//        String response = core.getHttp("https://test.de");
        val response = "{\"test\":true}"
        return !isDeactivated && response.contains("\"test\":true")
    }
}

/**
 * ServerTimer - Checks with the server at "test.de/checkTimer?id="
 */
internal class ServerTimer(clientId: UUID?, serverId: UUID?, time: Instant?, message: String?) :
    BaseTimer(clientId, serverId, time, message) {
    override fun wouldRing(core: MiniCore?): Boolean {
//        String response = core.getHttp("https://test.de/checkTimer?id=" + id);
        val response = "{\"ring\":true}"
        return !isDeactivated && response.contains("\"ring\":true")
    }
}

/**
 * Timer Service - Handles scheduling and cancellation.
 */
class TimerService(core: MiniCore) {
    private val core: MiniCore
    private val timers = DualKeyMap<UUID?, UUID?, TimerWrapper>()
    private val alarmManager: AlarmManager?
    private val gson: Gson
    private val context: Context

    init {
        this.core = core
        this.context = core.context()
        this.alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager?
        gson = GsonBuilder()
            .registerTypeAdapter(Instant::class.java, InstantAdapter())
            .registerTypeAdapter(BaseTimer::class.java, TimerAdapter(core))
            .create()
        loadTimers()
    }

    val newClientId: UUID?
        /**
         * Get a new client ID for a timer. Guaranteed to be unique.
         */
        get() {
            var id: UUID?
            do {
                id = UUID.randomUUID()
            } while (timers.containsPrimaryKey(id))
            return id
        }

    fun addOrReplaceTimer(timer: TimerWrapper) {
        val oldTimer = timers.removeByPrimary(timer.getClientId())
        oldTimer.cancel()
        timers.put(timer.getClientId(), timer.getServerId(), timer)
        scheduleTimer(timer)
        saveTimers()
    }

    /**
     * Keep in mind that this will only account for the client id and not the server id!
     *
     * @param timer The timer to cancel and remove.
     */
    fun cancelAndRemoveTimer(timer: TimerWrapper) {
        cancelAndRemoveTimer(timer.getClientId())
    }

    fun cancelAndRemoveTimer(clientId: UUID?) {
        val timer = timers.removeByPrimary(clientId)
        if (timer != null) {
            cancelTimer(timer)
            saveTimers()
        }
    }

    private fun cancelTimer(timer: TimerWrapper) {
        val pendingIntent = getSchedulingIntent(timer)
        if (alarmManager != null) {
            alarmManager.cancel(pendingIntent!!)
        }
    }

    @SuppressLint("MissingPermission")
    private fun scheduleTimer(timer: TimerWrapper) {
        if (alarmManager != null) {
            val pendingIntent = getSchedulingIntent(timer)
            val alarmClockInfo = AlarmClockInfo(timer.getTime().toEpochMilli(), pendingIntent) // Set alarm to go off in 1 minute
            alarmManager.setAlarmClock(alarmClockInfo, pendingIntent!!)
            val next = alarmManager.getNextAlarmClock()
        }
    }

    private fun getSchedulingIntent(timer: TimerWrapper): PendingIntent? {
        val intent = StaticIntents.TIMER_HIT.getAsIntent(context)
        intent.putExtra("timerId", timer.getClientId().toString())
        return intent.getAsPending()
    }

    private fun saveTimers() {
        core.saveData("timers", gson.toJson(timers))
    }

    private fun loadTimers() {
        val loadedTimers =
            gson.fromJson<DualKeyMap<UUID?, UUID?, TimerWrapper?>?>(
                core.getStringData("timers"),
                object : TypeToken<DualKeyMap<Int?, Int?, TimerWrapper?>?>() {
                }.getType()
            )
        if (loadedTimers != null) {
            timers.putAll(loadedTimers)
        }
        val timer = ServerUtils.getServerTimers(core)
        for (t in timer) {
            t.onInit(core)
            timers.put(t.getClientId(), t.getServerId(), TimerWrapper(t, this))
        }
    }

    fun getTimerByClientId(clientId: UUID?): TimerWrapper? {
        return timers.getPrimary(clientId)
    }

    fun addTimer(replacementTimer: JsonElement?) {
        val timer = gson.fromJson<BaseTimer>(replacementTimer, BaseTimer::class.java)
        timer.onInit(core)
        timers.put(timer.getClientId(), timer.getServerId(), TimerWrapper(timer, this))
        saveTimers()
    }

    fun createNewTimer(timer: ICreateTimer) {
        val clientId = this.newClientId
        val timerWrapper = TimerWrapper(timer.createTimer(clientId), this)
        timers.put(timerWrapper.getClientId(), timerWrapper.getServerId(), timerWrapper)
        saveTimers()
    }

    fun interface ICreateTimer {
        fun createTimer(clientId: UUID?): BaseTimer?
    }
}

