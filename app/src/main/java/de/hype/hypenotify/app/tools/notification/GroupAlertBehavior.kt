package de.hype.hypenotify.app.tools.notification

import androidx.core.app.NotificationCompat

enum class GroupAlertBehavior(behavior: Int) {
    ALL(NotificationCompat.GROUP_ALERT_ALL),
    CHILDREN(NotificationCompat.GROUP_ALERT_CHILDREN),
    SUMMARY(NotificationCompat.GROUP_ALERT_SUMMARY);

    val int: Int

    init {
        this.int = behavior
    }
}
