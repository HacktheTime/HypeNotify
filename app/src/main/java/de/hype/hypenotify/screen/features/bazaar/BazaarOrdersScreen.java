package de.hype.hypenotify.screen.features.bazaar;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.widget.*;
import de.hype.hypenotify.R;
import de.hype.hypenotify.core.interfaces.Core;
import de.hype.hypenotify.layouts.autodetection.Layout;
import de.hype.hypenotify.tools.bazaar.BazaarProduct;
import de.hype.hypenotify.tools.bazaar.BazaarResponse;
import de.hype.hypenotify.tools.bazaar.BazaarService;
import de.hype.hypenotify.tools.bazaar.TrackedBazaarItem;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;

import static de.hype.hypenotify.tools.bazaar.BazaarService.CHECK_INTERVAL;
import static de.hype.hypenotify.tools.bazaar.TrackedBazaarItem.amountFormat;

@Layout(name = "Bazaar Order Tracker")

public class BazaarOrdersScreen extends LinearLayout {
    private final Core core;
    private final Context context;
    private Integer checkWifiStateCounter = 0;
    private Switch toggleTrackingButton;
    private Button checkNowButton;
    private ScheduledFuture<?> nextCheck;
    private ProgressBar progressBar;
    private ObjectAnimator progressBarAnimation;
    private BazaarService bazaarService;
    private Map<TrackedBazaarItem, TextView> trackedItemLabels = new HashMap<>();
    private Map<TrackedBazaarItem, TableLayout> trackedItemTables = new HashMap<>();
    private LinearLayout trackedItemsLayout;

    public BazaarOrdersScreen(Core core) {
        super(core.context());
        this.context = core.context();
        this.core = core;
        bazaarService = core.bazaarService();
        init();
    }


    private void init() {
        removeAllViews();
        LayoutInflater.from(context).inflate(R.layout.bazaar_orders_screen, this, true);
        trackedItemsLayout = findViewById(R.id.bazaar_item_layout);
        TextView loading = new TextView(context);
        loading.setText(R.string.loading);
        loading.setGravity(Gravity.CENTER);
        trackedItemsLayout.addView(loading);
        toggleTrackingButton = findViewById(R.id.toggle_tracking_button);
        checkNowButton = findViewById(R.id.check_now_button);
        progressBar = findViewById(R.id.next_bazaar_update);

        toggleTrackingButton.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (!isChecked) {
                nextCheck.cancel(false);
                progressBarAnimation.pause();
                progressBar.setProgress(0);
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
        core.executionService().execute(() -> {
            checkPrice();
            registerNextCheck();
            post(() -> trackedItemsLayout.removeView(loading));
        });
    }

    private void registerNextCheck() {
        int timeBetweenChecks = CHECK_INTERVAL;
        startProgressBarCountdown(timeBetweenChecks);
        nextCheck = core.executionService().schedule(() -> {
            checkPrice();
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
            BazaarResponse response = bazaarService.getMaxAgeResponse();
            Map<String, BazaarProduct> items = response.getProducts();
            LinkedHashMap<TrackedBazaarItem, List<BazaarProduct.Offer>> displayTables = new LinkedHashMap<>();
            for (TrackedBazaarItem toTrackItem : bazaarService.trackedItems) {
                BazaarProduct product = items.get(toTrackItem.itemId);
                if (product == null) {
                    Toast.makeText(context, "Item not found: %s. Skipping it".formatted(toTrackItem.itemId), Toast.LENGTH_SHORT).show();
                    continue;
                }
                displayTables.put(toTrackItem, product.getOfferType(toTrackItem.trackType));
            }
            post(() -> {
                trackedItemsLayout.removeAllViews();
                for (Map.Entry<TrackedBazaarItem, List<BazaarProduct.Offer>> responseEntry : displayTables.sequencedEntrySet()) {
                    String displayName = responseEntry.getKey().getDisplayName();
                    List<BazaarProduct.Offer> toDisplayOrders = responseEntry.getValue();

                    TextView itemLabel = trackedItemLabels.get(responseEntry.getKey());
                    if (itemLabel == null) {
                        itemLabel = new TextView(context);
                        trackedItemLabels.put(responseEntry.getKey(), itemLabel);
                        itemLabel.setGravity(Gravity.CENTER);
                    }
                    trackedItemsLayout.addView(itemLabel);
                    itemLabel.setText("Item: %s (%s)\n".formatted(displayName, responseEntry.getKey().trackType));
                    if (toDisplayOrders.isEmpty()) {
                        itemLabel.append("No orders found");
                        continue;
                    }
                    TableLayout orderTable = trackedItemTables.get(responseEntry.getKey());
                    if (orderTable == null) {
                        orderTable = new TableLayout(context);
                        orderTable.setStretchAllColumns(true);
                        LayoutParams tableLayoutParams = new LayoutParams(
                                LayoutParams.WRAP_CONTENT, // Change to MATCH_PARENT
                                LayoutParams.WRAP_CONTENT
                        );
                        tableLayoutParams.gravity = Gravity.CENTER; // Ensure gravity is set
                        orderTable.setLayoutParams(tableLayoutParams);
                        trackedItemTables.put(responseEntry.getKey(), orderTable);
                    }
                    trackedItemsLayout.addView(orderTable);
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