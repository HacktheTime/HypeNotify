package de.hype.hypenotify.tools.bazaar;

import com.google.gson.Gson;
import de.hype.hypenotify.core.interfaces.MiniCore;
import de.hype.hypenotify.tools.notification.NotificationBuilder;
import de.hype.hypenotify.tools.notification.NotificationChannels;
import de.hype.hypenotify.tools.notification.NotificationVisibility;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;

public class BazaarService {
    private static final Gson gson = new Gson();
    private static final String API_URL = "https://api.hypixel.net/v2/skyblock/bazaar";
    private final MiniCore core;
    private static BazaarResponse lastResponse;
    public List<TrackedBazaarItem> trackedItems = new ArrayList<>();
    private static OrderTrackingService orderTracker;
    public static final int CHECK_INTERVAL = 15;

    public BazaarService(MiniCore core) {
        this.core = core;
        initTrackedItems();
        orderTracker = new OrderTrackingService(core, this);
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
        if (lastResponse == null || lastResponse.isOlderThan(maxAge)) fetchBazaar();
        return lastResponse;
    }

    public BazaarResponse getMaxAgeResponse() throws IOException {
        return getMaxAgeResponse(Duration.ofSeconds(CHECK_INTERVAL - 1));
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
            int timeBetweenChecks = CHECK_INTERVAL;
            nextCheck = core.executionService().schedule(() -> {
                checkPrice();
                checkWifiStateCounter++;
                if (checkWifiStateCounter >= 40) {
                    if (!core.isInHomeNetwork()) {
                        NotificationBuilder notificationBuilder = new NotificationBuilder(core.context(), "Bazaar Price Checker", "You are not in the home network. Tracking stopped.", NotificationChannels.BAZAAR_TRACKER);
                        notificationBuilder.send();
                        return;
                    }
                }
                registerNextCheck();
            }, timeBetweenChecks, java.util.concurrent.TimeUnit.SECONDS);
        }

        private void checkPrice() {
            try {
                BazaarResponse response = bazaarService.getMaxAgeResponse();
                Map<String, BazaarProduct> items = response.getProducts();
                for (TrackedBazaarItem toTrackItem : bazaarService.trackedItems) {
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
