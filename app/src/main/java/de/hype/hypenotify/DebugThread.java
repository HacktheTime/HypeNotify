package de.hype.hypenotify;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

public class DebugThread extends Thread {
    private Core core;
    /**
     * Can be used to temporarily store objects for debugging purposes. such as variables so you dont need to restart the app creating it.
     */
    private List<Object> storage = new ArrayList<>();

    public DebugThread(Core core) {
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
                throw new RuntimeException(e);
            }
        }
    }

    public void test(){
        try {
            core.scheduleTimer(new TimerData(1, Instant.now().plus(10, ChronoUnit.SECONDS), true));
        }catch (Throwable e){
            e.printStackTrace();
        }
    }
}
