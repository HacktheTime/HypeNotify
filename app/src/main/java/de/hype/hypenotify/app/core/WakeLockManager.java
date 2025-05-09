package de.hype.hypenotify.app.core;

import android.content.Context;
import android.os.PowerManager;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class WakeLockManager {
    private final MiniCore core;
    PowerManager powerManager;
    Map<WakeLockRequests, PowerManager.WakeLock> wakeLocks = new HashMap<>();

    public WakeLockManager(MiniCore core) {
        this.core = core;
        powerManager = (PowerManager) core.context.getSystemService(Context.POWER_SERVICE);
    }

    public void acquire(WakeLockRequests request) {
        if (wakeLocks.get(request) != null) return;
        PowerManager.WakeLock wakeLock = request.getWakeLock(powerManager);
        wakeLocks.put(request, wakeLock);
        Integer timeLimit = request.getTimeLockLimit();
        if (timeLimit == null) wakeLock.acquire();
        else wakeLock.acquire(timeLimit * 1000L);
    }


    public void onDestroy() {
        releaseAll();
    }

    public void releaseAll() {
        Set<Map.Entry<WakeLockRequests, PowerManager.WakeLock>> entries = wakeLocks.entrySet();
        entries.forEach(entry -> {
            PowerManager.WakeLock wakeLock = entry.getValue();
            if (wakeLock != null && wakeLock.isHeld()) {
                wakeLock.release();
            }
        });
    }

    public void release(WakeLockRequests request) {
        PowerManager.WakeLock wakeLock = wakeLocks.get(request);
        if (wakeLock != null && wakeLock.isHeld()) {
            wakeLock.release();
        }
    }

    public List<WakeLockRequests> getActiveWakeLocks() {
        return List.copyOf(wakeLocks.keySet());
    }

    public enum WakeLockRequests {
        TIMER_WAKE_LOCK("TimerWakeLock"),
        ;
        private String tagName;

        WakeLockRequests(String tagName) {
            this.tagName = tagName;
        }

        public String getAsTag() {
            return "HypeNotify::" + tagName;
        }

        public PowerManager.WakeLock getWakeLock(PowerManager powerManager) {
            return powerManager.newWakeLock(getWakeLockType(), getAsTag());
        }

        @Nullable
        public Integer getTimeLockLimit() {
            return null;
        }

        public int getWakeLockType() {
            return PowerManager.PARTIAL_WAKE_LOCK;
        }
    }
}
