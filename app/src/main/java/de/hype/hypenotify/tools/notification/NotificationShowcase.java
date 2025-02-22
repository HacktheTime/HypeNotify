package de.hype.hypenotify.tools.notification;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.RemoteViews;
import androidx.core.app.NotificationCompat;
import androidx.core.app.Person;
import androidx.core.content.pm.ShortcutInfoCompat;
import de.hype.hypenotify.Core;
import de.hype.hypenotify.Intents;
import de.hype.hypenotify.NotificationUtils;
import de.hype.hypenotify.R;

public enum NotificationShowcase {
    ADD_ACTION_NOTIFICATION_COMPAT_ACTION("addAction(@Nullable NotificationCompat.Action action)") {
        @Override
        public void doCustom(NotificationCompat.Builder builder, Context context) {
            NotificationCompat.Action action = new NotificationCompat.Action.Builder(
                    android.R.drawable.ic_menu_send, "Reply", getIntent(context)).build();
            builder.addAction(action);
        }
    },
    ADD_ACTION_INT_CHARSEQUENCE_PENDINGINTENT("addAction(int icon, @Nullable CharSequence title, @Nullable PendingIntent intent)") {
        @Override
        public void doCustom(NotificationCompat.Builder builder, Context context) {
            PendingIntent pendingIntent = getIntent(context);
            builder.addAction(android.R.drawable.ic_menu_view, "View", pendingIntent);
        }
    },
    ADD_EXTRAS_BUNDLE("addExtras(@Nullable Bundle extras)") {
        @Override
        public void doCustom(NotificationCompat.Builder builder, Context context) {
            Bundle extras = new Bundle();
            extras.putString("extra_key", "extra_value");
            builder.addExtras(extras);
        }
    },
    ADD_INVISIBLE_ACTION_NOTIFICATION_COMPAT_ACTION("addInvisibleAction(@Nullable NotificationCompat.Action action)") {
        @Override
        public void doCustom(NotificationCompat.Builder builder, Context context) {
            NotificationCompat.Action invisibleAction = new NotificationCompat.Action.Builder(
                    android.R.drawable.ic_menu_call, "Call", getIntent(context)).build();
            builder.addInvisibleAction(invisibleAction);
        }
    },
    ADD_PERSON_PERSON("addPerson(@Nullable Person person)") {
        @Override
        public void doCustom(NotificationCompat.Builder builder, Context context) {
            Person person = new Person.Builder().setName("John Doe").build();
            builder.addPerson(person);
        }
    },
    CREATE_BIG_CONTENT_VIEW("createBigContentView()") {
        @Override
        public void doCustom(NotificationCompat.Builder builder, Context context) {
            RemoteViews bigContentView = new RemoteViews(context.getPackageName(), R.layout.notification_big_content);
            builder.setCustomBigContentView(bigContentView);
        }
    },
    CREATE_CONTENT_VIEW("createContentView()") {
        @Override
        public void doCustom(NotificationCompat.Builder builder, Context context) {
            RemoteViews contentView = new RemoteViews(context.getPackageName(), R.layout.notification_content);
            builder.setCustomContentView(contentView);
        }
    },
    CREATE_HEADS_UP_CONTENT_VIEW("createHeadsUpContentView()") {
        @Override
        public void doCustom(NotificationCompat.Builder builder, Context context) {
            RemoteViews headsUpContentView = new RemoteViews(context.getPackageName(), R.layout.notification_heads_up_content);
            builder.setCustomHeadsUpContentView(headsUpContentView);
        }
    },
    SET_ALLOW_SYSTEM_GENERATED_CONTEXTUAL_ACTIONS("setAllowSystemGeneratedContextualActions(boolean allowed)") {
        @Override
        public void doCustom(NotificationCompat.Builder builder, Context context) {
            builder.setAllowSystemGeneratedContextualActions(true);
        }
    },
    SET_AUTO_CANCEL("setAutoCancel(boolean autoCancel)") {
        @Override
        public void doCustom(NotificationCompat.Builder builder, Context context) {
            builder.setAutoCancel(true);
        }
    },
    SET_BUBBLE_METADATA("setBubbleMetadata(@Nullable NotificationCompat.BubbleMetadata data)") {
        @Override
        public void doCustom(NotificationCompat.Builder builder, Context context) {
            NotificationCompat.BubbleMetadata bubbleMetadata = new NotificationCompat.BubbleMetadata.Builder()
                    .setDesiredHeight(600)
                    .setIntent(PendingIntent.getActivity(context, 0, Intents.TIMER_HIT.getAsIntent(context), PendingIntent.FLAG_IMMUTABLE))
                    .build();
            builder.setBubbleMetadata(bubbleMetadata);
        }
    },
    SET_COLORIZED("setColorized(boolean colorize)") {
        @Override
        public void doCustom(NotificationCompat.Builder builder, Context context) {
            builder.setColorized(true);
        }
    },
    SET_CONTENT("setContent(@Nullable RemoteViews views)") {
        @Override
        public void doCustom(NotificationCompat.Builder builder, Context context) {
            RemoteViews contentView = new RemoteViews(context.getPackageName(), R.layout.notification_content);
            builder.setContent(contentView);
        }
    },
    SET_CONTENT_INTENT("setContentIntent(@Nullable PendingIntent intent)") {
        @Override
        public void doCustom(NotificationCompat.Builder builder, Context context) {
            PendingIntent contentIntent = getIntent(context);
            builder.setContentIntent(contentIntent);
        }
    },
    SET_CUSTOM_BIG_CONTENT_VIEW("setCustomBigContentView(@Nullable RemoteViews contentView)") {
        @Override
        public void doCustom(NotificationCompat.Builder builder, Context context) {
            RemoteViews bigContentView = new RemoteViews(context.getPackageName(), R.layout.notification_big_content);
            builder.setCustomBigContentView(bigContentView);
        }
    },
    SET_CUSTOM_CONTENT_VIEW("setCustomContentView(@Nullable RemoteViews contentView)") {
        @Override
        public void doCustom(NotificationCompat.Builder builder, Context context) {
            RemoteViews contentView = new RemoteViews(context.getPackageName(), R.layout.notification_content);
            builder.setCustomContentView(contentView);
        }
    },
    SET_CUSTOM_HEADS_UP_CONTENT_VIEW("setCustomHeadsUpContentView(@Nullable RemoteViews contentView)") {
        @Override
        public void doCustom(NotificationCompat.Builder builder, Context context) {
            RemoteViews headsUpContentView = new RemoteViews(context.getPackageName(), R.layout.notification_heads_up_content);
            builder.setCustomHeadsUpContentView(headsUpContentView);
        }
    },
    SET_FULL_SCREEN_INTENT("setFullScreenIntent(@Nullable PendingIntent intent, boolean highPriority)") {
        @Override
        public void doCustom(NotificationCompat.Builder builder, Context context) {
            PendingIntent fullScreenIntent = getIntent(context);
            builder.setFullScreenIntent(fullScreenIntent, true);
        }
    },
    SET_GROUP_ALERT_BEHAVIOR("setGroupAlertBehavior(int groupAlertBehavior)") {
        @Override
        public void doCustom(NotificationCompat.Builder builder, Context context) {
            builder.setGroupAlertBehavior(NotificationCompat.GROUP_ALERT_SUMMARY);
        }
    },
    SET_LIGHTS("setLights(@ColorInt int argb, int onMs, int offMs)") {
        @Override
        public void doCustom(NotificationCompat.Builder builder, Context context) {
            builder.setLights(0xFF00FF00, 300, 1000);
        }
    },
    SET_ONGOING("setOngoing(boolean ongoing)") {
        @Override
        public void doCustom(NotificationCompat.Builder builder, Context context) {
            builder.setOngoing(true);
        }
    },
    SET_PRIORITY("setPriority(int pri)") {
        @Override
        public void doCustom(NotificationCompat.Builder builder, Context context) {
            builder.setPriority(NotificationCompat.PRIORITY_HIGH);
        }
    },
    SET_REMOTE_INPUT_HISTORY("setRemoteInputHistory(@Nullable CharSequence[] text)") {
        @Override
        public void doCustom(NotificationCompat.Builder builder, Context context) {
            CharSequence[] history = {"Previous message"};
            builder.setRemoteInputHistory(history);
        }
    },
    SET_SETTINGS_TEXT("setSettingsText(@Nullable CharSequence text)") {
        @Override
        public void doCustom(NotificationCompat.Builder builder, Context context) {
            builder.setSettingsText("Settings text");
        }
    },
    SET_SHORTCUT_ID("setShortcutId(@Nullable String shortcutId)") {
        @Override
        public void doCustom(NotificationCompat.Builder builder, Context context) {
            builder.setShortcutId("shortcut_id");
        }
    },
    SET_SHORTCUT_INFO("setShortcutInfo(@Nullable ShortcutInfoCompat shortcutInfo)") {
        @Override
        public void doCustom(NotificationCompat.Builder builder, Context context) {
            ShortcutInfoCompat shortcutInfo = new ShortcutInfoCompat.Builder(context, "shortcut_id").setShortLabel("Shortcut").build();
            builder.setShortcutInfo(shortcutInfo);
        }
    },
    SET_TICKER("setTicker(@Nullable CharSequence tickerText)") {
        @Override
        public void doCustom(NotificationCompat.Builder builder, Context context) {
            builder.setTicker("Ticker text");
        }
    };


    private final String method;

    NotificationShowcase(String method) {
        this.method = method;
    }

    public void run(Core core) {
        NotificationBuilder notificationBuilder = new NotificationBuilder(core, "Showcase", method, NotificationChannels.OTHER, NotificationImportance.LOW, NotificationVisibility.PUBLIC);
        doCustom(notificationBuilder.getHiddenBuilder(), core.context);
        NotificationUtils.sendNotification(core.context, notificationBuilder);
    }

    public abstract void doCustom(NotificationCompat.Builder builder, Context context);

    public PendingIntent getIntent(Context context) {
        Intent intent = Intents.TIMER_HIT.getAsIntent(context);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP); // Optional: Adjust based on use case

        return PendingIntent.getActivity(
                context,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_MUTABLE // Use FLAG_IMMUTABLE if you don’t need mutability
        );
    }

}