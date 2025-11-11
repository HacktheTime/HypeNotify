package de.hype.hypenotify.app.tools.notification;

import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.content.Context;
import android.graphics.drawable.Icon;
import androidx.core.app.NotificationCompat;
import de.hype.hypenotify.R;
import de.hype.hypenotify.app.core.interfaces.Core;

public class NotificationBuilder {
    NotificationCompat.Builder builder;
    Context context;

    public NotificationBuilder(Context context, String title, String message, NotificationChannels channel, NotificationImportance importance, NotificationVisibility visibility) {
        this.context = context;
        builder = new NotificationCompat.Builder(context, channel.channelId);
        builder.setContentText(message);
        builder.setStyle(new NotificationCompat.BigTextStyle().bigText(message));
        builder.setContentTitle(title);
        builder.setPriority(importance.getInt());
        builder.setSmallIcon(R.mipmap.icon);
        setGroupAlertBehaviour(GroupBehaviour.GROUP_ALERT_ALL);
        builder.setOnlyAlertOnce(false);
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

    public NotificationBuilder setOngoing(boolean ongoing) {
        builder.setOngoing(ongoing);
        return this;
    }

    public NotificationBuilder setAutoCancel(boolean autoCancel) {
        builder.setAutoCancel(autoCancel);
        return this;
    }

    public NotificationCompat.Builder getHiddenBuilder() {
        return builder;
    }

    public de.hype.hypenotify.app.tools.notification.Notification send() {
        builder.setGroup("google_bug"); //TODO remove but its needed rn due to a issue with android 16
        de.hype.hypenotify.app.tools.notification.Notification notification = new de.hype.hypenotify.app.tools.notification.Notification(context, this);
        notification.send();
        return notification;
    }

    public NotificationBuilder setCategory(NotificationCategory category) {
        builder.setCategory(category.categoryId);
        return this;
    }

    public NotificationBuilder setFullScreenIntent(PendingIntent intent) {
        builder.setFullScreenIntent(intent, true);
        return this;
    }

    public NotificationBuilder setAlertOnlyOnce(boolean alertOnlyOnce) {
        builder.setOnlyAlertOnce(alertOnlyOnce);
        return this;
    }

    public void setAction(PendingIntent pendingIntent) {
        builder.setContentIntent(pendingIntent);
    }

    public void addActionButton(String test, PendingIntent pendingIntent) {
        builder.addAction(new NotificationCompat.Action.Builder(R.mipmap.icon, test, pendingIntent).build());
    }

    public void setPriority(Priority priority) {
        builder.setPriority(priority.getInt());
    }

    /**
     * Request that this notification is delivered silently (no sound/vibration).
     * This maps to the compat API where available.
     */
    public NotificationBuilder setSilent(boolean silent) {
        // NotificationCompat.Builder#setSilent(boolean) exists in recent support libs.
        // Fallback: if not available at runtime it's a no-op, but calling it is safe.
        builder.setSilent(silent);
        return this;
    }
}
