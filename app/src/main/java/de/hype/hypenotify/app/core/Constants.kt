package de.hype.hypenotify.app.core

object Constants {
    const val PREFS_NAME: String = "DevicePrefs"
    const val KEY_API: String = "apiKey"
    const val KEY_DEVICE: String = "deviceName"
    const val KEY_USER_ID: String = "userId"

    // When true, the service has been stopped due to low battery and should not be auto-restarted.
    const val KEY_LOW_BATTERY_STOP: String = "lowBatteryStop"
}
