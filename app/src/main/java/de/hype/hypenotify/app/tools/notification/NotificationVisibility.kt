package de.hype.hypenotify.app.tools.notification

import androidx.core.app.NotificationCompat

abstract class NotificationVisibility {
    abstract fun setVisibility(builder: NotificationBuilder?)

    companion object {
        /**
         * Notification visibility: Show this notification in its entirety on all lockscreens.
         *
         * @see android.app.Notification.visibility
         */
        val SECRET: NotificationVisibility = object : NotificationVisibility() {
            override fun setVisibility(builder: NotificationBuilder) {
                builder.builder.setVisibility(NotificationCompat.VISIBILITY_SECRET)
            }
        }

        /**
         * Notification visibility: Only show the basic Information on the lockscreen. To Hide it completely, use [.SECRET].
         */
        val PRIVATE: NotificationVisibility = object : NotificationVisibility() {
            override fun setVisibility(builder: NotificationBuilder) {
                builder.builder.setVisibility(NotificationCompat.VISIBILITY_PRIVATE)
            }
        }

        /**
         * Notification visibility: Show this notification in its entirety on the lockscreen.
         *
         * @see Notification.visibility
         */
        val PUBLIC: NotificationVisibility = object : NotificationVisibility() {
            override fun setVisibility(builder: NotificationBuilder) {
                builder.builder.setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            }
        }
    }
}
