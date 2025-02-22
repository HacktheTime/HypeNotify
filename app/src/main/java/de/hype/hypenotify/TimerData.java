package de.hype.hypenotify;

import android.media.MediaPlayer;

import java.time.Instant;

public class TimerData {
    int id;
    long time;
    boolean active;

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