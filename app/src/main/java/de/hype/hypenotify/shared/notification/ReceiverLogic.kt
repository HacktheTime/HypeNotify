package de.hype.hypenotify.shared.notification

import java.time.Instant

class ReceiverLogic(
    receiverId: Regex,
    status: OnlineState,
    val priority: MessagePriority = MessagePriority.NORMAL,
    val ping : Boolean = true,
    val returnOnAnyReceived: Boolean = true,
    val disregardAfter: Instant? = null
) : ReceiverState(receiverId.pattern, status) {
}