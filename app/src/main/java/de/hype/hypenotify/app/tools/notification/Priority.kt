package de.hype.hypenotify.app.tools.notification

import androidx.core.app.NotificationCompat

enum class Priority(priority: Int) {
    HIGH(NotificationCompat.PRIORITY_HIGH),
    DEFAULT(NotificationCompat.PRIORITY_DEFAULT),
    LOW(NotificationCompat.PRIORITY_LOW),
    MIN(NotificationCompat.PRIORITY_MIN);

    val int: Int

    init {
        this.int = priority
    }
}
