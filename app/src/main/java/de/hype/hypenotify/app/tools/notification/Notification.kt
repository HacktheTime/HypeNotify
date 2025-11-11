package de.hype.hypenotify.app.tools.notification

import android.app.Notification
import android.app.NotificationManager
import android.content.Context

class Notification(context: Context, builder: NotificationBuilder) {
    val context: Context

    /**
     * Get the builder of this notification. (to possibly modify it.)
     */
    var builder: NotificationBuilder
        private set
    val iD: Int = notificationId++

    init {
        this.context = context
        this.builder = builder
    }

    fun send() {
        context.getSystemService<NotificationManager?>(NotificationManager::class.java).notify(this.iD, builder.builder.build())
    }

    fun update(builder: NotificationBuilder) {
        this.builder = builder
        context.getSystemService<NotificationManager?>(NotificationManager::class.java).notify(this.iD, builder.builder.build())
    }

    fun get(): Notification {
        return builder.builder.build()
    }

    companion object {
        private var notificationId = 20
        fun generateId(): Int {
            return notificationId++
        }
    }
}
