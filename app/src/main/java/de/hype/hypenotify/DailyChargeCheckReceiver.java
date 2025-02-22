package de.hype.hypenotify;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.BatteryManager;
import de.hype.hypenotify.tools.notification.NotificationChannels;
import de.hype.hypenotify.tools.notification.NotificationImportance;

import java.util.concurrent.ExecutionException;

import static android.content.Context.BATTERY_SERVICE;

public class DailyChargeCheckReceiver extends BroadcastReceiver {
    Core core;
    @Override
    public void onReceive(Context context, Intent intent) {
        try {
            core = new Core(context);
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
        if (core.isInHomeNetwork() && isBatteryLow(context)) {
            playPingSound(context);
        }
    }

    private boolean isBatteryLow(Context context) {
        BatteryManager bm = (BatteryManager) context.getSystemService(BATTERY_SERVICE);
        if (bm.isCharging()) return false;
        return bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY) < 50;
    }

    private void playPingSound(Context context) {
        NotificationUtils.createNotification(context,"Charge the Battery","The Mobile Phone is not plugged in. Daily Reminder to charge it.", NotificationChannels.BATTERY_WARNING, NotificationImportance.DEFAULT);
        MediaPlayer mediaPlayer = MediaPlayer.create(context, R.raw.alarm);
        mediaPlayer.setLooping(true);
        mediaPlayer.start();

        // Stop the sound after 5 minutes or if acknowledged
        new android.os.Handler().postDelayed(() -> {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.stop();
                mediaPlayer.release();
            }
        }, 5 * 60 * 1000);
    }
}