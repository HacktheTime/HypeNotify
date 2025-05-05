package de.hype.hypenotify.server;

import de.hype.hypenotify.shared.data.Client;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

public class HypeNotifyUser {
    @Getter
    private Integer userId;
    private Map<String, Client> devices = new HashMap<>();

    public HypeNotifyUser(Integer userId) {
        this.userId = userId;
    }

    public Map<String, Client> getDevices() {
        return devices;
    }

    public void addDevice(String name, Client client) {
        devices.put(name, client);
    }
}