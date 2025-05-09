package de.hype.hypenotify.app.screen.features.bazaar;

import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.*;
import de.hype.hypenotify.R;
import de.hype.hypenotify.app.core.interfaces.Core;
import de.hype.hypenotify.app.screen.Screen;
import de.hype.hypenotify.app.tools.bazaar.BazaarProduct;
import de.hype.hypenotify.app.tools.bazaar.BazaarResponse;
import de.hype.hypenotify.app.tools.bazaar.BazaarService;
import de.hype.hypenotify.app.tools.bazaar.TrackedBazaarItem;
import de.hype.hypenotify.layouts.autodetection.Layout;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import static de.hype.hypenotify.app.tools.bazaar.TrackedBazaarItem.amountFormat;

@SuppressLint("ViewConstructor")
@Layout(name = "Bazaar Order Tracker")

public class BazaarOrdersScreen extends Screen {
    private Switch toggleTrackingButton;
    private Button checkNowButton;
    private Button editTrackersButton;
    private ScheduledFuture<?> nextCheck;
    private ScheduledFuture<?> updateLastUpdated;
    private ProgressBar progressBar;
    private ObjectAnimator progressBarAnimation;
    private BazaarService bazaarService;
    private Map<TrackedBazaarItem, TextView> trackedItemLabels = new HashMap<>();
    private Map<TrackedBazaarItem, TableLayout> trackedItemTables = new HashMap<>();
    private TextView loading;
    private TextView lastUpdated;

    public BazaarOrdersScreen(Core core, View parent) {
        super(core, parent);
        loading = new TextView(context);
        loading.setText(R.string.loading);
        loading.setGravity(Gravity.CENTER);
        bazaarService = core.bazaarService();
        updateScreen();
        core.context().getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        // Add loading indicator
        registerNextCheck();
    }

    @Override
    protected void inflateLayouts() {
        setOrientation(VERTICAL);
        LayoutInflater.from(context).inflate(R.layout.bazaar_orders_screen, this, true);
        LinearLayout layout = (findViewById(R.id.bazaar_item_layout));
        layout.removeAllViews();
        layout.addView(loading);
    }

    @Override
    protected void updateScreen(LinearLayout dynamicScreen) {
        toggleTrackingButton = findViewById(R.id.toggle_tracking_button);
        checkNowButton = findViewById(R.id.check_now_button);
        progressBar = findViewById(R.id.next_bazaar_update);
        editTrackersButton = findViewById(R.id.bazaar_edit_trackers);
        lastUpdated = findViewById(R.id.bzaar_order_screen_last_updated);
        dynamicScreen = findViewById(R.id.bazaar_item_layout);


        // Set up listeners
        toggleTrackingButton.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (!isChecked) {
                if (nextCheck != null) {
                    nextCheck.cancel(false);
                }
                if (lastUpdated != null) {
                    updateLastUpdated.cancel(false);
                }
                if (progressBarAnimation != null) {
                    progressBarAnimation.pause();
                }
                progressBar.setProgress(0);
            } else {
                checkPrice();
                registerNextCheck();
            }
        });

        checkNowButton.setOnClickListener(v -> {
            checkNowButton.setText(R.string.checking);
            checkNowButton.requestLayout();
            nextCheck.cancel(false);
            core.executionService().execute(() -> {
                try {
                    checkPrice(bazaarService.getMaxAgeResponse(Duration.ZERO));
                } catch (IOException e) {
                    e.printStackTrace();
                }
                registerNextCheck();
                post(() -> {
                    checkNowButton.setText(R.string.check_now);
                    checkNowButton.requestLayout();
                });
            });
        });

        editTrackersButton.setOnClickListener(v -> {
            context.setContentView(new CurrentTrackersScreen(core, this));
        });

        progressBar.setTooltipText("Time until next Refresh");
    }

    @Override
    protected LinearLayout getDynamicScreen() {
        dynamicScreen = findViewById(R.id.bazaar_item_layout);
        core.executionService().execute(() -> {
            checkPrice();
            post(() -> dynamicScreen.removeView(loading));
        });
        return dynamicScreen;
    }


    private void registerNextCheck() {
        int timeBetweenChecks = bazaarService.getCheckInterval();
        startProgressBarCountdown(timeBetweenChecks);
        nextCheck = core.executionService().schedule(() -> {
            checkPrice();
            if (toggleTrackingButton.isChecked()) registerNextCheck();
        }, timeBetweenChecks, java.util.concurrent.TimeUnit.SECONDS);
        updateLastUpdated = core.executionService().scheduleWithFixedDelay(() -> {
            Instant lastResponseTime = BazaarService.getLastUpdate();
            if (lastResponseTime == null) return;
            post(() -> {
                lastUpdated.setText(getContext().getString(R.string.last_updated_s_seconds_ago).formatted(Duration.between(lastResponseTime, Instant.now()).getSeconds()));
                lastUpdated.requestLayout();
            });
        }, 0, 1, TimeUnit.SECONDS);
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
            checkPrice(bazaarService.getMaxAgeResponse());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void checkPrice(BazaarResponse response) {
        try {
            if (response == null) {
                dynamicScreen.removeAllViews();
                TextView info = new TextView(context);
                info.setText(R.string.no_data_tracking_stopped);
                info.setGravity(Gravity.CENTER);
                dynamicScreen.addView(info);
                return;
            }
            Map<String, BazaarProduct> items = response.getProducts();
            LinkedHashMap<TrackedBazaarItem, List<BazaarProduct.Offer>> displayTables = new LinkedHashMap<>();
            for (TrackedBazaarItem toTrackItem : bazaarService.trackedItems) {
                if (!toTrackItem.showInOrderScreen()) continue;
                BazaarProduct product = items.get(toTrackItem.itemId);
                if (product == null) {
                    Toast.makeText(context, "Item not found: %s. Skipping it".formatted(toTrackItem.itemId), Toast.LENGTH_SHORT).show();
                    continue;
                }
                displayTables.put(toTrackItem, product.getOfferType(toTrackItem.trackType));
            }
            post(() -> {
                dynamicScreen.removeAllViews();
                for (Map.Entry<TrackedBazaarItem, List<BazaarProduct.Offer>> responseEntry : displayTables.sequencedEntrySet()) {
                    String displayName = responseEntry.getKey().getDisplayName();
                    List<BazaarProduct.Offer> toDisplayOrders = responseEntry.getValue();

                    TextView itemLabel = trackedItemLabels.get(responseEntry.getKey());
                    if (itemLabel == null) {
                        itemLabel = new TextView(context);
                        trackedItemLabels.put(responseEntry.getKey(), itemLabel);
                        itemLabel.setGravity(Gravity.CENTER);
                    }
                    dynamicScreen.addView(itemLabel);
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
                    dynamicScreen.addView(orderTable);
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

    @Override
    public void close() {
        core.context().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        super.close();
        if (nextCheck != null) nextCheck.cancel(true);
        if (updateLastUpdated != null) updateLastUpdated.cancel(true);
    }

    @Override
    public void onPause() {
        // Timer und Animationen pausieren
        if (nextCheck != null) nextCheck.cancel(false);
        if (updateLastUpdated != null) updateLastUpdated.cancel(false);
        if (progressBarAnimation != null) progressBarAnimation.pause();
    }

    @Override
    public void onResume() {
        // Nur neu starten, wenn Tracking aktiv ist
        if (toggleTrackingButton.isChecked()) {
            registerNextCheck();
        }
    }
}