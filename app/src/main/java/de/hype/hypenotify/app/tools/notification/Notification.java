package de.hype.hypenotify.app.tools.notification;

import android.app.NotificationManager;
import android.content.Context;

public class Notification {
    public final Context context;
    private NotificationBuilder builder;
    private static int notificationId = 20;
    private int id = notificationId++;

    public Notification(Context context, NotificationBuilder builder) {
        this.context = context;
        this.builder = builder;
    }

    public static int generateId() {
        return notificationId++;
    }

    public void send() {
        context.getSystemService(NotificationManager.class).notify(id, builder.builder.build());
    }

    public void update(NotificationBuilder builder) {
        this.builder = builder;
        context.getSystemService(NotificationManager.class).notify(id, builder.builder.build());
    }

    /**
     * Get the builder of this notification. (to possibly modify it.)
     */
    public NotificationBuilder getBuilder() {
        return builder;
    }

    public android.app.Notification get() {
        return builder.builder.build();
    }

    public int getID() {
        return id;
    }
}
