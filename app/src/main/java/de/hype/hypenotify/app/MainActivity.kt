package de.hype.hypenotify.app

import android.app.ComponentCaller
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import de.hype.hypenotify.R
import de.hype.hypenotify.app.core.BackgroundService
import de.hype.hypenotify.app.core.DynamicIntents
import de.hype.hypenotify.app.core.interfaces.Core
import de.hype.hypenotify.app.screen.EnterDetailsLayout
import de.hype.hypenotify.app.screen.OverviewScreen
import de.hype.hypenotify.app.screen.Screen
import de.hype.hypenotify.app.screen.ScreenStateManager
import de.hype.hypenotify.app.screen.StatefulScreen
import de.hype.hypenotify.app.services.HypeNotifyService
import de.hype.hypenotify.app.services.HypeNotifyServiceConnection
import de.hype.hypenotify.app.tools.timers.TimerService

class MainActivity : AppCompatActivity() {
    var core: Core? = null
        private set
    private var serviceConnection: HypeNotifyServiceConnection? = null
    private var backgroundService: BackgroundService? = null
    private var overviewScreen: OverviewScreen? = null
    private var currentScreen: View? = null
    private val parents: MutableList<View?> = ArrayList<View?>()
    private var isPaused = false
    private var mainThread: Thread? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.serviceConnection = getServiceConnection()
        DynamicIntents.Companion.startBackgroundService(this)
        mainThread = Thread(Runnable {
            try {
                backgroundService = BackgroundService.getInstanceBlocking()
                core = backgroundService!!.getCore(this)
            } catch (e: Exception) {
                throw RuntimeException(e)
            }
            runOnUiThread(Runnable {
                super.setContentView(R.layout.activity_main)
                // Memory-optimierte Toolbar-Setup
                val toolbar = findViewById<Toolbar>(R.id.toolbar)
                setSupportActionBar(toolbar)
                toolbar.setNavigationOnClickListener(View.OnClickListener { view: View? ->
                    if (currentScreen != null) {
                        if (currentScreen is Screen) {
                            currentScreen.close()
                            // Explicit cleanup
                            currentScreen = null
                        } else {
                            val parent = if (parents.isEmpty()) null else parents.removeLast()
                            setContentView(parent)
                        }
                    }
                })
            })
            try {
                val intent = getIntent()
                if (DynamicIntents.Companion.handleIntent(intent, core, this)) {
                    return@Runnable
                }
                PermissionUtils.requestPermissionsBlocking(this)
                if (!core!!.areKeysSet()) {
                    val enterDetailsLayout = EnterDetailsLayout(core)
                    runOnUiThread(Runnable {
                        setContentView(enterDetailsLayout)
                    })
                    try {
                        enterDetailsLayout.awaitDone()
                    } catch (e: InterruptedException) {
                        throw RuntimeException(e)
                    }
                }
                overviewScreen = OverviewScreen(core)
                setOverviewPage()
            } catch (e: Exception) {
                Log.e(TAG, "Error: ", e)
                throw RuntimeException(e)
            }
        })
        mainThread!!.setName("New Main Thread")
        mainThread!!.start()
    }

    private fun getServiceConnection(): HypeNotifyServiceConnection {
        return object : HypeNotifyServiceConnection() {
            public override fun serviceChange(serviceName: String?, service: HypeNotifyService<*>?) {
                println("Service changed: " + serviceName)
            }
        }
    }


    override fun onDestroy() {
        super.onDestroy()

        // Cleanup für Memory Leaks
        try {
            if (core != null) {
                core!!.onDestroy()
            }

            // Clear view references
            if (currentScreen is Screen) {
                (currentScreen as Screen).close()
            }
            currentScreen = null

            // Clear parent views
            for (parent in parents) {
                if (parent is Screen) {
                    parent.close()
                }
            }
            parents.clear()

            // Clear other references
            overviewScreen = null
            backgroundService = null
            core = null

            // Interrupt background thread if still running
            if (mainThread != null && mainThread!!.isAlive()) {
                mainThread!!.interrupt()
            }

            // Force garbage collection
            System.gc()
        } catch (e: Exception) {
            Log.e(TAG, "Error during cleanup", e)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String?>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        PermissionUtils.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
    }

    override fun onStart() {
        super.onStart()
        val intent = Intent(this, TimerService::class.java)
        if (serviceConnection != null) bindService(intent, serviceConnection!!, BIND_AUTO_CREATE)
    }

    override fun onStop() {
        super.onStop()
        if (serviceConnection != null) {
            unbindService(serviceConnection!!)
        }
    }

    override fun onPause() {
        isPaused = true
        super.onPause()
        if (currentScreen is Screen) currentScreen.onPause()
        for (parent in parents) {
            if (parent is Screen) {
                parent.onPause()
            }
        }
    }

    override fun onResume() {
        isPaused = false
        super.onResume()
        if (currentScreen is Screen) {
            currentScreen.updateScreen()
            currentScreen.onResume()
        }
        for (parent in parents) {
            if (parent is Screen) {
                parent.onResume()
            }
        }
    }

    override fun setContentView(view: View?) {
        if (isPaused) return
        if (view == null) {
            finish()
            return
        }

        // Clean up previous view if it's a Screen
        if (currentScreen is Screen) {
            (currentScreen as Screen).onPause()
        }

        // Add to parents but limit size to prevent memory bloat
        if (currentScreen != null) {
            parents.add(currentScreen)
            // Limit parent stack size to prevent memory issues
            if (parents.size > 10) {
                val oldest = parents.removeAt(0)
                if (oldest is Screen) {
                    oldest.close()
                }
            }
        }

        currentScreen = view
        val scrollView = findViewById<LinearLayout?>(R.id.scroll_view)
        if (scrollView != null) {
            scrollView.removeAllViews()
            if (view.getParent() == null) {
                scrollView.addView(view)
            }
            if (view is Screen) {
                view.updateScreen()
            }
        } else {
            super.setContentView(view)
        }
    }

    override fun onLowMemory() {
        super.onLowMemory()
        Log.w(TAG, "Low memory warning received - emergency screen save and cleanup")

        // Emergency: Alle Screens speichern
        saveAndCloseAllScreens()
        System.gc()
    }

    override fun onTrimMemory(level: Int) {
        super.onTrimMemory(level)
        Log.d(TAG, "Memory trim requested: level " + level)

        when (level) {
            TRIM_MEMORY_UI_HIDDEN -> {
                Log.d(TAG, "Memory trim: UI_HIDDEN - App UI hidden but still foreground")
                // Moderate Bereinigung mit State-Speicherung
                saveAndCloseScreensUntilSize(5)
            }
            TRIM_MEMORY_BACKGROUND -> {
                Log.d(TAG, "Memory trim: BACKGROUND - App moved to background")
                // Aggressivere Bereinigung mit State-Speicherung
                saveAndCloseScreensUntilSize(2)
                System.gc()
            }
            TRIM_MEMORY_MODERATE -> {
                Log.d(TAG, "Memory trim: MODERATE - Moderate memory pressure in background")
                saveAndCloseScreensUntilSize(1)
                System.gc()
            }
            TRIM_MEMORY_COMPLETE -> {
                Log.w(TAG, "Memory trim: COMPLETE - Critical memory pressure, app likely to be killed")
                // Alle Screens speichern und schließen
                saveAndCloseAllScreens()
                System.gc()
            }
            TRIM_MEMORY_RUNNING_MODERATE, TRIM_MEMORY_RUNNING_LOW, TRIM_MEMORY_RUNNING_CRITICAL -> {
                Log.d(TAG, "Memory trim: RUNNING_* (legacy) - level " + level)
                saveAndCloseScreensUntilSize(3)
            }
        }
    }

    /**
     * Speichert und schließt Screens bis nur noch die angegebene Anzahl übrig ist
     */
    private fun saveAndCloseScreensUntilSize(targetSize: Int) {
        val stateManager: ScreenStateManager = ScreenStateManager

        while (parents.size > targetSize) {
            val oldest = parents.removeAt(0)
            if (oldest is Screen) {
                // Zustand speichern wenn möglich
                if (oldest is StatefulScreen) {
                    val screenId = "parent_" + System.currentTimeMillis() + "_" + screen.getScreenId()
                    stateManager.saveScreenState(oldest, screenId)
                    Log.d(TAG, "Saved state for screen: " + screenId)
                }
                oldest.close()
            }
        }

        // Alte States bereinigen (älter als 1 Stunde)
        stateManager.cleanupOldStates((60 * 60 * 1000).toLong())
    }

    /**
     * Speichert und schließt alle Screens
     */
    private fun saveAndCloseAllScreens() {
        val stateManager: ScreenStateManager = ScreenStateManager

        // Aktueller Screen speichern
        if (currentScreen is Screen && currentScreen is StatefulScreen) {
            val screenId = "current_" + screen.getScreenId()
            stateManager.saveScreenState(currentScreen, screenId)
            Log.d(TAG, "Saved current screen state: " + screenId)
        }

        // Alle Parent-Screens speichern
        for (i in parents.indices) {
            val parent = parents.get(i)
            if (parent is Screen && parent is StatefulScreen) {
                val screenId = "parent_" + i + "_" + screen.getScreenId()
                stateManager.saveScreenState(parent, screenId)
                parent.close()
                Log.d(TAG, "Saved parent screen state: " + screenId)
            }
        }

        parents.clear()
    }

    /**
     * Versucht einen Screen aus dem gespeicherten Zustand wiederherzustellen
     */
    fun tryRestoreScreen(screenId: String?, parent: View?): Screen? {
        try {
            val stateManager: ScreenStateManager = ScreenStateManager
            if (stateManager.hasState(screenId)) {
                val restoredScreen = stateManager.restoreScreen(screenId, core, parent)
                if (restoredScreen != null) {
                    Log.d(TAG, "Successfully restored screen: " + screenId)
                    return restoredScreen
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to restore screen: " + screenId, e)
        }
        return null
    }

    public override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        DynamicIntents.Companion.handleIntent(intent, core, this)
    }

    fun setContentViewNoOverride(screen: View?) {
        super.setContentView(screen)
    }

    fun setContentViewNoOverrideInlined(screen: View?) {
        runOnUiThread(Runnable { super.setContentView(screen) })
    }

    override fun onNewIntent(intent: Intent, caller: ComponentCaller) {
        super.onNewIntent(intent, caller)
        DynamicIntents.Companion.handleIntent(intent, core, this)
    }

    fun setOverviewPage() {
        runOnUiThread(Runnable { setContentView(overviewScreen) })
    }

    companion object {
        private const val TAG = "MainActivity"
    }
}