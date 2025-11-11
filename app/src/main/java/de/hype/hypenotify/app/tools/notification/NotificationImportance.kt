package de.hype.hypenotify.app.tools.notification

import android.app.NotificationManager

enum class NotificationImportance(importance: Int) {
    MIN(NotificationManager.IMPORTANCE_MIN),
    LOW(NotificationManager.IMPORTANCE_LOW),
    DEFAULT(NotificationManager.IMPORTANCE_DEFAULT),
    HIGH(NotificationManager.IMPORTANCE_HIGH),
    MAX(NotificationManager.IMPORTANCE_MAX);

    val int: Int

    init {
        this.int = importance
    }
}

