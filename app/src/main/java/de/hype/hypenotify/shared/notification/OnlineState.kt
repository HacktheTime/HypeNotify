package de.hype.hypenotify.shared.notification

enum class OnlineState {
    //Warning the Order Matters! Ordinal is used to compare the states!
    OFFLINE,
    AFK,
    USED,

    ;

    fun isBetterThan(compareTo: OnlineState): Boolean {
        return compareTo.ordinal > this.ordinal
    }
}