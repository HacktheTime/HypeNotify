package de.hype.hypenotify.layouts;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.widget.*;
import com.google.gson.JsonObject;
import de.hype.hypenotify.Core;
import de.hype.hypenotify.R;
import de.hype.hypenotify.layouts.autodetection.Layout;
import de.hype.hypenotify.tools.bazaar.BazaarProduct;
import de.hype.hypenotify.tools.bazaar.BazaarResponse;
import de.hype.hypenotify.tools.bazaar.BazaarService;
import de.hype.hypenotify.tools.bazaar.TrackedBazaarItem;
import de.hype.hypenotify.tools.notification.NotificationBuilder;
import de.hype.hypenotify.tools.notification.NotificationChannels;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;

import static de.hype.hypenotify.tools.bazaar.TrackedBazaarItem.amountFormat;

@Layout(name = "Enchanted Redstone Lamps Notifier")

public class SkyblockEnchantedRedstoneLampsNotifier extends LinearLayout {
    private static final String API_URL = "https://api.hypixel.net/v2/skyblock/bazaar";
    private static final String ITEM_NAME = "ENCHANTED_REDSTONE_LAMP";
    private static final String CHANNEL_ID = "price_update_channel";
    private final Core core;
    private final Context context;
    private TextView priceLabel;
    private Integer checkWifiStateCounter = 0;
    private Switch toggleTrackingButton;
    private Button checkNowButton;
    private ScheduledFuture<?> nextCheck;
    private ProgressBar progressBar;
    private ObjectAnimator progressBarAnimation;
    private BazaarService bazaarService;
    /**
     * DO NOT MODIFY THIS LIST DIRECTLY. Use the {@link #addTrackedItem(TrackedBazaarItem)} and {@link #removeTrackedItem(TrackedBazaarItem)} methods instead!
     */
    private List<TrackedBazaarItem> toTrackItems = new ArrayList<>();
    private Map<String, TextView> trackedItemLabels = new HashMap<>();
    private Map<String, TableLayout> trackedItemTables = new HashMap<>();
    private LinearLayout trackedItemsLayout;

    public SkyblockEnchantedRedstoneLampsNotifier(Core core) {
        super(core.context);
        this.context = core.context;
        this.core = core;
        bazaarService = core.getBazaarService();
        init();
        initItems();
    }

    /**
     * Define the items you want to track by default here.
     */
    private void initItems() {
        List<TrackedBazaarItem> addToTrack = List.of(
                new TrackedBazaarItem("ENCHANTED_REDSTONE_LAMP", BazaarProduct.OfferType.INSTANT_BUY)
        );
        for (TrackedBazaarItem item : addToTrack) {
            addTrackedItem(item);
        }
    }


