package de.hype.hypenotify.app.core

import android.Manifest
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.os.BatteryManager
import android.os.Handler
import androidx.annotation.RequiresPermission
import androidx.core.app.NotificationCompat
import de.hype.hypenotify.R
import de.hype.hypenotify.app.MainActivity
import de.hype.hypenotify.app.NotificationUtils
import de.hype.hypenotify.app.core.IntentBuilder.IntentFlag
import de.hype.hypenotify.app.core.interfaces.Core
import de.hype.hypenotify.app.core.interfaces.MiniCore
import de.hype.hypenotify.app.tools.notification.NotificationBuilder
import de.hype.hypenotify.app.tools.notification.NotificationCategory
import de.hype.hypenotify.app.tools.notification.NotificationChannels
import de.hype.hypenotify.app.tools.notification.NotificationImportance
import de.hype.hypenotify.app.tools.pojav.PojavLauncherUtils
import java.util.*

enum class StaticIntents : de.hype.hypenotify.app.core.Intent {
    TIMER_HIT {
        override fun handleIntentInternal(intent: Intent, core: MiniCore, context: Context?) {
            val uuidString = intent.getStringExtra("timerId")
            if (uuidString == null) {
                val notificationBuilder =
                    NotificationBuilder(context, "SmartTimer hit", "Timer hit intent received without timerId", NotificationChannels.ERROR)
                notificationBuilder.send()
                return
            }
            val timerId = UUID.fromString(uuidString)
            val timer = core.timerService().getTimerByClientId(timerId)
            if (timer != null && timer.shouldRing(core)) {
                val bypass = launchAPP(core, NotificationCategory.CATEGORY_ALARM, DynamicIntents.TIMER_HIT)
                bypass.setString("timerId", timerId.toString())
                bypass.launch()
            }
        }

        override fun getFlags(): MutableList<IntentFlag?>? {
            return IntentBuilder.Companion.DEFAULT_CREATE_NEW
        }
    },
    LAUNCH_BAZAAR {
        override fun handleIntentInternal(intent: Intent?, core: MiniCore?, context: Context?) {
            PojavLauncherUtils.Companion.launchToHub(core)
        }

        override fun getFlags(): MutableList<IntentFlag?> {
            return mutableListOf<IntentFlag?>()
        }
    },
    BATTERY_REMINDER_CHECK {
        override fun handleIntentInternal(intent: Intent?, core: MiniCore, context: Context) {
            if (core.isInFreeNetwork() && isBatteryLow(context)) {
                notifyUser(context)
            }
            StaticIntents.Companion.scheduleDailyBatteryCheck(core, false)
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
            Handler().postDelayed(Runnable {
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

    override fun intentId(): String {
        return name
    }

    override fun handleIntentInternal(intent: Intent?, core: Core?, context: MainActivity?) {
        handleIntentInternal(intent, core, context as Context?)
    }

    abstract fun handleIntentInternal(intent: Intent?, core: MiniCore?, context: Context?)

    abstract val flags: MutableList<IntentFlag?>?

    protected fun launchAPP(core: MiniCore, category: NotificationCategory, dynamicIntent: DynamicIntents): LauchAppBypass {
        return LauchAppBypass(core, category, dynamicIntent)
    }

    fun getAsIntent(context: Context?): IntentBuilder {
        val builder = IntentBuilder(context, this)
        builder.setFlags(this.flags)
        return builder
    }

    protected class LauchAppBypass(core: MiniCore, category: NotificationCategory, dynamicIntent: DynamicIntents) {
        private val context: Context
        private val intent: Intent
        private val notifyicationBuilder: NotificationCompat.Builder

        init {
            this.context = core.context()
            notifyicationBuilder = NotificationCompat.Builder(context, NotificationChannels.PRIORITY_LAUNCH.channelId)
            notifyicationBuilder.setCategory(category.categoryId)
            notifyicationBuilder.setSmallIcon(R.mipmap.icon)
            notifyicationBuilder.setContentText(
                "The Fact your seeing this means that your Functionality with Timers is extremely limited on this Device and are likely to not work SILENTLY\nLauch App Bypass for Category: ’%s’ | Id: ’%s'".formatted(
                    category,
                    dynamicIntent.intentId
                )
            )
            intent = dynamicIntent.getAsIntent(context).getAsIntent()
        }

        fun setString(key: String?, value: String?) {
            intent.putExtra(key, value)
        }

        fun setInt(key: String?, value: Int?) {
            intent.putExtra(key, value)
        }

        fun launch() {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            val pendingIntent =
                PendingIntent.getActivity(context, IntentBuilder.Companion.generateId(), intent, PendingIntent.FLAG_IMMUTABLE)
            notifyicationBuilder.setFullScreenIntent(pendingIntent, true)
            notifyicationBuilder.setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            notifyicationBuilder.setPriority(NotificationCompat.PRIORITY_MAX)
            //            context.getSystemService(NotificationManager.class).notify(Notification.generateId(), notifyicationBuilder.build());
            context.startActivity(intent)
        }
    }


    companion object {
        var BASE_INTENT_NAME: String = "de.hype.hypenotify.ENUM_INTENT"

        fun onIntent(core: MiniCore?, context: Context?, basicIntent: Intent) {
            val intent: StaticIntents?
            try {
                intent = StaticIntents.valueOf(basicIntent.getStringExtra("intentId")!!)
            } catch (e: Exception) {
                e.printStackTrace()
                return
            }
            intent.handleIntentInternal(basicIntent, core, context)
        }

        @RequiresPermission(Manifest.permission.SCHEDULE_EXACT_ALARM)
        fun scheduleDailyBatteryCheck(core: MiniCore, fromInit: Boolean) {
            val alarmManager = core.context().getSystemService(Context.ALARM_SERVICE) as AlarmManager?
            val intent = DynamicIntents.BATTERY_REMINDER_CHECK.getAsIntent(core.context())

            val pendingIntent = intent.getAsPending()

            val calendar = Calendar.getInstance()
            calendar.set(Calendar.HOUR_OF_DAY, 19) // 7 PM
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            if (!fromInit) calendar.add(Calendar.DAY_OF_MONTH, 1)

            if (alarmManager != null) {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent!!)
            }
        }
    }
}
