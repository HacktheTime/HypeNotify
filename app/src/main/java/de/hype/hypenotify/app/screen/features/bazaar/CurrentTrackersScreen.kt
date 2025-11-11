package de.hype.hypenotify.app.screen.features.bazaar

import android.annotation.SuppressLint
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.AdapterView.OnItemClickListener
import android.widget.Button
import android.widget.CheckBox
import android.widget.CompoundButton
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ListView
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import de.hype.hypenotify.R
import de.hype.hypenotify.app.core.interfaces.Core
import de.hype.hypenotify.app.screen.Screen
import de.hype.hypenotify.app.tools.bazaar.BazaarProduct
import de.hype.hypenotify.app.tools.bazaar.BazaarProduct.OfferType
import de.hype.hypenotify.app.tools.bazaar.BazaarResponse
import de.hype.hypenotify.app.tools.bazaar.BazaarService
import de.hype.hypenotify.app.tools.bazaar.TrackedBazaarItem
import java.util.*
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit

@SuppressLint("ViewConstructor")
internal class CurrentTrackersScreen(core: Core, parent: Screen?) : Screen(core, parent) {
    init {
        updateScreen()
    }

    override fun inflateLayouts() {
    }

    override fun updateScreen(dynamicScreen: LinearLayout) {
        var dynamicScreen = dynamicScreen
        LayoutInflater.from(context).inflate(R.layout.current_bazaar_trackers_screen, this, true)
        dynamicScreen = findViewById<LinearLayout?>(R.id.tracker_list)
        val addNewTrackerButton = findViewById<Button>(R.id.add_new_tracker_button)
        addNewTrackerButton.setOnClickListener(OnClickListener { v: View? ->
            context.setContentView(CreateTrackerScreen(core, this))
        })

        val trackedItems = core.bazaarService().trackedItems
        for (item in trackedItems) {
            val trackerItemLayout = LinearLayout(getContext())
            trackerItemLayout.setOrientation(HORIZONTAL)
            trackerItemLayout.setPadding(8, 8, 8, 8)

            val enabledCheckbox = CheckBox(getContext())
            enabledCheckbox.setChecked(item.isEnabled())
            enabledCheckbox.setOnCheckedChangeListener(CompoundButton.OnCheckedChangeListener { buttonView: CompoundButton?, isChecked: Boolean ->
                item.setEnabled(isChecked)
            })

            val itemName = TextView(getContext())
            itemName.setText(item.getDisplayName())
            itemName.setTextSize(16f)
            val itemNameParams = LayoutParams(0, LayoutParams.WRAP_CONTENT, 1f)
            itemName.setLayoutParams(itemNameParams)
            itemName.setPadding(8, 0, 8, 0)

            val deleteButton = Button(getContext())
            deleteButton.setText(R.string.delete)
            deleteButton.setOnClickListener(OnClickListener { v: View? ->
                trackedItems.remove(item)
                resetDynamicScreen()
            })

            trackerItemLayout.addView(enabledCheckbox)
            trackerItemLayout.addView(itemName)
            trackerItemLayout.addView(deleteButton)

            trackerItemLayout.setOnClickListener(OnClickListener { v: View? ->
                core.context().setContentView(EditTrackerScreen(core, this, item))
            })

            // Add the tracker item layout to the container
            dynamicScreen.addView(trackerItemLayout)
        }
    }

    override fun onPause() {
        // Nothing to pause for this screen
    }

    override fun onResume() {
        // Refresh the screen when resuming to show any changes made in child screens
        updateScreen()
    }

    override fun getDynamicScreen(): LinearLayout? {
        return findViewById<LinearLayout?>(R.id.tracker_list)
    }
}

@SuppressLint("ViewConstructor")
internal class CreateTrackerScreen(core: Core, parent: Screen?) : Screen(core, parent) {
    private var itemIdInput: EditText? = null
    private var itemSuggestions: ListView? = null
    private var notifyGoodChangesCheckbox: CheckBox? = null
    private var trackPriceChangesCheckbox: CheckBox? = null
    private var pendingFilterTask: ScheduledFuture<*>? = null

