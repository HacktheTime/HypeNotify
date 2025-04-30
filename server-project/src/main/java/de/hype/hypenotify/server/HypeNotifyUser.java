package de.hype.hypenotify.server;

import java.util.HashMap;
import java.util.Map;

public class HypeNotifyUser {
    private Integer userId;
    private Map<String, String> devices = new HashMap<>();

    public HypeNotifyUser(Integer userId) {
        this.userId = userId;
    }

    public Integer getUserId() {
        return userId;
    }

    public Map<String, String> getDevices() {
        return devices;
    }

    public void addDevice(String name, String firebaseKey) {
        devices.put(name, firebaseKey);
    }
}