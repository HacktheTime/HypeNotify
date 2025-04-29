package de.hype.hypenotify;

import de.hype.hypenotify.core.interfaces.MiniCore;

public class Config {
    public static Config INSTANCE;
    private transient final MiniCore core;
    @ConfigField(description = "URL to the Hype Notify Server. Defaults to https://hackthetime.de.")
    public String serverURL = "https://hackthetime.de";
    @ConfigField(description = "Whether the Background Service shall run even if the App is not currently started. (after start or on boot event from system boot on)")
    public boolean useBackgroundService = true;
    @ConfigField(description = "Whether you want the Bazaar Notification Processor to run.")
    public boolean useBazaarChecker = true;
    @ConfigField(description = "How often do you want the Bazaar Checker to check while not connected to WLAN. (in seconds). Null for never", allowNull = true)
    public Integer bazaarCheckerNoWlanDelaySeconds = 60;

    public Config(MiniCore core) {
        this.core = core;
        INSTANCE = this;
    }
}
