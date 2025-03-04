package de.hype.hypenotify.screen.features.bazaar;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.widget.*;
import de.hype.hypenotify.MainActivity;
import de.hype.hypenotify.R;
import de.hype.hypenotify.core.interfaces.Core;
import de.hype.hypenotify.screen.Screen;
import de.hype.hypenotify.tools.bazaar.TrackedBazaarItem;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@SuppressLint("ViewConstructor")
class CurrentTrackersScreen extends Screen {
    public CurrentTrackersScreen(Core core, Screen parent) {
        super(core, parent);
        init(core.context());
    }


    private void init(MainActivity context) {
        LayoutInflater.from(context).inflate(R.layout.current_bazaar_trackers_screen, this, true);

        Button addNewTrackerButton = findViewById(R.id.add_new_tracker_button);
        addNewTrackerButton.setOnClickListener((v) -> {
            context.setContentView(new CreateTrackerScreen(core, this));
        });
        updateScreen();
        // Set up any additional logic or listeners here
    }

    @Override
    protected void updateScreen(LinearLayout dynamicScreen) {
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
        }
    }

    @Override
    protected LinearLayout getDynamicScreen() {
        return findViewById(R.id.tracker_list);
    }
}

@SuppressLint("ViewConstructor")
class CreateTrackerScreen extends Screen {
    public CreateTrackerScreen(Core core, Screen parent) {
        super(core, parent);
        updateScreen();
    }

    @Override
    protected void updateScreen(LinearLayout dynamicScreen) {
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

    @Override
    protected LinearLayout getDynamicScreen() {
        return null;
    }
}

@SuppressLint("ViewConstructor")
class EditTrackerScreen extends Screen {

    private final @NotNull TrackedBazaarItem trackedBazaarItem;

    public EditTrackerScreen(Core core, Screen parent, @NotNull TrackedBazaarItem trackedBazaarItem) {
        super(core, parent);
        this.trackedBazaarItem = trackedBazaarItem;
    }

    @Override
    protected void updateScreen(LinearLayout dynamicScreen) {
        LayoutInflater.from(context).inflate(R.layout.edit_bazaar_tracker, this, true);

        Button saveChangesButton = findViewById(R.id.done_button);
        saveChangesButton.setOnClickListener((v) -> {
            // Handle save changes logic here
            if (parent != null) {
                context.setContentView(parent);
            }
        });
    }

    @Override
    protected LinearLayout getDynamicScreen() {
        return null;
    }
}

