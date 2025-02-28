package de.hype.hypenotify.tools.timers;

import com.google.gson.JsonElement;
import de.hype.hypenotify.R;
import de.hype.hypenotify.core.interfaces.MiniCore;

import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * Timer Wrapper - Manages timers without needing direct TimerService calls.
 */
public class TimerWrapper {
    private BaseTimer timer;
    private TimerService timerService;

    public TimerWrapper(BaseTimer timer, TimerService timerService) {
        this.timer = timer;
        this.timerService = timerService;
    }

    public void setTime(Instant newTime) {
        timerService.cancelAndRemoveTimer(this);
        timer.setTime(newTime);
        timerService.addOrReplaceTimer(this);
    }

    public int getSound() {
        return R.raw.alarm;
    }

    public Instant getTime() {
        return timer.getTime();
    }

    public void cancel() {
        timerService.cancelAndRemoveTimer(timer.getClientId());
    }

    /**
     * @param core Core object to run custom checks in {@link #wouldRing(MiniCore)}
     *             return true if the timers condition matches as well as it not being deactivated.
     */
    public final boolean shouldRing(MiniCore core) {
        return timer.shouldRing(core);
    }

    /**
     * @param core Core object to run custom checks.
     *             return true if the timers condition matches.
     *             <p>
     *             You may use blocking code or throw exceptions. if you do not return a false until the timer is supposed to ring it will ring anyway.
     */
    public boolean wouldRing(MiniCore core) {
        return timer.wouldRing(core);
    }

    public UUID getClientId() {
        return timer.getClientId();
    }

    public BaseTimer getBaseTimer() {
        return timer;
    }

    public UUID getServerId() {
        return timer.getServerId();
    }

    public void sleep(int timeAmount, TimeUnit timeUnit) {
        cancel();
        timer.time = Instant.now().plusSeconds(timeUnit.toSeconds(timeAmount));
        timerService.addOrReplaceTimer(new TimerWrapper(timer, timerService));
    }

    public String getMessage() {
        return timer.message;
    }

    public void deactivate() {
        timer.deactivate();
        cancel();
    }

    public void replaceTimer(JsonElement replacementTimer) {
        deactivate();
        timerService.addTimer(replacementTimer);
    }
}
