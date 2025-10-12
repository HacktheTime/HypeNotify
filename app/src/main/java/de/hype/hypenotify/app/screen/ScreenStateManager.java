package de.hype.hypenotify.app.screen;

import android.os.Bundle;
import android.util.Log;
import de.hype.hypenotify.app.core.interfaces.Core;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Verwaltet den Zustand von Screens für Wiederherstellung nach Memory-Pressure
 */
public class ScreenStateManager {
    private static final String TAG = "ScreenStateManager";
    private static ScreenStateManager instance;
    private final Map<String, ScreenState> savedStates = new ConcurrentHashMap<>();
    private final Map<String, Class<? extends Screen>> screenClasses = new HashMap<>();

    public static class ScreenState {
        public Bundle state = new Bundle();
        public String screenClassName;
        public long timestamp = System.currentTimeMillis();

        public ScreenState(String screenClassName) {
            this.screenClassName = screenClassName;
        }
    }

    private ScreenStateManager() {
    }

    public static synchronized ScreenStateManager getInstance() {
        if (instance == null) {
            instance = new ScreenStateManager();
        }
        return instance;
    }

    /**
     * Registriert eine Screen-Klasse für die Wiederherstellung
     */
    public void registerScreenClass(Class<? extends Screen> screenClass) {
        screenClasses.put(screenClass.getSimpleName(), screenClass);
        Log.d(TAG, "Registered screen class: " + screenClass.getSimpleName());
    }

    /**
     * Speichert den Zustand eines Screens
     */
    public void saveScreenState(Screen screen, String screenId) {
        try {
            ScreenState state = new ScreenState(screen.getClass().getSimpleName());

            if (screen instanceof StatefulScreen) {
                ((StatefulScreen) screen).saveState(state.state);
            }

            savedStates.put(screenId, state);
            Log.d(TAG, "Saved state for screen: " + screenId + " (" + screen.getClass().getSimpleName() + ")");
        } catch (Exception e) {
            Log.e(TAG, "Error saving screen state for " + screenId, e);
        }
    }

    /**
     * Gibt das gespeicherte State-Bundle für eine Screen-ID zurück
     */
    public Bundle getStateBundle(String screenId) {
        ScreenState savedState = savedStates.get(screenId);
        return savedState != null ? savedState.state : null;
    }

    /**
     * Speichert den Zustand eines Screens (vereinfachte Version für Auto-State)
     */
    public void saveScreenState(Screen screen, String screenId, Bundle state) {
        try {
            ScreenState screenState = new ScreenState(screen.getClass().getSimpleName());
            screenState.state = state;

            savedStates.put(screenId, screenState);
            Log.d(TAG, "Auto-saved state for screen: " + screenId + " (" + screen.getClass().getSimpleName() + ")");
        } catch (Exception e) {
            Log.e(TAG, "Error auto-saving screen state for " + screenId, e);
        }
    }

    /**
     * Stellt einen Screen aus dem gespeicherten Zustand wieder her
     */
    public Screen restoreScreen(String screenId, Core core, android.view.View parent) {
        try {
            ScreenState savedState = savedStates.get(screenId);
            if (savedState == null) {
                Log.w(TAG, "No saved state found for screen: " + screenId);
                return null;
            }

            Class<? extends Screen> screenClass = screenClasses.get(savedState.screenClassName);
            if (screenClass == null) {
                Log.w(TAG, "Screen class not registered: " + savedState.screenClassName);
                return null;
            }

            // Screen über Reflection erstellen
            Screen restoredScreen = screenClass.getConstructor(Core.class, android.view.View.class)
                    .newInstance(core, parent);

            // Zustand wiederherstellen wenn möglich
            if (restoredScreen instanceof StatefulScreen) {
                ((StatefulScreen) restoredScreen).restoreState(savedState.state);
            }

            Log.d(TAG, "Restored screen: " + screenId + " (" + savedState.screenClassName + ")");
            return restoredScreen;

        } catch (Exception e) {
            Log.e(TAG, "Error restoring screen " + screenId, e);
            return null;
        }
    }

    /**
     * Entfernt gespeicherte Zustände die älter als die angegebene Zeit sind
     */
    public void cleanupOldStates(long maxAgeMs) {
        long now = System.currentTimeMillis();
        savedStates.entrySet().removeIf(entry -> {
            boolean isOld = (now - entry.getValue().timestamp) > maxAgeMs;
            if (isOld) {
                Log.d(TAG, "Cleaned up old state for: " + entry.getKey());
            }
            return isOld;
        });
    }

    /**
     * Überprüft ob ein gespeicherter Zustand für eine Screen-ID existiert
     */
    public boolean hasState(String screenId) {
        return savedStates.containsKey(screenId);
    }

    /**
     * Entfernt einen gespeicherten Zustand
     */
    public void removeState(String screenId) {
        savedStates.remove(screenId);
        Log.d(TAG, "Removed state for: " + screenId);
    }

    /**
     * Gibt Debug-Informationen über gespeicherte Zustände zurück
     */
    public String getDebugInfo() {
        StringBuilder sb = new StringBuilder();
        sb.append("ScreenStateManager Debug Info:\n");
        sb.append("Saved states: ").append(savedStates.size()).append("\n");
        sb.append("Registered classes: ").append(screenClasses.size()).append("\n");

        for (Map.Entry<String, ScreenState> entry : savedStates.entrySet()) {
            sb.append("  - ").append(entry.getKey())
                    .append(" (").append(entry.getValue().screenClassName).append(") ")
                    .append("age: ").append((System.currentTimeMillis() - entry.getValue().timestamp) / 1000).append("s\n");
        }

        return sb.toString();
    }
}