    init {
        updateScreen()
    }

    private fun setupItemSuggestions() {
        val bazaarResponse: BazaarResponse? = BazaarService.Companion.getLastResponse()
        if (bazaarResponse != null) {
            val suggestions: MutableList<BazaarSuggestionItem?> = ArrayList<BazaarSuggestionItem?>()
            bazaarResponse.getProducts().forEach { (itemId: String?, product: BazaarProduct?) ->
                suggestions.add(BazaarSuggestionItem(itemId, product!!.displayName))
            }

            val adapter = BazaarSuggestionAdapter(context, suggestions)
            itemSuggestions!!.setAdapter(adapter)

            // Handle item selection
            itemSuggestions!!.setOnItemClickListener(OnItemClickListener { parent: AdapterView<*>?, view: View?, position: Int, id: Long ->
                val item = parent!!.getItemAtPosition(position) as BazaarSuggestionItem
                itemIdInput!!.setText(item.itemId)
                itemSuggestions!!.setVisibility(GONE)
            })

            // Filter suggestions as user types
            itemIdInput!!.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                }

                override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                    // Cancel previous scheduled task to prevent memory leaks and task accumulation
                    if (pendingFilterTask != null && !pendingFilterTask!!.isDone()) {
                        pendingFilterTask!!.cancel(false)
                    }
                    pendingFilterTask = core.executionService().schedule(Runnable {
                        post(Runnable {
                            val filter = s.toString().lowercase(Locale.getDefault())
                            // Use regular stream instead of parallelStream to avoid potential thread leaks
                            val filtered: MutableList<BazaarSuggestionItem?> = ArrayList<BazaarSuggestionItem?>()
                            for (entry in bazaarResponse.getProducts().entries) {
                                val itemId = entry.key.lowercase(Locale.getDefault())
                                val displayName = entry.value.displayName.lowercase(Locale.getDefault())

                                if (itemId.contains(filter) || displayName.contains(filter)) {
                                    filtered.add(BazaarSuggestionItem(entry.key, entry.value.displayName))
                                }
                            }

                            val filteredAdapter = BazaarSuggestionAdapter(context, filtered)
                            itemSuggestions!!.setAdapter(filteredAdapter)
                            itemSuggestions!!.setVisibility(VISIBLE)
                        })
                    }, 300, TimeUnit.MILLISECONDS)
                }

