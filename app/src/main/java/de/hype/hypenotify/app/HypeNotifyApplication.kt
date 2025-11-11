package de.hype.hypenotify.app

import android.content.res.Configuration
import android.util.Log
import androidx.multidex.MultiDexApplication
import de.hype.hypenotify.app.core.BackgroundService
import de.hype.hypenotify.app.core.interfaces.MiniCore
import de.hype.hypenotify.app.skyblockconstants.NeuRepoManager
import de.hype.hypenotify.app.skyblockconstants.NeuRepoManager.clearCache

class HypeNotifyApplication : MultiDexApplication() {
    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "Application created for background service")
    }

    private val isBazaarTrackingActive: Boolean
        // Dynamische Überprüfung ob Bazaar-Tracking aktiv ist
        get() {
            try {
                // Über BackgroundService zum Core und dann zum BazaarService
                if (BackgroundService.Companion.instance != null) {
                    val core: MiniCore? = BackgroundService.Companion.instance.getCore()
                    if (core != null && core.bazaarService() != null) {
                        // Prüfe ob trackedItems vorhanden sind und Service läuft
                        return !core.bazaarService().trackedItems.isEmpty()
                    }
                }
            } catch (e: Exception) {
                Log.w(TAG, "Could not check bazaar tracking status", e)
            }
            return false
        }

    override fun onTrimMemory(level: Int) {
        super.onTrimMemory(level)

        // Cooldown für häufige Memory Trim Requests
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastMemoryTrim < MEMORY_TRIM_COOLDOWN) {
            Log.d(TAG, "Memory trim skipped - cooldown active")
            return
        }
        lastMemoryTrim = currentTime

        val bazaarActive = this.isBazaarTrackingActive
        Log.d(TAG, "Memory trim requested: level " + level + " (Bazaar active: " + bazaarActive + ")")

        when (level) {
            TRIM_MEMORY_RUNNING_MODERATE -> {
                Log.d(TAG, "Memory trim: RUNNING_MODERATE")
                if (!bazaarActive) {
                    clearCache()
                }
            }
            TRIM_MEMORY_RUNNING_LOW -> {
                Log.d(TAG, "Memory trim: RUNNING_LOW")
                if (bazaarActive) {
                    clearItemCacheOnly()
                } else {
                    clearCache()
                    System.gc()
                }
            }
            TRIM_MEMORY_RUNNING_CRITICAL -> {
                Log.w(TAG, "Memory trim: RUNNING_CRITICAL - Emergency cleanup")
                clearCache()
                System.gc()
            }
            TRIM_MEMORY_UI_HIDDEN -> {
                Log.d(TAG, "Memory trim: UI_HIDDEN")
                if (!bazaarActive) {
                    clearCache()
                }
            }
            TRIM_MEMORY_BACKGROUND, TRIM_MEMORY_MODERATE, TRIM_MEMORY_COMPLETE -> {
                Log.d(TAG, "Memory trim: BACKGROUND/MODERATE/COMPLETE")
                if (bazaarActive) {
                    clearItemCacheOnly()
                } else {
                    clearCache()
                    System.gc()
                }
            }
        }
    }

    private fun clearItemCacheOnly() {
        try {
            // Nur Item-Cache leeren, Repository-Daten behalten für Bazaar-Tracking
            Log.d(TAG, "Clearing item cache only (preserving repo for bazaar tracking)")
            NeuRepoManager.clearItemCacheOnly()
        } catch (e: Exception) {
            Log.e(TAG, "Error during selective cache cleanup", e)
        }
    }

    override fun onLowMemory() {
        super.onLowMemory()
        val bazaarActive = this.isBazaarTrackingActive
        Log.w(TAG, "Low memory warning - emergency cleanup (Bazaar active: " + bazaarActive + ")")

        if (bazaarActive) {
            Log.w(TAG, "Critical memory situation with active bazaar tracking - selective cleanup")
            clearItemCacheOnly()
        } else {
            clearCache()
        }

        System.gc()
        Log.d(TAG, "Emergency cleanup completed")
    }

    override fun onConfigurationChanged(newConfig: Configuration?) {
        super.onConfigurationChanged(newConfig!!)
        Log.d(TAG, "Configuration changed")
    }

    override fun onTerminate() {
        Log.d(TAG, "Application terminating")

        // Final cleanup
        clearCache()

        super.onTerminate()
    }

    companion object {
        private const val TAG = "HypeNotifyApp"
        private var lastMemoryTrim: Long = 0
        private const val MEMORY_TRIM_COOLDOWN: Long = 30000 // 30 Sekunden
    }
}
