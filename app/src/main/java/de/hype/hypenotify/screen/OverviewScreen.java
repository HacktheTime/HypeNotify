package de.hype.hypenotify.screen;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import de.hype.hypenotify.R;
import de.hype.hypenotify.core.interfaces.Core;
import de.hype.hypenotify.layouts.autodetection.LayoutRegistry;

import java.util.Map;

public class OverviewScreen extends Screen {
    private EditText searchBox;
    private LinearLayout layoutList;
    public OverviewScreen(Core core) {
        super(core, null);
        updateScreen();
    }

    @Override
    public void close() {
        super.close();
    }

    private void updateLayoutList(String filter) {
        // Clear only dynamic items, preserving the search box.
        Map<String, Class<? extends Screen>> layouts = LayoutRegistry.getAllLayouts();
        for (Map.Entry<String, Class<? extends Screen>> entry : layouts.entrySet()) {
            if (filter.isEmpty() || entry.getKey().toLowerCase().contains(filter.toLowerCase())) {
                TextView layoutItem = new TextView(getContext());
                layoutItem.setText(entry.getKey());
                layoutItem.setOnClickListener(v -> switchLayout(entry.getValue()));
                layoutItem.setTextSize(20);
                layoutList.addView(layoutItem);
            }
        }
    }

    private void switchLayout(Class<? extends Screen> layoutClass) {
        try {
            View layoutInstance = layoutClass.getConstructor(Core.class, View.class).newInstance(core, this);
            // Create a container for the back button and the dynamic content.
            LinearLayout container = new LinearLayout(context);
            container.setOrientation(LinearLayout.VERTICAL);

            // Add the dynamic content to the container.
            container.addView(layoutInstance);

            // Set the container as the content view.
            ((AppCompatActivity) getContext()).setContentView(container);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void inflateLayouts() {
        LayoutInflater.from(core.context()).inflate(R.layout.sidebar, this, true);
    }

    @Override
    protected void updateScreen(LinearLayout dynamicScreen) {
        searchBox = findViewById(R.id.search_box);
        layoutList = findViewById(R.id.layout_list);

        searchBox.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                updateLayoutList(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
        updateLayoutList("");
    }

    @Override
    protected LinearLayout getDynamicScreen() {
        return null;
    }
}