                override fun afterTextChanged(s: Editable?) {
                }
            })
        }
    }

    override fun close() {
        // Cancel any pending filter task to prevent memory leaks
        if (pendingFilterTask != null && !pendingFilterTask!!.isDone()) {
            pendingFilterTask!!.cancel(false)
        }
        super.close()
    }

    override fun onPause() {
        // Cancel pending tasks when pausing
        if (pendingFilterTask != null && !pendingFilterTask!!.isDone()) {
            pendingFilterTask!!.cancel(false)
        }
    }

    override fun onResume() {
        // Nothing to resume
    }

    private fun createTracker() {
        val itemId = itemIdInput!!.getText().toString().trim { it <= ' ' }

        if (itemId.isEmpty()) {
            Toast.makeText(context, "Please enter a valid item ID", Toast.LENGTH_SHORT).show()
            return
        }

        // Get tracking type
        val trackTypeGroup = findViewById<RadioGroup>(R.id.track_type_group)
        val trackType = if (trackTypeGroup.getCheckedRadioButtonId() == R.id.track_buy) OfferType.INSTANT_BUY else OfferType.INSTANT_SELL

        // Create new tracker
        val tracker = TrackedBazaarItem(itemId, trackType)

        // Set options
        tracker.setTrackPriceChanges(trackPriceChangesCheckbox!!.isChecked())
        tracker.setNotifyGoodChanges(notifyGoodChangesCheckbox!!.isChecked())
        tracker.setInformAboutOrderAttachments((findViewById<View?>(R.id.inform_order_attachments) as CheckBox).isChecked())
        tracker.setShowInOrderScreen((findViewById<View?>(R.id.show_in_order_screen) as CheckBox).isChecked())
        tracker.setEnabled(true)

        // Add tracker to service
        core.bazaarService().trackedItems.add(tracker)

        Toast.makeText(context, "Tracker created for " + itemId, Toast.LENGTH_SHORT).show()

        // Return to parent screen and refresh it (so newly created tracker appears in order screen immediately)
        core.context().setContentView(parent)
        if (parent is Screen) {
            parent.updateScreen()
        }
    }

    override fun inflateLayouts() {
        LayoutInflater.from(context).inflate(R.layout.create_bazaar_tracker, this, true)
    }

    override fun updateScreen(dynamicScreen: LinearLayout?) {
        // Initialize views
        itemIdInput = findViewById<EditText>(R.id.item_id_input)
        itemSuggestions = findViewById<ListView>(R.id.item_suggestions)
        notifyGoodChangesCheckbox = findViewById<CheckBox>(R.id.notify_good_changes)
        trackPriceChangesCheckbox = findViewById<CheckBox>(R.id.track_price_changes)

        // Set up item suggestions with custom adapter
        setupItemSuggestions()

        // Ensure the "show in order screen" checkbox defaults to true so newly created trackers are visible
        val showInOrderCheckbox = findViewById<CheckBox?>(R.id.show_in_order_screen)
        if (showInOrderCheckbox != null) {
            showInOrderCheckbox.setChecked(true)
        }
        // Set up dependent checkboxes
        trackPriceChangesCheckbox!!.setOnCheckedChangeListener(CompoundButton.OnCheckedChangeListener { buttonView: CompoundButton?, isChecked: Boolean ->
            if (!isChecked) {
                notifyGoodChangesCheckbox!!.setChecked(false)
                notifyGoodChangesCheckbox!!.setEnabled(false)
            } else {
                notifyGoodChangesCheckbox!!.setEnabled(true)
            }
        })

        val createTrackerButton = findViewById<Button>(R.id.create_tracker_button)
        createTrackerButton.setOnClickListener(OnClickListener { v: View? -> createTracker() })
    }

    override fun getDynamicScreen(): LinearLayout? {
        return null
    }
}

@SuppressLint("ViewConstructor")
internal class EditTrackerScreen(core: Core, parent: Screen?, trackedItem: TrackedBazaarItem) : Screen(core, parent) {
    private val trackedItem: TrackedBazaarItem
    private var itemIdInput: EditText? = null
    private var itemSuggestions: ListView? = null
    private var notifyGoodChangesCheckbox: CheckBox? = null
    private var trackPriceChangesCheckbox: CheckBox? = null
    private var pendingFilterTask: ScheduledFuture<*>? = null

    init {
        this.trackedItem = trackedItem
        updateScreen()
    }

    private fun addTrackingTypeSelector(container: ViewGroup) {
        val title = TextView(context)
        title.setText("Tracking Type")
        title.setPadding(0, 16, 0, 8)

        val group = RadioGroup(context)
        group.setId(generateViewId())
        group.setOrientation(HORIZONTAL)

        val buyButton = RadioButton(context)
        buyButton.setId(generateViewId())
        buyButton.setText("Instant Buy")
        buyButton.setChecked(trackedItem.trackType == OfferType.INSTANT_BUY)

        val sellButton = RadioButton(context)
        sellButton.setId(generateViewId())
        sellButton.setText("Instant Sell")
        sellButton.setChecked(trackedItem.trackType == OfferType.INSTANT_SELL)

        group.addView(buyButton)
        group.addView(sellButton)

        container.addView(title)
        container.addView(group)
    }

