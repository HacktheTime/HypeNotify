package de.hype.hypenotify.core;

import android.content.Context;
import de.hype.hypenotify.ExecutionService;
import de.hype.hypenotify.services.TimerService;
import de.hype.hypenotify.tools.bazaar.BazaarService;

public class ExpandedMiniCore extends MiniCore {
    public ExpandedMiniCore(Context context) {
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
}
