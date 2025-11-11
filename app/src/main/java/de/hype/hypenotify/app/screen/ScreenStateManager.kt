package de.hype.hypenotify.app.screen

import android.os.Bundle
import android.util.Log
import android.view.View
import de.hype.hypenotify.app.core.interfaces.Core
import java.util.concurrent.ConcurrentHashMap

/**
 * Verwaltet den Zustand von Screens für Wiederherstellung nach Memory-Pressure
 */
object ScreenStateManager {
    private val savedStates: MutableMap<String, ScreenState> = ConcurrentHashMap<String, ScreenState>()
    private val screenClasses: MutableMap<String, Class<out Screen>> = HashMap<String, Class<out Screen>>()

    class ScreenState(screenClassName: String) {
        var state: Bundle = Bundle()
        var screenClassName: String
        var timestamp: Long = System.currentTimeMillis()

        init {
            this.screenClassName = screenClassName
        }
    }

    /**
     * Registriert eine Screen-Klasse für die Wiederherstellung
     */
    fun registerScreenClass(screenClass: Class<out Screen>) {
        screenClasses.put(screenClass.getSimpleName(), screenClass)
        Log.d(TAG, "Registered screen class: " + screenClass.getSimpleName())
    }

    /**
     * Speichert den Zustand eines Screens
     */
    fun saveScreenState(screen: Screen, screenId: String) {
        try {
            val state = ScreenState(screen.javaClass.getSimpleName())

            if (screen is StatefulScreen) {
                (screen as StatefulScreen).saveState(state.state)
            }

            savedStates.put(screenId, state)
            Log.d(TAG, "Saved state for screen: " + screenId + " (" + screen.javaClass.getSimpleName() + ")")
        } catch (e: Exception) {
            Log.e(TAG, "Error saving screen state for " + screenId, e)
        }
    }

    /**
     * Gibt das gespeicherte State-Bundle für eine Screen-ID zurück
     */
    fun getStateBundle(screenId: String): Bundle? {
        val savedState = savedStates.get(screenId)
        return if (savedState != null) savedState.state else null
    }

    /**
     * Speichert den Zustand eines Screens (vereinfachte Version für Auto-State)
     */
    fun saveScreenState(screen: Screen, screenId: String, state: Bundle) {
        try {
            val screenState = ScreenState(screen.javaClass.getSimpleName())
            screenState.state = state

            savedStates.put(screenId, screenState)
            Log.d(TAG, "Auto-saved state for screen: " + screenId + " (" + screen.javaClass.getSimpleName() + ")")
        } catch (e: Exception) {
            Log.e(TAG, "Error auto-saving screen state for " + screenId, e)
        }
    }

    /**
     * Stellt einen Screen aus dem gespeicherten Zustand wieder her
     */
    fun restoreScreen(screenId: String, core: Core, parent: View): Screen? {
        try {
            val savedState = savedStates.get(screenId)
            if (savedState == null) {
                Log.w(TAG, "No saved state found for screen: " + screenId)
                return null
            }

            val screenClass = screenClasses.get(savedState.screenClassName)
            if (screenClass == null) {
                Log.w(TAG, "Screen class not registered: " + savedState.screenClassName)
                return null
            }

            // Screen über Reflection erstellen
            val restoredScreen: Screen = screenClass.getConstructor(Core::class.java, View::class.java)
                .newInstance(core, parent)

            // Zustand wiederherstellen wenn möglich
            if (restoredScreen is StatefulScreen) {
                (restoredScreen as StatefulScreen).restoreState(savedState.state)
            }

            Log.d(TAG, "Restored screen: " + screenId + " (" + savedState.screenClassName + ")")
            return restoredScreen
        } catch (e: Exception) {
            Log.e(TAG, "Error restoring screen " + screenId, e)
            return null
        }
    }

    /**
     * Entfernt gespeicherte Zustände die älter als die angegebene Zeit sind
     */
    fun cleanupOldStates(maxAgeMs: Long) {
        val now = System.currentTimeMillis()
        savedStates.entries.removeIf { entry: MutableMap.MutableEntry<String, ScreenState> ->
            val isOld = (now - entry!!.value!!.timestamp) > maxAgeMs
            if (isOld) {
                Log.d(TAG, "Cleaned up old state for: " + entry.key)
            }
            isOld
        }
    }

    /**
     * Überprüft ob ein gespeicherter Zustand für eine Screen-ID existiert
     */
    fun hasState(screenId: String): Boolean {
        return savedStates.containsKey(screenId)
    }

    /**
     * Entfernt einen gespeicherten Zustand
     */
    fun removeState(screenId: String) {
        savedStates.remove(screenId)
        Log.d(TAG, "Removed state for: " + screenId)
    }

    val debugInfo: String
        /**
         * Gibt Debug-Informationen über gespeicherte Zustände zurück
         */
        get() {
            val sb = StringBuilder()
            sb.append("ScreenStateManager Debug Info:\n")
            sb.append("Saved states: ").append(savedStates.size).append("\n")
            sb.append("Registered classes: ").append(screenClasses.size).append("\n")

            for (entry in savedStates.entries) {
                sb.append("  - ").append(entry.key)
                    .append(" (").append(entry.value!!.screenClassName).append(") ")
                    .append("age: ").append((System.currentTimeMillis() - entry.value!!.timestamp) / 1000).append("s\n")
            }

            return sb.toString()
        }
    private const val TAG = "ScreenStateManager"
}
