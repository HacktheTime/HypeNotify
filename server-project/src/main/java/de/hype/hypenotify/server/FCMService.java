package de.hype.hypenotify.server;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.FirebaseMessagingException;
import de.hype.hypenotify.CustomMessageBuilder;

import java.io.FileInputStream;

public class FCMService {
    static {
        try {
            FileInputStream serviceAccount = new FileInputStream("HypeNotfiyFirebaseAdminKey.json");
            FirebaseOptions options = new FirebaseOptions.Builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount)).build();
            FirebaseApp.initializeApp(options);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws FirebaseMessagingException {
        CustomMessageBuilder messageBuilder = new CustomMessageBuilder();
        messageBuilder.setToken("token")
                .setNotification("title", "body", "imageUrl")
                .putData("key", "value")
                .setTopic("topic");
        messageBuilder.send();
    }
}