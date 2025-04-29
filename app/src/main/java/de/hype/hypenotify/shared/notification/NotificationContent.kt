package de.hype.hypenotify.shared.notification

import java.time.Instant

class NotificationContent(
    val title: String,
    val content: String,
    val sender: String,
    val receiverLogic: List<ReceiverState>
) {
    val creationTime: Instant = Instant.now()
}