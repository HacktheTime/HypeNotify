package de.hype.hypenotify.app

import de.hype.hypenotify.app.core.interfaces.MiniCore
import de.hype.hypenotify.app.tools.timers.AlwaysRingingTimer
import de.hype.hypenotify.app.tools.timers.TimerService.ICreateTimer
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.*

class DebugThread(core: MiniCore) : Thread("Debug Thread") {
    private val core: MiniCore

    /**
     * Can be used to temporarily store objects for debugging purposes. such as variables so you dont need to restart the app creating it.
     */
    private val storage: MutableList<Any?> = ArrayList<Any?>()

    init {
        this.core = core
    }

    override fun run() {
        var passedOnce = false
        while (true) {
            try {
                sleep(3000)
                passedOnce = true
            } catch (e: Throwable) {
                break
            }
        }
    }

    fun test() {
        try {
            val service = core.timerService()
            service.createNewTimer(ICreateTimer { id: UUID? -> AlwaysRingingTimer(id, Instant.now().plus(5, ChronoUnit.SECONDS), "Test") })
        } catch (e: Throwable) {
            e.printStackTrace()
        }
    }
}
