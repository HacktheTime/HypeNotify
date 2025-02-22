package de.hype.hypenotify.tools.bazaar;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;

public class BazaarResponse {
    private boolean success;
    private Long lastUpdated;
    private Map<String, BazaarProduct> products;

    public Map<String, BazaarProduct> getProducts() {
        return products;
    }

    public Instant getLastUpdated() {
        return Instant.ofEpochSecond(lastUpdated);
    }

    public boolean isOlderThan(Duration maxAge){
        return Instant.now().getEpochSecond() - lastUpdated > maxAge.getSeconds();
    }
}