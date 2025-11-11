package de.hype.hypenotify.app.screen

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import de.hype.hypenotify.R
import de.hype.hypenotify.app.MainActivity
import java.util.concurrent.CountDownLatch

@SuppressLint("ViewConstructor")
class RequestPermissionScreen(activity: MainActivity, latch: CountDownLatch?) : LinearLayout(activity) {
    init {
        LayoutInflater.from(activity).inflate(R.layout.requestpermissions_screen, this, true)
        val goToSettingsButton = findViewById<Button>(R.id.btn_go_to_settings)
        goToSettingsButton.setOnClickListener(OnClickListener { view: View ->
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:" + activity.getPackageName()))
            activity.startActivity(intent)
            if (latch != null) latch.countDown()
        })
    }
}
