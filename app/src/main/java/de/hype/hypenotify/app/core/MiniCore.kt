package de.hype.hypenotify.app.core

import android.content.Context
import android.content.SharedPreferences
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.wifi.WifiInfo
import android.os.BatteryManager
import com.google.android.gms.tasks.Tasks
import com.google.firebase.messaging.FirebaseMessaging
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import de.hype.hypenotify.app.Config
import de.hype.hypenotify.app.DebugThread
import de.hype.hypenotify.app.ExecutionService
import de.hype.hypenotify.app.NotificationUtils
import de.hype.hypenotify.app.core.interfaces.MiniCore
import de.hype.hypenotify.app.skyblockconstants.NeuRepoManager.init
import de.hype.hypenotify.app.tools.bazaar.BazaarService
import de.hype.hypenotify.app.tools.notification.GroupBehaviour
import de.hype.hypenotify.app.tools.notification.NotificationBuilder
import de.hype.hypenotify.app.tools.notification.NotificationChannels
import de.hype.hypenotify.app.tools.notification.NotificationVisibility
import de.hype.hypenotify.app.tools.notification.Priority
import de.hype.hypenotify.app.tools.timers.TimerService
import java.util.concurrent.ExecutionException
import java.util.concurrent.TimeUnit

internal abstract class MiniCore protected constructor(var context: BackgroundService) : MiniCore {
    protected var fireBaseToken: String? = null
    var wakeLock: WakeLockManager
    protected var deviceName: String
    var userId: Int
    var userAPIKey: String
    protected var prefs: SharedPreferences
    val executionService: ExecutionService = ExecutionService(5)

    var bazaarService: BazaarService = BazaarService(this)
    var timerService: TimerService?
    protected var config: Config?
    private val debugThread = DebugThread(this)

    init {
        prefs = context.getSharedPreferences(Constants.PREFS_NAME, Context.MODE_PRIVATE)
        userAPIKey = prefs.getString(Constants.KEY_API, "")!!
        userId = prefs.getInt(Constants.KEY_USER_ID, -1)
        config = Config(this)
        deviceName = prefs.getString(Constants.KEY_DEVICE, "")!!
        wakeLock = WakeLockManager(this)
        timerService = TimerService(this)
        StaticIntents.Companion.scheduleDailyBatteryCheck(this, true)
        debugThread.setName("DebugThread")
        debugThread.start()
        NotificationUtils.synchronizeNotificationChannels(context)
        val builder = NotificationBuilder(context, "Title here", "message here", NotificationChannels.BAZAAR_TRACKER)
        builder.setAction(StaticIntents.LAUNCH_BAZAAR.getAsIntent(context).getAsPending())
        builder.setVisibility(NotificationVisibility.Companion.PUBLIC)
        builder.setAlertOnlyOnce(false)
        builder.setGroupAlertBehaviour(GroupBehaviour.GROUP_ALERT_ALL)
        builder.setPriority(Priority.HIGH)
        builder.send()
        executionService.execute(Runnable {
            init(this)
        })

        // Periodic battery watcher: pause tracking while battery is low (<=15%) and resume when >15% or charging
        executionService.scheduleAtFixedRate(Runnable {
            try {
                val bm = context.getSystemService(Context.BATTERY_SERVICE) as BatteryManager?
                if (bm == null) return@scheduleAtFixedRate
                val pct = bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
                val charging = bm.isCharging()

                if (!charging && pct <= 15) {
                    // If tracking is active, pause it to avoid draining the device further
                    try {
                        bazaarService.pauseTrackingForLowBattery()
                    } catch (ignored: Exception) {
                    }
                } else {
                    // battery recovered or charging -> resume tracking
                    try {
                        bazaarService.resumeTrackingAfterBatteryOK()
                    } catch (ignored: Exception) {
                    }
                }
            } catch (t: Throwable) {
                // swallow errors from battery check
            }
        }, 10, 60, TimeUnit.SECONDS)
    }

    val currentWifiNetwork: WifiInfo?
        get() {
            val manager = context.getSystemService<ConnectivityManager>(ConnectivityManager::class.java)

            val currentNetwork = manager.getActiveNetwork()
            if (currentNetwork == null) return null
            val networkCapabilities: NetworkCapabilities = manager.getNetworkCapabilities(currentNetwork)!!
            if (networkCapabilities.getTransportInfo() is WifiInfo) {
                return (networkCapabilities.getTransportInfo() as WifiInfo?)
            }
            return null
        }

    override fun isInFreeNetwork(): Boolean {
        val wifiInfo = this.currentWifiNetwork
        if (wifiInfo == null) return false
        if (wifiInfo.isRestricted()) {
            return false
        }
        return true
    }

    @Throws(ExecutionException::class, InterruptedException::class)
    fun fullInit() {
        val tokenTask = FirebaseMessaging.getInstance().getToken()
        Tasks.await<String?>(tokenTask)
        fireBaseToken = tokenTask.getResult()
    }

    override fun saveData(key: String?, data: Any?) {
        val editor = prefs.edit()
        val json: String? = gson.toJson(data)
        editor.putString(key, json)
        editor.apply()
    }

    fun <T> getData(key: String?, clazz: Class<T?>): T? {
        val json: String = prefs.getString(key, "")!!
        if (json.isEmpty()) {
            return null
        }
        return gson.fromJson<T?>(json, clazz)
    }

    override fun <T> getData(key: String?, clazz: TypeToken<T?>): T? {
        val json: String = prefs.getString(key, "")!!
        if (json.isEmpty()) {
            return null
        }
        return gson.fromJson<T?>(json, clazz.getType())
    }

    override fun getStringData(key: String?): String {
        return prefs.getString(key, null)!!
    }

    override fun config(): Config? {
        return config
    }

    override fun areKeysSet(): Boolean {
        if (userAPIKey.isEmpty() || userId == -1 || deviceName.isEmpty()) {
            return false
        }
        return true
    }

    fun saveConfig() {
        val editor = prefs.edit()
        editor.putString(Constants.KEY_API, userAPIKey)
        editor.putInt(Constants.KEY_USER_ID, userId)
        editor.putString(Constants.KEY_DEVICE, deviceName)
        editor.apply()
    }

    override fun gson(): Gson {
        return gson
    }

    override fun setUserData(userId: Int, bbAPIKey: String, deviceName: String) {
        this.userId = userId
        this.userAPIKey = bbAPIKey
        this.deviceName = deviceName
        saveConfig()
    }

    fun onDestroy() {
        executionService.shutdown()
        debugThread.interrupt()
        wakeLock.onDestroy()
    }

    companion object {
        val gson: Gson = GsonBuilder().setPrettyPrinting().create()
    }
}