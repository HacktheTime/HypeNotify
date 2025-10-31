package de.hype.hypenotify.app.screen.features.bazaar

import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.*
import de.hype.hypenotify.R
import de.hype.hypenotify.app.core.interfaces.Core
import de.hype.hypenotify.app.screen.Screen
import de.hype.hypenotify.app.skyblockconstants.getCost
import de.hype.hypenotify.app.tools.bazaar.BazaarProduct
import de.hype.hypenotify.app.tools.bazaar.BazaarProduct.Offer
import de.hype.hypenotify.app.tools.bazaar.BazaarResponse
import de.hype.hypenotify.app.tools.bazaar.BazaarService
import de.hype.hypenotify.app.tools.bazaar.TrackedBazaarItem
import de.hype.hypenotify.layouts.autodetection.Layout
import java.io.IOException
import java.time.Duration
import java.time.Instant
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit
import kotlin.collections.HashMap
import kotlin.collections.LinkedHashMap
import kotlin.collections.MutableList
import kotlin.collections.MutableMap

@SuppressLint("ViewConstructor")
@Layout(name = "Bazaar Order Tracker")
class BazaarOrdersScreen(core: Core, parent: View?) : Screen(core, parent) {
    private var toggleTrackingButton: Switch? = null
    private var checkNowButton: Button? = null
    private var editTrackersButton: Button? = null
    private var nextCheck: ScheduledFuture<*>? = null
    private var updateLastUpdated: ScheduledFuture<*>? = null
    private var progressBar: ProgressBar? = null
    private var progressBarAnimation: ObjectAnimator? = null
    private val bazaarService: BazaarService
    private val trackedItemLabels: MutableMap<TrackedBazaarItem?, TextView?> = HashMap()
    private val trackedItemTables: MutableMap<TrackedBazaarItem?, TableLayout?> =
        HashMap()
    private val loading: TextView
    private var lastUpdated: TextView? = null

    // Configurable thresholds (could be loaded from settings)
    private val minProfitPercent = 10.0 // x% (example default)
    private val minProfitCoins = 1000.0 // y coins (example default)

    private var suggestedItemsSection: LinearLayout? = null
    private var suggestedInstaSellHeader: TextView? = null
    private var suggestedSellOfferHeader: TextView? = null
    private var suggestedInstaSellList: LinearLayout? = null
    private var suggestedSellOfferList: LinearLayout? = null

    init {
        // Kein manuelles registrieren mehr nötig - passiert automatisch in Screen-Basisklasse
        loading = TextView(context)
        loading.setText(R.string.loading)
        loading.setGravity(Gravity.CENTER)
        bazaarService = core.bazaarService()
        updateScreen()
        core.context().getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        registerNextCheck()
    }

    override fun inflateLayouts() {
        setOrientation(VERTICAL)
        LayoutInflater.from(context).inflate(R.layout.bazaar_orders_screen, this, true)
        val layout = (findViewById<LinearLayout>(R.id.bazaar_item_layout))
        layout.removeAllViews()
        layout.addView(loading)
    }

