package de.hype.hypenotify.core.interfaces;

import android.content.Context;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import de.hype.hypenotify.ExecutionService;
import de.hype.hypenotify.core.WakeLockManager;
import de.hype.hypenotify.services.TimerService;
import de.hype.hypenotify.tools.bazaar.BazaarService;

public interface MiniCore {
    Context context();

    boolean areKeysSet();

    TimerService timerService();

    WakeLockManager wakeLock();

    String userAPIKey();

    int userId();

    void setUserData(int userId, String bbAPIKey, String deviceName);

    BazaarService bazaarService();

    boolean isInHomeNetwork();

    ExecutionService executionService();

    void saveData(String key, Object data);

    Gson gson();

    <T> T getData(String timers, TypeToken<T> type);
}
