package de.hype.hypenotify;

import de.hype.hypenotify.tools.notification.NotificationChannels;

public class NotificationService{
    private final Core core;

    public NotificationService(Core core) {
        this.core = core;
        NotificationUtils.synchronizeNotificationChannels(core.context);
    }
    
    public void notifyUser(String title, String content, NotificationChannels channel) {
        NotificationUtils.createNotification(core.context, title, content, channel, channel.importance);
    }
}
