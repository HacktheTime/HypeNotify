package de.hype.hypenotify.app.tools.timers

import com.google.gson.JsonElement
import de.hype.hypenotify.R
import de.hype.hypenotify.app.core.interfaces.MiniCore
import java.time.Instant
import java.util.concurrent.TimeUnit

/**
 * Timer Wrapper - Manages timers without needing direct TimerService calls.
 */
class TimerWrapper(timer: BaseTimer, timerService: TimerService) {
    val baseTimer: BaseTimer
    private val timerService: TimerService

    init {
        this.baseTimer = timer
        this.timerService = timerService
    }

    val sound: Int
        get() = R.raw.alarm

    var time: Instant?
        get() = baseTimer.getTime()
        set(newTime) {
            timerService.cancelAndRemoveTimer(this)
            baseTimer.setTime(newTime)
            timerService.addOrReplaceTimer(this)
        }

    fun cancel() {
        timerService.cancelAndRemoveTimer(baseTimer.getClientId())
    }

    /**
     * @param core Core object to run custom checks in [.wouldRing]
     * return true if the timers condition matches as well as it not being deactivated.
     */
    fun shouldRing(core: MiniCore?): Boolean {
        return baseTimer.shouldRing(core)
    }

    /**
     * @param core Core object to run custom checks.
     * return true if the timers condition matches.
     *
     *
     * You may use blocking code or throw exceptions. if you do not return a false until the timer is supposed to ring it will ring anyway.
     */
    fun wouldRing(core: MiniCore?): Boolean {
        return baseTimer.wouldRing(core)
    }

    val clientId: UUID?
        get() = baseTimer.getClientId()

    val serverId: UUID?
        get() = baseTimer.getServerId()

    fun sleep(timeAmount: Int, timeUnit: TimeUnit) {
        cancel()
        baseTimer.time = Instant.now().plusSeconds(timeUnit.toSeconds(timeAmount.toLong()))
        timerService.addOrReplaceTimer(TimerWrapper(this.baseTimer, timerService))
    }

    val message: String?
        get() = baseTimer.message

    fun deactivate() {
        baseTimer.deactivate()
        cancel()
    }

    fun replaceTimer(replacementTimer: JsonElement?) {
        deactivate()
        timerService.addTimer(replacementTimer)
    }
}