    private void init() {
        LayoutInflater.from(context).inflate(R.layout.skyblock_enchanted_redstone_lamps_notifier, this, true);
        toggleTrackingButton = findViewById(R.id.toggle_tracking_button);
        checkNowButton = findViewById(R.id.check_now_button);
        priceLabel = findViewById(R.id.price_label);
        trackedItemsLayout = findViewById(R.id.bazaar_item_layout);
        progressBar = findViewById(R.id.next_bazaar_update);

        toggleTrackingButton.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (!isChecked) {
                nextCheck.cancel(false);
                progressBarAnimation.cancel();
            } else {
                checkPrice();
                registerNextCheck();
            }
        });

        checkNowButton.setOnClickListener(v -> {
            checkNowButton.setText(R.string.checking);
            checkNowButton.requestLayout();
            checkNowButton.invalidate();
            checkPrice();
            checkNowButton.setText(R.string.check_now);
            checkNowButton.requestLayout();
            checkNowButton.invalidate();
        });

        progressBar.setTooltipText("Time until next Refresh");
        core.executionService.execute(() -> {
            checkPrice();
            registerNextCheck();
        });
    }

    public void addTrackedItem(TrackedBazaarItem item) {
        TableLayout tableLayout = new TableLayout(context);
        tableLayout.setStretchAllColumns(true);
        LinearLayout.LayoutParams tableLayoutParams = new LinearLayout.LayoutParams(
                LayoutParams.WRAP_CONTENT, // Change to MATCH_PARENT
                LayoutParams.WRAP_CONTENT
        );
        tableLayoutParams.gravity = Gravity.CENTER; // Ensure gravity is set
        tableLayout.setLayoutParams(tableLayoutParams);
        TextView itemLabel = new TextView(context);
        trackedItemTables.put(item.itemId, tableLayout);
        trackedItemLabels.put(item.itemId, itemLabel);
        trackedItemsLayout.addView(itemLabel);
        trackedItemsLayout.addView(tableLayout);
        toTrackItems.add(item);
    }

    public void removeTrackedItem(TrackedBazaarItem item) {
        TableLayout tableLayout = trackedItemTables.get(item.itemId);
        TextView itemLabel = trackedItemLabels.get(item.itemId);
        trackedItemsLayout.removeView(tableLayout);
        trackedItemsLayout.removeView(itemLabel);
        toTrackItems.remove(item);
    }

    private void registerNextCheck() {
        int timeBetweenChecks = 15;
        startProgressBarCountdown(timeBetweenChecks);
        nextCheck = core.executionService.schedule(() -> {
            checkPrice();
            checkWifiStateCounter++;
            if (checkWifiStateCounter >= 40) {
                if (!core.isInHomeNetwork()) {
                    NotificationBuilder notificationBuilder = new NotificationBuilder(core, "Bazaar Price Checker", "You are not in the home network. Tracking stopped.", NotificationChannels.BAZAAR_TRACKER);
                    notificationBuilder.send();
                    return;
                }
            }
            if (toggleTrackingButton.isChecked()) registerNextCheck();
        }, timeBetweenChecks, java.util.concurrent.TimeUnit.SECONDS);
    }

    private void startProgressBarCountdown(int timeBetweenChecks) {
        post(() -> {
            if (progressBarAnimation != null) {
                progressBarAnimation.cancel();
            }

            long max = ((long) timeBetweenChecks) * 1000;
            int countDownSteps = 500;
            progressBar.setMax((int) (max / countDownSteps));
            progressBar.setProgress(progressBar.getMax());


            progressBarAnimation = ObjectAnimator.ofInt(progressBar, "progress", progressBar.getMax(), 0);
            progressBarAnimation.setDuration(max); // Duration of the progressBarAnimation in milliseconds

            progressBarAnimation.start();
        });
    }

    private void checkPrice() {
        try {
            BazaarResponse response = bazaarService.getMaxAgeResponse(Duration.ofSeconds(15));
            Map<String, BazaarProduct> items = response.getProducts();
            Map<String, List<BazaarProduct.Offer>> displayTables = new HashMap<>();
            for (TrackedBazaarItem toTrackItem : toTrackItems) {
                BazaarProduct product = items.get(toTrackItem.itemId);
                if (product == null) {
                    Toast.makeText(context, "Item not found: %s. Skipping it".formatted(toTrackItem.itemId), Toast.LENGTH_SHORT).show();
                    continue;
                }
                TrackedBazaarItem.TrackChanges wrappedChanges = toTrackItem.checkForChanges(product);
                List<BazaarProduct.Offer> tableOrders = wrappedChanges.getOfferTableValues();
                if (tableOrders != null) displayTables.put(product.getDisplayName(), tableOrders);
                String notificationText = wrappedChanges.getNotificationText();
                if (notificationText != null) {
                    NotificationBuilder notificationBuilder = new NotificationBuilder(core, "Bazaar Price Checker", notificationText, NotificationChannels.BAZAAR_TRACKER);
                    notificationBuilder.send();
                }
            }
            post(() -> {
                for (Map.Entry<String, List<BazaarProduct.Offer>> stringListEntry : displayTables.entrySet()) {
                    String displayName = stringListEntry.getKey();
                    List<BazaarProduct.Offer> toDisplayOrders = stringListEntry.getValue();
                    TextView priceLabel = trackedItemLabels.get(displayName);
                    priceLabel.setText("Item: %s\n".formatted(displayName));
                    if (toDisplayOrders.isEmpty()) {
                        priceLabel.append("No orders found");
                        continue;
                    }
                    TableLayout orderTable = trackedItemTables.get(displayName);
                    orderTable.removeAllViews();

                    TableRow headerRow = new TableRow(context);
                    TextView headerAmount = new TextView(context);
                    headerAmount.setText(R.string.amount);
                    headerAmount.setGravity(Gravity.CENTER); // Center the text
                    TextView headerCoins = new TextView(context);
                    headerCoins.setText(R.string.coins);
                    headerCoins.setGravity(Gravity.CENTER); // Center the text
                    headerRow.addView(headerAmount);
                    headerRow.addView(headerCoins);
                    orderTable.addView(headerRow);

                    for (BazaarProduct.Offer order : toDisplayOrders) {
                        TableRow dataRow = new TableRow(context);
                        TextView amountCell = new TextView(context);
                        amountCell.setText(amountFormat.format(order.amount()));
                        amountCell.setGravity(Gravity.CENTER); // Center the text
                        TextView coinsCell = new TextView(context);
                        coinsCell.setText(amountFormat.format(order.pricePerUnit()));
                        coinsCell.setGravity(Gravity.CENTER); // Center the text
                        dataRow.addView(amountCell);
                        dataRow.addView(coinsCell);
                        orderTable.addView(dataRow);
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}