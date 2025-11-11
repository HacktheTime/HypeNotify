package de.hype.hypenotify.app.core.interfaces;

import android.content.Context;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import de.hype.hypenotify.app.Config;
import de.hype.hypenotify.app.ExecutionService;
import de.hype.hypenotify.app.core.WakeLockManager;
import de.hype.hypenotify.app.tools.bazaar.BazaarService;
import de.hype.hypenotify.app.tools.timers.TimerService;

public interface MiniCore {
    Context context();

    boolean areKeysSet();

    TimerService timerService();

    WakeLockManager wakeLock();

    String userAPIKey();

    int userId();

    void setUserData(int userId, String bbAPIKey, String deviceName);

    BazaarService bazaarService();

    boolean isInFreeNetwork();

    ExecutionService executionService();

    void saveData(String key, Object data);

    Gson gson();

    <T> T getData(String timers, TypeToken<T> type);

    String getStringData(String key);

    Config config();

    // Mark that the service was stopped due to low battery and should not be auto-restarted.
    void setLowBatteryStop(boolean value);

    // Query whether a low-battery stop was set previously.
    boolean isLowBatteryStop();
}
