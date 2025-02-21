package de.hype.hypenotify;

public class DebugThread extends Thread{
    private Core core;
    public DebugThread(Core core) {
        super("Debug Thread");
        this.core = core;
        start();
    }
    @Override
    public void run() {
        while (true){
            try {
                Thread.sleep(10_000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
