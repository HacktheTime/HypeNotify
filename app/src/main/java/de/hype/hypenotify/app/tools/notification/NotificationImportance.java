package de.hype.hypenotify.app.tools.notification;

import android.app.NotificationManager;

public enum NotificationImportance {
    MIN(NotificationManager.IMPORTANCE_MIN),
    LOW(NotificationManager.IMPORTANCE_LOW),
    DEFAULT(NotificationManager.IMPORTANCE_DEFAULT),
    HIGH(NotificationManager.IMPORTANCE_HIGH),
    MAX(NotificationManager.IMPORTANCE_MAX);

    private final int importance;

    NotificationImportance(int importance) {
        this.importance = importance;
    }

    public int getInt() {
        return importance;
    }
}

