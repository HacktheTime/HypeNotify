package de.hype.hypenotify.app.screen.components

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.widget.ListView
import kotlin.math.min

class NonInterceptingListView : ListView {
    constructor(context: Context?) : super(context)

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)

    constructor(context: Context?, attrs: AttributeSet?, defStyle: Int) : super(context, attrs, defStyle)

    override fun onTouchEvent(ev: MotionEvent): Boolean {
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            // Disallow ScrollView to intercept touch events when ListView is touched
            getParent().requestDisallowInterceptTouchEvent(true)
        }
        return super.onTouchEvent(ev)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        // Get the original height from the layout
        var heightMeasureSpec = heightMeasureSpec
        val originalHeight = MeasureSpec.getSize(heightMeasureSpec)
        var totalHeight = originalHeight

        // Calculate height for items only if adapter exists and has items
        if (getAdapter() != null && getAdapter().getCount() > 0) {
            var itemHeight = 0
            try {
                val item = getAdapter().getView(0, null, this)
                if (item != null) {
                    item.measure(0, 0)
                    itemHeight = item.getMeasuredHeight()
                }

                // Set height to accommodate 5 items (or fewer if fewer exist)
                val totalItemsCount = getAdapter().getCount()
                val itemsToShow = min(5, totalItemsCount)

                if (itemHeight > 0) {
                    totalHeight = itemHeight * itemsToShow

                    // Add divider heights if dividers are shown
                    if (getDividerHeight() > 0 && itemsToShow > 1) {
                        totalHeight += getDividerHeight() * (itemsToShow - 1)
                    }
                }
            } catch (e: Exception) {
                // If any error occurs during measurement, use the original height
                totalHeight = originalHeight
            }
        }

        // Create a new height spec
        heightMeasureSpec = MeasureSpec.makeMeasureSpec(totalHeight, MeasureSpec.EXACTLY)
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
    }
}