package de.hype.hypenotify.screen.features.bazaar;

import android.view.LayoutInflater;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import de.hype.hypenotify.MainActivity;
import de.hype.hypenotify.R;
import de.hype.hypenotify.core.interfaces.Core;
import de.hype.hypenotify.tools.bazaar.TrackedBazaarItem;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

class CurrentTrackersScreen extends LinearLayout {
    private final Core core;

    public CurrentTrackersScreen(Core core) {
        super(core.context());
        this.core = core;
        init(core.context());
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
        List<TrackedBazaarItem> trackedItems = core.bazaarService().trackedItems;


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

