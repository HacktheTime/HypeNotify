package de.hype.hypenotify.tools.bazaar;

import android.util.Log;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import de.hype.hypenotify.core.StaticIntents;
import de.hype.hypenotify.core.interfaces.MiniCore;
import de.hype.hypenotify.tools.notification.NotificationBuilder;
import de.hype.hypenotify.tools.notification.NotificationChannels;
import de.hype.hypenotify.tools.notification.NotificationVisibility;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.UnknownHostException;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;

public class BazaarService {
    private static final Gson gson = new GsonBuilder().registerTypeAdapter(Double.class, new PriceDoubleAdapter()).create();
    private static final String API_URL = "https://api.hypixel.net/v2/skyblock/bazaar";
    private final MiniCore core;
    private static BazaarResponse lastResponse;
    public List<TrackedBazaarItem> trackedItems = new ArrayList<>();
    private static OrderTrackingService orderTracker;
    private static Instant lastUpdate;

    public BazaarService(MiniCore core) {
        this.core = core;
        initTrackedItems();
        orderTracker = new OrderTrackingService(core, this);
    }

    public static Instant getLastUpdate() {
        return lastUpdate;
    }

    public Integer getCheckInterval() {
        boolean isInFreeNetwork = core.isInFreeNetwork();
        if (isInFreeNetwork) {
            return 15;
        } else {
            return core.config().bazaarCheckerNoWlanDelaySeconds;
        }
    }

    private void initTrackedItems() {
        List<TrackedBazaarItem> addToTrack = List.of(
                new TrackedBazaarItem("ENCHANTED_REDSTONE_LAMP", BazaarProduct.OfferType.INSTANT_BUY)
        );
        for (TrackedBazaarItem item : addToTrack) {
            trackedItems.add(item);
        }
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
        if (lastResponse == null || lastUpdate.plusSeconds(maxAge.getSeconds()).isBefore(Instant.now())) fetchBazaar();
        return lastResponse;
    }

    public BazaarResponse getMaxAgeResponse() throws IOException {
        Integer delay = getCheckInterval();
        if (delay == null) {
            return null;
        }
        return getMaxAgeResponse(Duration.ofSeconds(delay - 1));
    }

    private static void fetchBazaar() {
        try {
            HttpURLConnection connection = (HttpURLConnection) new URL(API_URL).openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);
            connection.getResponseCode();

            InputStreamReader reader =
                    new InputStreamReader(connection.getInputStream());
            BazaarService.lastResponse = gson.fromJson(reader, BazaarResponse.class);
            lastUpdate = Instant.now();
        } catch (SocketTimeoutException e) {
            Log.i("BazaarService", "Hypixel BZ Connection Timeout");
        } catch (UnknownHostException e) {
            Log.i("BazaarService", "Hypixel BZ Connection Unknown Host");
        } catch (EOFException e) {
            Log.i("BazaarService", "Hypixel BZ Connection EOF");
        } catch (IOException e) {
            Log.i("BazaarService", "Hypixel BZ Connection Error: " + e.getMessage());
        }
    }


    public static class OrderTrackingService {
        private final MiniCore core;
        private Integer checkWifiStateCounter = 0;
        private BazaarService bazaarService;
        private ScheduledFuture<?> nextCheck;

        public OrderTrackingService(MiniCore core, BazaarService bazaarService) {
            this.core = core;
            this.bazaarService = bazaarService;         // Bazaar Service is needed since this is called from within ints constructor meaning that it is still null in core
            start();
        }

        public void stop() {
            nextCheck.cancel(false);
        }

        public void start() {
            core.executionService().execute(() -> {
                checkPrice();
                registerNextCheck();
            });
        }

        private void registerNextCheck() {
            Integer timeBetweenChecks = bazaarService.getCheckInterval();
            if (timeBetweenChecks == null) {
                checkWifiStateCounter++;
                if (checkWifiStateCounter >= 20) {
                    if (!core.isInFreeNetwork()) {
                        NotificationBuilder notificationBuilder = new NotificationBuilder(core.context(), "Bazaar Price Checker", "You are no longer in a Wifi. Tracking stopped!", NotificationChannels.BAZAAR_TRACKER);
                        notificationBuilder.send();
                        return;
                    }
                }
                return;
            }
            checkWifiStateCounter = 0;
            nextCheck = core.executionService().schedule(() -> {
                checkPrice();
                registerNextCheck();
            }, timeBetweenChecks, java.util.concurrent.TimeUnit.SECONDS);
        }

        private void checkPrice() {
            try {
                BazaarResponse response = bazaarService.getMaxAgeResponse();
                if (response == null) return;
                Map<String, BazaarProduct> items = response.getProducts();
                for (TrackedBazaarItem toTrackItem : bazaarService.trackedItems) {
                    if (!toTrackItem.trackPriceChanges()) continue;
                    BazaarProduct product = items.get(toTrackItem.itemId);
                    if (product == null) {
                        continue;
                    }
                    TrackedBazaarItem.TrackChanges wrappedChanges = toTrackItem.checkForChanges(product);
                    if (wrappedChanges != null) {
                        String notificationText = wrappedChanges.getNotificationText();
                        if (notificationText != null) {
                            NotificationBuilder notificationBuilder = new NotificationBuilder(core.context(), "Bazaar Price Checker", notificationText, NotificationChannels.BAZAAR_TRACKER);
                            notificationBuilder.setVisibility(NotificationVisibility.PUBLIC);
                            notificationBuilder.setAction(StaticIntents.LAUNCH_BAZAAR.getAsIntent(core.context()).getAsPending());
                            notificationBuilder.send();
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
