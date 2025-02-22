package de.hype.hypenotify.layouts;

import android.content.Context;
import android.media.MediaPlayer;
import android.view.LayoutInflater;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import de.hype.hypenotify.Core;
import de.hype.hypenotify.R;
import de.hype.hypenotify.TimerData;

public class TimerAlarmScreen extends LinearLayout {
    private MediaPlayer mediaPlayer;
    private Core core;

    public TimerAlarmScreen(Core core, TimerData timerData) {
        super(core.context);
        this.core = core;
        Context context = core.context;
        LayoutInflater.from(context).inflate(R.layout.alarm_screen, this, true);
        mediaPlayer = MediaPlayer.create(context, timerData.getSound());
        mediaPlayer.setLooping(true);
        mediaPlayer.start();
        Button stopButton = findViewById(R.id.alarm_screen_alarm_stop);
        stopButton.setOnClickListener(view -> {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
            core.scheduleTimer(timerData);
        });
        Button snoozeButton = findViewById(R.id.alarm_screen_alarm_sleep);
        snoozeButton.setOnClickListener(view -> {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        });
        TextView message = findViewById(R.id.alarm_screen_message);
        message.setText(timerData.getMessage());
    }
}
