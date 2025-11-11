package de.hype.hypenotify.app.tools.notification

import android.app.PendingIntent
import android.content.Context
import android.os.Bundle
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import androidx.core.app.Person
import androidx.core.content.pm.ShortcutInfoCompat
import de.hype.hypenotify.R
import de.hype.hypenotify.app.core.DynamicIntents
import de.hype.hypenotify.app.core.interfaces.Core

enum class NotificationShowcase(method: String) {
    ADD_ACTION_NOTIFICATION_COMPAT_ACTION("addAction(@Nullable NotificationCompat.Action action)") {
        override fun doCustom(builder: NotificationCompat.Builder, context: Context?) {
            val action = NotificationCompat.Action.Builder(
                android.R.drawable.ic_menu_send, "Reply", getIntent(context)
            ).build()
            builder.addAction(action)
        }
    },
    ADD_ACTION_INT_CHARSEQUENCE_PENDINGINTENT("addAction(int icon, @Nullable CharSequence title, @Nullable PendingIntent intent)") {
        override fun doCustom(builder: NotificationCompat.Builder, context: Context?) {
            val pendingIntent = getIntent(context)
            builder.addAction(android.R.drawable.ic_menu_view, "View", pendingIntent)
        }
    },
    ADD_EXTRAS_BUNDLE("addExtras(@Nullable Bundle extras)") {
        override fun doCustom(builder: NotificationCompat.Builder, context: Context?) {
            val extras = Bundle()
            extras.putString("extra_key", "extra_value")
            builder.addExtras(extras)
        }
    },
    ADD_INVISIBLE_ACTION_NOTIFICATION_COMPAT_ACTION("addInvisibleAction(@Nullable NotificationCompat.Action action)") {
        override fun doCustom(builder: NotificationCompat.Builder, context: Context?) {
            val invisibleAction = NotificationCompat.Action.Builder(
                android.R.drawable.ic_menu_call, "Call", getIntent(context)
            ).build()
            builder.addInvisibleAction(invisibleAction)
        }
    },
    ADD_PERSON_PERSON("addPerson(@Nullable Person person)") {
        override fun doCustom(builder: NotificationCompat.Builder, context: Context?) {
            val person = Person.Builder().setName("John Doe").build()
            builder.addPerson(person)
        }
    },
    CREATE_BIG_CONTENT_VIEW("createBigContentView()") {
        override fun doCustom(builder: NotificationCompat.Builder, context: Context) {
            val bigContentView = RemoteViews(context.getPackageName(), R.layout.notification_big_content)
            builder.setCustomBigContentView(bigContentView)
        }
    },
    CREATE_CONTENT_VIEW("createContentView()") {
        override fun doCustom(builder: NotificationCompat.Builder, context: Context) {
            val contentView = RemoteViews(context.getPackageName(), R.layout.notification_content)
            builder.setCustomContentView(contentView)
        }
    },
    CREATE_HEADS_UP_CONTENT_VIEW("createHeadsUpContentView()") {
        override fun doCustom(builder: NotificationCompat.Builder, context: Context) {
            val headsUpContentView = RemoteViews(context.getPackageName(), R.layout.notification_heads_up_content)
            builder.setCustomHeadsUpContentView(headsUpContentView)
        }
    },
    SET_ALLOW_SYSTEM_GENERATED_CONTEXTUAL_ACTIONS("setAllowSystemGeneratedContextualActions(boolean allowed)") {
        override fun doCustom(builder: NotificationCompat.Builder, context: Context?) {
            builder.setAllowSystemGeneratedContextualActions(true)
        }
    },
    SET_AUTO_CANCEL("setAutoCancel(boolean autoCancel)") {
        override fun doCustom(builder: NotificationCompat.Builder, context: Context?) {
            builder.setAutoCancel(true)
        }
    },
    SET_BUBBLE_METADATA("setBubbleMetadata(@Nullable NotificationCompat.BubbleMetadata data)") {
        override fun doCustom(builder: NotificationCompat.Builder, context: Context?) {
            val bubbleMetadata = NotificationCompat.BubbleMetadata.Builder()
                .setDesiredHeight(600)
                .setIntent(DynamicIntents.TIMER_HIT.getAsIntent(context).getAsPending())
                .build()
            builder.setBubbleMetadata(bubbleMetadata)
        }
    },
    SET_COLORIZED("setColorized(boolean colorize)") {
        override fun doCustom(builder: NotificationCompat.Builder, context: Context?) {
            builder.setColorized(true)
        }
    },
    SET_CONTENT("setContent(@Nullable RemoteViews views)") {
        override fun doCustom(builder: NotificationCompat.Builder, context: Context) {
            val contentView = RemoteViews(context.getPackageName(), R.layout.notification_content)
            builder.setContent(contentView)
        }
    },
    SET_CONTENT_INTENT("setContentIntent(@Nullable PendingIntent intent)") {
        override fun doCustom(builder: NotificationCompat.Builder, context: Context?) {
            val contentIntent = getIntent(context)
            builder.setContentIntent(contentIntent)
        }
    },
    SET_CUSTOM_BIG_CONTENT_VIEW("setCustomBigContentView(@Nullable RemoteViews contentView)") {
        override fun doCustom(builder: NotificationCompat.Builder, context: Context) {
            val bigContentView = RemoteViews(context.getPackageName(), R.layout.notification_big_content)
            builder.setCustomBigContentView(bigContentView)
        }
    },
    SET_CUSTOM_CONTENT_VIEW("setCustomContentView(@Nullable RemoteViews contentView)") {
        override fun doCustom(builder: NotificationCompat.Builder, context: Context) {
            val contentView = RemoteViews(context.getPackageName(), R.layout.notification_content)
            builder.setCustomContentView(contentView)
        }
    },
    SET_CUSTOM_HEADS_UP_CONTENT_VIEW("setCustomHeadsUpContentView(@Nullable RemoteViews contentView)") {
        override fun doCustom(builder: NotificationCompat.Builder, context: Context) {
            val headsUpContentView = RemoteViews(context.getPackageName(), R.layout.notification_heads_up_content)
            builder.setCustomHeadsUpContentView(headsUpContentView)
        }
    },
    SET_FULL_SCREEN_INTENT("setFullScreenIntent(@Nullable PendingIntent intent, boolean highPriority)") {
        override fun doCustom(builder: NotificationCompat.Builder, context: Context?) {
            val fullScreenIntent = getIntent(context)
            builder.setFullScreenIntent(fullScreenIntent, true)
        }
    },
    SET_GROUP_ALERT_BEHAVIOR("setGroupAlertBehavior(int groupAlertBehavior)") {
        override fun doCustom(builder: NotificationCompat.Builder, context: Context?) {
            builder.setGroupAlertBehavior(NotificationCompat.GROUP_ALERT_SUMMARY)
        }
    },
    SET_LIGHTS("setLights(@ColorInt int argb, int onMs, int offMs)") {
        override fun doCustom(builder: NotificationCompat.Builder, context: Context?) {
            builder.setLights(-0xff0100, 300, 1000)
        }
    },
    SET_ONGOING("setOngoing(boolean ongoing)") {
        override fun doCustom(builder: NotificationCompat.Builder, context: Context?) {
            builder.setOngoing(true)
        }
    },
    SET_PRIORITY("setPriority(int pri)") {
        override fun doCustom(builder: NotificationCompat.Builder, context: Context?) {
            builder.setPriority(NotificationCompat.PRIORITY_HIGH)
        }
    },
    SET_REMOTE_INPUT_HISTORY("setRemoteInputHistory(@Nullable CharSequence[] text)") {
        override fun doCustom(builder: NotificationCompat.Builder, context: Context?) {
            val history = arrayOf<CharSequence?>("Previous message")
            builder.setRemoteInputHistory(history)
        }
    },
    SET_SETTINGS_TEXT("setSettingsText(@Nullable CharSequence text)") {
        override fun doCustom(builder: NotificationCompat.Builder, context: Context?) {
            builder.setSettingsText("Settings text")
        }
    },
    SET_SHORTCUT_ID("setShortcutId(@Nullable String shortcutId)") {
        override fun doCustom(builder: NotificationCompat.Builder, context: Context?) {
            builder.setShortcutId("shortcut_id")
        }
    },
    SET_SHORTCUT_INFO("setShortcutInfo(@Nullable ShortcutInfoCompat shortcutInfo)") {
        override fun doCustom(builder: NotificationCompat.Builder, context: Context) {
            val shortcutInfo = ShortcutInfoCompat.Builder(context, "shortcut_id").setShortLabel("Shortcut").build()
            builder.setShortcutInfo(shortcutInfo)
        }
    },
    SET_TICKER("setTicker(@Nullable CharSequence tickerText)") {
        override fun doCustom(builder: NotificationCompat.Builder, context: Context?) {
            builder.setTicker("Ticker text")
        }
    };


    private val method: String?

    init {
        this.method = method
    }

    fun run(core: Core) {
        val notificationBuilder =
            NotificationBuilder(
                core,
                "Showcase",
                method,
                NotificationChannels.OTHER,
                NotificationImportance.LOW,
                NotificationVisibility.Companion.PUBLIC
            )
        doCustom(notificationBuilder.getHiddenBuilder(), core.context())
        notificationBuilder.send()
    }

    abstract fun doCustom(builder: NotificationCompat.Builder?, context: Context?)

    fun getIntent(context: Context?): PendingIntent? {
        val intent = DynamicIntents.TIMER_HIT.getAsIntent(context).getAsPending()
        return intent
    }
}