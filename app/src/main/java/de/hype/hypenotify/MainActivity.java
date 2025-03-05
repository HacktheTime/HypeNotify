package de.hype.hypenotify;

import android.app.ComponentCaller;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import de.hype.hypenotify.core.BackgroundService;
import de.hype.hypenotify.core.DynamicIntents;
import de.hype.hypenotify.core.interfaces.Core;
import de.hype.hypenotify.screen.EnterDetailsLayout;
import de.hype.hypenotify.screen.OverviewScreen;
import de.hype.hypenotify.screen.Screen;
import de.hype.hypenotify.services.HypeNotifyService;
import de.hype.hypenotify.services.HypeNotifyServiceConnection;
import de.hype.hypenotify.tools.timers.TimerService;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private Core core;
    private HypeNotifyServiceConnection serviceConnection;
    private BackgroundService backgroundService;
    private OverviewScreen overviewScreen;
    private View currentScreen;
    private View parent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.serviceConnection = getServiceConnection();
        DynamicIntents.startBackgroundService(this);
        Thread newMainThread = new Thread(() -> {
            try {
                backgroundService = BackgroundService.getInstanceBlocking();
                core = backgroundService.getCore(this);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            runOnUiThread(() -> {
                super.setContentView(R.layout.activity_main);
                Toolbar toolbar = findViewById(R.id.toolbar);
                setSupportActionBar(toolbar);
                toolbar.setNavigationOnClickListener(view -> {
                    if (currentScreen != null) {
                        if (currentScreen instanceof Screen screen) screen.close();
                        else setContentView(parent);
                    }
                });
            });
            try {
                Intent intent = getIntent();
                if (DynamicIntents.handleIntent(intent, core, this)) {
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
                overviewScreen = new OverviewScreen(core);
                runOnUiThread(() -> {
                    setContentView(overviewScreen);
                });
            } catch (Exception e) {
                Log.e(TAG, "Error: ", e);
                throw new RuntimeException(e);
            }
        });
        newMainThread.setName("New Main Thread");
        newMainThread.start();
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
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (serviceConnection != null) {
            unbindService(serviceConnection);
        }
    }

    @Override
    public void setContentView(View view) {
        if (view == null) finish();
        parent = currentScreen;
        currentScreen = view;
        LinearLayout scrollView = findViewById(R.id.scroll_view);
        if (scrollView != null) {
            scrollView.removeAllViews();
            if (view.getParent() == null) scrollView.addView(view);
            if (view instanceof Screen screen) screen.updateScreen();
        } else {
            super.setContentView(view);
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        DynamicIntents.handleIntent(intent, core, this);
    }

    public void setContentViewNoOverride(View screen) {
        super.setContentView(screen);
    }

    public void setContentViewNoOverrideInlined(View screen) {
        runOnUiThread(() -> super.setContentView(screen));
    }

    @Override
    public void onNewIntent(@NonNull Intent intent, @NonNull ComponentCaller caller) {
        super.onNewIntent(intent, caller);
        DynamicIntents.handleIntent(intent, core, this);
    }

    public void setOverviewPage() {
        runOnUiThread(() -> {
            super.setContentView(overviewScreen);
        });
    }

    public Core getCore() {
        return core;
    }
}