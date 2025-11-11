package de.hype.hypenotify.app.screen

import android.content.Context
import android.text.InputType
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import com.google.android.gms.tasks.Tasks
import com.google.firebase.messaging.FirebaseMessaging
import de.hype.hypenotify.R
import de.hype.hypenotify.app.ServerUtils
import de.hype.hypenotify.app.core.Constants
import de.hype.hypenotify.app.core.interfaces.Core
import java.util.concurrent.CountDownLatch

class EnterDetailsLayout(core: Core) : LinearLayout(core.context()) {
    private val apiKeyInput: EditText
    private val deviceNameInput: EditText
    private val userIdInput: EditText
    private val core: Core
    private val context: Context
    private val saveDeviceButton: Button
    private val latch = CountDownLatch(1)

    init {
        this.core = core
        this.context = core.context()
        // New fields for entering API key and device name
        apiKeyInput = EditText(context)
        apiKeyInput.setHint("Enter API Key")

        deviceNameInput = EditText(context)
        deviceNameInput.setHint("Enter Device Name")

        userIdInput = EditText(context)
        userIdInput.setHint("Enter User ID")
        userIdInput.setInputType(InputType.TYPE_CLASS_NUMBER)

        saveDeviceButton = Button(context)
        saveDeviceButton.setText(R.string.save_device)

        setOrientation(VERTICAL)

        // Save device on button click
        saveDeviceButton.setOnClickListener(OnClickListener { v: View ->
            val apiKey = apiKeyInput.getText().toString().trim { it <= ' ' }
            val deviceName = deviceNameInput.getText().toString().trim { it <= ' ' }
            if (!apiKey.isEmpty() && !deviceName.isEmpty()) {
                //POST to add device
                Thread(Runnable {
                    try {
                        val tokenTask = FirebaseMessaging.getInstance().getToken()
                        Tasks.await<String?>(tokenTask)
                        val token = tokenTask.getResult()
                        ServerUtils.sendTokenToServer(
                            apiKey, deviceName, token, userIdInput.getText().toString().toInt(), context.getSharedPreferences(
                                Constants.PREFS_NAME, Context.MODE_PRIVATE
                            )
                        )
                        latch.countDown()
                    } catch (e: Exception) {
                        post(Runnable { Toast.makeText(context, "Error: " + e.message, Toast.LENGTH_SHORT).show() })
                        Log.e(TAG, "Error registering device: ", e)
                    }
                }).start()
            }
        })
        addView(apiKeyInput)
        addView(deviceNameInput)
        addView(userIdInput)
        addView(saveDeviceButton)
    }

    @Throws(InterruptedException::class)
    fun awaitDone() {
        latch.await()
        core.setUserData(userIdInput.getText().toString().toInt(), apiKeyInput.getText().toString(), deviceNameInput.getText().toString())
    }

    companion object {
        private const val TAG = "EnterDetailsLayout"
    }
}
