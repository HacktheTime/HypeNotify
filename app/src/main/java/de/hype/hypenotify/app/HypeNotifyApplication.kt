package de.hype.hypenotify.app;

import android.app.Application;
import android.content.ComponentCallbacks2;
import android.content.res.Configuration;
import android.util.Log;
import androidx.multidex.MultiDexApplication;
import de.hype.hypenotify.app.skyblockconstants.NeuRepoManager;

public class HypeNotifyApplication extends MultiDexApplication {
    private static final String TAG = "HypeNotifyApp";
    private static long lastMemoryTrim = 0;
    private static final long MEMORY_TRIM_COOLDOWN = 30000; // 30 Sekunden

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "Application created for background service");
    }

    // Dynamische Überprüfung ob Bazaar-Tracking aktiv ist
    private boolean isBazaarTrackingActive() {
        try {
            // Über BackgroundService zum Core und dann zum BazaarService
            if (de.hype.hypenotify.app.core.BackgroundService.instance != null) {
                var core = de.hype.hypenotify.app.core.BackgroundService.instance.getCore();
                if (core != null && core.bazaarService() != null) {
                    // Prüfe ob trackedItems vorhanden sind und Service läuft
                    return !core.bazaarService().trackedItems.isEmpty();
                }
            }
        } catch (Exception e) {
            Log.w(TAG, "Could not check bazaar tracking status", e);
        }
        return false;
    }

    @Override
    public void onTrimMemory(int level) {
        super.onTrimMemory(level);

        // Cooldown für häufige Memory Trim Requests
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastMemoryTrim < MEMORY_TRIM_COOLDOWN) {
            Log.d(TAG, "Memory trim skipped - cooldown active");
            return;
        }
        lastMemoryTrim = currentTime;

        boolean bazaarActive = isBazaarTrackingActive();
        Log.d(TAG, "Memory trim requested: level " + level + " (Bazaar active: " + bazaarActive + ")");

        switch (level) {
            case ComponentCallbacks2.TRIM_MEMORY_RUNNING_MODERATE:
                Log.d(TAG, "Memory trim: RUNNING_MODERATE");
                if (!bazaarActive) {
                    NeuRepoManager.INSTANCE.clearCache();
                }
                break;

            case ComponentCallbacks2.TRIM_MEMORY_RUNNING_LOW:
                Log.d(TAG, "Memory trim: RUNNING_LOW");
                if (bazaarActive) {
                    clearItemCacheOnly();
                } else {
                    NeuRepoManager.INSTANCE.clearCache();
                    System.gc();
                }
                break;

            case ComponentCallbacks2.TRIM_MEMORY_RUNNING_CRITICAL:
                Log.w(TAG, "Memory trim: RUNNING_CRITICAL - Emergency cleanup");
                NeuRepoManager.INSTANCE.clearCache();
                System.gc();
                break;

            case ComponentCallbacks2.TRIM_MEMORY_UI_HIDDEN:
                Log.d(TAG, "Memory trim: UI_HIDDEN");
                if (!bazaarActive) {
                    NeuRepoManager.INSTANCE.clearCache();
                }
                break;

            case ComponentCallbacks2.TRIM_MEMORY_BACKGROUND:
            case ComponentCallbacks2.TRIM_MEMORY_MODERATE:
            case ComponentCallbacks2.TRIM_MEMORY_COMPLETE:
                Log.d(TAG, "Memory trim: BACKGROUND/MODERATE/COMPLETE");
                if (bazaarActive) {
                    clearItemCacheOnly();
                } else {
                    NeuRepoManager.INSTANCE.clearCache();
                    System.gc();
                }
                break;
        }
    }

    private void clearItemCacheOnly() {
        try {
            // Nur Item-Cache leeren, Repository-Daten behalten für Bazaar-Tracking
            Log.d(TAG, "Clearing item cache only (preserving repo for bazaar tracking)");
            NeuRepoManager.INSTANCE.clearItemCacheOnly();
        } catch (Exception e) {
            Log.e(TAG, "Error during selective cache cleanup", e);
        }
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        boolean bazaarActive = isBazaarTrackingActive();
        Log.w(TAG, "Low memory warning - emergency cleanup (Bazaar active: " + bazaarActive + ")");

        if (bazaarActive) {
            Log.w(TAG, "Critical memory situation with active bazaar tracking - selective cleanup");
            clearItemCacheOnly();
        } else {
            NeuRepoManager.INSTANCE.clearCache();
        }

        System.gc();
        Log.d(TAG, "Emergency cleanup completed");
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        Log.d(TAG, "Configuration changed");
    }

    @Override
    public void onTerminate() {
        Log.d(TAG, "Application terminating");

        // Final cleanup
        NeuRepoManager.INSTANCE.clearCache();

        super.onTerminate();
    }
}
