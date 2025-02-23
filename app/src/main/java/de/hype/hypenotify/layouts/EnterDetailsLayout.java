package de.hype.hypenotify.layouts;

import android.content.Context;
import android.text.InputType;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.messaging.FirebaseMessaging;
import de.hype.hypenotify.core.Constants;
import de.hype.hypenotify.core.interfaces.Core;
import de.hype.hypenotify.R;


import java.util.concurrent.CountDownLatch;

import static de.hype.hypenotify.ServerUtils.sendTokenToServer;

public class EnterDetailsLayout extends LinearLayout {
    private static final String TAG = "EnterDetailsLayout";
    private final EditText apiKeyInput, deviceNameInput, userIdInput;
    private final Core core;
    private final Context context;
    private Button saveDeviceButton;
    private CountDownLatch latch = new CountDownLatch(1);

    public EnterDetailsLayout(Core core) {
        super(core.context());
        this.core = core;
        this.context = core.context();
        // New fields for entering API key and device name
        apiKeyInput = new EditText(context);
        apiKeyInput.setHint("Enter API Key");

        deviceNameInput = new EditText(context);
        deviceNameInput.setHint("Enter Device Name");

        userIdInput = new EditText(context);
        userIdInput.setHint("Enter User ID");
        userIdInput.setInputType(InputType.TYPE_CLASS_NUMBER);

        saveDeviceButton = new Button(context);
        saveDeviceButton.setText(R.string.save_device);

        setOrientation(LinearLayout.VERTICAL);

        // Save device on button click
        saveDeviceButton.setOnClickListener(v -> {
            String apiKey = apiKeyInput.getText().toString().trim();
            String deviceName = deviceNameInput.getText().toString().trim();

            if (!apiKey.isEmpty() && !deviceName.isEmpty()) {
                //POST to add device
                new Thread(() -> {
                    try {
                        Task<String> tokenTask = FirebaseMessaging.getInstance().getToken();
                        Tasks.await(tokenTask);
                        String token = tokenTask.getResult();
                        sendTokenToServer(apiKey, deviceName, token, Integer.parseInt(userIdInput.getText().toString()), context.getSharedPreferences(Constants.PREFS_NAME, Context.MODE_PRIVATE));
                        latch.countDown();
                    } catch (Exception e) {
                        post(()->Toast.makeText(context, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                        Log.e(TAG, "Error registering device: ", e);
                    }
                }).start();
            }
        });
        addView(apiKeyInput);
        addView(deviceNameInput);
        addView(userIdInput);
        addView(saveDeviceButton);
    }
    public void awaitDone() throws InterruptedException {
        latch.await();
        core.setUserData(Integer.parseInt(userIdInput.getText().toString()), apiKeyInput.getText().toString(), deviceNameInput.getText().toString());
    }
}
