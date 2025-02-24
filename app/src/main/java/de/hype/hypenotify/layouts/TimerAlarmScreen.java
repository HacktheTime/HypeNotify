package de.hype.hypenotify.layouts;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import de.hype.hypenotify.R;
import de.hype.hypenotify.core.interfaces.Core;
import de.hype.hypenotify.services.TimerService;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class TimerAlarmScreen extends LinearLayout {
    private final ScheduledFuture<?> increaseVolumeFuture;
    private final ScheduledFuture<?> stopAlarmFuture;
    private MediaPlayer mediaPlayer;
    private Core core;
    private AudioManager audioManager;
    private int previousVolume;
    private View parent;

    public TimerAlarmScreen(Core core, TimerService.SmartTimer smartTimer) {
        super(core.context());
        this.parent = (core.context()).findViewById(android.R.id.content);
        this.core = core;
        Context context = core.context();
        LayoutInflater.from(context).inflate(R.layout.alarm_screen, this, true);
        mediaPlayer = MediaPlayer.create(context, smartTimer.getSound());
        mediaPlayer.setLooping(true);
        mediaPlayer.start();

        audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        previousVolume = audioManager.getStreamVolume(AudioManager.STREAM_ALARM);

        this.increaseVolumeFuture = core.executionService().scheduleWithFixedDelay(this::increaseVolume, 15, 15, TimeUnit.SECONDS);

        Button sleepButton = findViewById(R.id.alarm_screen_alarm_sleep);
        sleepButton.setOnClickListener(view -> {
            stopAlarm();
            smartTimer.sleep(5, TimeUnit.MINUTES);
        });
        stopAlarmFuture = core.executionService().schedule(this::stopAlarm, 5, TimeUnit.MINUTES);
        Button stopButton = findViewById(R.id.alarm_screen_alarm_stop);
        stopButton.setOnClickListener(view -> stopAlarm());
        TextView message = findViewById(R.id.alarm_screen_message);
        message.setText(smartTimer.getMessage());
    }

    private void increaseVolume() {
        int currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_ALARM);
        int maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_ALARM);
        if (currentVolume < maxVolume) {
            audioManager.setStreamVolume(AudioManager.STREAM_ALARM, currentVolume + 1, AudioManager.FLAG_SHOW_UI);
        }
    }

    private void stopAlarm() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }
        if (increaseVolumeFuture != null) {
            increaseVolumeFuture.cancel(false);
        }
        if (stopAlarmFuture != null) {
            stopAlarmFuture.cancel(false);
        }
        audioManager.setStreamVolume(AudioManager.STREAM_ALARM, previousVolume, AudioManager.FLAG_SHOW_UI);
        core.context().finish();
    }
}