package de.hype.hypenotify.app.core;

import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.BatteryManager;
import android.util.Log;
import de.hype.hypenotify.MainActivity;
import de.hype.hypenotify.NotificationUtils;
import de.hype.hypenotify.R;
import de.hype.hypenotify.app.core.interfaces.Core;
import de.hype.hypenotify.app.screen.TimerAlarmScreen;
import de.hype.hypenotify.app.tools.notification.NotificationBuilder;
import de.hype.hypenotify.app.tools.notification.NotificationChannels;
import de.hype.hypenotify.app.tools.notification.NotificationImportance;
import de.hype.hypenotify.app.tools.timers.TimerWrapper;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import static android.content.Context.BATTERY_SERVICE;
import static de.hype.hypenotify.app.core.IntentBuilder.DEFAULT_CREATE_NEW;
import static de.hype.hypenotify.app.core.IntentBuilder.DEFAULT_FRONT_OR_CREATE;

public enum DynamicIntents implements de.hype.hypenotify.app.core.Intent {
    TIMER_HIT("timer_hit") {
        @Override
        public void handleIntentInternal(Intent intent, Core core, MainActivity context) {
            context.setShowWhenLocked(true);
            context.setTurnScreenOn(true);
            NotificationBuilder notificationBuilder = new NotificationBuilder(context, "SmartTimer hit", "SmartTimer hit intent received without timerId", NotificationChannels.ERROR);
            String uuidString = intent.getStringExtra("timerId");
            if (uuidString == null) {
                notificationBuilder.send();
                return;
            }
            UUID timerId = UUID.fromString(uuidString);
            TimerWrapper timer = core.timerService().getTimerByClientId(timerId);
            if (timer != null) {
                Duration between = Duration.between(Instant.now(), timer.getTime());
                ScheduledFuture<?> alarm = core.executionService().schedule(() -> {
                    TimerAlarmScreen timerAlarmScreen = new TimerAlarmScreen(core, timer);
                    context.runOnUiThread(() -> {
                        context.setContentViewNoOverride(timerAlarmScreen);
                    });
                }, between.getSeconds(), TimeUnit.SECONDS);
                core.executionService().execute(() -> {
                    boolean shouldRing = timer.shouldRing(core);
                    if (shouldRing) alarm.cancel(false);
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
            if (core.isInFreeNetwork() && isBatteryLow(context)) {
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
     */
    public static void startBackgroundService(Context context) {
        Intent serviceIntent = new Intent(context, BackgroundService.class);
        context.startService(serviceIntent);
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
