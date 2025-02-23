package de.hype.hypenotify.tools.notification;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.drawable.Icon;
import androidx.core.app.NotificationCompat;
import de.hype.hypenotify.R;
import de.hype.hypenotify.core.interfaces.Core;

public class NotificationBuilder {
    NotificationCompat.Builder builder;
    Context context;

    public NotificationBuilder(Context context, String title, String message, NotificationChannels channel, NotificationImportance importance, NotificationVisibility visibility) {
        this.context = context;
        builder = new NotificationCompat.Builder(context, channel.channelId);
        builder.setContentText(message);
        builder.setContentTitle(title);
        builder.setPriority(importance.getInt());
        builder.setSmallIcon(R.mipmap.icon);
        setVisibility(visibility);
    }

    public NotificationBuilder(Context context, String title, String message, NotificationChannels channel) {
        this(context, title, message, channel, channel.importance, NotificationVisibility.PUBLIC);
    }

    public NotificationBuilder(Core core, String title, String message, NotificationChannels channel) {
        this(core.context(), title, message, channel);
    }

    public NotificationBuilder(Core core, String title, String message, NotificationChannels channel, NotificationImportance importance, NotificationVisibility visibility) {
        this(core.context(), title, message, channel, importance, visibility);
    }


    /**
     * Set the icon to be displayed in the notification bar. This is the icon that will be displayed in the status bar.
     *
     * @param icon The resource id of the icon to be displayed. {@link de.hype.hypenotify.R}
     *             <p>
     *             Example: {@code R.drawable.icon}
     */
    public NotificationBuilder setSmallIcon(int icon) {
        builder.setSmallIcon(icon);
        return this;
    }

    /**
     * Set the icon to be displayed in the notification bar. This is the icon that will be displayed in the status bar.
     *
     * @param icon The resource id of the icon to be displayed. {@link de.hype.hypenotify.R}
     *             <p>
     *             Example: {@code R.drawable.icon}
     */
    public NotificationBuilder setLargeImage(int icon) {
        builder.setLargeIcon(Icon.createWithResource(context, icon));
        return this;
    }


    public Notification build() {
        return new Notification(context, this);
    }

    public NotificationBuilder setVisibility(NotificationVisibility visibility) {
        visibility.setVisibility(this);
        return this;
    }


    public NotificationBuilder setGroup(Group group) {
        builder.setGroup(group.getKey());
        return this;
    }

    @SuppressLint("WrongConstant")
    public NotificationBuilder setGroupAlertBehaviour(GroupBehaviour behaviour) {
        builder.setGroupAlertBehavior(behaviour.getInt());
        return this;
    }

    public NotificationCompat.Builder getHiddenBuilder() {
        return builder;
    }

    public de.hype.hypenotify.tools.notification.Notification send() {
        de.hype.hypenotify.tools.notification.Notification notification = new de.hype.hypenotify.tools.notification.Notification(context, this);
        notification.send();
        return notification;
    }
}
