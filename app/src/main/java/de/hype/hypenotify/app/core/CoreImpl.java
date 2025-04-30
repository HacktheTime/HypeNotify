package de.hype.hypenotify.app.core;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import de.hype.hypenotify.Config;
import de.hype.hypenotify.ExecutionService;
import de.hype.hypenotify.MainActivity;
import de.hype.hypenotify.app.core.interfaces.Core;
import de.hype.hypenotify.app.tools.bazaar.BazaarService;
import de.hype.hypenotify.app.tools.timers.TimerService;

public class CoreImpl implements Core {
    private String TAG = "CoreImpl";
    public MainActivity context;
    MiniCore miniCore;

    public CoreImpl(MainActivity context, MiniCore core) {
        this.miniCore = core;
        this.context = context;
        // Load stored values
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
        miniCore.setUserData(userId, bbAPIKey, deviceName);
    }

    @Override
    public BazaarService bazaarService() {
        return miniCore.bazaarService;
    }

    @Override
    public boolean isInFreeNetwork() {
        return miniCore.isInFreeNetwork();
    }

    @Override
    public ExecutionService executionService() {
        return miniCore.executionService;
    }

    @Override
    public void onDestroy() {
    }

    @Override
    public void saveData(String key, Object data) {
        miniCore.saveData(key, data);
    }

    @Override
    public Gson gson() {
        return miniCore.gson();
    }

    @Override
    public <T> T getData(String timers, TypeToken<T> type) {
        return miniCore.getData(timers, type);
    }

    @Override
    public String getStringData(String key) {
        return miniCore.getStringData(key);
    }

    @Override
    public Config config() {
        return miniCore.config();
    }
}
