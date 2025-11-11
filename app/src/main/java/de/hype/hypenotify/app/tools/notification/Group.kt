package de.hype.hypenotify.app.tools.notification

enum class Group {
    GROUP_MESSAGES,
    GROUP_CALLS,
    GROUP_NOTIFICATIONS;

    val key: String
        get() = name
}