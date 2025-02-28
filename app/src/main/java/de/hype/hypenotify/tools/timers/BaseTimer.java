package de.hype.hypenotify.tools.timers;

import de.hype.hypenotify.ServerUtils;
import de.hype.hypenotify.core.interfaces.MiniCore;

import java.time.Instant;
import java.util.UUID;

/**
 * Abstract Base Timer
 */
public abstract class BaseTimer {
    protected UUID clientId;
    protected UUID serverId;
    protected Instant time;
    protected String message;
    //    protected boolean hasSleepButton;
    protected boolean isDeactivated;

    public BaseTimer(UUID clientId, UUID serverId, Instant time, String message, boolean hasSleepButton, boolean isDeactivated) {
        this.clientId = clientId;
        this.time = time;
        this.message = message;
//        this.hasSleepButton = hasSleepButton;
        this.isDeactivated = isDeactivated;
        this.serverId = serverId;
    }

    public BaseTimer(UUID clientId, UUID serverId, Instant time, String message) {
        this(clientId, serverId, time, message, true, false);
    }

    public UUID getClientId() {
        return clientId;
    }

    public Instant getTime() {
        return time;
    }

    public void setTime(Instant newTime) {
        this.time = newTime;
    }

    public boolean isDeactivated() {
        return isDeactivated;
    }

    /**
     * checks whether the timer is disabled and if its condition ({@link #wouldRing(MiniCore)} ]}) is met.
     */
    public final boolean shouldRing(MiniCore core) {
        return !isDeactivated() && wouldRing(core);
    }

    /**
     * whether the timer should ring. you may use blocking code or throw exceptions. if you do not return a cancel within the time limit (usually ~1 Minute) the timer ring anyway showing that problem.
     */
    public abstract boolean wouldRing(MiniCore core);

    public void deactivate() {
        isDeactivated = true;
    }

    /**
     * @param core core object to use code with.
     *             use the {@link #onInitCustom(MiniCore)} for custom code to run. the code in this methods executes it first and then its own code.
     */
    public final void onInit(MiniCore core) {
        onInitCustom(core);
        ServerUtils.uploadTimer(core, this);
    }

    public void onInitCustom(MiniCore core) {

    }

    public UUID getServerId() {
        return serverId;
    }
}
