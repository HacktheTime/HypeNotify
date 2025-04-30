package de.hype.hypenotify.app.screen.features.bazaar;

import android.annotation.SuppressLint;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import de.hype.hypenotify.R;
import de.hype.hypenotify.app.core.interfaces.Core;
import de.hype.hypenotify.app.screen.Screen;
import de.hype.hypenotify.app.tools.bazaar.BazaarProduct;
import de.hype.hypenotify.app.tools.bazaar.BazaarResponse;
import de.hype.hypenotify.app.tools.bazaar.TrackedBazaarItem;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

@SuppressLint("ViewConstructor")
class CurrentTrackersScreen extends Screen {
    public CurrentTrackersScreen(Core core, Screen parent) {
        super(core, parent);
        updateScreen();
    }

    @Override
    protected void inflateLayouts() {
    }

    @Override
    protected void updateScreen(LinearLayout dynamicScreen) {
        LayoutInflater.from(context).inflate(R.layout.current_bazaar_trackers_screen, this, true);
        dynamicScreen = findViewById(R.id.tracker_list);
        Button addNewTrackerButton = findViewById(R.id.add_new_tracker_button);
        addNewTrackerButton.setOnClickListener((v) -> {
            context.setContentView(new CreateTrackerScreen(core, this));
        });

        List<TrackedBazaarItem> trackedItems = core.bazaarService().trackedItems;
        for (TrackedBazaarItem item : trackedItems) {
            LinearLayout trackerItemLayout = new LinearLayout(getContext());
            trackerItemLayout.setOrientation(LinearLayout.HORIZONTAL);
            trackerItemLayout.setPadding(8, 8, 8, 8);

            CheckBox enabledCheckbox = new CheckBox(getContext());
            enabledCheckbox.setChecked(item.isEnabled());
            enabledCheckbox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                item.setEnabled(isChecked);
                // Update the item's enabled state in the core or database
            });

            TextView itemName = new TextView(getContext());
            itemName.setText(item.getDisplayName());
            itemName.setTextSize(16);
            LinearLayout.LayoutParams itemNameParams = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1);
            itemName.setLayoutParams(itemNameParams);
            itemName.setPadding(8, 0, 8, 0);

            Button deleteButton = new Button(getContext());
            deleteButton.setText(R.string.delete);
            deleteButton.setOnClickListener((v) -> {
                trackedItems.remove(item);
                resetDynamicScreen();
            });

            trackerItemLayout.addView(enabledCheckbox);
            trackerItemLayout.addView(itemName);
            trackerItemLayout.addView(deleteButton);

            trackerItemLayout.setOnClickListener((v) -> {
                core.context().setContentView(new EditTrackerScreen(core, this, item));
            });

            // Add the tracker item layout to the container
            dynamicScreen.addView(trackerItemLayout);
        }
    }

    @Override
    public void onPause() {

    }

    @Override
    public void onResume() {

    }

    @Override
    protected LinearLayout getDynamicScreen() {
        return findViewById(R.id.tracker_list);
    }
}

@SuppressLint("ViewConstructor")
class CreateTrackerScreen extends Screen {
    private EditText itemIdInput;
    private ListView itemSuggestions;
    private CheckBox notifyGoodChangesCheckbox;
    private CheckBox trackPriceChangesCheckbox;

    public CreateTrackerScreen(Core core, Screen parent) {
        super(core, parent);
        updateScreen();
    }

