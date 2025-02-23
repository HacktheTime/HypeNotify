package de.hype.hypenotify.core;

import de.hype.hypenotify.DebugThread;
import de.hype.hypenotify.ExecutionService;
import de.hype.hypenotify.MainActivity;
import de.hype.hypenotify.core.interfaces.Core;
import de.hype.hypenotify.services.TimerService;
import de.hype.hypenotify.tools.bazaar.BazaarService;

class CoreImpl implements Core {
    private String TAG = "CoreImpl";
    public MainActivity context;
    private DebugThread debugThread = new DebugThread(this);
    MiniCore miniCore;

    public CoreImpl(MainActivity context, MiniCore core) {
        this.miniCore = core;
        this.context = context;
        // Load stored values
        debugThread.setName("DebugThread");
        debugThread.start();
    }

    @Override
    public MainActivity context() {
        return context;
    }

    @Override
    public boolean areKeysSet() {
        return miniCore.areKeysSet();
    }

    @Override
    public TimerService timerService() {
        return miniCore.timerService;
    }

    @Override
    public WakeLockManager wakeLock() {
        return miniCore.wakeLock;
    }

    @Override
    public String userAPIKey() {
        return miniCore.userAPIKey;
    }

    @Override
    public int userId() {
        return miniCore.userId;
    }

    @Override
    public void setUserData(int userId, String bbAPIKey, String deviceName) {
        miniCore.userId = userId;
        miniCore.userAPIKey = bbAPIKey;
        miniCore.deviceName = deviceName;
        miniCore.saveConfig();
    }

    @Override
    public BazaarService bazaarService() {
        return miniCore.bazaarService;
    }

    @Override
    public boolean isInHomeNetwork() {
        return miniCore.isInHomeNetwork();
    }

    @Override
    public ExecutionService executionService() {
        return miniCore.executionService;
    }

    @Override
    public void onDestroy() {
        debugThread.interrupt();
        miniCore.executionService.shutdown();
        miniCore.wakeLock.onDestroy();
    }
}
