package de.hype.hypenotify.app

import de.hype.hypenotify.app.core.interfaces.MiniCore

class Config(core: MiniCore?) {
    @Transient
    private val core: MiniCore?

    @ConfigField(description = "URL to the Hype Notify Server. Defaults to https://hackthetime.de.")
    var serverURL: String = "https://hackthetime.de"

    @ConfigField(description = "Whether the Background Service shall run even if the App is not currently started. (after start or on boot event from system boot on)")
    var useBackgroundService: Boolean = true

    @ConfigField(description = "Whether you want the Bazaar Notification Processor to run.")
    var useBazaarChecker: Boolean = true

    @ConfigField(
        description = "How often do you want the Bazaar Checker to check while not connected to WLAN. (in seconds). Null for never",
        allowNull = true
    )
    var bazaarCheckerNoWlanDelaySeconds: Int = 60

    init {
        this.core = core
        INSTANCE = this
    }

    companion object {
        var INSTANCE: Config?
    }
}