    private void setupItemSuggestions() {
        BazaarResponse bazaarResponse = core.bazaarService().getLastResponse();
        if (bazaarResponse != null) {
            List<BazaarSuggestionItem> suggestions = new ArrayList<>();
            bazaarResponse.getProducts().forEach((itemId, product) -> {
                suggestions.add(new BazaarSuggestionItem(itemId, product.getDisplayName()));
            });

            BazaarSuggestionAdapter adapter = new BazaarSuggestionAdapter(context, suggestions);
            itemSuggestions.setAdapter(adapter);

            // Handle item selection
            itemSuggestions.setOnItemClickListener((parent, view, position, id) -> {
                BazaarSuggestionItem item = (BazaarSuggestionItem) parent.getItemAtPosition(position);
                itemIdInput.setText(item.itemId);
                itemSuggestions.setVisibility(GONE);
            });

            // Filter suggestions as user types
            itemIdInput.addTextChangedListener(new TextWatcher() {
                ScheduledFuture<?> update = null;
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (update != null) update.cancel(false);
                    update = core.executionService().schedule(() -> {
                        post(() -> {
                            String filter = s.toString().toLowerCase();
                            List<BazaarSuggestionItem> filtered = new ArrayList<>();

                            for (Map.Entry<String, BazaarProduct> entry : bazaarResponse.getProducts().entrySet()) {
                                String itemId = entry.getKey().toLowerCase();
                                String displayName = entry.getValue().getDisplayName().toLowerCase();

                                if (itemId.contains(filter) || displayName.contains(filter)) {
                                    filtered.add(new BazaarSuggestionItem(entry.getKey(), entry.getValue().getDisplayName()));
                                }
                            }

                            BazaarSuggestionAdapter filteredAdapter = new BazaarSuggestionAdapter(context, filtered);
                            itemSuggestions.setAdapter(filteredAdapter);
                            itemSuggestions.setVisibility(VISIBLE);
                        });
                    }, 2, TimeUnit.SECONDS);
                }

                @Override
                public void afterTextChanged(Editable s) {
                }
            });
        }
    }


    @Override
    public void onPause() {

    }

    @Override
    public void onResume() {

    }

    private void createTracker() {
        String itemId = itemIdInput.getText().toString().trim();

        if (itemId.isEmpty()) {
            Toast.makeText(context, "Please enter a valid item ID", Toast.LENGTH_SHORT).show();
            return;
        }

        // Get tracking type
        RadioGroup trackTypeGroup = findViewById(R.id.track_type_group);
        BazaarProduct.OfferType trackType = trackTypeGroup.getCheckedRadioButtonId() == R.id.track_buy ?
                BazaarProduct.OfferType.INSTANT_BUY : BazaarProduct.OfferType.INSTANT_SELL;

        // Create new tracker
        TrackedBazaarItem tracker = new TrackedBazaarItem(itemId, trackType);

        // Set options
        tracker.setTrackPriceChanges(trackPriceChangesCheckbox.isChecked());
        tracker.setNotifyGoodChanges(notifyGoodChangesCheckbox.isChecked());
        tracker.setInformAboutOrderAttachments(((CheckBox) findViewById(R.id.inform_order_attachments)).isChecked());
        tracker.setShowInOrderScreen(((CheckBox) findViewById(R.id.show_in_order_screen)).isChecked());
        tracker.setEnabled(true);

        // Add tracker to service
        core.bazaarService().trackedItems.add(tracker);

        Toast.makeText(context, "Tracker created for " + itemId, Toast.LENGTH_SHORT).show();

        // Return to parent screen
        context.setContentView(parent);
    }

    @Override
    protected void inflateLayouts() {
        LayoutInflater.from(context).inflate(R.layout.create_bazaar_tracker, this, true);
    }

    @Override
    protected void updateScreen(LinearLayout dynamicScreen) {
        // Initialize views
        itemIdInput = findViewById(R.id.item_id_input);
        itemSuggestions = findViewById(R.id.item_suggestions);
        notifyGoodChangesCheckbox = findViewById(R.id.notify_good_changes);
        trackPriceChangesCheckbox = findViewById(R.id.track_price_changes);

        // Set up item suggestions with custom adapter
        setupItemSuggestions();

        // Set up dependent checkboxes
        trackPriceChangesCheckbox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (!isChecked) {
                notifyGoodChangesCheckbox.setChecked(false);
                notifyGoodChangesCheckbox.setEnabled(false);
            } else {
                notifyGoodChangesCheckbox.setEnabled(true);
            }
        });

        Button createTrackerButton = findViewById(R.id.create_tracker_button);
        createTrackerButton.setOnClickListener(v -> createTracker());
    }

    @Override
    protected LinearLayout getDynamicScreen() {
        return null;
    }
}

@SuppressLint("ViewConstructor")
class EditTrackerScreen extends Screen {
    private final TrackedBazaarItem trackedItem;
    private EditText itemIdInput;
    private ListView itemSuggestions;
    private CheckBox notifyGoodChangesCheckbox;
    private CheckBox trackPriceChangesCheckbox;

