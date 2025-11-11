package de.hype.hypenotify.app.tools.notification

import androidx.core.app.NotificationCompat

enum class GroupBehaviour(intKey: Int) {
    /**
     * @see NotificationCompat.GROUP_ALERT_ALL
     */
    GROUP_ALERT_ALL(NotificationCompat.GROUP_ALERT_ALL),

    /**
     * @see NotificationCompat.GROUP_ALERT_CHILDREN
     */
    GROUP_ALERT_CHILDREN(NotificationCompat.GROUP_ALERT_CHILDREN),

    /**
     * @see NotificationCompat.GROUP_ALERT_SUMMARY
     */
    GROUP_ALERT_SUMMARY(NotificationCompat.GROUP_ALERT_SUMMARY);

    val int: Int

    init {
        this.int = intKey
    }
}

