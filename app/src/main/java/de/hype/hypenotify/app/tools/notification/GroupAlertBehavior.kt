package de.hype.hypenotify.app.tools.notification;

import androidx.core.app.NotificationCompat;

public enum GroupAlertBehavior {
    ALL(NotificationCompat.GROUP_ALERT_ALL),
    CHILDREN(NotificationCompat.GROUP_ALERT_CHILDREN),
    SUMMARY(NotificationCompat.GROUP_ALERT_SUMMARY);

    private final int behavior;

    GroupAlertBehavior(int behavior) {
        this.behavior = behavior;
    }

    public int getInt() {
        return behavior;
    }
}
