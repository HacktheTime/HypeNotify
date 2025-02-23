package de.hype.hypenotify.tools.bazaar;

import com.google.gson.Gson;
import de.hype.hypenotify.core.MiniCore;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.Duration;

public class BazaarService {
    private static final Gson gson = new Gson();
    private static final String API_URL = "https://api.hypixel.net/v2/skyblock/bazaar";
    private final MiniCore core;
    private static BazaarResponse lastResponse;

    public BazaarService(MiniCore core) {
        this.core = core;
    }

    public static BazaarResponse getLastResponse() {
        return lastResponse;
    }

    public void update() throws IOException {
        fetchBazaar();
    }

    /**
     * Returns the last fetched Bazaar response if it is not older than the given maxAge.
     */
    public BazaarResponse getMaxAgeResponse(Duration maxAge) throws IOException {
        if (lastResponse.isOlderThan(maxAge)) fetchBazaar();
        return lastResponse;
    }

    private static void fetchBazaar() throws IOException {
        HttpURLConnection connection = (HttpURLConnection) new URL(API_URL).openConnection();
        connection.setRequestMethod("GET");
        connection.setConnectTimeout(5000);
        connection.setReadTimeout(5000);
        connection.getResponseCode();

        InputStreamReader reader =
                new InputStreamReader(connection.getInputStream());
        BazaarService.lastResponse = gson.fromJson(reader, BazaarResponse.class);
    }
}
