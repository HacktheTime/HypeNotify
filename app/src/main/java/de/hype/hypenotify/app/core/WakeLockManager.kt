package de.hype.hypenotify.app.core

import android.content.Context
import android.os.PowerManager
import android.os.PowerManager.WakeLock
import java.util.List
import java.util.function.Consumer

class WakeLockManager(core: MiniCore) {
    private val core: MiniCore?
    var powerManager: PowerManager
    var wakeLocks: MutableMap<WakeLockRequests?, WakeLock?> = HashMap<WakeLockRequests?, WakeLock?>()

    init {
        this.core = core
        powerManager = core.context.getSystemService(Context.POWER_SERVICE) as PowerManager
    }

    fun acquire(request: WakeLockRequests) {
        if (wakeLocks.get(request) != null) return
        val wakeLock = request.getWakeLock(powerManager)
        wakeLocks.put(request, wakeLock)
        val timeLimit = request.timeLockLimit
        if (timeLimit == null) wakeLock.acquire()
        else wakeLock.acquire(timeLimit * 1000L)
    }


    fun onDestroy() {
        releaseAll()
    }

    fun releaseAll() {
        val entries: MutableSet<MutableMap.MutableEntry<WakeLockRequests?, WakeLock?>?> = wakeLocks.entries
        entries.forEach(Consumer { entry: MutableMap.MutableEntry<WakeLockRequests?, WakeLock?>? ->
            val wakeLock = entry!!.value
            if (wakeLock != null && wakeLock.isHeld()) {
                wakeLock.release()
            }
        })
    }

    fun release(request: WakeLockRequests?) {
        val wakeLock = wakeLocks.get(request)
        if (wakeLock != null && wakeLock.isHeld()) {
            wakeLock.release()
        }
    }

    val activeWakeLocks: MutableList<WakeLockRequests?>
        get() = List.copyOf<WakeLockRequests?>(wakeLocks.keys)

    enum class WakeLockRequests(private val tagName: String) {
        TIMER_WAKE_LOCK("TimerWakeLock"),
        ;

        val asTag: String
            get() = "HypeNotify::" + tagName

        fun getWakeLock(powerManager: PowerManager): WakeLock {
            return powerManager.newWakeLock(this.wakeLockType, this.asTag)
        }

        val timeLockLimit: Int?
            get() = null

        val wakeLockType: Int
            get() = PowerManager.PARTIAL_WAKE_LOCK
    }
}
