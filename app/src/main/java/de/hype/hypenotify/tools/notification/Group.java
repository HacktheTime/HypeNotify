package de.hype.hypenotify.tools.notification;

public enum Group {
        GROUP_MESSAGES,
        GROUP_CALLS,
        GROUP_NOTIFICATIONS;

        public String getKey() {
            return name();
        }
    }