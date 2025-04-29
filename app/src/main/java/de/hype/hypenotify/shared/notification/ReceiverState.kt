package de.hype.hypenotify.shared.notification

import com.google.android.datatransport.Priority

open class ReceiverState(
    open val receiverId: String,
    val requiredState: OnlineState,
)
