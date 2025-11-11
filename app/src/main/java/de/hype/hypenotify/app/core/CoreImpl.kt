package de.hype.hypenotify.app.core

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import de.hype.hypenotify.app.Config
import de.hype.hypenotify.app.ExecutionService
import de.hype.hypenotify.app.MainActivity
import de.hype.hypenotify.app.core.interfaces.Core
import de.hype.hypenotify.app.tools.bazaar.BazaarService
import de.hype.hypenotify.app.tools.timers.TimerService

class CoreImpl // Load stored values
    (var context: MainActivity?, var miniCore: MiniCore?) : Core {
    private val TAG = "CoreImpl"

    override fun context(): MainActivity? {
        return context
    }

    override fun areKeysSet(): Boolean {
        return miniCore!!.areKeysSet()
    }

    override fun timerService(): TimerService? {
        return miniCore!!.timerService
    }

    override fun wakeLock(): WakeLockManager? {
        return miniCore!!.wakeLock
    }

    override fun userAPIKey(): String? {
        return miniCore!!.userAPIKey
    }

    override fun userId(): Int {
        return miniCore!!.userId
    }

    override fun setUserData(userId: Int, bbAPIKey: String?, deviceName: String?) {
        miniCore!!.setUserData(userId, bbAPIKey, deviceName)
    }

    override fun bazaarService(): BazaarService? {
        return miniCore!!.bazaarService
    }

    override fun isInFreeNetwork(): Boolean {
        return miniCore!!.isInFreeNetwork()
    }

    override fun executionService(): ExecutionService {
        return miniCore!!.executionService
    }

    override fun onDestroy() {
    }

    override fun saveData(key: String?, data: Any?) {
        miniCore!!.saveData(key, data)
    }

    override fun gson(): Gson? {
        return miniCore!!.gson()
    }

    override fun <T> getData(timers: String?, type: TypeToken<T?>?): T? {
        return miniCore!!.getData<T?>(timers, type)
    }

    override fun getStringData(key: String?): String? {
        return miniCore!!.getStringData(key)
    }

    override fun config(): Config? {
        return miniCore!!.config()
    }


    override fun isLowBatteryStop(): Boolean {
        return miniCore != null && miniCore!!.isLowBatteryStop()
    }

    override fun setLowBatteryStop(stop: Boolean) {
        if (miniCore != null) {
            miniCore!!.setLowBatteryStop(stop)
        }
    }
}
