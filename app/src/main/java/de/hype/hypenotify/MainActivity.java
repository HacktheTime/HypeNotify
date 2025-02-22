package de.hype.hypenotify;

import android.app.ComponentCaller;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.widget.NestedScrollView;
import de.hype.hypenotify.layouts.EnterDetailsLayout;
import de.hype.hypenotify.layouts.autodetection.Sidebar;
import de.hype.hypenotify.services.TimerService;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    Core core;
    private ServiceConnection connection;
    private final EnumIntentReceiver enumIntentReceiver = new EnumIntentReceiver();

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
                if (Intents.handleIntent(intent, core, this)) {
                    finish();
                    return;
                }
                Intent serviceIntent = new Intent(this, TimerService.class);
                startForegroundService(serviceIntent);
                PermissionUtils.requestPermissionsBlocking(this);
                core.fullInit();
                if (!core.areKeysSet()) {
                    EnterDetailsLayout enterDetailsLayout = new EnterDetailsLayout(core);
                    runOnUiThread(() -> {
                        setContentView(enterDetailsLayout);
                    });
                    try {
                        enterDetailsLayout.awaitDone();
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
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

        IntentFilter filter = new IntentFilter("de.hype.hypenotify.ENUM_INTENT");
        registerReceiver(enumIntentReceiver, filter, Context.RECEIVER_NOT_EXPORTED);

    }

    @Override
    protected void onStop() {
        super.onStop();
        if (connection != null) {
            unbindService(connection);
        }
        unregisterReceiver(enumIntentReceiver);
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

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Intents.handleIntent(intent, core, this);
    }

    public void setContentViewNoOverride(LinearLayout screen) {
        super.setContentView(screen);
    }

    @Override
    public void onNewIntent(@NonNull Intent intent, @NonNull ComponentCaller caller) {
        super.onNewIntent(intent, caller);
        Intents.handleIntent(intent, core, this);
    }
}