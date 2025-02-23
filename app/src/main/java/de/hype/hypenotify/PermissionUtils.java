package de.hype.hypenotify;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.concurrent.CountDownLatch;

public class PermissionUtils {
    private static final int PERMISSION_REQUEST_CODE = 1;
    private static final String[] REQUIRED_PERMISSIONS = {
            Manifest.permission.USE_EXACT_ALARM,
            Manifest.permission.RECEIVE_BOOT_COMPLETED,
            Manifest.permission.WAKE_LOCK,
            Manifest.permission.INTERNET,
            Manifest.permission.ACCESS_NOTIFICATION_POLICY,
            Manifest.permission.POST_NOTIFICATIONS,
            Manifest.permission.FOREGROUND_SERVICE,
            Manifest.permission.ACCESS_NETWORK_STATE,
            Manifest.permission.ACCESS_WIFI_STATE,
            Manifest.permission.CHANGE_WIFI_STATE,
            Manifest.permission.CHANGE_NETWORK_STATE,
            Manifest.permission.VIBRATE,
            Manifest.permission.USE_BIOMETRIC,
            "com.android.alarm.permission.SET_ALARM"
    };

    private static CountDownLatch latch;

    public static boolean checkPermissions(Context activity) {
        for (String permission : REQUIRED_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(activity, permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    public static void requestPermissions(Activity activity) {
        ActivityCompat.requestPermissions(activity, REQUIRED_PERMISSIONS, PERMISSION_REQUEST_CODE);
    }

    public static void requestPermissionsBlocking(Activity activity) throws InterruptedException {
        if (!checkPermissions(activity)) {
            latch = new CountDownLatch(1);
            requestPermissions(activity);
            latch.await();
        }
    }

    static void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults, MainActivity mainActivity) {
        if (requestCode == PERMISSION_REQUEST_CODE) {
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    mainActivity.finish();
                    return;
                }
            }
            // All permissions are granted
            if (latch != null) {
                latch.countDown();
            }
        }
    }
}