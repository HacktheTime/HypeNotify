package de.hype.hypenotify.server;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;

public class CustomMessageBuilder {
    private final Message.Builder builder;

    public CustomMessageBuilder() {
        this.builder = Message.builder();
    }

    public CustomMessageBuilder setToken(String token) {
        builder.setToken(token);
        return this;
    }

    public CustomMessageBuilder setNotification(String title, String body, String imageUrl) {
        builder.setNotification(Notification.builder().setBody(body).setTitle(title).setImage(imageUrl).build());
        return this;
    }

    public CustomMessageBuilder putData(String key, String value) {
        builder.putData(key, value);
        return this;
    }

    public CustomMessageBuilder setTopic(String topic) {
        builder.setTopic(topic);
        return this;
    }

    public CustomMessageBuilder setCondition(String condition) {
        builder.setCondition(condition);
        return this;
    }

    public Message build() {
        return builder.build();
    }

    public void send() throws FirebaseMessagingException {
        FirebaseMessaging.getInstance().send(this.build());
    }
}