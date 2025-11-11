package de.hype.hypenotify.app.core

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.SystemClock
import androidx.core.app.NotificationCompat
import de.hype.hypenotify.R
import de.hype.hypenotify.app.MainActivity
import de.hype.hypenotify.app.core.interfaces.Core
import de.hype.hypenotify.app.core.interfaces.MiniCore
import de.hype.hypenotify.app.services.HypeNotifyService
import de.hype.hypenotify.app.tools.notification.NotificationBuilder
import de.hype.hypenotify.app.tools.notification.NotificationChannels
import org.jetbrains.annotations.Blocking
import java.util.function.Consumer

class BackgroundService : HypeNotifyService<BackgroundService>() {
    override var core: MiniCore

    private val closeReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(ctx: Context?, i: Intent?) {
            stopSelf()
        }
    }
    private val dismissReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(ctx: Context?, i: Intent?) {
            showNotificationWithCloseButton()
        }
    }

    override fun onCreate() {
        super.onCreate()

        // Register receivers
        registerReceiver(
            closeReceiver,
            IntentFilter(CLOSE_SERVICE_ACTION),
            RECEIVER_NOT_EXPORTED
        )
        registerReceiver(
            dismissReceiver,
            IntentFilter(DISMISS_ACTION),
            RECEIVER_NOT_EXPORTED
        )

        showNotificationWithCloseButton()
        instance = this
        core = ExpandedMiniCore(this)
    }

    private fun showNotificationWithCloseButton() {
        val closeIntent: Intent? = Intent(CLOSE_SERVICE_ACTION)
            .setPackage(getPackageName())
        val closePI = PendingIntent.getBroadcast(
            this, 0, closeIntent!!, PendingIntent.FLAG_IMMUTABLE
        )

        val deleteIntent: Intent? = Intent(DISMISS_ACTION)
            .setPackage(getPackageName())
        val deletePI = PendingIntent.getBroadcast(
            this, 0, deleteIntent!!, PendingIntent.FLAG_IMMUTABLE
        )

        val nb = NotificationBuilder(
            this,
            "Background",
            "A HypeNotify Service is running in the background.",
            NotificationChannels.BACKGROUND_SERVICE
        )

        nb.setSmallIcon(R.mipmap.icon)
        nb.setLargeImage(R.mipmap.icon)
        nb.hiddenBuilder
            .setOngoing(true)
            .setForegroundServiceBehavior(
                NotificationCompat.FOREGROUND_SERVICE_DEFERRED
            )
            .addAction(
                android.R.drawable.ic_menu_close_clear_cancel,
                "Stop Service",
                closePI
            )
            .setDeleteIntent(deletePI)

        startForeground(nb.build().iD, nb.build().get())
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (isRunning) return START_NOT_STICKY
        isRunning = true
        return START_STICKY
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        // If we previously stopped service due to low battery, don't restart automatically.
        if (core != null && core.isLowBatteryStop()) {
            super.onTaskRemoved(rootIntent)
            return
        }

        val restart: Intent? = Intent(
            getApplicationContext(),
            BackgroundService::class.java
        )
            .setPackage(getPackageName())
        val pi = PendingIntent.getService(
            getApplicationContext(), 1, restart!!,
            PendingIntent.FLAG_IMMUTABLE
        )
        val am = getSystemService(
            ALARM_SERVICE
        ) as AlarmManager
        am.set(
            AlarmManager.ELAPSED_REALTIME,
            SystemClock.elapsedRealtime() + 1000, pi!!
        )
        super.onTaskRemoved(rootIntent)
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(closeReceiver)
        unregisterReceiver(dismissReceiver)
        instance = null
        isRunning = false
    }


    @Blocking
    fun getCore(context: MainActivity): Core {
        while (core == null) {
            try {
                Thread.sleep(200)
            } catch (e: InterruptedException) {
            }
        }
        return CoreImpl(context, core)
    }

    companion object {
        @SuppressLint("StaticFieldLeak")
        var instance: BackgroundService? = null
        private var isRunning = false
        private const val CLOSE_SERVICE_ACTION = "de.hype.hypenotify.CLOSE_SERVICE_ACTION"
        private const val DISMISS_ACTION = "de.hype.hypenotify.NOTIF_DISMISSED"

        @get:Blocking val instanceBlocking: BackgroundService
            /**
             * Make sure you start the Service before calling this method!
             */
            get() {
                while (instance == null) {
                    try {
                        Thread.sleep(500)
                    } catch (e: InterruptedException) {
                        throw RuntimeException(e)
                    }
                }
                return instance!!
            }

        /**
         * Will wait until
         *
         * @param consumer consumer to be run.
         */
        fun executeWithBackgroundService(consumer: Consumer<BackgroundService?>) {
            val thread = Thread(Runnable {
                val instance: BackgroundService = instanceBlocking
                consumer.accept(instance)
            })
            thread.start()
        }
    }
}