    public EditTrackerScreen(Core core, Screen parent, TrackedBazaarItem trackedItem) {
        super(core, parent);
        this.trackedItem = trackedItem;
        updateScreen();
    }

    private void addTrackingTypeSelector(ViewGroup container) {
        TextView title = new TextView(context);
        title.setText("Tracking Type");
        title.setPadding(0, 16, 0, 8);

        RadioGroup group = new RadioGroup(context);
        group.setId(View.generateViewId());
        group.setOrientation(LinearLayout.HORIZONTAL);

        RadioButton buyButton = new RadioButton(context);
        buyButton.setId(View.generateViewId());
        buyButton.setText("Instant Buy");
        buyButton.setChecked(trackedItem.trackType == BazaarProduct.OfferType.INSTANT_BUY);

        RadioButton sellButton = new RadioButton(context);
        sellButton.setId(View.generateViewId());
        sellButton.setText("Instant Sell");
        sellButton.setChecked(trackedItem.trackType == BazaarProduct.OfferType.INSTANT_SELL);

        group.addView(buyButton);
        group.addView(sellButton);

        container.addView(title);
        container.addView(group);
    }

    private CheckBox addOptionWithDescription(ViewGroup container, String title, String description, boolean checked) {
        CheckBox checkBox = new CheckBox(context);
        checkBox.setId(View.generateViewId());
        checkBox.setText(title);
        checkBox.setChecked(checked);

        TextView descText = new TextView(context);
        descText.setText(description);
        descText.setTextSize(12);
        descText.setPadding(32, 0, 0, 8);

        container.addView(checkBox);
        container.addView(descText);

        return checkBox;
    }

    @Override
    public void onPause() {

    }

    @Override
    public void onResume() {

    }

