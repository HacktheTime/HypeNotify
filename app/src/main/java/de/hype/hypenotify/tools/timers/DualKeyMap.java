package de.hype.hypenotify.tools.timers;

import java.util.HashMap;

public class DualKeyMap<Primary, Secondary, V> {
    private final HashMap<Primary, V> map = new HashMap<>();
    private final HashMap<Secondary, Primary> secondKeyMap = new HashMap<>();

    public void put(Primary primaryKey, Secondary secondaryKey, V value) {
        map.put(primaryKey, value);
        secondKeyMap.put(secondaryKey, primaryKey);
    }

    public V getPrimary(Primary primaryKey) {
        return map.get(primaryKey);
    }

    public V getSecondary(Secondary secondaryKey) {
        Primary primaryKey = secondKeyMap.get(secondaryKey);
        return primaryKey != null ? map.get(primaryKey) : null;
    }

    public boolean containsPrimaryKey(Primary primaryKey) {
        return map.containsKey(primaryKey);
    }

    public boolean containsSecondaryKey(Secondary secondaryKey) {
        return secondKeyMap.containsKey(secondaryKey);
    }

    public V removeByPrimary(Primary primaryKey) {
        V value = map.remove(primaryKey);
        if (value != null) {
            secondKeyMap.values().removeIf(v -> v.equals(primaryKey));
        }
        return value;
    }

    public void removeBySecondary(Secondary secondaryKey) {
        Primary primaryKey = secondKeyMap.remove(secondaryKey);
        if (primaryKey != null) {
            map.remove(primaryKey);
        }
    }

    public void putAll(DualKeyMap<Primary, Secondary, V> loadedTimers) {
        map.putAll(loadedTimers.map);
    }
}