    override fun updateScreen(dynamicScreen: LinearLayout) {
        toggleTrackingButton = findViewById(R.id.toggle_tracking_button)
        checkNowButton = findViewById(R.id.check_now_button)
        progressBar = findViewById(R.id.next_bazaar_update)
        editTrackersButton = findViewById(R.id.bazaar_edit_trackers)
        lastUpdated = findViewById(R.id.bzaar_order_screen_last_updated)
        val dynamicScreen: LinearLayout = findViewById(R.id.bazaar_item_layout)


        // Set up listeners
        toggleTrackingButton!!.setOnCheckedChangeListener { buttonView: CompoundButton?, isChecked: Boolean ->
            if (!isChecked) {
                if (nextCheck != null) {
                    nextCheck!!.cancel(false)
                }
                if (lastUpdated != null) {
                    updateLastUpdated!!.cancel(false)
                }
                if (progressBarAnimation != null) {
                    progressBarAnimation!!.pause()
                }
                progressBar!!.setProgress(0)
            } else {
                checkPrice()
                registerNextCheck()
            }
        }

        checkNowButton!!.setOnClickListener { v: View? ->
            checkNowButton!!.setText(R.string.checking)
            checkNowButton!!.requestLayout()
            nextCheck!!.cancel(false)
            core.executionService().execute {
                try {
                    checkPrice(bazaarService.getMaxAgeResponse(Duration.ZERO))
                } catch (e: IOException) {
                    e.printStackTrace()
                }
                registerNextCheck()
                post {
                    checkNowButton!!.setText(R.string.check_now)
                    checkNowButton!!.requestLayout()
                }
            }
        }

        editTrackersButton!!.setOnClickListener { v: View? ->
            context.setContentView(CurrentTrackersScreen(core, this))
        }

        progressBar!!.setTooltipText("Time until next Refresh")

        // Add suggested items section at the bottom
        if (suggestedItemsSection == null) {
            suggestedItemsSection = LinearLayout(context)
            suggestedItemsSection!!.setOrientation(VERTICAL)
            suggestedInstaSellHeader = TextView(context)
            suggestedInstaSellHeader!!.setText(R.string.suggested_insta_sell_header)
            suggestedSellOfferHeader = TextView(context)
            suggestedSellOfferHeader!!.setText(R.string.suggested_sell_offer_header)
            suggestedInstaSellList = LinearLayout(context)
            suggestedInstaSellList!!.setOrientation(VERTICAL)
            suggestedSellOfferList = LinearLayout(context)
            suggestedSellOfferList!!.setOrientation(VERTICAL)
            suggestedItemsSection!!.addView(suggestedInstaSellHeader)
            suggestedItemsSection!!.addView(suggestedInstaSellList)
            suggestedItemsSection!!.addView(suggestedSellOfferHeader)
            suggestedItemsSection!!.addView(suggestedSellOfferList)
            dynamicScreen.addView(suggestedItemsSection)
        } else if (suggestedItemsSection!!.parent == null) {
            dynamicScreen.addView(suggestedItemsSection)
        }
    }

    override fun getDynamicScreen(): LinearLayout? {
        dynamicScreen = findViewById(R.id.bazaar_item_layout)
        core.executionService().execute {
            checkPrice()
            post { dynamicScreen.removeView(loading) }
        }
        return dynamicScreen
    }


