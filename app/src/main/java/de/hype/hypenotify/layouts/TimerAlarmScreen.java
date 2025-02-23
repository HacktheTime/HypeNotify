package de.hype.hypenotify.layouts;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Handler;
import android.view.LayoutInflater;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import de.hype.hypenotify.core.interfaces.Core;
import de.hype.hypenotify.R;
import de.hype.hypenotify.services.TimerService;

import java.util.concurrent.TimeUnit;

public class TimerAlarmScreen extends LinearLayout {
    private MediaPlayer mediaPlayer;
    private Core core;
    private AudioManager audioManager;
    private Handler handler;
    private Runnable volumeRunnable;
    private int previousVolume;

    public TimerAlarmScreen(Core core, TimerService.SmartTimer smartTimer) {
        super(core.context());
        this.core = core;
        Context context = core.context();
        LayoutInflater.from(context).inflate(R.layout.alarm_screen, this, true);
        mediaPlayer = MediaPlayer.create(context, smartTimer.getSound());
        mediaPlayer.setLooping(true);
        mediaPlayer.start();

        audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        previousVolume = audioManager.getStreamVolume(AudioManager.STREAM_ALARM);
        handler = new Handler();
        volumeRunnable = new Runnable() {
            @Override
            public void run() {
                increaseVolume();
                handler.postDelayed(this, 15_000); // 15 seconds
            }
        };
        handler.post(volumeRunnable);

        Button stopButton = findViewById(R.id.alarm_screen_alarm_stop);
        stopButton.setOnClickListener(view -> {
            stopAlarm();
            smartTimer.sleep(5, TimeUnit.MINUTES);
        });
        Button snoozeButton = findViewById(R.id.alarm_screen_alarm_sleep);
        snoozeButton.setOnClickListener(view -> stopAlarm());
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
        handler.removeCallbacks(volumeRunnable);
        audioManager.setStreamVolume(AudioManager.STREAM_ALARM, previousVolume, AudioManager.FLAG_SHOW_UI);
    }
}