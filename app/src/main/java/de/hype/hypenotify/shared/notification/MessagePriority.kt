package de.hype.hypenotify.shared.notification

enum class MessagePriority {
    //Warning the Order Matters! Ordinal is used to compare the states!
    LOW,
    NORMAL,
    HIGH,
    IMMEDIATE,
    ;
}