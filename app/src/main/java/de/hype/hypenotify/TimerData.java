package de.hype.hypenotify;

import java.time.Instant;

public abstract class TimerData {
    public int id;
    public long time;
    public boolean active;

    /**
     * If true this Alarm will ring even if the checks etc could not be run because the device was locked.
     */

    public TimerData(int id, long time, boolean active) {
        this.id = id;
        this.time = time;
        this.active = active;
    }
    public TimerData(int id, Instant time, boolean active) {
        this.id = id;
        this.time = time.getEpochSecond();
        this.active = active;
    }

    public TimerData(TimerData data) {
        this.id = data.id;
        this.time = data.time;
        this.active = data.active;
    }

    public int getSound(){
        return R.raw.alarm;
    }

    public Instant getTime() {
        return Instant.ofEpochSecond(time);
    }

    public String getMessage() {
        return "No message set";
    }
}