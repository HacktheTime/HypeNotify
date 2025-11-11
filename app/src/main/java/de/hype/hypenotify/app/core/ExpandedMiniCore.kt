package de.hype.hypenotify.app.core

import android.content.Context
import de.hype.hypenotify.app.ExecutionService
import de.hype.hypenotify.app.tools.bazaar.BazaarService
import de.hype.hypenotify.app.tools.timers.TimerService

class ExpandedMiniCore(context: BackgroundService) : MiniCore(context) {
    override fun context(): Context? {
        return super.context
    }

    override fun timerService(): TimerService? {
        return super.timerService
    }

    override fun wakeLock(): WakeLockManager? {
        return super.wakeLock
    }

    override fun userAPIKey(): String? {
        return super.userAPIKey
    }

    override fun userId(): Int {
        return super.userId
    }

    override fun setUserData(userId: Int, bbAPIKey: String?, deviceName: String?) {
        super.setUserData(userId, bbAPIKey, deviceName)
    }

    override fun bazaarService(): BazaarService? {
        return super.bazaarService
    }

    override fun executionService(): ExecutionService? {
        return super.executionService
    }

    private var lowBatteryStop = false

    override fun isLowBatteryStop(): Boolean {
        return lowBatteryStop
    }

    override fun setLowBatteryStop(stop: Boolean) {
        this.lowBatteryStop = stop
    }
}
