package de.hype.hypenotify;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import androidx.core.content.ContextCompat;
import de.hype.hypenotify.screen.RequestPermissionScreen;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

public class PermissionUtils {
    private static final List<String> REQUIRED_PERMISSIONS = List.of(
            Manifest.permission.USE_EXACT_ALARM,
            Manifest.permission.RECEIVE_BOOT_COMPLETED,
            Manifest.permission.WAKE_LOCK,
            Manifest.permission.INTERNET,
            Manifest.permission.ACCESS_NOTIFICATION_POLICY,
            Manifest.permission.USE_FULL_SCREEN_INTENT,
            Manifest.permission.MODIFY_AUDIO_SETTINGS,
            Manifest.permission.POST_NOTIFICATIONS,
            Manifest.permission.FOREGROUND_SERVICE,
            Manifest.permission.ACCESS_NETWORK_STATE,
            Manifest.permission.ACCESS_WIFI_STATE,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_BACKGROUND_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.CHANGE_WIFI_STATE,
            Manifest.permission.CHANGE_NETWORK_STATE,
            Manifest.permission.VIBRATE,
            Manifest.permission.MODIFY_AUDIO_SETTINGS,
            "com.android.alarm.permission.SET_ALARM");

    public static boolean checkPermissions(Context activity) {
        List<String> permissions = new ArrayList<>(REQUIRED_PERMISSIONS);
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(activity, permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    public static void requestPermissionsBlocking(MainActivity activity) throws InterruptedException {
        if (!checkPermissions(activity)) {
            CountDownLatch latch = new CountDownLatch(1);
            requestPermissions(activity, latch);
            latch.await();
        }
    }

    private static void requestPermissions(MainActivity activity, CountDownLatch latch) {
        activity.setContentViewNoOverrideInlined(new RequestPermissionScreen(activity, latch));
    }

    static void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults, MainActivity mainActivity) {
    }
}