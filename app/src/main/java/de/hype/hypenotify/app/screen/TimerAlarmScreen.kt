package de.hype.hypenotify.app.screen

import android.content.Context
import android.media.AudioManager
import android.media.MediaPlayer
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import de.hype.hypenotify.R
import de.hype.hypenotify.app.core.interfaces.Core
import de.hype.hypenotify.app.tools.timers.TimerWrapper
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit

class TimerAlarmScreen(core: Core, timer: TimerWrapper) : LinearLayout(core.context()) {
    private val increaseVolumeFuture: ScheduledFuture<*>?
    private val stopAlarmFuture: ScheduledFuture<*>?
    private var mediaPlayer: MediaPlayer?
    private val core: Core
    private val audioManager: AudioManager
    private val previousVolume: Int
    private val parent: View?

    init {
        this.parent = (core.context()).findViewById<View?>(android.R.id.content)
        this.core = core
        val context: Context = core.context()
        LayoutInflater.from(context).inflate(R.layout.alarm_screen, this, true)
        mediaPlayer = MediaPlayer.create(context, timer.sound)
        mediaPlayer!!.setLooping(true)
        mediaPlayer!!.start()

        audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        previousVolume = audioManager.getStreamVolume(AudioManager.STREAM_ALARM)

        this.increaseVolumeFuture =
            core.executionService().scheduleWithFixedDelay(Runnable { this.increaseVolume() }, 15, 15, TimeUnit.SECONDS)

        val sleepButton = findViewById<Button>(R.id.alarm_screen_alarm_sleep)
        sleepButton.setOnClickListener(OnClickListener { view: View? ->
            stopAlarm()
            timer.sleep(5, TimeUnit.MINUTES)
        })
        stopAlarmFuture = core.executionService().schedule(Runnable { this.stopAlarm() }, 5, TimeUnit.MINUTES)
        val stopButton = findViewById<Button>(R.id.alarm_screen_alarm_stop)
        stopButton.setOnClickListener(OnClickListener { view: View? -> stopAlarm() })
        val message = findViewById<TextView>(R.id.alarm_screen_message)
        message.setText(timer.message)
    }

    private fun increaseVolume() {
        val currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_ALARM)
        val maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_ALARM)
        if (currentVolume < maxVolume) {
            audioManager.setStreamVolume(AudioManager.STREAM_ALARM, currentVolume + 1, AudioManager.FLAG_SHOW_UI)
        }
    }

    private fun stopAlarm() {
        if (mediaPlayer != null) {
            mediaPlayer!!.stop()
            mediaPlayer!!.release()
            mediaPlayer = null
        }
        if (increaseVolumeFuture != null) {
            increaseVolumeFuture.cancel(false)
        }
        if (stopAlarmFuture != null) {
            stopAlarmFuture.cancel(false)
        }
        audioManager.setStreamVolume(AudioManager.STREAM_ALARM, previousVolume, AudioManager.FLAG_SHOW_UI)
        core.context().finish()
    }
}