    private fun addOptionWithDescription(container: ViewGroup, title: String?, description: String?, checked: Boolean): CheckBox {
        val checkBox = CheckBox(context)
        checkBox.setId(generateViewId())
        checkBox.setText(title)
        checkBox.setChecked(checked)

        val descText = TextView(context)
        descText.setText(description)
        descText.setTextSize(12f)
        descText.setPadding(32, 0, 0, 8)

        container.addView(checkBox)
        container.addView(descText)

        return checkBox
    }

    override fun close() {
        // Cancel any pending filter task to prevent memory leaks
        if (pendingFilterTask != null && !pendingFilterTask!!.isDone()) {
            pendingFilterTask!!.cancel(false)
        }
        super.close()
    }

    override fun onPause() {
        // Cancel pending tasks when pausing
        if (pendingFilterTask != null && !pendingFilterTask!!.isDone()) {
            pendingFilterTask!!.cancel(false)
        }
    }

    override fun onResume() {
        // Nothing to resume
    }

    private fun setupItemSuggestions() {
        val bazaarResponse: BazaarResponse? = BazaarService.lastResponse
        if (bazaarResponse != null) {
            val suggestions: MutableList<BazaarSuggestionItem> = ArrayList<BazaarSuggestionItem>()
            bazaarResponse.products.forEach { (itemId: String?, product: BazaarProduct?) ->
                suggestions.add(BazaarSuggestionItem(itemId, product!!.displayName))
            }

            val adapter = BazaarSuggestionAdapter(context, suggestions)
            itemSuggestions!!.setAdapter(adapter)

            // Handle item selection
            itemSuggestions!!.setOnItemClickListener(OnItemClickListener { parent: AdapterView<*>?, view: View?, position: Int, id: Long ->
                val item = parent!!.getItemAtPosition(position) as BazaarSuggestionItem
                itemIdInput!!.setText(item.itemId)
                itemSuggestions!!.setVisibility(GONE)
            })

            // Filter suggestions as user types (with debouncing to prevent excessive updates)
            itemIdInput!!.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                }

                override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                    // Cancel previous scheduled task to prevent memory leaks
                    if (pendingFilterTask != null && !pendingFilterTask!!.isDone()) {
                        pendingFilterTask!!.cancel(false)
                    }

                    pendingFilterTask = core.executionService().schedule(Runnable {
                        post(Runnable {
                            val filter = s.toString().lowercase(Locale.getDefault())
                            val filtered: MutableList<BazaarSuggestionItem> = ArrayList<BazaarSuggestionItem>()

                            for (entry in bazaarResponse.products.entries) {
                                val itemId = entry.key.lowercase(Locale.getDefault())
                                val displayName = entry.value.displayName.lowercase(Locale.getDefault())

                                if (itemId.contains(filter) || displayName.contains(filter)) {
                                    filtered.add(BazaarSuggestionItem(entry.key, entry.value.displayName))
                                }
                            }

                            val filteredAdapter = BazaarSuggestionAdapter(context, filtered)
                            itemSuggestions!!.setAdapter(filteredAdapter)
                            itemSuggestions!!.setVisibility(VISIBLE)
                        })
                    }, 300, TimeUnit.MILLISECONDS)
                }

