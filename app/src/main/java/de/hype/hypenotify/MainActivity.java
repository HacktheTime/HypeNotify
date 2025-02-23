package de.hype.hypenotify;

import android.app.ComponentCaller;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.widget.NestedScrollView;
import de.hype.hypenotify.core.BackgroundService;
import de.hype.hypenotify.core.Intents;
import de.hype.hypenotify.core.interfaces.Core;
import de.hype.hypenotify.layouts.EnterDetailsLayout;
import de.hype.hypenotify.layouts.autodetection.Sidebar;
import de.hype.hypenotify.services.HypeNotifyService;
import de.hype.hypenotify.services.HypeNotifyServiceConnection;
import de.hype.hypenotify.services.TimerService;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private Core core;
    private HypeNotifyServiceConnection serviceConnection;
    private EnumIntentReceiver enumIntentReceiver;
    private BackgroundService backgroundService;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.serviceConnection = getServiceConnection();
        Intents.startBackgroundService(this, serviceConnection);
        Thread newMainThread = new Thread(() -> {
            try {
                backgroundService = this.serviceConnection.getService(BackgroundService.class).get();
                core = backgroundService.getCore(this);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            Toolbar toolbar = findViewById(R.id.toolbar);
            runOnUiThread(() -> {
                super.setContentView(R.layout.activity_main);
                setSupportActionBar(toolbar);
                setContentView(R.layout.activity_main);
            });
            try {
                Intent intent = getIntent();
                if (Intents.handleIntent(intent, core, this)) {
                    return;
                }
                PermissionUtils.requestPermissionsBlocking(this);
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
                Sidebar sidebar = new Sidebar(core);
                runOnUiThread(() -> {
                    setContentView(sidebar);
                });
            } catch (Exception e) {
                Log.e(TAG, "Error: ", e);
                throw new RuntimeException(e);
            }
        });
        newMainThread.setName("New Main Thread");
        newMainThread.start();
        enumIntentReceiver = new EnumIntentReceiver(core);
    }

    private HypeNotifyServiceConnection getServiceConnection() {
        return new HypeNotifyServiceConnection() {
            @Override
            public void serviceChange(String serviceName, HypeNotifyService<?> service) {
                System.out.println("Service changed: " + serviceName);
            }
        };
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Release WakeLock
        core.onDestroy();
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
        if (serviceConnection != null) bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);

        IntentFilter filter = new IntentFilter("de.hype.hypenotify.ENUM_INTENT");
        registerReceiver(enumIntentReceiver, filter, Context.RECEIVER_NOT_EXPORTED);

    }

    @Override
    protected void onStop() {
        super.onStop();
        if (serviceConnection != null) {
            unbindService(serviceConnection);
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