package de.hype.hypenotify.layouts.autodetection;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import de.hype.hypenotify.Core;
import de.hype.hypenotify.R;
import java.util.Map;

public class Sidebar extends LinearLayout {
    private EditText searchBox;
    private final LinearLayout layoutList;
    private final Core core;
    private final Context context;

    public Sidebar(Core core) {
        super(core.context);
        this.core = core;
        this.context = core.context;
        LayoutInflater.from(core.context).inflate(R.layout.sidebar, this, true);

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
                layoutList.addView(layoutItem);
            }
        }
    }

    private void switchLayout(Class<?> layoutClass) {
        try {
            Object layoutInstance = layoutClass.getConstructor(Context.class).newInstance(getContext());
            if (layoutInstance instanceof View) {
                View newView = (View) layoutInstance;
                // Insert a back button if the new view is a container.
                if (newView instanceof ViewGroup) {
                    Button backButton = new Button(getContext());
                    backButton.setText("Back");
                    backButton.setOnClickListener(v -> ((AppCompatActivity) getContext()).setContentView(Sidebar.this));

                    // If the view is a LinearLayout, add the back button at the top.
                    if (newView instanceof LinearLayout) {
                        ((LinearLayout) newView).addView(backButton, 0);
                    } else {
                        // Otherwise wrap it in a LinearLayout to add the button.
                        LinearLayout wrapper = new LinearLayout(context);
                        wrapper.setOrientation(LinearLayout.VERTICAL);
                        wrapper.addView(backButton);
                        wrapper.addView(newView);
                        newView = wrapper;
                    }
                }
                ((AppCompatActivity) getContext()).setContentView(newView);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}