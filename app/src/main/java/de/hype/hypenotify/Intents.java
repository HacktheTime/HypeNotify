package de.hype.hypenotify;

import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.BatteryManager;
import android.util.Log;
import de.hype.hypenotify.layouts.TimerAlarmScreen;
import de.hype.hypenotify.services.TimerService;
import de.hype.hypenotify.tools.notification.NotificationBuilder;
import de.hype.hypenotify.tools.notification.NotificationChannels;
import de.hype.hypenotify.tools.notification.NotificationImportance;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static android.content.Context.BATTERY_SERVICE;

public enum Intents {
    TIMER_HIT("timer_hit") {
        @Override
        public void handleIntentInternal(Intent intent, Core core, MainActivity context) {
            Integer timerId = intent.getIntExtra("timerId", 0);
            if (timerId == 0) {
                NotificationBuilder notificationBuilder = new NotificationBuilder(context, "SmartTimer hit", "SmartTimer hit intent received without timerId", NotificationChannels.ERROR);
                notificationBuilder.send();
                return;
            }
            TimerService.SmartTimer timer = core.timerService.getTimerById(timerId);
            if (timer != null && timer.active) {
                TimerAlarmScreen timerAlarmScreen = new TimerAlarmScreen(core, timer);
                context.runOnUiThread(()->{
                    core.context.setContentViewNoOverride(timerAlarmScreen);
                });
            }
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
    }
    ;

    private final String intentId;
    private static final String PACKAGE_NAME = "de.hype.hypenotify";
    private static final String CLASS_NAME = "MainActivity";

    Intents(String intentId) {
        this.intentId = intentId;
    }

    protected abstract void handleIntentInternal(Intent intent, Core core, MainActivity context);

    public static boolean handleIntent(Intent intent, Core core, MainActivity context) {
        Log.i("Intents", "hypeNotify: Intent specified in onCreate(): %s (%s)".formatted(intent.getAction(), intent.getData()));
        Intents smartIntent = getIntentByAction(intent.getStringExtra("intentId"));
        if (smartIntent == null) return false;
        smartIntent.handleIntentInternal(intent, core, context);
        return true;
    }

    private static Intents getIntentByAction(String intentId) {
        if (intentId == null) return null;
        for (Intents i : Intents.values()) {
            if (i.intentId.equals(intentId)) {
                return i;
            }
        }
        return null;
    }

    public static List<IntentFlag> DEFAULT_FRONT_OR_CREATE = Collections.unmodifiableList(List.of(IntentFlag.FLAG_ACTIVITY_NEW_TASK, IntentFlag.FLAG_ACTIVITY_CLEAR_TOP));
    public static List<IntentFlag> DEFAULT_CREATE_NEW = Collections.unmodifiableList(List.of(IntentFlag.FLAG_ACTIVITY_NEW_TASK, IntentFlag.FLAG_ACTIVITY_MULTIPLE_TASK));

    public Intent getAsIntent(Context context) {
        return getAsIntent(context, DEFAULT_FRONT_OR_CREATE);
    }

    public Intent getAsIntent(Context context, List<IntentFlag> flags) {
        Intent intent = new Intent(context, MainActivity.class);
        intent.setAction("de.hype.hypenotify.ENUM_INTENT");
        intent.putExtra("intentId", intentId);
        for (IntentFlag flag : flags) {
            flag.addFlags(intent);
        }
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        return intent;
    }

    public enum IntentFlag {
        FLAG_GRANT_READ_URI_PERMISSION(Intent.FLAG_GRANT_READ_URI_PERMISSION),
        FLAG_GRANT_WRITE_URI_PERMISSION(Intent.FLAG_GRANT_WRITE_URI_PERMISSION),
        FLAG_FROM_BACKGROUND(Intent.FLAG_FROM_BACKGROUND),
        FLAG_DEBUG_LOG_RESOLUTION(Intent.FLAG_DEBUG_LOG_RESOLUTION),
        FLAG_EXCLUDE_STOPPED_PACKAGES(Intent.FLAG_EXCLUDE_STOPPED_PACKAGES),
        FLAG_INCLUDE_STOPPED_PACKAGES(Intent.FLAG_INCLUDE_STOPPED_PACKAGES),
        FLAG_GRANT_PERSISTABLE_URI_PERMISSION(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION),
        FLAG_GRANT_PREFIX_URI_PERMISSION(Intent.FLAG_GRANT_PREFIX_URI_PERMISSION),
        FLAG_DIRECT_BOOT_AUTO(Intent.FLAG_DIRECT_BOOT_AUTO),
        FLAG_ACTIVITY_NO_HISTORY(Intent.FLAG_ACTIVITY_NO_HISTORY),
        FLAG_ACTIVITY_SINGLE_TOP(Intent.FLAG_ACTIVITY_SINGLE_TOP),
        FLAG_ACTIVITY_NEW_TASK(Intent.FLAG_ACTIVITY_NEW_TASK),
        FLAG_ACTIVITY_MULTIPLE_TASK(Intent.FLAG_ACTIVITY_MULTIPLE_TASK),
        FLAG_ACTIVITY_CLEAR_TOP(Intent.FLAG_ACTIVITY_CLEAR_TOP),
        FLAG_ACTIVITY_FORWARD_RESULT(Intent.FLAG_ACTIVITY_FORWARD_RESULT),
        FLAG_ACTIVITY_PREVIOUS_IS_TOP(Intent.FLAG_ACTIVITY_PREVIOUS_IS_TOP),
        FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS),
        FLAG_ACTIVITY_BROUGHT_TO_FRONT(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT),
        FLAG_ACTIVITY_RESET_TASK_IF_NEEDED(Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED),
        FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY(Intent.FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY),
        FLAG_ACTIVITY_NEW_DOCUMENT(Intent.FLAG_ACTIVITY_NEW_DOCUMENT),
        FLAG_ACTIVITY_NO_USER_ACTION(Intent.FLAG_ACTIVITY_NO_USER_ACTION),
        FLAG_ACTIVITY_REORDER_TO_FRONT(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT),
        FLAG_ACTIVITY_NO_ANIMATION(Intent.FLAG_ACTIVITY_NO_ANIMATION),
        FLAG_ACTIVITY_CLEAR_TASK(Intent.FLAG_ACTIVITY_CLEAR_TASK),
        FLAG_ACTIVITY_TASK_ON_HOME(Intent.FLAG_ACTIVITY_TASK_ON_HOME),
        FLAG_ACTIVITY_RETAIN_IN_RECENTS(Intent.FLAG_ACTIVITY_RETAIN_IN_RECENTS),
        FLAG_ACTIVITY_LAUNCH_ADJACENT(Intent.FLAG_ACTIVITY_LAUNCH_ADJACENT),
        FLAG_ACTIVITY_MATCH_EXTERNAL(Intent.FLAG_ACTIVITY_MATCH_EXTERNAL),
        FLAG_ACTIVITY_REQUIRE_NON_BROWSER(Intent.FLAG_ACTIVITY_REQUIRE_NON_BROWSER),
        FLAG_ACTIVITY_REQUIRE_DEFAULT(Intent.FLAG_ACTIVITY_REQUIRE_DEFAULT);

        private final int value;

        IntentFlag(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }

        public void addFlags(Intent intent) {
            intent.addFlags(value);
        }

        public static List<IntentFlag> getFlags(Intent intent) {
            List<IntentFlag> flags = new ArrayList<>();
            for (IntentFlag flag : IntentFlag.values()) {
                if ((intent.getFlags() & flag.getValue()) != 0) {
                    flags.add(flag);
                }
            }
            return flags;
        }
    }
}
