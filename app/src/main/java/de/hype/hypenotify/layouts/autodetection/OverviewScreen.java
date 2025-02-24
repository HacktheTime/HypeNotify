package de.hype.hypenotify.layouts.autodetection;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import de.hype.hypenotify.R;
import de.hype.hypenotify.core.interfaces.Core;

import java.util.Map;

public class OverviewScreen extends LinearLayout {
    private EditText searchBox;
    private final LinearLayout layoutList;
    private final Core core;
    private final Context context;

    public OverviewScreen(Core core) {
        super(core.context());
        this.core = core;
        this.context = core.context();
        LayoutInflater.from(core.context()).inflate(R.layout.sidebar, this, true);

        searchBox = findViewById(R.id.search_box);
        layoutList = findViewById(R.id.layout_list);

        searchBox.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                updateLayoutList(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) { }
        });

        updateLayoutList("");
    }

    private void updateLayoutList(String filter) {
        // Clear only dynamic items, preserving the search box.
        layoutList.removeAllViews();
        Map<String, Class<?>> layouts = LayoutRegistry.getAllLayouts();
        for (Map.Entry<String, Class<?>> entry : layouts.entrySet()) {
            if (filter.isEmpty() || entry.getKey().toLowerCase().contains(filter.toLowerCase())) {
                TextView layoutItem = new TextView(getContext());
                layoutItem.setText(entry.getKey());
                layoutItem.setOnClickListener(v -> switchLayout(entry.getValue()));
                layoutItem.setTextSize(20);
                layoutList.addView(layoutItem);
            }
        }
    }

    private void switchLayout(Class<?> layoutClass) {
        try {
            Object layoutInstance = layoutClass.getConstructor(Core.class).newInstance(core);
            if (layoutInstance instanceof View) {
                View newView = (View) layoutInstance;
                // Create a container for the back button and the dynamic content.
                LinearLayout container = new LinearLayout(context);
                container.setOrientation(LinearLayout.VERTICAL);

                // Create and add the back button.
                Button backButton = new Button(getContext());
                backButton.setText(R.string.back);
                backButton.setOnClickListener(v -> ((AppCompatActivity) getContext()).setContentView(OverviewScreen.this));
                container.addView(backButton);

                // Add the dynamic content to the container.
                container.addView(newView);

                // Set the container as the content view.
                ((AppCompatActivity) getContext()).setContentView(container);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}