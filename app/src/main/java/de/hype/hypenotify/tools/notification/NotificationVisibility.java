package de.hype.hypenotify.tools.notification;

import androidx.core.app.NotificationCompat;
import android.app.Notification;

public abstract class NotificationVisibility {
        /**
         * Notification visibility: Show this notification in its entirety on all lockscreens.
         *
         * @see android.app.Notification#visibility
         */
        public static final NotificationVisibility SECRET = new NotificationVisibility() {
            @Override
            public void setVisibility(NotificationBuilder builder) {
                builder.builder.setVisibility(NotificationCompat.VISIBILITY_SECRET);
            }
        };
        /**
         * Notification visibility: Only show the basic Information on the lockscreen. To Hide it completely, use {@link #SECRET}.
         */
        public static final NotificationVisibility PRIVATE = new NotificationVisibility() {
            @Override
            public void setVisibility(NotificationBuilder builder) {
                builder.builder.setVisibility(NotificationCompat.VISIBILITY_PRIVATE);
            }
        };
        /**
         * Notification visibility: Show this notification in its entirety on the lockscreen.
         *
         * @see Notification#visibility
         */
        public static final NotificationVisibility PUBLIC = new NotificationVisibility() {
            @Override
            public void setVisibility(NotificationBuilder builder) {
                builder.builder.setVisibility(NotificationCompat.VISIBILITY_PUBLIC);
            }
        };

        public abstract void setVisibility(NotificationBuilder builder);
    }
