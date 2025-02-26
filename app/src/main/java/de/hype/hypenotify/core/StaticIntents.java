package de.hype.hypenotify.core;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.BatteryManager;
import androidx.core.app.NotificationCompat;
import de.hype.hypenotify.MainActivity;
import de.hype.hypenotify.NotificationUtils;
import de.hype.hypenotify.R;
import de.hype.hypenotify.core.interfaces.Core;
import de.hype.hypenotify.core.interfaces.MiniCore;
import de.hype.hypenotify.services.TimerService;
import de.hype.hypenotify.tools.notification.NotificationBuilder;
import de.hype.hypenotify.tools.notification.NotificationCategory;
import de.hype.hypenotify.tools.notification.NotificationChannels;
import de.hype.hypenotify.tools.notification.NotificationImportance;

import java.util.List;

import static android.content.Context.BATTERY_SERVICE;
import static de.hype.hypenotify.core.IntentBuilder.DEFAULT_CREATE_NEW;
import static de.hype.hypenotify.core.IntentBuilder.DEFAULT_FRONT_OR_CREATE;

public enum StaticIntents implements de.hype.hypenotify.core.Intent {
    TIMER_HIT {
        @Override
        public void handleIntentInternal(Intent intent, de.hype.hypenotify.core.interfaces.MiniCore core, Context context) {
            int timerId = intent.getIntExtra("timerId", 0);
            if (timerId == 0) {
                NotificationBuilder notificationBuilder = new NotificationBuilder(context, "SmartTimer hit", "SmartTimer hit intent received without timerId", NotificationChannels.ERROR);
                notificationBuilder.send();
                return;
            }
            TimerService.SmartTimer timer = core.timerService().getTimerById(timerId);
            if (timer != null && timer.shallRing()) {
                LauchAppBypass bypass = launchAPP(core, NotificationCategory.CATEGORY_ALARM, DynamicIntents.TIMER_HIT);
                bypass.setInt("timerId", timerId);
                bypass.launch();
            }
        }

        @Override
        public List<IntentBuilder.IntentFlag> getFlags() {
            return DEFAULT_CREATE_NEW;
        }
    },
    BATTERY_REMINDER_CHECK {
        @Override
        public void handleIntentInternal(Intent intent, MiniCore core, Context context) {
            if (core.isInHomeNetwork() && isBatteryLow(context)) {
                notifyUser(context);
            }
        }

        private boolean isBatteryLow(Context context) {
            BatteryManager bm = (BatteryManager) context.getSystemService(BATTERY_SERVICE);
            if (bm.isCharging()) return false;
            return bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY) < 50;
        }

        private void notifyUser(Context context) {
            NotificationUtils.createNotification(context, "Charge the Battery", "The Mobile Phone is not plugged in. Daily Reminder to charge it.", NotificationChannels.BATTERY_WARNING, NotificationImportance.DEFAULT);
            MediaPlayer mediaPlayer = MediaPlayer.create(context, R.raw.alarm);
            mediaPlayer.setLooping(true);
            mediaPlayer.start();

            // Stop the sound after 5 minutes or if acknowledged
            new android.os.Handler().postDelayed(() -> {
                if (mediaPlayer.isPlaying()) {
                    mediaPlayer.stop();
                    mediaPlayer.release();
                }
            }, 5 * 60 * 1000);
        }

        @Override
        public List<IntentBuilder.IntentFlag> getFlags() {
            return DEFAULT_FRONT_OR_CREATE;
        }
    };

    public static String BASE_INTENT_NAME = "de.hype.hypenotify.ENUM_INTENT";

    @Override
    public String intentId() {
        return name();
    }

    public static void onIntent(MiniCore core, Context context, Intent basicIntent) {
        StaticIntents intent;
        try {
            intent = StaticIntents.valueOf(basicIntent.getStringExtra("intentId"));
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
        intent.handleIntentInternal(basicIntent, core, context);
    }

    @Override
    public void handleIntentInternal(Intent intent, Core core, MainActivity context) {
        handleIntentInternal(intent, core, (Context) context);
    }

    public abstract void handleIntentInternal(Intent intent, MiniCore core, Context context);

    public abstract List<IntentBuilder.IntentFlag> getFlags();

    protected LauchAppBypass launchAPP(MiniCore core, NotificationCategory category, DynamicIntents dynamicIntent) {
        return new LauchAppBypass(core, category, dynamicIntent);
    }

    public IntentBuilder getAsIntent(Context context) {
        IntentBuilder builder = new IntentBuilder(context, this);
        builder.setFlags(getFlags());
        return builder;
    }

    protected static class LauchAppBypass {
        private final Context context;
        private Intent intent;
        private final NotificationCompat.Builder notifyicationBuilder;

        public LauchAppBypass(MiniCore core, NotificationCategory category, DynamicIntents dynamicIntent) {
            this.context = core.context();
            notifyicationBuilder = new NotificationCompat.Builder(context, NotificationChannels.PRIORITY_LAUNCH.channelId);
            notifyicationBuilder.setCategory(category.categoryId);
            notifyicationBuilder.setSmallIcon(R.mipmap.icon);
            notifyicationBuilder.setContentText("The Fact your seeing this means that your Functionality with Timers is extremely limited on this Device and are likely to not work SILENTLY\nLauch App Bypass for Category: ’%s’ | Id: ’%s’".formatted(category, dynamicIntent.intentId));
            intent = dynamicIntent.getAsIntent(context).getAsIntent();
        }

        public void setString(String key, String value) {
            intent.putExtra(key, value);
        }

        public void setInt(String key, Integer value) {
            intent.putExtra(key, value);
        }

        public void launch() {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            PendingIntent pendingIntent = PendingIntent.getActivity(context, IntentBuilder.generateId(), intent, PendingIntent.FLAG_IMMUTABLE);
            notifyicationBuilder.setFullScreenIntent(pendingIntent, true);
            notifyicationBuilder.setVisibility(NotificationCompat.VISIBILITY_PUBLIC);
            notifyicationBuilder.setPriority(NotificationCompat.PRIORITY_MAX);
//            context.getSystemService(NotificationManager.class).notify(Notification.generateId(), notifyicationBuilder.build());
            context.startActivity(intent);
        }
    }
}
