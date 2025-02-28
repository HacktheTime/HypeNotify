package de.hype.hypenotify.screen;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.widget.Button;
import android.widget.LinearLayout;
import de.hype.hypenotify.MainActivity;
import de.hype.hypenotify.R;

import java.util.concurrent.CountDownLatch;

@SuppressLint("ViewConstructor")
public class RequestPermissionScreen extends LinearLayout {
    public RequestPermissionScreen(MainActivity activity, CountDownLatch latch) {
        super(activity);
        LayoutInflater.from(activity).inflate(R.layout.requestpermissions_screen, this, true);
        Button goToSettingsButton = findViewById(R.id.btn_go_to_settings);
        goToSettingsButton.setOnClickListener(view -> {
            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:" + activity.getPackageName()));
            activity.startActivity(intent);
            if (latch != null) latch.countDown();
        });
    }

}
