package de.hype.hypenotify.app.tools.notification;

import androidx.core.app.NotificationCompat;

public enum GroupBehaviour {
        /**
         * @see NotificationCompat#GROUP_ALERT_ALL
         */
        GROUP_ALERT_ALL(NotificationCompat.GROUP_ALERT_ALL),
        /**
         * @see NotificationCompat#GROUP_ALERT_CHILDREN
         */
        GROUP_ALERT_CHILDREN(NotificationCompat.GROUP_ALERT_CHILDREN),
        /**
         * @see NotificationCompat#GROUP_ALERT_SUMMARY
         */
        GROUP_ALERT_SUMMARY(NotificationCompat.GROUP_ALERT_SUMMARY)
        ;
        private final int intKey;

        GroupBehaviour(int intKey) {
            this.intKey = intKey;
        }

        public int getInt() {
            return intKey;
        }
    }

