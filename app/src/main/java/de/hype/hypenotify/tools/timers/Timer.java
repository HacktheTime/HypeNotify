package de.hype.hypenotify.tools.timers;

import java.time.Instant;

public interface Timer {
    Instant getRingTime();

    boolean shouldRing();

    String getMessage();

    boolean hasSleepButton();

    boolean isEnabled();
}