    private fun registerNextCheck() {
        val timeBetweenChecks = bazaarService.getCheckInterval()
        startProgressBarCountdown(timeBetweenChecks)
        nextCheck = core.executionService().schedule({
            try {
                checkPrice()
            } catch (e: Exception) {
                android.util.Log.e("BazaarOrdersScreen", "Error checking price", e)
            } finally {
                // Always reschedule next check, even if checkPrice fails
                if (toggleTrackingButton?.isChecked == true) {
                    registerNextCheck()
                }
            }
        }, timeBetweenChecks.toLong(), TimeUnit.SECONDS)
        updateLastUpdated = core.executionService().scheduleWithFixedDelay(Runnable {
            try {
                val lastResponseTime = BazaarService.getLastUpdate()
                if (lastResponseTime == null) return@Runnable
                val newText = getContext().getString(R.string.last_updated_s_seconds_ago)
                    .format(Duration.between(lastResponseTime, Instant.now()).getSeconds())
                post {
                    // Only update if text actually changed to avoid unnecessary layout recalculations
                    if (lastUpdated?.text?.toString() != newText) {
                        lastUpdated?.setText(newText)
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("BazaarOrdersScreen", "Error updating last updated time", e)
            }
        }, 0, 1, TimeUnit.SECONDS)
    }

    private fun startProgressBarCountdown(timeBetweenChecks: Int) {
        post {
            if (progressBarAnimation != null) {
                progressBarAnimation!!.cancel()
            }
            val max = (timeBetweenChecks.toLong()) * 1000
            val countDownSteps = 500
            progressBar!!.setMax((max / countDownSteps).toInt())
            progressBar!!.setProgress(progressBar!!.getMax())


            progressBarAnimation = ObjectAnimator.ofInt(progressBar, "progress", progressBar!!.getMax(), 0)
            progressBarAnimation!!.setDuration(max) // Duration of the progressBarAnimation in milliseconds
            progressBarAnimation!!.start()
        }
    }

    private fun checkPrice() {
        try {
            checkPrice(bazaarService.getMaxAgeResponse())
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun checkPrice(response: BazaarResponse?) {
        try {
            if (response == null) {
                post {
                    dynamicScreen.removeAllViews()
                    val info = TextView(context)
                    info.setText(R.string.no_data_tracking_stopped)
                    info.setGravity(Gravity.CENTER)
                    dynamicScreen.addView(info)
                    // Ensure suggestions section is present
                    if (suggestedItemsSection != null && suggestedItemsSection!!.parent == null) {
                        dynamicScreen.addView(suggestedItemsSection)
                    }
                }
                return
            }
            val items = response.getProducts()
            val displayTables = LinkedHashMap<TrackedBazaarItem, MutableList<Offer>>()
            for (toTrackItem in bazaarService.trackedItems) {
                if (!toTrackItem.showInOrderScreen()) continue
                val product = items.get(toTrackItem.itemId)
                if (product == null) {
                    Toast.makeText(
                        context,
                        "Item not found: ${toTrackItem.itemId}. Skipping it",
                        Toast.LENGTH_SHORT
                    ).show()
                    continue
                }
                displayTables[toTrackItem] = product.getOfferType(toTrackItem.trackType)
            }
            post {
                // Remove only tracked item views, keep suggestions section
                dynamicScreen.removeAllViews()
                for (responseEntry in displayTables.sequencedEntrySet()) {
                    val displayName = responseEntry.key!!.getDisplayName()
                    val toDisplayOrders: MutableList<Offer> = responseEntry.value

                    var itemLabel = trackedItemLabels.get(responseEntry.key)
                    if (itemLabel == null) {
                        itemLabel = TextView(context)
                        trackedItemLabels.put(responseEntry.key, itemLabel)
                        itemLabel.setGravity(Gravity.CENTER)
                    }
                    dynamicScreen.addView(itemLabel)
                    itemLabel.setText("Item: %s (%s)\n".format(displayName, responseEntry.key!!.trackType))
                    if (toDisplayOrders.isEmpty()) {
                        itemLabel.append("No orders found")
                        continue
                    }
                    var orderTable = trackedItemTables.get(responseEntry.key)
                    if (orderTable == null) {
                        orderTable = TableLayout(context)
                        orderTable.setStretchAllColumns(true)
                        val tableLayoutParams = LayoutParams(
                            LayoutParams.WRAP_CONTENT,  // Change to MATCH_PARENT
                            LayoutParams.WRAP_CONTENT
                        )
                        tableLayoutParams.gravity = Gravity.CENTER // Ensure gravity is set
                        orderTable.setLayoutParams(tableLayoutParams)
                        trackedItemTables.put(responseEntry.key, orderTable)
                    }
                    dynamicScreen.addView(orderTable)
                    orderTable.removeAllViews()

                    val headerRow = TableRow(context)
                    val headerAmount = TextView(context)
                    headerAmount.setText(R.string.amount)
                    headerAmount.setGravity(Gravity.CENTER) // Center the text
                    val headerCoins = TextView(context)
                    headerCoins.setText(R.string.coins)
                    headerCoins.setGravity(Gravity.CENTER) // Center the text
                    headerRow.addView(headerAmount)
                    headerRow.addView(headerCoins)
                    orderTable.addView(headerRow)

                    for (order in toDisplayOrders) {
                        val dataRow = TableRow(context)
                        val amountCell = TextView(context)
                        amountCell.setText(TrackedBazaarItem.amountFormat.format(order.amount.toLong()))
                        amountCell.setGravity(Gravity.CENTER) // Center the text
                        val coinsCell = TextView(context)
                        coinsCell.setText(TrackedBazaarItem.amountFormat.format(order.pricePerUnit))
                        coinsCell.setGravity(Gravity.CENTER) // Center the text
                        dataRow.addView(amountCell)
                        dataRow.addView(coinsCell)
                        orderTable.addView(dataRow)
                    }
                }
                // Ensure suggestions section is present at the end
                //TODO reenable
//                if (suggestedItemsSection != null && suggestedItemsSection!!.parent != dynamicScreen) {
//                    dynamicScreen.addView(suggestedItemsSection)
//                }
//                updateSuggestedItems()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun updateSuggestedItems() {
        suggestedInstaSellList!!.removeAllViews()
        suggestedSellOfferList!!.removeAllViews()
        val response = bazaarService.getMaxAgeResponse()
        if (response == null) return
        val products = response.getProducts()
        for (product in products.values) {
            // Filter: stackable to 64, all materials sackable, meets profit thresholds
            if (product.maxAmountPerStack < 32) continue
            if (!product.canGoIntoSacks) continue
            val craftCost = product.neuItem.getCost() ?: continue
            val instaSellPrice: Double =
                (if (product.getBestPrice(BazaarProduct.OfferType.INSTANT_SELL) != null) product.getBestPrice(
                    BazaarProduct.OfferType.INSTANT_SELL
                ) else Double.MAX_VALUE)!!
            val sellOfferPrice: Double =
                (if (product.getBestPrice(BazaarProduct.OfferType.INSTANT_BUY) != null) product.getBestPrice(
                    BazaarProduct.OfferType.INSTANT_BUY
                ) else Double.MAX_VALUE)!!
            val profitInstaSell = instaSellPrice - craftCost
            val profitSellOffer = sellOfferPrice - craftCost
            val profitPercentInstaSell = if (craftCost > 0) (profitInstaSell / craftCost) * 100 else 0.0
            val profitPercentSellOffer = if (craftCost > 0) (profitSellOffer / craftCost) * 100 else 0.0
            // Score and filter
            if (profitInstaSell >= minProfitCoins && profitPercentInstaSell >= minProfitPercent) {
                val itemView = TextView(context)
                itemView.text = context.getString(
                    R.string.suggested_item_format,
                    product.displayName,
                    profitInstaSell.toInt(),
                    profitPercentInstaSell.toInt()
                )
                suggestedInstaSellList!!.addView(itemView)
            }
            if (profitSellOffer >= minProfitCoins && profitPercentSellOffer >= minProfitPercent) {
                val itemView = TextView(context)
                itemView.text = context.getString(
                    R.string.suggested_item_format,
                    product.displayName,
                    profitSellOffer.toInt(),
                    profitPercentSellOffer.toInt()
                )
                suggestedSellOfferList!!.addView(itemView)
            }
        }
    }

    override fun close() {
        core.context().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        
        // Cancel all scheduled tasks
        if (nextCheck != null) nextCheck!!.cancel(true)
        if (updateLastUpdated != null) updateLastUpdated!!.cancel(true)
        if (progressBarAnimation != null) progressBarAnimation!!.cancel()
        
        // Clear cached views to prevent memory leaks
        trackedItemLabels.clear()
        trackedItemTables.clear()
        
        super.close()
    }

    override fun onPause() {
        // Timer und Animationen pausieren
        if (nextCheck != null) nextCheck!!.cancel(false)
        if (updateLastUpdated != null) updateLastUpdated!!.cancel(false)
        if (progressBarAnimation != null) progressBarAnimation!!.pause()
    }

    override fun onResume() {
        // Nur neu starten, wenn Tracking aktiv ist
        if (toggleTrackingButton!!.isChecked()) {
            registerNextCheck()
        }
    }

    override fun onSaveState(state: Bundle) {
        // Nur wichtige Daten speichern
        state.putBoolean("tracking_enabled", toggleTrackingButton?.isChecked ?: false)

        val trackedItemIds = bazaarService.trackedItems.map { it.itemId }.toTypedArray()
        state.putStringArray("tracked_items", trackedItemIds)

        android.util.Log.d("BazaarOrdersScreen", "State saved: tracking=${toggleTrackingButton?.isChecked}")
    }

    override fun onRestoreState(state: Bundle) {
        // State wiederherstellen
        val wasTrackingEnabled = state.getBoolean("tracking_enabled", false)

        // Nach Layout-Update anwenden
        post {
            toggleTrackingButton?.isChecked = wasTrackingEnabled

            if (wasTrackingEnabled) {
                // Tracking wieder starten
                checkPrice()
                registerNextCheck()
            }
        }

        android.util.Log.d("BazaarOrdersScreen", "State restored: tracking=$wasTrackingEnabled")
    }

    override fun getScreenId(): String {
        return "bazaar_orders_screen"
    }
}