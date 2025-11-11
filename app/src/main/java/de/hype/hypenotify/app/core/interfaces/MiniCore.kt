package de.hype.hypenotify.app.core.interfaces

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import de.hype.hypenotify.app.Config
import de.hype.hypenotify.app.ExecutionService
import de.hype.hypenotify.app.core.WakeLockManager
import de.hype.hypenotify.app.tools.bazaar.BazaarService
import de.hype.hypenotify.app.tools.timers.TimerService

interface MiniCore {
    fun context(): Context

    fun areKeysSet(): Boolean

    fun timerService(): TimerService

    fun wakeLock(): WakeLockManager

    fun userAPIKey(): String?

    fun userId(): Int

    fun setUserData(userId: Int, bbAPIKey: String, deviceName: String)

    fun bazaarService(): BazaarService

    val isInFreeNetwork: Boolean

    fun executionService(): ExecutionService

    fun saveData(key: String, data: Any?)

    fun gson(): Gson?

    fun <T> getData(timers: String, type: TypeToken<T>): T?

    fun getStringData(key: String): String?

    fun config(): Config

    // Mark that the service was stopped due to low battery and should not be auto-restarted.
    // Query whether a low-battery stop was set previously.
    var isLowBatteryStop: Boolean
}
