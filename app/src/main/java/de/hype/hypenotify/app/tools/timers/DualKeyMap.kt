package de.hype.hypenotify.app.tools.timers

class DualKeyMap<Primary, Secondary, V> {
    private val map = HashMap<Primary?, V?>()
    private val secondKeyMap = HashMap<Secondary?, Primary?>()

    fun put(primaryKey: Primary?, secondaryKey: Secondary?, value: V?) {
        map.put(primaryKey, value)
        secondKeyMap.put(secondaryKey, primaryKey)
    }

    fun getPrimary(primaryKey: Primary?): V? {
        return map.get(primaryKey)
    }

    fun getSecondary(secondaryKey: Secondary?): V? {
        val primaryKey = secondKeyMap.get(secondaryKey)
        return if (primaryKey != null) map.get(primaryKey) else null
    }

    fun containsPrimaryKey(primaryKey: Primary?): Boolean {
        return map.containsKey(primaryKey)
    }

    fun containsSecondaryKey(secondaryKey: Secondary?): Boolean {
        return secondKeyMap.containsKey(secondaryKey)
    }

    fun removeByPrimary(primaryKey: Primary?): V? {
        val value = map.remove(primaryKey)
        if (value != null) {
            secondKeyMap.values.removeIf { v: Primary? -> v == primaryKey }
        }
        return value
    }

    fun removeBySecondary(secondaryKey: Secondary?) {
        val primaryKey = secondKeyMap.remove(secondaryKey)
        if (primaryKey != null) {
            map.remove(primaryKey)
        }
    }

    fun putAll(loadedTimers: DualKeyMap<Primary?, Secondary?, V?>) {
        map.putAll(loadedTimers.map)
    }
}