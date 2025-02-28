package de.hype.hypenotify.screen.features.bazaar;

import android.view.LayoutInflater;
import android.widget.*;
import de.hype.hypenotify.MainActivity;
import de.hype.hypenotify.R;
import de.hype.hypenotify.core.interfaces.Core;
import de.hype.hypenotify.tools.bazaar.TrackedBazaarItem;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

class CurrentTrackersScreen extends LinearLayout {
    private final Core core;
    private final @Nullable BazaarOrdersScreen parent;

    public CurrentTrackersScreen(Core core, @Nullable BazaarOrdersScreen parent) {
        super(core.context());
        this.core = core;
        init(core.context());
        this.parent = parent;
    }

    private void init(MainActivity context) {
        LayoutInflater.from(context).inflate(R.layout.current_bazaar_trackers_screen, this, true);

        Button addNewTrackerButton = findViewById(R.id.add_new_tracker_button);
        addNewTrackerButton.setOnClickListener((v) -> {
            context.setContentView(new CreateTrackerScreen(core, this));
        });
        updateView();
        // Set up any additional logic or listeners here
    }

    public void updateView() {
        LinearLayout trackerList = findViewById(R.id.tracker_list);
        trackerList.removeAllViews();

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
            LinearLayout.LayoutParams itemNameParams = new LinearLayout.LayoutParams(0, LayoutParams.WRAP_CONTENT, 1);
            itemName.setLayoutParams(itemNameParams);
            itemName.setPadding(8, 0, 8, 0);

            Button deleteButton = new Button(getContext());
            deleteButton.setText(R.string.delete);
            deleteButton.setOnClickListener((v) -> {
                trackedItems.remove(item);
                updateView();
            });

            trackerItemLayout.addView(enabledCheckbox);
            trackerItemLayout.addView(itemName);
            trackerItemLayout.addView(deleteButton);

            trackerItemLayout.setOnClickListener((v) -> {
                core.context().setContentView(new EditTrackerScreen(core, item, this));
            });

            trackerList.addView(trackerItemLayout);
        }
    }
}

class CreateTrackerScreen extends LinearLayout {
    private final Core core;
    private final @Nullable CurrentTrackersScreen parent;

    public CreateTrackerScreen(Core core, @Nullable CurrentTrackersScreen parent) {
        super(core.context());
        this.core = core;
        init(core.context());
        this.parent = parent;
    }

    private void init(MainActivity context) {
        LayoutInflater.from(context).inflate(R.layout.create_bazaar_tracker, this, true);

        EditText itemIdInput = findViewById(R.id.item_id_input);
        ListView itemSuggestions = findViewById(R.id.item_suggestions);
        Button createTrackerButton = findViewById(R.id.create_tracker_button);
        createTrackerButton.setOnClickListener((v) -> {
            // Handle tracker creation logic here
            if (parent != null) {
                context.setContentView(parent);
            }
        });
    }
}

class EditTrackerScreen extends LinearLayout {

    private final Core core;
    private final @NotNull TrackedBazaarItem trackedBazaarItem;
    private final @Nullable CurrentTrackersScreen parent;

    public EditTrackerScreen(Core core, @NotNull TrackedBazaarItem trackedBazaarItem, @Nullable CurrentTrackersScreen parent) {
        super(core.context());
        this.core = core;
        init(core.context());
        this.parent = parent;
        this.trackedBazaarItem = trackedBazaarItem;
    }

    private void init(MainActivity context) {
        LayoutInflater.from(context).inflate(R.layout.edit_bazaar_tracker, this, true);

        Button saveChangesButton = findViewById(R.id.done_button);
        saveChangesButton.setOnClickListener((v) -> {
            // Handle save changes logic here
            if (parent != null) {
                context.setContentView(parent);
            }
        });
        // Set up any additional logic or listeners here
    }
}

