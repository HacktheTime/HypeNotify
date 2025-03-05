package de.hype.hypenotify.screen;

import android.view.View;
import android.widget.LinearLayout;
import androidx.activity.OnBackPressedCallback;
import de.hype.hypenotify.MainActivity;
import de.hype.hypenotify.core.interfaces.Core;

public abstract class Screen extends LinearLayout {
    protected final View parent;
    protected final Core core;
    private OnBackPressedCallback backPressedCallback;
    protected MainActivity context;
    protected LinearLayout dynamicScreen;

    public Screen(Core core, View parent) {
        super(core.context());
        context = core.context();
        this.core = core;
        this.parent = parent;

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
        if (parent != null) {
            core.context().setContentView(parent);
        } else {
            core.context().finish();
        }
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
        removeAllViews();
        if (dynamicScreen != null) {
            dynamicScreen.removeAllViews();
        }
        try {
            inflateLayouts();
            LinearLayout newDynamicScreen = getDynamicScreen();
            updateScreen(newDynamicScreen);
            if (newDynamicScreen != null) {
                if (newDynamicScreen.getParent() == null) {
                    if (dynamicScreen != null) removeView(dynamicScreen);
                    addView(newDynamicScreen);
                }
                dynamicScreen = newDynamicScreen;
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    /**
     * Inflate all layouts you need here. Called before the Dynamic Screen is obtained to give you back at update Screen.
     */
    protected abstract void inflateLayouts();


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
