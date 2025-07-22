package de.hype.hypenotify.app.core;

import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import de.hype.hypenotify.app.MainActivity;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class IntentBuilder {
    public final Context context;
    public static List<IntentBuilder.IntentFlag> DEFAULT_FRONT_OR_CREATE = List.of(IntentFlag.FLAG_ACTIVITY_NEW_TASK, IntentFlag.FLAG_ACTIVITY_CLEAR_TOP);
    public static List<IntentBuilder.IntentFlag> DEFAULT_CREATE_NEW = List.of(IntentFlag.FLAG_ACTIVITY_NEW_TASK, IntentFlag.FLAG_ACTIVITY_MULTIPLE_TASK);
    private final Intent intent;
    private final Set<ToDefineInBuilder> whatNeedsToBeSet = new HashSet<>(Set.of(ToDefineInBuilder.values()));
    private static int intentIdCounter = 1;
    private int intentId = intentIdCounter++;
    @NotNull
    public PendingType pendingType;

    public IntentBuilder(Context context, String action, @NotNull PendingType pendingType) {
        this.context = context;
        this.pendingType = pendingType;
        intent = new Intent(action);
    }

    public IntentBuilder(Context context, de.hype.hypenotify.app.core.Intent intent) {
        this.context = context;
        if (intent instanceof StaticIntents) {
            this.pendingType = PendingType.BROADCAST;
            this.intent = new Intent(context, BroadcastIntentWatcher.class);
            this.intent.setAction(StaticIntents.BASE_INTENT_NAME);
        } else {
            this.pendingType = PendingType.ACTIVITY;
            this.intent = new Intent(context, MainActivity.class);
            this.intent.setAction(DynamicIntents.DYNAMIC_INTENT);
        }

        this.intent.putExtra("intentId", intent.intentId());
    }

    public static int generateId() {
        return intentIdCounter++;
    }

    public PendingIntent getAsPending(boolean mutable) {
        return getAsPending(mutable, pendingType);
    }

    public PendingIntent getAsPending(boolean mutable, PendingType pendingType) {
        Intent intent = getAsIntent();
        return switch (pendingType) {
            case SERVICE ->
                    PendingIntent.getService(context, intentId, intent, mutable ? PendingIntent.FLAG_MUTABLE : PendingIntent.FLAG_IMMUTABLE);
            case FOREGROUND_SERVICE ->
                    PendingIntent.getForegroundService(context, intentId, intent, mutable ? PendingIntent.FLAG_MUTABLE : PendingIntent.FLAG_IMMUTABLE);
            case BROADCAST ->
                    PendingIntent.getBroadcast(context, intentId, intent, mutable ? PendingIntent.FLAG_MUTABLE : PendingIntent.FLAG_IMMUTABLE);
            case ACTIVITY ->
                    PendingIntent.getActivity(context, intentId, intent, mutable ? PendingIntent.FLAG_MUTABLE : PendingIntent.FLAG_IMMUTABLE);
            case ACTIVITIES ->
                    PendingIntent.getActivities(context, intentId, new Intent[]{intent}, mutable ? PendingIntent.FLAG_MUTABLE : PendingIntent.FLAG_IMMUTABLE);
        };
    }

    public void setComponent(String packageName, String className) {
        intent.setComponent(new ComponentName(packageName, className));
    }

    public enum PendingType {
        ACTIVITY,
        BROADCAST,
        SERVICE,
        FOREGROUND_SERVICE,
        ACTIVITIES
    }

    public PendingIntent getAsPending() {
        return getAsPending(false);
    }


    public IntentBuilder setFlags(IntentFlag... flags) {
        return setFlags(Set.of(flags));
    }

    public IntentBuilder setFlags(Collection<IntentFlag> flags) {
        intent.setFlags(0);
        for (IntentFlag flag : flags) {
            flag.addFlags(intent);
        }
        whatNeedsToBeSet.remove(ToDefineInBuilder.FLAGS);
        return this;
    }

    public Intent getAsIntent() {
        if (!whatNeedsToBeSet.isEmpty()) throw new IllegalStateException("You still need to set: " + whatNeedsToBeSet);
        return intent;
    }

    public void putExtra(String key, String value) {
        intent.putExtra(key, value);
    }

    public void putExtra(String key, Integer value) {
        intent.putExtra(key, value);
    }

    public void setCustomID(int id) {
        this.intentId = id;
    }

    public void setPackage(String id) {
        intent.setPackage(id);
    }

    private enum ToDefineInBuilder {
        FLAGS,
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
