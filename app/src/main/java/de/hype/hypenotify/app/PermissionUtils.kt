package de.hype.hypenotify.app

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import de.hype.hypenotify.app.screen.RequestPermissionScreen
import java.util.List
import java.util.concurrent.CountDownLatch
import kotlin.collections.ArrayList
import kotlin.collections.MutableList

object PermissionUtils {
    private val REQUIRED_PERMISSIONS: MutableList<String?> = List.of<String?>(
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
        "com.android.alarm.permission.SET_ALARM"
    )

    fun checkPermissions(activity: Context): Boolean {
        val permissions: MutableList<String> = ArrayList<String>(REQUIRED_PERMISSIONS)
        for (permission in permissions) {
            if (ContextCompat.checkSelfPermission(activity, permission) != PackageManager.PERMISSION_GRANTED) {
                return false
            }
        }
        return true
    }

    @Throws(InterruptedException::class)
    fun requestPermissionsBlocking(activity: MainActivity) {
        if (!checkPermissions(activity)) {
            val latch = CountDownLatch(1)
            requestPermissions(activity, latch)
            latch.await()
        }
    }

    private fun requestPermissions(activity: MainActivity, latch: CountDownLatch?) {
        activity.setContentViewNoOverrideInlined(RequestPermissionScreen(activity, latch))
    }

    fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String?>?, grantResults: IntArray?, mainActivity: MainActivity?) {
    }
}