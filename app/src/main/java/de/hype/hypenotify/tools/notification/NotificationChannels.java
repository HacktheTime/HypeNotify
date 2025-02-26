package de.hype.hypenotify.tools.notification;

public enum NotificationChannels {
    BACKGROUND_SERVICE("Background Service", "background_service", "Required for Background Services", NotificationImportance.MIN),
    BAZAAR_TRACKER("Bazaar Tracker", "bazaar_tracker", "Bazaar Tracking Notifications", NotificationImportance.MIN),
    SERVER_NOTIFICATIONS("Server Notifications", "server_notifications", "Notifications from the Server", NotificationImportance.DEFAULT),
    BATTERY_WARNING("Battery Warning", "battery_warning", "Daily 7pm Battery Warning if not plugged in.", NotificationImportance.DEFAULT),
    OTHER("Other", "other", "Other", NotificationImportance.MAX),
    ERROR("Error", "error", "Errors are sent into here", NotificationImportance.MAX),
    PRIORITY_LAUNCH("Priority App Launch", "priority_launch", "Due too this App doing unconventional things which normally would be undesirable behaviour this is needed. While this Channel has Max priority it is recommended to deactivate the sound. It is activated by default so users which systems block the full screen intent at least get notified about it.", NotificationImportance.MAX);

    public final String displayName;
    public final String channelId;
    public final String description;
    public final NotificationImportance importance;

    NotificationChannels(String displayName, String channelId, String description, NotificationImportance importance) {
        this.displayName = displayName;
        this.channelId = channelId;
        this.description = description;
        this.importance = importance;
    }

    public NotificationImportance getImportance() {
        return importance;
    }
}