                override fun afterTextChanged(s: Editable?) {
                }
            })
        }
    }

    private fun saveChanges() {
        // Find all option checkboxes by their container
        val container = findViewById<View?>(R.id.done_button).getParent() as ViewGroup

        // Update tracking type
        val trackTypeGroup = findFirstViewOfType<RadioGroup?>(container, RadioGroup::class.java)
        if (trackTypeGroup != null) {
            for (i in 0..<trackTypeGroup.getChildCount()) {
                val button = trackTypeGroup.getChildAt(i) as RadioButton
                if (button.isChecked()) {
                    trackedItem.trackType =
                        if (button.getText().toString() == "Instant Buy") OfferType.INSTANT_BUY else OfferType.INSTANT_SELL
                    break
                }
            }
        }

        // Update other options
        trackedItem.setTrackPriceChanges(trackPriceChangesCheckbox!!.isChecked())
        trackedItem.setNotifyGoodChanges(notifyGoodChangesCheckbox!!.isChecked())
        trackedItem.setInformAboutOrderAttachments(
            findCheckBoxByLabel(container, "Inform about order attachments")!!.isChecked()
        )
        trackedItem.setShowInOrderScreen(
            findCheckBoxByLabel(container, "Show in order screen")!!.isChecked()
        )

        Toast.makeText(context, "Tracker updated", Toast.LENGTH_SHORT).show()
        context.setContentView(parent)
    }

    private fun <T : View?> findFirstViewOfType(root: ViewGroup, type: Class<T?>): T? {
        for (i in 0..<root.getChildCount()) {
            val child = root.getChildAt(i)
            if (type.isInstance(child)) {
                return child as T
            }
            if (child is ViewGroup) {
                val result = findFirstViewOfType<T?>(child, type)
                if (result != null) {
                    return result
                }
            }
        }
        return null
    }

    private fun findCheckBoxByLabel(root: ViewGroup, label: String?): CheckBox? {
        for (i in 0..<root.getChildCount()) {
            val child = root.getChildAt(i)
            if (child is CheckBox && child.getText().toString() == label) {
                return child
            }
            if (child is ViewGroup) {
                val result = findCheckBoxByLabel(child, label)
                if (result != null) {
                    return result
                }
            }
        }
        return null
    }

    override fun inflateLayouts() {
        LayoutInflater.from(context).inflate(R.layout.edit_bazaar_tracker, this, true)
    }

    override fun updateScreen(dynamicScreen: LinearLayout?) {
// Initialize views
        itemIdInput = findViewById<EditText>(R.id.edit_item_id_input)
        itemSuggestions = findViewById<ListView>(R.id.edit_item_suggestions)

        // Set existing item ID
        itemIdInput!!.setText(trackedItem.itemId)

        // Add options with descriptions
        val container = LinearLayout(context)
        container.setOrientation(VERTICAL)
        container.setLayoutParams(
            LayoutParams(
                LayoutParams.MATCH_PARENT,
                LayoutParams.WRAP_CONTENT
            )
        )

        // Add tracking type selector
        addTrackingTypeSelector(container)

        // Add option checkboxes with descriptions
        trackPriceChangesCheckbox = addOptionWithDescription(
            container,
            "Track price changes",
            "Inform about price changes with notifications",
            trackedItem.trackPriceChanges()
        )

        notifyGoodChangesCheckbox = addOptionWithDescription(
            container,
            "Notify about beneficial changes",
            "Inform about order cancellations or orders being fully filled",
            trackedItem.isNotifyGoodChanges()
        )

        addOptionWithDescription(
            container,
            "Inform about order attachments",
            "Inform when amount increases compared to last check",
            trackedItem.isInformAboutOrderAttachments()
        )

        addOptionWithDescription(
            container,
            "Show in order screen",
            "Display this tracker in the Bazaar Order Screen",
            trackedItem.showInOrderScreen()
        )

        // Set up dependency between options
        trackPriceChangesCheckbox!!.setOnCheckedChangeListener(CompoundButton.OnCheckedChangeListener { buttonView: CompoundButton?, isChecked: Boolean ->
            if (!isChecked) {
                notifyGoodChangesCheckbox!!.setChecked(false)
                notifyGoodChangesCheckbox!!.setEnabled(false)
            } else {
                notifyGoodChangesCheckbox!!.setEnabled(true)
            }
        })

        // Ensure initial state is correct
        if (!trackPriceChangesCheckbox!!.isChecked()) {
            notifyGoodChangesCheckbox!!.setEnabled(false)
        }

        // Add container before done button
        addView(container, indexOfChild(findViewById<View?>(R.id.done_button)))

        // Set up item suggestions with filter
        setupItemSuggestions()

        // Set up done button
        val doneButton = findViewById<Button>(R.id.done_button)
        doneButton.setOnClickListener(OnClickListener { v: View? -> saveChanges() })
    }

    override fun getDynamicScreen(): LinearLayout? {
        return null
    }
}

