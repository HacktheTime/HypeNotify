package de.hype.hypenotify;

public class TimerData {
    int id;
    long time;
    boolean active;

    public TimerData(int id, long time, boolean active) {
        this.id = id;
        this.time = time;
        this.active = active;
    }
}