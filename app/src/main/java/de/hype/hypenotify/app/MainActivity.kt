package de.hype.hypenotify.app;

import android.app.ComponentCaller;
import android.content.ComponentCallbacks2;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import de.hype.hypenotify.R;
import de.hype.hypenotify.app.core.BackgroundService;
import de.hype.hypenotify.app.core.DynamicIntents;
import de.hype.hypenotify.app.core.interfaces.Core;
import de.hype.hypenotify.app.screen.EnterDetailsLayout;
import de.hype.hypenotify.app.screen.OverviewScreen;
import de.hype.hypenotify.app.screen.Screen;
import de.hype.hypenotify.app.screen.ScreenStateManager;
import de.hype.hypenotify.app.screen.StatefulScreen;
import de.hype.hypenotify.app.services.HypeNotifyService;
import de.hype.hypenotify.app.services.HypeNotifyServiceConnection;
import de.hype.hypenotify.app.tools.timers.TimerService;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private Core core;
    private HypeNotifyServiceConnection serviceConnection;
    private BackgroundService backgroundService;
    private OverviewScreen overviewScreen;
    private View currentScreen;
    private final List<View> parents = new ArrayList<>();
    private boolean isPaused = false;
    private Thread mainThread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.serviceConnection = getServiceConnection();
        DynamicIntents.startBackgroundService(this);
        mainThread = new Thread(() -> {
            try {
                backgroundService = BackgroundService.getInstanceBlocking();
                core = backgroundService.getCore(this);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            runOnUiThread(() -> {
                super.setContentView(R.layout.activity_main);

                // Memory-optimierte Toolbar-Setup
                Toolbar toolbar = findViewById(R.id.toolbar);
                setSupportActionBar(toolbar);
                toolbar.setNavigationOnClickListener(view -> {
                    if (currentScreen != null) {
                        if (currentScreen instanceof Screen screen) {
                            screen.close();
                            // Explicit cleanup
                            currentScreen = null;
                        } else {
                            View parent = parents.isEmpty() ? null : parents.removeLast();
                            setContentView(parent);
                        }
                    }
                });
            });
            try {
                Intent intent = getIntent();
                if (DynamicIntents.handleIntent(intent, core, this)) {
                    return;
                }
                PermissionUtils.requestPermissionsBlocking(this);
                if (!core.areKeysSet()) {
                    EnterDetailsLayout enterDetailsLayout = new EnterDetailsLayout(core);
                    runOnUiThread(() -> {
                        setContentView(enterDetailsLayout);
                    });
                    try {
                        enterDetailsLayout.awaitDone();
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
                overviewScreen = new OverviewScreen(core);
                setOverviewPage();
            } catch (Exception e) {
                Log.e(TAG, "Error: ", e);
                throw new RuntimeException(e);
            }
        });
        mainThread.setName("New Main Thread");
        mainThread.start();
    }

    private HypeNotifyServiceConnection getServiceConnection() {
        return new HypeNotifyServiceConnection() {
            @Override
            public void serviceChange(String serviceName, HypeNotifyService<?> service) {
                System.out.println("Service changed: " + serviceName);
            }
        };
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();

        // Cleanup für Memory Leaks
        try {
            if (core != null) {
                core.onDestroy();
            }

            // Clear view references
            if (currentScreen instanceof Screen) {
                ((Screen) currentScreen).close();
            }
            currentScreen = null;

            // Clear parent views
            for (View parent : parents) {
                if (parent instanceof Screen) {
                    ((Screen) parent).close();
                }
            }
            parents.clear();

            // Clear other references
            overviewScreen = null;
            backgroundService = null;
            core = null;

            // Interrupt background thread if still running
            if (mainThread != null && mainThread.isAlive()) {
                mainThread.interrupt();
            }

            // Force garbage collection
            System.gc();

        } catch (Exception e) {
            Log.e(TAG, "Error during cleanup", e);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        PermissionUtils.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        Intent intent = new Intent(this, TimerService.class);
        if (serviceConnection != null) bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (serviceConnection != null) {
            unbindService(serviceConnection);
        }
    }

    @Override
    protected void onPause() {
        isPaused = true;
        super.onPause();
        if (currentScreen instanceof Screen c) c.onPause();
        for (View parent : parents) {
            if (parent instanceof Screen screen) {
                screen.onPause();
            }
        }
    }

    @Override
    protected void onResume() {
        isPaused = false;
        super.onResume();
        if (currentScreen instanceof Screen c) {
            c.updateScreen();
            c.onResume();
        }
        for (View parent : parents) {
            if (parent instanceof Screen screen) {
                screen.onResume();
            }
        }
    }

    @Override
    public void setContentView(View view) {
        if (isPaused) return;
        if (view == null) {
            finish();
            return;
        }

        // Clean up previous view if it's a Screen
        if (currentScreen instanceof Screen) {
            ((Screen) currentScreen).onPause();
        }

        // Add to parents but limit size to prevent memory bloat
        if (currentScreen != null) {
            parents.add(currentScreen);
            // Limit parent stack size to prevent memory issues
            if (parents.size() > 10) {
                View oldest = parents.remove(0);
                if (oldest instanceof Screen) {
                    ((Screen) oldest).close();
                }
            }
        }

        currentScreen = view;
        LinearLayout scrollView = findViewById(R.id.scroll_view);
        if (scrollView != null) {
            scrollView.removeAllViews();
            if (view.getParent() == null) {
                scrollView.addView(view);
            }
            if (view instanceof Screen screen) {
                screen.updateScreen();
            }
        } else {
            super.setContentView(view);
        }
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        Log.w(TAG, "Low memory warning received - emergency screen save and cleanup");

        // Emergency: Alle Screens speichern
        saveAndCloseAllScreens();
        System.gc();
    }

    @Override
    public void onTrimMemory(int level) {
        super.onTrimMemory(level);
        Log.d(TAG, "Memory trim requested: level " + level);

        switch (level) {
            case ComponentCallbacks2.TRIM_MEMORY_UI_HIDDEN:
                Log.d(TAG, "Memory trim: UI_HIDDEN - App UI hidden but still foreground");
                // Moderate Bereinigung mit State-Speicherung
                saveAndCloseScreensUntilSize(5);
                break;

            case ComponentCallbacks2.TRIM_MEMORY_BACKGROUND:
                Log.d(TAG, "Memory trim: BACKGROUND - App moved to background");
                // Aggressivere Bereinigung mit State-Speicherung
                saveAndCloseScreensUntilSize(2);
                System.gc();
                break;

            case ComponentCallbacks2.TRIM_MEMORY_MODERATE:
                Log.d(TAG, "Memory trim: MODERATE - Moderate memory pressure in background");
                saveAndCloseScreensUntilSize(1);
                System.gc();
                break;

            case ComponentCallbacks2.TRIM_MEMORY_COMPLETE:
                Log.w(TAG, "Memory trim: COMPLETE - Critical memory pressure, app likely to be killed");
                // Alle Screens speichern und schließen
                saveAndCloseAllScreens();
                System.gc();
                break;

            // Legacy support für ältere Android-Versionen
            case ComponentCallbacks2.TRIM_MEMORY_RUNNING_MODERATE:
            case ComponentCallbacks2.TRIM_MEMORY_RUNNING_LOW:
            case ComponentCallbacks2.TRIM_MEMORY_RUNNING_CRITICAL:
                Log.d(TAG, "Memory trim: RUNNING_* (legacy) - level " + level);
                saveAndCloseScreensUntilSize(3);
                break;
        }
    }

    /**
     * Speichert und schließt Screens bis nur noch die angegebene Anzahl übrig ist
     */
    private void saveAndCloseScreensUntilSize(int targetSize) {
        ScreenStateManager stateManager = ScreenStateManager.getInstance();

        while (parents.size() > targetSize) {
            View oldest = parents.remove(0);
            if (oldest instanceof Screen screen) {
                // Zustand speichern wenn möglich
                if (screen instanceof StatefulScreen statefulScreen) {
                    String screenId = "parent_" + System.currentTimeMillis() + "_" + statefulScreen.getScreenId();
                    stateManager.saveScreenState(screen, screenId);
                    Log.d(TAG, "Saved state for screen: " + screenId);
                }
                screen.close();
            }
        }

        // Alte States bereinigen (älter als 1 Stunde)
        stateManager.cleanupOldStates(60 * 60 * 1000);
    }

    /**
     * Speichert und schließt alle Screens
     */
    private void saveAndCloseAllScreens() {
        ScreenStateManager stateManager = ScreenStateManager.getInstance();

        // Aktueller Screen speichern
        if (currentScreen instanceof Screen screen && screen instanceof StatefulScreen statefulScreen) {
            String screenId = "current_" + statefulScreen.getScreenId();
            stateManager.saveScreenState(screen, screenId);
            Log.d(TAG, "Saved current screen state: " + screenId);
        }

        // Alle Parent-Screens speichern
        for (int i = 0; i < parents.size(); i++) {
            View parent = parents.get(i);
            if (parent instanceof Screen screen && screen instanceof StatefulScreen statefulScreen) {
                String screenId = "parent_" + i + "_" + statefulScreen.getScreenId();
                stateManager.saveScreenState(screen, screenId);
                screen.close();
                Log.d(TAG, "Saved parent screen state: " + screenId);
            }
        }

        parents.clear();
    }

    /**
     * Versucht einen Screen aus dem gespeicherten Zustand wiederherzustellen
     */
    public Screen tryRestoreScreen(String screenId, View parent) {
        try {
            ScreenStateManager stateManager = ScreenStateManager.getInstance();
            if (stateManager.hasState(screenId)) {
                Screen restoredScreen = stateManager.restoreScreen(screenId, core, parent);
                if (restoredScreen != null) {
                    Log.d(TAG, "Successfully restored screen: " + screenId);
                    return restoredScreen;
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Failed to restore screen: " + screenId, e);
        }
        return null;
    }

    @Override
    public void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        DynamicIntents.handleIntent(intent, core, this);
    }

    public void setContentViewNoOverride(View screen) {
        super.setContentView(screen);
    }

    public void setContentViewNoOverrideInlined(View screen) {
        runOnUiThread(() -> super.setContentView(screen));
    }

    @Override
    public void onNewIntent(@NonNull Intent intent, @NonNull ComponentCaller caller) {
        super.onNewIntent(intent, caller);
        DynamicIntents.handleIntent(intent, core, this);
    }

    public void setOverviewPage() {
        runOnUiThread(() -> setContentView(overviewScreen));
    }

    public Core getCore() {
        return core;
    }
}