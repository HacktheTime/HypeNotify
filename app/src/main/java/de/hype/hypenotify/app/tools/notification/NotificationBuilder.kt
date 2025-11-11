package de.hype.hypenotify.app.tools.notification

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Context
import android.graphics.drawable.Icon
import androidx.core.app.NotificationCompat
import de.hype.hypenotify.R
import de.hype.hypenotify.app.core.interfaces.Core

class NotificationBuilder @JvmOverloads constructor(
    var context: Context,
    title: String?,
    message: String?,
    channel: NotificationChannels,
    importance: NotificationImportance = channel.importance,
    visibility: NotificationVisibility = NotificationVisibility.Companion.PUBLIC
) {
    var builder: NotificationCompat.Builder = NotificationCompat.Builder(context, channel.channelId)

    init {
        builder.setContentText(message)
        builder.setStyle(NotificationCompat.BigTextStyle().bigText(message))
        builder.setContentTitle(title)
        builder.setPriority(importance.int)
        builder.setSmallIcon(R.mipmap.icon)
        setGroupAlertBehaviour(GroupBehaviour.GROUP_ALERT_ALL)
        builder.setOnlyAlertOnce(false)
        setVisibility(visibility)
    }

    constructor(core: Core, title: String?, message: String?, channel: NotificationChannels) : this(core.context(), title, message, channel)

    constructor(
        core: Core,
        title: String?,
        message: String?,
        channel: NotificationChannels,
        importance: NotificationImportance,
        visibility: NotificationVisibility
    ) : this(core.context(), title, message, channel, importance, visibility)


    /**
     * Set the icon to be displayed in the notification bar. This is the icon that will be displayed in the status bar.
     *
     * @param icon The resource id of the icon to be displayed. [R]
     *
     *
     * Example: `R.drawable.icon`
     */
    fun setSmallIcon(icon: Int): NotificationBuilder {
        builder.setSmallIcon(icon)
        return this
    }

    /**
     * Set the icon to be displayed in the notification bar. This is the icon that will be displayed in the status bar.
     *
     * @param icon The resource id of the icon to be displayed. [R]
     *
     *
     * Example: `R.drawable.icon`
     */
    fun setLargeImage(icon: Int): NotificationBuilder {
        builder.setLargeIcon(Icon.createWithResource(context, icon))
        return this
    }


    fun build(): Notification {
        return Notification(context, this)
    }

    fun setVisibility(visibility: NotificationVisibility): NotificationBuilder {
        visibility.setVisibility(this)
        return this
    }


    fun setGroup(group: Group): NotificationBuilder {
        builder.setGroup(group.key)
        return this
    }

    @SuppressLint("WrongConstant")
    fun setGroupAlertBehaviour(behaviour: GroupBehaviour): NotificationBuilder {
        builder.setGroupAlertBehavior(behaviour.int)
        return this
    }

    fun setOngoing(ongoing: Boolean): NotificationBuilder {
        builder.setOngoing(ongoing)
        return this
    }

    fun setAutoCancel(autoCancel: Boolean): NotificationBuilder {
        builder.setAutoCancel(autoCancel)
        return this
    }

    val hiddenBuilder: NotificationCompat.Builder
        get() = builder

    fun send(): Notification {
        builder.setGroup("google_bug") //TODO remove but its needed rn due to a issue with android 16
        val notification = Notification(context, this)
        notification.send()
        return notification
    }

    fun setCategory(category: NotificationCategory): NotificationBuilder {
        builder.setCategory(category.categoryId)
        return this
    }

    fun setFullScreenIntent(intent: PendingIntent?): NotificationBuilder {
        builder.setFullScreenIntent(intent, true)
        return this
    }

    fun setAlertOnlyOnce(alertOnlyOnce: Boolean): NotificationBuilder {
        builder.setOnlyAlertOnce(alertOnlyOnce)
        return this
    }

    fun setAction(pendingIntent: PendingIntent?) {
        builder.setContentIntent(pendingIntent)
    }

    fun addActionButton(test: String?, pendingIntent: PendingIntent?) {
        builder.addAction(NotificationCompat.Action.Builder(R.mipmap.icon, test, pendingIntent).build())
    }

    fun setPriority(priority: Priority) {
        builder.setPriority(priority.int)
    }

    /**
     * Request that this notification is delivered silently (no sound/vibration).
     * This maps to the compat API where available.
     */
    fun setSilent(silent: Boolean): NotificationBuilder {
        // NotificationCompat.Builder#setSilent(boolean) exists in recent support libs.
        // Fallback: if not available at runtime it's a no-op, but calling it is safe.
        builder.setSilent(silent)
        return this
    }
}
