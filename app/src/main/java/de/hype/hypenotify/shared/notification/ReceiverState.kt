package de.hype.hypenotify.shared.notification

open class ReceiverState(
    open val receiverId: String,
    val requiredState: OnlineState,
)
