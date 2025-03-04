package de.hype.hypenotify.screen;

import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import androidx.activity.OnBackPressedCallback;
import de.hype.hypenotify.MainActivity;
import de.hype.hypenotify.R;
import de.hype.hypenotify.core.interfaces.Core;

public abstract class Screen extends LinearLayout {
    protected final View parent;
    protected final Core core;
    private OnBackPressedCallback backPressedCallback;
    protected MainActivity context;
    protected Button backButton;
    protected LinearLayout dynamicScreen;

    public Screen(Core core, View parent) {
        super(core.context());
        context = core.context();
        this.core = core;
        this.parent = parent;

        // Create back button with full width
        backButton = new Button(core.context());
        backButton.setText(R.string.back);
        backButton.setOnClickListener(v -> close());

        // Make back button layout take full width
        LinearLayout backLayout = new LinearLayout(core.context());
        backLayout.setOrientation(LinearLayout.VERTICAL);
        backLayout.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
        backLayout.addView(backButton, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));

        // Add back button layout at the top
        addView(backLayout);

        backPressedCallback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                close();
            }
        };
        context.getOnBackPressedDispatcher().addCallback(backPressedCallback);
    }

    public void close() {
        if (backPressedCallback != null) {
            backPressedCallback.remove();
        }
        core.context().setContentView(parent);
    }

    public final void resetDynamicScreen() {
        if (dynamicScreen != null) {
            dynamicScreen.removeAllViews();
            removeView(dynamicScreen);
        }
        dynamicScreen = getDynamicScreen();
        addView(dynamicScreen);
    }

    public final void updateScreen() {
        if (dynamicScreen != null) {
            dynamicScreen.removeAllViews();
            removeView(dynamicScreen);
        }
        updateScreen(getDynamicScreen());
    }

    /**
     * The View is the Dynamic Screen you returned in {@link #getDynamicScreen()}
     */
    protected abstract void updateScreen(LinearLayout dynamicScreen);

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        // Clean up the callback when the view is detached
        if (backPressedCallback != null) {
            backPressedCallback.remove();
        }
    }

    protected abstract LinearLayout getDynamicScreen();
}
