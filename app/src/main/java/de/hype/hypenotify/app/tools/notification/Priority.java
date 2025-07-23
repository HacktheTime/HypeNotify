package de.hype.hypenotify.app.tools.notification;

import androidx.core.app.NotificationCompat;

public enum Priority {
    HIGH(NotificationCompat.PRIORITY_HIGH),
    DEFAULT(NotificationCompat.PRIORITY_DEFAULT),
    LOW(NotificationCompat.PRIORITY_LOW),
    MIN(NotificationCompat.PRIORITY_MIN);

    private final int priority;

    Priority(int priority) {
        this.priority = priority;
    }

    public int getInt() {
        return priority;
    }
}
