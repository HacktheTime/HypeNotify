package de.hype.hypenotify.app.tools.bazaar

import java.time.Duration
import java.time.Instant

class BazaarResponse {
    private val success = false
    private val lastUpdated: Long? = null
    val products: MutableMap<String, BazaarProduct> = mutableMapOf()

    fun getLastUpdated(): Instant? {
        return if (lastUpdated == null) null else Instant.ofEpochMilli(lastUpdated)
    }

    fun isOlderThan(maxAge: Duration): Boolean {
        if (lastUpdated == null) return true
        return Instant.now().toEpochMilli() - lastUpdated > maxAge.seconds
    }
}