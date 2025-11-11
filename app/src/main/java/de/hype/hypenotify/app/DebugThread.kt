package de.hype.hypenotify.app;

import de.hype.hypenotify.app.core.interfaces.MiniCore;
import de.hype.hypenotify.app.tools.timers.AlwaysRingingTimer;
import de.hype.hypenotify.app.tools.timers.TimerService;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

public class DebugThread extends Thread {
    private MiniCore core;
    /**
     * Can be used to temporarily store objects for debugging purposes. such as variables so you dont need to restart the app creating it.
     */
    private List<Object> storage = new ArrayList<>();

    public DebugThread(MiniCore core) {
        super("Debug Thread");
        this.core = core;
    }

    @Override
    public void run() {
        boolean passedOnce = false;
        while (true) {
            try {
                Thread.sleep(3_000);
                passedOnce = true;
            } catch (Throwable e) {
                break;
            }
        }
    }

    public void test() {
        try {
            TimerService service = core.timerService();
            service.createNewTimer((id) -> new AlwaysRingingTimer(id, Instant.now().plus(5, ChronoUnit.SECONDS), "Test"));
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }
}
