package de.hype.hypenotify;

import java.util.concurrent.ExecutionException;

public class Core extends MiniCore {
    private String TAG = "Core";
    public MainActivity context;
    private DebugThread debugThread = new DebugThread(this);

    public Core(MainActivity context) throws ExecutionException, InterruptedException {
        super(context);
        this.context = context;
        // Load stored values
        debugThread.setDaemon(true);
        debugThread.setName("DebugThread");
        debugThread.start();
    }
}
