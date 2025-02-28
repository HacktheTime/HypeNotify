package de.hype.hypenotify.tools.timers;

import de.hype.hypenotify.core.interfaces.MiniCore;

import java.time.Instant;
import java.util.UUID;

/**
 * AlwaysRingingTimer - Rings at a set time, no conditions.
 */
public class AlwaysRingingTimer extends BaseTimer {
    public AlwaysRingingTimer(UUID id, Instant time, String message) {
        super(id, null, time, message);
    }

    @Override
    public boolean wouldRing(MiniCore core) {
        return true;
    }
}
