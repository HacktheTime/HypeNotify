package de.hype.hypenotify.app.core

import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.os.BatteryManager
import android.os.Handler
import android.os.Looper
import android.util.Log
import de.hype.hypenotify.R
import de.hype.hypenotify.app.MainActivity
import de.hype.hypenotify.app.NotificationUtils
import de.hype.hypenotify.app.core.IntentBuilder.IntentFlag
import de.hype.hypenotify.app.core.interfaces.Core
import de.hype.hypenotify.app.screen.TimerAlarmScreen
import de.hype.hypenotify.app.tools.notification.NotificationBuilder
import de.hype.hypenotify.app.tools.notification.NotificationChannels
import de.hype.hypenotify.app.tools.notification.NotificationImportance
import java.time.Duration
import java.time.Instant
import java.util.*
import java.util.concurrent.TimeUnit

enum class DynamicIntents(val intentId: String) : de.hype.hypenotify.app.core.Intent {
    TIMER_HIT("timer_hit") {
        override fun handleIntentInternal(intent: Intent, core: Core, context: MainActivity) {
            context.setShowWhenLocked(true)
            context.setTurnScreenOn(true)
            val notificationBuilder =
                NotificationBuilder(context, "SmartTimer hit", "SmartTimer hit intent received without timerId", NotificationChannels.ERROR)
            val uuidString = intent.getStringExtra("timerId")
            if (uuidString == null) {
                notificationBuilder.send()
                return
            }
            val timerId = UUID.fromString(uuidString)
            val timer = core.timerService().getTimerByClientId(timerId)
            if (timer != null) {
                val between = Duration.between(Instant.now(), timer.getTime())
                val alarm = core.executionService().schedule(Runnable {
                    val timerAlarmScreen = TimerAlarmScreen(core, timer)
                    context.runOnUiThread(Runnable {
                        context.setContentViewNoOverride(timerAlarmScreen)
                    })
                }, between.getSeconds(), TimeUnit.SECONDS)
                core.executionService().execute(Runnable {
                    val shouldRing = timer.shouldRing(core)
                    if (shouldRing) alarm.cancel(false)
                })
            }
        }

        override fun getFlags(): MutableList<IntentFlag?>? {
            return IntentBuilder.Companion.DEFAULT_CREATE_NEW
        }
    },
    BATTERY_REMINDER_CHECK("battery_reminder_check") {
        override fun handleIntentInternal(intent: Intent?, core: Core, context: MainActivity) {
            if (core.isInFreeNetwork() && isBatteryLow(context)) {
                notifyUser(context)
            }
        }

        private fun isBatteryLow(context: Context): Boolean {
            val bm = context.getSystemService(Context.BATTERY_SERVICE) as BatteryManager
            if (bm.isCharging()) return false
            return bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY) < 50
        }

        private fun notifyUser(context: Context?) {
            NotificationUtils.createNotification(
                context,
                "Charge the Battery",
                "The Mobile Phone is not plugged in. Daily Reminder to charge it.",
                NotificationChannels.BATTERY_WARNING,
                NotificationImportance.DEFAULT
            )
            val mediaPlayer = MediaPlayer.create(context, R.raw.alarm)
            mediaPlayer.setLooping(true)
            mediaPlayer.start()

            // Stop the sound after 5 minutes or if acknowledged
            val handler = Handler(Looper.getMainLooper())
            handler.postDelayed(Runnable {
                if (mediaPlayer.isPlaying()) {
                    mediaPlayer.stop()
                    mediaPlayer.release()
                }
            }, (5 * 60 * 1000).toLong())
        }

        override fun getFlags(): MutableList<IntentFlag?>? {
            return IntentBuilder.Companion.DEFAULT_FRONT_OR_CREATE
        }
    };

    abstract override fun handleIntentInternal(intent: Intent?, core: Core?, context: MainActivity?)

    fun getAsIntent(context: Context?): IntentBuilder {
        val builder = IntentBuilder(context, this)
        builder.setFlags(this.flags)
        return builder
    }

    abstract val flags: MutableList<IntentFlag?>?

    override fun intentId(): String {
        return intentId
    }


    companion object {
        const val PACKAGE_NAME: String = "de.hype.hypenotify"
        val DYNAMIC_INTENT: String = PACKAGE_NAME + ".DYNAMIC_ENUM_INTENT"


        /**
         * @param context    the current context
         */
        fun startBackgroundService(context: Context) {
            val serviceIntent = Intent(context, BackgroundService::class.java)
            context.startService(serviceIntent)
        }

        fun handleIntent(intent: Intent, core: Core?, context: MainActivity?): Boolean {
            Log.i("DynamicIntents", "hypeNotify: Intent specified in onCreate(): %s (%s)".formatted(intent.getAction(), intent.getData()))
            val smartIntent: DynamicIntents? = getIntentByAction(intent.getStringExtra("intentId"))
            if (smartIntent == null) return false
            smartIntent.handleIntentInternal(intent, core, context)
            return true
        }

        private fun getIntentByAction(intentId: String?): DynamicIntents? {
            if (intentId == null) return null
            for (i in DynamicIntents.entries) {
                if (i.intentId == intentId) {
                    return i
                }
            }
            return null
        }
    }
}
