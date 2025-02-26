package de.hype.hypenotify.core;

import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.BatteryManager;
import android.util.Log;
import de.hype.hypenotify.MainActivity;
import de.hype.hypenotify.NotificationUtils;
import de.hype.hypenotify.R;
import de.hype.hypenotify.core.interfaces.Core;
import de.hype.hypenotify.layouts.TimerAlarmScreen;
import de.hype.hypenotify.services.HypeNotifyServiceConnection;
import de.hype.hypenotify.services.TimerService;
import de.hype.hypenotify.tools.notification.NotificationBuilder;
import de.hype.hypenotify.tools.notification.NotificationChannels;
import de.hype.hypenotify.tools.notification.NotificationImportance;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static android.content.Context.BATTERY_SERVICE;
import static de.hype.hypenotify.core.IntentBuilder.DEFAULT_CREATE_NEW;
import static de.hype.hypenotify.core.IntentBuilder.DEFAULT_FRONT_OR_CREATE;

public enum DynamicIntents implements de.hype.hypenotify.core.Intent {
    TIMER_HIT("timer_hit") {
        @Override
        public void handleIntentInternal(Intent intent, Core core, MainActivity context) {
            context.setShowWhenLocked(true);
            context.setTurnScreenOn(true);
            NotificationBuilder notificationBuilder = new NotificationBuilder(context, "SmartTimer hit", "SmartTimer hit intent received without timerId", NotificationChannels.ERROR);
            int timerId = intent.getIntExtra("timerId", 0);
            if (timerId == 0) {
                notificationBuilder.send();
                return;
            }
            TimerService.SmartTimer timer = core.timerService().getTimerById(timerId);
            if (timer != null && timer.active) {
                TimerAlarmScreen timerAlarmScreen = new TimerAlarmScreen(core, timer);
                context.runOnUiThread(() -> {
                    context.setContentViewNoOverride(timerAlarmScreen);
                });
            }
        }

        @Override
        public List<IntentBuilder.IntentFlag> getFlags() {
            return DEFAULT_CREATE_NEW;
        }
    },
    BATTERY_REMINDER_CHECK("battery_reminder_check") {
        @Override
        public void handleIntentInternal(Intent intent, Core core, MainActivity context) {
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

    public final String intentId;
    public static final String PACKAGE_NAME = "de.hype.hypenotify";
    public static final String DYNAMIC_INTENT = PACKAGE_NAME + ".DYNAMIC_ENUM_INTENT";


    DynamicIntents(String intentId) {
        this.intentId = intentId;
    }

    /**
     * @param context    the current context
     * @param connection OPTIONAL: the service connection to use
     */
    public static void startBackgroundService(Context context, @Nullable HypeNotifyServiceConnection connection) {
        Intent serviceIntent = new Intent(context, BackgroundService.class);
        context.startService(serviceIntent);
        if (connection != null) {
            context.bindService(serviceIntent, connection, Context.BIND_AUTO_CREATE);
        }
    }

    @Override
    public abstract void handleIntentInternal(Intent intent, Core core, MainActivity context);

    public static boolean handleIntent(Intent intent, Core core, MainActivity context) {
        Log.i("DynamicIntents", "hypeNotify: Intent specified in onCreate(): %s (%s)".formatted(intent.getAction(), intent.getData()));
        DynamicIntents smartIntent = getIntentByAction(intent.getStringExtra("intentId"));
        if (smartIntent == null) return false;
        smartIntent.handleIntentInternal(intent, core, context);
        return true;
    }

    private static DynamicIntents getIntentByAction(String intentId) {
        if (intentId == null) return null;
        for (DynamicIntents i : DynamicIntents.values()) {
            if (i.intentId.equals(intentId)) {
                return i;
            }
        }
        return null;
    }

    public IntentBuilder getAsIntent(Context context) {
        IntentBuilder builder = new IntentBuilder(context, this);
        builder.setFlags(getFlags());
        return builder;
    }

    public abstract List<IntentBuilder.IntentFlag> getFlags();

    @Override
    public String intentId() {
        return intentId;
    }


}
