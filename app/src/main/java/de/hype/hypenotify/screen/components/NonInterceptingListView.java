package de.hype.hypenotify.screen.components;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ListView;

public class NonInterceptingListView extends ListView {
    public NonInterceptingListView(Context context) {
        super(context);
    }

    public NonInterceptingListView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public NonInterceptingListView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            // Disallow ScrollView to intercept touch events when ListView is touched
            getParent().requestDisallowInterceptTouchEvent(true);
        }
        return super.onTouchEvent(ev);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // Get the original height from the layout
        int originalHeight = MeasureSpec.getSize(heightMeasureSpec);
        int totalHeight = originalHeight;

        // Calculate height for items only if adapter exists and has items
        if (getAdapter() != null && getAdapter().getCount() > 0) {
            int itemHeight = 0;
            try {
                View item = getAdapter().getView(0, null, this);
                if (item != null) {
                    item.measure(0, 0);
                    itemHeight = item.getMeasuredHeight();
                }

                // Set height to accommodate 5 items (or fewer if fewer exist)
                int totalItemsCount = getAdapter().getCount();
                int itemsToShow = Math.min(5, totalItemsCount);

                if (itemHeight > 0) {
                    totalHeight = itemHeight * itemsToShow;

                    // Add divider heights if dividers are shown
                    if (getDividerHeight() > 0 && itemsToShow > 1) {
                        totalHeight += getDividerHeight() * (itemsToShow - 1);
                    }
                }
            } catch (Exception e) {
                // If any error occurs during measurement, use the original height
                totalHeight = originalHeight;
            }
        }

        // Create a new height spec
        heightMeasureSpec = MeasureSpec.makeMeasureSpec(totalHeight, MeasureSpec.EXACTLY);
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }
}