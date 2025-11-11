package de.hype.hypenotify.app.screen

import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.activity.OnBackPressedCallback
import de.hype.hypenotify.app.MainActivity
import de.hype.hypenotify.app.core.interfaces.Core
import java.util.*

abstract class Screen(core: Core, parent: View?) : LinearLayout(core.context()) {
    protected val parent: View?
    protected val core: Core
    private val backPressedCallback: OnBackPressedCallback?
    protected var context: MainActivity
    protected var dynamicScreen: LinearLayout? = null

    /**
     * Gibt die Screen-ID zurück (auto-generiert oder custom)
     */
    /**
     * Ermöglicht es, eine custom Screen-ID zu setzen
     */
    // Auto-State-Management
    abstract var screenId: String
        /**
         * Ermöglicht es, eine custom Screen-ID zu setzen
         */
        protected set
    private var autoStateEnabled = true

    init {
        context = core.context()
        this.core = core
        this.parent = parent

        // Auto-Screen-ID generieren basierend auf Klassenname
        this.screenId = this.javaClass.getSimpleName().lowercase(Locale.getDefault())

        // Screen-Klasse automatisch registrieren
        ScreenStateManager.registerScreenClass(this.javaClass)

        backPressedCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                close()
            }
        }
        context.onBackPressedDispatcher.addCallback(backPressedCallback)
    }

    open fun close() {
        // Auto-State speichern vor dem Schließen
        if (autoStateEnabled) {
            autoSaveState()
        }

        if (backPressedCallback != null) {
            backPressedCallback.remove()
        }
        if (parent != null) {
            core.context().setContentView(parent)
            if (parent is Screen) {
                parent.updateScreen()
            }
        } else {
            core.context().finish()
        }
    }

    /**
     * Auto-State-Management: Speichert automatisch den Zustand
     */
    private fun autoSaveState() {
        try {
            val state = Bundle()

            // Standard-State sammeln
            saveBasicState(state)

            // Custom-State von abgeleiteter Klasse
            onSaveState(state)

            // State speichern mit korrekten Parametern
            ScreenStateManager.saveScreenState(this, this.screenId, state)
        } catch (e: Exception) {
            Log.e("Screen", "Auto-save state failed for " + this.screenId, e)
        }
    }

    /**
     * Auto-State-Management: Stellt automatisch den Zustand wieder her
     */
    fun autoRestoreState() {
        if (!autoStateEnabled) return

        try {
            val stateManager = ScreenStateManager
            if (stateManager.hasState(this.screenId)) {
                // Custom-State von abgeleiteter Klasse wiederherstellen
                val state = stateManager.getStateBundle(this.screenId)
                if (state != null) {
                    restoreBasicState(state)
                    onRestoreState(state)
                }
            }
        } catch (e: Exception) {
            Log.e("Screen", "Auto-restore state failed for " + this.screenId, e)
        }
    }

    /**
     * Sammelt Standard-State-Informationen
     */
    private fun saveBasicState(state: Bundle) {
        // Scroll-Position falls ScrollView vorhanden
        if (dynamicScreen != null) {
            state.putInt("scroll_y", dynamicScreen!!.getScrollY())
        }

        // Visibility-Status
        state.putInt("visibility", getVisibility())

        // Timestamp
        state.putLong("state_timestamp", System.currentTimeMillis())
    }

    /**
     * Stellt Standard-State-Informationen wieder her
     */
    private fun restoreBasicState(state: Bundle) {
        // Nach Layout-Update anwenden
        post(Runnable {
            // Scroll-Position
            val scrollY = state.getInt("scroll_y", 0)
            if (dynamicScreen != null && scrollY > 0) {
                dynamicScreen!!.scrollTo(0, scrollY)
            }

            // Visibility
            val visibility = state.getInt("visibility", VISIBLE)
            setVisibility(visibility)
        })
    }

    /**
     * Override diese Methode um Custom-State zu speichern
     *
     * @param state Bundle zum Speichern der Daten
     */
    protected open fun onSaveState(state: Bundle?) {
        // Default: Nichts speichern
        // Überschreiben in abgeleiteten Klassen bei Bedarf
    }

    /**
     * Override diese Methode um Custom-State wiederherzustellen
     *
     * @param state Bundle mit gespeicherten Daten
     */
    protected open fun onRestoreState(state: Bundle?) {
        // Default: Nichts wiederherstellen
        // Überschreiben in abgeleiteten Klassen bei Bedarf
    }

    /**
     * Aktiviert/Deaktiviert Auto-State-Management
     */
    protected fun setAutoStateEnabled(enabled: Boolean) {
        this.autoStateEnabled = enabled
    }

    fun resetDynamicScreen() {
        if (dynamicScreen != null) {
            dynamicScreen!!.removeAllViews()
            removeView(dynamicScreen)
        }
        dynamicScreen = getDynamicScreen()
        val parent = dynamicScreen!!.getParent()
        if (parent is ViewGroup) {
            parent.removeView(dynamicScreen)
        }
        addView(dynamicScreen)
    }

    fun updateScreen() {
        removeAllViews()
        if (dynamicScreen != null) {
            dynamicScreen!!.removeAllViews()
        }
        try {
            inflateLayouts()
            val newDynamicScreen = getDynamicScreen()
            updateScreen(newDynamicScreen)
            if (newDynamicScreen != null) {
                if (newDynamicScreen.getParent() == null) {
                    if (dynamicScreen != null) removeView(dynamicScreen)
                    addView(newDynamicScreen)
                }
                dynamicScreen = newDynamicScreen
            }

            // State nach Update wiederherstellen
            autoRestoreState()
        } catch (e: Throwable) {
            e.printStackTrace()
        }
    }

    /**
     * Inflate all layouts you need here. Called before the Dynamic Screen is obtained to give you back at update Screen.
     */
    protected abstract fun inflateLayouts()


    /**
     * The View is the Dynamic Screen you returned in [.getDynamicScreen]
     */
    protected abstract fun updateScreen(dynamicScreen: LinearLayout)

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        // Clean up the callback when the view is detached
        if (backPressedCallback != null) {
            backPressedCallback.remove()
        }
    }

    abstract fun onPause()

    abstract fun onResume()

    protected abstract fun getDynamicScreen(): LinearLayout

    override fun toString(): String {
        return this.javaClass.getSimpleName()
    }
}
