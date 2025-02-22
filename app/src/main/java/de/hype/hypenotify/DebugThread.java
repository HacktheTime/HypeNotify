package de.hype.hypenotify;

import java.util.ArrayList;
import java.util.List;

public class DebugThread extends Thread{
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
        while (true){
            try {
                Thread.sleep(10_000);
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        }
    }
}
