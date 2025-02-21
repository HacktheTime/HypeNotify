package de.hype.hypenotify.layouts;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.graphics.Color;
import android.view.Gravity;
import android.widget.*;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import de.hype.hypenotify.Core;
import de.hype.hypenotify.NotificationUtils;
import de.hype.hypenotify.layouts.autodetection.Layout;

import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.NumberFormat;
import java.util.concurrent.ScheduledFuture;
@Layout(name = "Enchanted Redstone Lamps Notifier")

public class SkyblockEnchantedRedstoneLampsNotifier extends LinearLayout {
    private static final String API_URL = "https://api.hypixel.net/v2/skyblock/bazaar";
    private static final String ITEM_NAME = "ENCHANTED_REDSTONE_LAMP";
    private static final String CHANNEL_ID = "price_update_channel";
    private final Core core;
    private TextView priceLabel;
    private JsonObject previousResponse = null;
    private Integer checkWifiStateCounter = 0;
    private ToggleButton toggleTrackingButton;
    private Button checkNowButton;
    private ScheduledFuture<?> nextCheck;

    public SkyblockEnchantedRedstoneLampsNotifier(Core core) {
        super(core.context);
        this.core = core;
        init(core.context);
    }

    private void init(Context context) {
        toggleTrackingButton = new ToggleButton(context);
        toggleTrackingButton.setChecked(true);
        toggleTrackingButton.setTextOn("Currently Tracking API");
        toggleTrackingButton.setTextOff("Currently NOT Tracking API");

        checkNowButton = new Button(context);
        checkNowButton.setText("Check Now");
        checkNowButton.setOnClickListener(v -> checkPrice(context));

        setOrientation(VERTICAL);
        LayoutParams layoutParams = new LayoutParams(
                LayoutParams.MATCH_PARENT,
                LayoutParams.MATCH_PARENT
        );
        setLayoutParams(layoutParams);
        setGravity(Gravity.CENTER);

        priceLabel = new TextView(context);
        priceLabel.setText("Loading...");
        priceLabel.setGravity(Gravity.CENTER);
        priceLabel.setTextSize(18);
        priceLabel.setTextColor(Color.WHITE);
        priceLabel.setPadding(16, 16, 16, 16);
        addView(priceLabel, new LayoutParams(
                LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT
        ));

        createNotificationChannel(context);

        toggleTrackingButton.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (!isChecked) {
                nextCheck.cancel(false);
            } else {
                checkPrice(context);
                registerNextCheck();
            }
        });
        checkPrice(context);
        registerNextCheck();
    }

    private void registerNextCheck() {
        nextCheck = core.executionService.schedule(() -> {
            checkPrice(core.context);
            checkWifiStateCounter++;
            if (checkWifiStateCounter >= 40) {
                if (!core.isInHomeNetwork()) {
                    NotificationUtils.createNotification(core.context,"Bazaar Price Checker","You are not in the home network. Tracking stopped.");
                    return;
                }
            }
            if (toggleTrackingButton.isChecked()) registerNextCheck();
        }, 15, java.util.concurrent.TimeUnit.SECONDS);
    }

    // In SkyblockEnchantedRedstoneLampsNotifier.java:
    private void checkPrice(Context context) {
        try {
            HttpURLConnection connection =
                    (HttpURLConnection) new URL(API_URL).openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);
            connection.connect();

            InputStreamReader reader =
                    new InputStreamReader(connection.getInputStream());
            JsonObject response = JsonParser.parseReader(reader).getAsJsonObject();
            JsonArray buySummary =
                    response.getAsJsonObject("products")
                            .getAsJsonObject(ITEM_NAME)
                            .getAsJsonArray("buy_summary");

            // 1) Format
            NumberFormat amountFormat = NumberFormat.getInstance(java.util.Locale.US);
            NumberFormat priceFormat = NumberFormat.getInstance(java.util.Locale.US);
            priceFormat.setMinimumFractionDigits(1);
            priceFormat.setMaximumFractionDigits(1);

            // 2) Get top 3 new orders
            JsonArray newTop3 = new JsonArray();
            for (int i = 0; i < Math.min(3, buySummary.size()); i++) {
                newTop3.add(buySummary.get(i));
            }

            // 3) Compare only top 3 changes (price or amount goes up)
            boolean hasChange = false;
            if (previousResponse != null) {
                JsonArray oldBuySummary =
                        previousResponse.getAsJsonObject("products")
                                .getAsJsonObject(ITEM_NAME)
                                .getAsJsonArray("buy_summary");
                for (int i = 0; i < newTop3.size(); i++) {
                    if (i >= oldBuySummary.size()) {
                        hasChange = true;
                        break;
                    }
                    JsonObject oldObj = oldBuySummary.get(i).getAsJsonObject();
                    JsonObject newObj = newTop3.get(i).getAsJsonObject();
                    double oldPrice = oldObj.get("pricePerUnit").getAsDouble();
                    double newPrice = newObj.get("pricePerUnit").getAsDouble();
                    int oldAmount = oldObj.get("amount").getAsInt();
                    int newAmount = newObj.get("amount").getAsInt();

                    // Only price change or amount increase
                    if (Double.compare(oldPrice, newPrice) != 0 || newAmount > oldAmount) {
                        hasChange = true;
                        break;
                    }
                }
            }
            post(() -> {
                // Clear or remove any previous views if needed
                removeAllViews();
                addView(toggleTrackingButton);
                StringBuilder priceText = new StringBuilder("Orders:\n");
                priceText.append("Item: Enchanted Redstone Lamp\n");
                priceLabel.setText(priceText.toString());
                addView(priceLabel);

                LinearLayout tableContainer = new LinearLayout(context);
                tableContainer.setOrientation(LinearLayout.VERTICAL);
                tableContainer.setGravity(Gravity.CENTER);
                LinearLayout.LayoutParams containerLayoutParams = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );
                containerLayoutParams.gravity = Gravity.CENTER_HORIZONTAL;
                tableContainer.setLayoutParams(containerLayoutParams);

                // Change the layout parameters for the TableLayout
                TableLayout tableLayout = new TableLayout(context);
                tableLayout.setStretchAllColumns(true);
                LinearLayout.LayoutParams tableLayoutParams = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT, // Change to MATCH_PARENT
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );
                tableLayoutParams.gravity = Gravity.CENTER; // Ensure gravity is set
                tableLayout.setLayoutParams(tableLayoutParams);

// Header row
                TableRow headerRow = new TableRow(context);
                TextView headerAmount = new TextView(context);
                headerAmount.setText("Amount");
                headerAmount.setGravity(Gravity.CENTER); // Center the text
                TextView headerCoins = new TextView(context);
                headerCoins.setText("Coins");
                headerCoins.setGravity(Gravity.CENTER); // Center the text
                headerRow.addView(headerAmount);
                headerRow.addView(headerCoins);
                tableLayout.addView(headerRow);

// Data rows from newTop3
                for (int i = 0; i < newTop3.size(); i++) {
                    JsonObject o = newTop3.get(i).getAsJsonObject();
                    TableRow dataRow = new TableRow(context);

                    TextView amountCell = new TextView(context);
                    amountCell.setText(amountFormat.format(o.get("amount").getAsInt()));
                    amountCell.setGravity(Gravity.CENTER); // Center the text

                    TextView coinsCell = new TextView(context);
                    coinsCell.setText(priceFormat.format(o.get("pricePerUnit").getAsDouble()));
                    coinsCell.setGravity(Gravity.CENTER); // Center the text

                    dataRow.addView(amountCell);
                    dataRow.addView(coinsCell);
                    tableLayout.addView(dataRow);
                }

                // Add the table to the container
                tableContainer.addView(tableLayout);

                // Finally, add the container to this layout
                addView(tableContainer);
            });

            // 4) Send notification if changed
            if (hasChange) {
                StringBuilder msg = new StringBuilder("Changed top 3 orders:\n");
                for (int i = 0; i < newTop3.size(); i++) {
                    JsonObject o = newTop3.get(i).getAsJsonObject();
                    msg.append("[").append(i + 1).append("] ");
                    msg.append(amountFormat.format(o.get("amount").getAsInt()));
                    msg.append(" @ ");
                    msg.append(priceFormat.format(o.get("pricePerUnit").getAsDouble()));
                    msg.append("\n");
                }
                android.app.NotificationManager notificationManager =
                        (android.app.NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                if (notificationManager != null) {
                    androidx.core.app.NotificationCompat.Builder builder =
                            new androidx.core.app.NotificationCompat.Builder(context, CHANNEL_ID)
                                    .setSmallIcon(de.hype.hypenotify.R.mipmap.icon)
                                    .setContentTitle("Bazaar Price Update")
                                    .setContentText("Enchanted Redstone Lamp Bazaar changed!")
                                    .setStyle(new androidx.core.app.NotificationCompat.BigTextStyle()
                                            .bigText(msg.toString()))
                                    .setPriority(androidx.core.app.NotificationCompat.PRIORITY_HIGH)
                                    .setAutoCancel(true);
                    notificationManager.notify(2, builder.build());
                }
            }

            previousResponse = response;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void createNotificationChannel(Context context) {
        CharSequence name = "Bazaar Notifications";
        String description = "Channel for price updates from the Hypixel Skyblock Bazaar API";
        int importance = NotificationManager.IMPORTANCE_HIGH;
        NotificationChannel channel =
                new NotificationChannel(CHANNEL_ID, name, importance);
        channel.setDescription(description);
        NotificationManager notificationManager =
                context.getSystemService(NotificationManager.class);
        notificationManager.createNotificationChannel(channel);
    }
}