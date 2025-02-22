package de.hype.hypenotify;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.widget.NestedScrollView;
import de.hype.hypenotify.layouts.EnterDetailsLayout;
import de.hype.hypenotify.layouts.autodetection.Sidebar;
import de.hype.hypenotify.services.TimerService;

public class TimerActivity extends AppCompatActivity {
    private static final String TAG = "TimerActivity";
    private Core core;
    private ServiceConnection connection;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            core = new Core(this);
        } catch (Exception e) {
            Log.e(TAG, "Error: ", e);
            finish();
            throw new RuntimeException(e);
        }
        this.connection = core.getServiceConnection();
        super.setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setContentView(R.layout.activity_main);
        Thread newMainThread = new Thread(() -> {
            try {
                Intent intent = getIntent();
                if (intent != null) {
                    Log.i(TAG, "hypeNotify: Intent specified in onCreate(): %s (%s)".formatted(intent, intent.getAction()));
                    if (intent.getAction().equals("TIMER_ALARM_ACTION")) {
                        alarmAction(this, intent);
                        return;
                    }
                }
                Intent serviceIntent = new Intent(this, TimerService.class);
                startForegroundService(serviceIntent);
                PermissionUtils.requestPermissionsBlocking(this);
                core.fullInit();
                if (!core.areKeysSet()) {
                    runOnUiThread(() -> {
                        EnterDetailsLayout enterDetailsLayout = new EnterDetailsLayout(core);
                        setContentView(enterDetailsLayout);
                        try {
                            enterDetailsLayout.awaitDone();
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                    });
                }
                core.loadTimers();
                Sidebar sidebar = new Sidebar(core);
                runOnUiThread(() -> {
                    setContentView(sidebar);
                });
                registerReceiver(new BootReceiver(core), new IntentFilter(Intent.ACTION_BOOT_COMPLETED));
            } catch (Exception e) {
                Log.e(TAG, "Error: ", e);
                finish();
                throw new RuntimeException(e);
            }
        });
        newMainThread.setName("New Main Thread");
        newMainThread.start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Release WakeLock
        core.wakeLock.onDestroy();
    }

    private void alarmAction(TimerActivity timerActivity, Intent intent) {
        int timerId = intent.getIntExtra("timerId", -1);
        TimerData timer = core.timers.get(timerId);
        if (timer != null && timer.active) {
            MediaPlayer mediaPlayer = MediaPlayer.create(this, R.raw.alarm);
            mediaPlayer.setLooping(true);
            mediaPlayer.start();
        } else {
            // Stop the alarm
            finish();
        }
        finish();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        PermissionUtils.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        Intent intent = new Intent(this, TimerService.class);
        if (connection != null) bindService(intent, connection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (connection != null) {
            unbindService(connection);
        }
    }

    @Override
    public void setContentView(int layoutResID) {
        NestedScrollView scrollView = findViewById(R.id.scroll_view);
        if (scrollView != null) {
            scrollView.removeAllViews();
            getLayoutInflater().inflate(layoutResID, scrollView);
        } else {
            super.setContentView(layoutResID);
        }
    }

    @Override
    public void setContentView(View view) {
        NestedScrollView scrollView = findViewById(R.id.scroll_view);
        if (scrollView != null) {
            scrollView.removeAllViews();
            scrollView.addView(view);
        } else {
            super.setContentView(view);
        }
    }
}