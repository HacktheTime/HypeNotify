package de.hype.hypenotify;

import de.hype.hypenotify.core.interfaces.MiniCore;

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
                test();
            } catch (Throwable e) {
                break;
            }
        }
    }

    public void test(){
        try {
            core.timerService().addAlarm(Instant.now().plus(5, ChronoUnit.SECONDS), "test", () -> true);
        }catch (Throwable e){
            e.printStackTrace();
        }
    }
}
