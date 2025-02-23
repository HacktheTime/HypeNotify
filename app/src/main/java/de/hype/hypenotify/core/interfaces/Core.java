package de.hype.hypenotify.core.interfaces;

import android.content.Context;
import de.hype.hypenotify.ExecutionService;
import de.hype.hypenotify.MainActivity;
import de.hype.hypenotify.core.WakeLockManager;
import de.hype.hypenotify.services.TimerService;
import de.hype.hypenotify.tools.bazaar.BazaarService;

public interface Core {

    MainActivity context();

    boolean areKeysSet();

    TimerService timerService();

    WakeLockManager wakeLock();

    String userAPIKey();

    int userId();

    void setUserData(int userId, String bbAPIKey, String deviceName);

    BazaarService bazaarService();

    boolean isInHomeNetwork();

    ExecutionService executionService();

    void onDestroy();
}
