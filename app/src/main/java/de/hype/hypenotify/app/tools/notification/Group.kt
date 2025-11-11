package de.hype.hypenotify.app.tools.notification;

public enum Group {
        GROUP_MESSAGES,
        GROUP_CALLS,
        GROUP_NOTIFICATIONS;

        public String getKey() {
            return name();
        }
    }