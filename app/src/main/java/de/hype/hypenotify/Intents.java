package de.hype.hypenotify;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import de.hype.hypenotify.layouts.TimerAlarmScreen;
import de.hype.hypenotify.tools.notification.NotificationBuilder;
import de.hype.hypenotify.tools.notification.NotificationChannels;

public enum Intents {
    TIMER_HIT("timer_hit") {
        @Override
        public void handleIntentInternal(Intent intent, Core core, Context context) {
            Integer timerId = intent.getIntExtra("timerId", 0);
            if (timerId == 0) {
                NotificationBuilder notificationBuilder = new NotificationBuilder(context, "Timer hit", "Timer hit intent received without timerId", NotificationChannels.ERROR);
                notificationBuilder.send();
                return;
            }
            TimerData timer = core.timers.get(timerId);
            if (timer != null && timer.active) {
                TimerAlarmScreen timerAlarmScreen = new TimerAlarmScreen(core, timer);
                core.context.setContentViewNoOverride(timerAlarmScreen);
            }
        }
    },
    ;

    private final String intentId;
    private static final String PACKAGE_NAME = "de.hype.hypenotify";
    private static final String CLASS_NAME = "MainActivity";

    Intents(String intentId) {
        this.intentId = intentId;
    }

    protected abstract void handleIntentInternal(Intent intent, Core core, Context context);

    public static boolean handleIntent(Intent intent, Core core, Context context) {
        Log.i("Intents", "hypeNotify: Intent specified in onCreate(): %s (%s)".formatted(intent, intent.getAction()));
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

    public Intent getAsIntent(Context context) {
        Intent intent = new Intent(context, MainActivity.class);
        intent.setAction("de.hype.hypenotify.ENUM_INTENT");
        intent.putExtra("intentId", intentId);
        return intent;
    }
}