    private void setupItemSuggestions() {
        BazaarResponse bazaarResponse = core.bazaarService().getLastResponse();
        if (bazaarResponse != null) {
            List<BazaarSuggestionItem> suggestions = new ArrayList<>();
            bazaarResponse.getProducts().forEach((itemId, product) -> {
                suggestions.add(new BazaarSuggestionItem(itemId, product.getDisplayName()));
            });

            BazaarSuggestionAdapter adapter = new BazaarSuggestionAdapter(context, suggestions);
            itemSuggestions.setAdapter(adapter);

            // Handle item selection
            itemSuggestions.setOnItemClickListener((parent, view, position, id) -> {
                BazaarSuggestionItem item = (BazaarSuggestionItem) parent.getItemAtPosition(position);
                itemIdInput.setText(item.itemId);
                itemSuggestions.setVisibility(GONE);
            });

            // Filter suggestions as user types
            itemIdInput.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    String filter = s.toString().toLowerCase();
                    List<BazaarSuggestionItem> filtered = new ArrayList<>();

                    for (Map.Entry<String, BazaarProduct> entry : bazaarResponse.getProducts().entrySet()) {
                        String itemId = entry.getKey().toLowerCase();
                        String displayName = entry.getValue().getDisplayName().toLowerCase();

                        if (itemId.contains(filter) || displayName.contains(filter)) {
                            filtered.add(new BazaarSuggestionItem(entry.getKey(), entry.getValue().getDisplayName()));
                        }
                    }

                    BazaarSuggestionAdapter filteredAdapter = new BazaarSuggestionAdapter(context, filtered);
                    itemSuggestions.setAdapter(filteredAdapter);
                    itemSuggestions.setVisibility(VISIBLE);
                }

                @Override
                public void afterTextChanged(Editable s) {
                }
            });
        }
    }

    private void saveChanges() {
        // Find all option checkboxes by their container
        ViewGroup container = (ViewGroup) findViewById(R.id.done_button).getParent();

        // Update tracking type
        RadioGroup trackTypeGroup = findFirstViewOfType(container, RadioGroup.class);
        if (trackTypeGroup != null) {
            for (int i = 0; i < trackTypeGroup.getChildCount(); i++) {
                RadioButton button = (RadioButton) trackTypeGroup.getChildAt(i);
                if (button.isChecked()) {
                    trackedItem.trackType = button.getText().toString().equals("Instant Buy") ?
                            BazaarProduct.OfferType.INSTANT_BUY : BazaarProduct.OfferType.INSTANT_SELL;
                    break;
                }
            }
        }

        // Update other options
        trackedItem.setTrackPriceChanges(trackPriceChangesCheckbox.isChecked());
        trackedItem.setNotifyGoodChanges(notifyGoodChangesCheckbox.isChecked());
        trackedItem.setInformAboutOrderAttachments(
                findCheckBoxByLabel(container, "Inform about order attachments").isChecked());
        trackedItem.setShowInOrderScreen(
                findCheckBoxByLabel(container, "Show in order screen").isChecked());

        Toast.makeText(context, "Tracker updated", Toast.LENGTH_SHORT).show();
        context.setContentView(parent);
    }

    @SuppressWarnings("unchecked")
    private <T extends View> T findFirstViewOfType(ViewGroup root, Class<T> type) {
        for (int i = 0; i < root.getChildCount(); i++) {
            View child = root.getChildAt(i);
            if (type.isInstance(child)) {
                return (T) child;
            }
            if (child instanceof ViewGroup) {
                T result = findFirstViewOfType((ViewGroup) child, type);
                if (result != null) {
                    return result;
                }
            }
        }
        return null;
    }

    private CheckBox findCheckBoxByLabel(ViewGroup root, String label) {
        for (int i = 0; i < root.getChildCount(); i++) {
            View child = root.getChildAt(i);
            if (child instanceof CheckBox && ((CheckBox) child).getText().toString().equals(label)) {
                return (CheckBox) child;
            }
            if (child instanceof ViewGroup) {
                CheckBox result = findCheckBoxByLabel((ViewGroup) child, label);
                if (result != null) {
                    return result;
                }
            }
        }
        return null;
    }

    @Override
    protected void inflateLayouts() {
        LayoutInflater.from(context).inflate(R.layout.edit_bazaar_tracker, this, true);
    }

    @Override
    protected void updateScreen(LinearLayout dynamicScreen) {
// Initialize views
        itemIdInput = findViewById(R.id.edit_item_id_input);
        itemSuggestions = findViewById(R.id.edit_item_suggestions);

        // Set existing item ID
        itemIdInput.setText(trackedItem.itemId);

        // Add options with descriptions
        LinearLayout container = new LinearLayout(context);
        container.setOrientation(LinearLayout.VERTICAL);
        container.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));

        // Add tracking type selector
        addTrackingTypeSelector(container);

        // Add option checkboxes with descriptions
        trackPriceChangesCheckbox = addOptionWithDescription(container,
                "Track price changes",
                "Inform about price changes with notifications",
                trackedItem.trackPriceChanges());

        notifyGoodChangesCheckbox = addOptionWithDescription(container,
                "Notify about beneficial changes",
                "Inform about order cancellations or orders being fully filled",
                trackedItem.isNotifyGoodChanges());

        addOptionWithDescription(container,
                "Inform about order attachments",
                "Inform when amount increases compared to last check",
                trackedItem.isInformAboutOrderAttachments());

        addOptionWithDescription(container,
                "Show in order screen",
                "Display this tracker in the Bazaar Order Screen",
                trackedItem.showInOrderScreen());

        // Set up dependency between options
        trackPriceChangesCheckbox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (!isChecked) {
                notifyGoodChangesCheckbox.setChecked(false);
                notifyGoodChangesCheckbox.setEnabled(false);
            } else {
                notifyGoodChangesCheckbox.setEnabled(true);
            }
        });

        // Ensure initial state is correct
        if (!trackPriceChangesCheckbox.isChecked()) {
            notifyGoodChangesCheckbox.setEnabled(false);
        }

        // Add container before done button
        addView(container, indexOfChild(findViewById(R.id.done_button)));

        // Set up item suggestions with filter
        setupItemSuggestions();

        // Set up done button
        Button doneButton = findViewById(R.id.done_button);
        doneButton.setOnClickListener(v -> saveChanges());
    }

    @Override
    protected LinearLayout getDynamicScreen() {
        return null;
    }
}

