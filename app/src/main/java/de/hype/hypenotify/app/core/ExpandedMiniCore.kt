package de.hype.hypenotify.app.core;

import android.content.Context;
import de.hype.hypenotify.app.ExecutionService;
import de.hype.hypenotify.app.tools.bazaar.BazaarService;
import de.hype.hypenotify.app.tools.timers.TimerService;

public class ExpandedMiniCore extends MiniCore {
    public ExpandedMiniCore(BackgroundService context) {
        super(context);
    }

    @Override
    public Context context() {
        return super.context;
    }

    @Override
    public TimerService timerService() {
        return super.timerService;
    }

    @Override
    public WakeLockManager wakeLock() {
        return super.wakeLock;
    }

    @Override
    public String userAPIKey() {
        return super.userAPIKey;
    }

    @Override
    public int userId() {
        return super.userId;
    }

    @Override
    public void setUserData(int userId, String bbAPIKey, String deviceName) {
        super.setUserData(userId, bbAPIKey, deviceName);
    }

    @Override
    public BazaarService bazaarService() {
        return super.bazaarService;
    }

    @Override
    public ExecutionService executionService() {
        return super.executionService;
    }

    private boolean lowBatteryStop = false;

    @Override
    public boolean isLowBatteryStop() {
        return lowBatteryStop;
    }

    @Override
    public void setLowBatteryStop(boolean stop) {
        this.lowBatteryStop = stop;
    }

}
