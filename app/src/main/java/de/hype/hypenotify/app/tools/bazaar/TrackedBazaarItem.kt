package de.hype.hypenotify.app.tools.bazaar

import de.hype.hypenotify.app.skyblockconstants.SBCollections
import de.hype.hypenotify.app.tools.bazaar.BazaarProduct.Offer
import de.hype.hypenotify.app.tools.bazaar.BazaarProduct.OfferType
import java.text.NumberFormat
import java.util.*
import kotlin.math.abs
import kotlin.math.min

class TrackedBazaarItem(val itemId: String, var trackType: OfferType) {
    private var lastTime: BazaarProduct? = null

    /**
     * inform about changes that are beneficial to you. for example if a order gets filled or canceled.
     */
    var isNotifyGoodChanges: Boolean = true
    var isInformAboutOrderAttachments: Boolean = false
    private var trackPriceChanges = true
    private var showInOrderScreen = true
    var isEnabled: Boolean = true
    fun showInOrderScreen(): Boolean {
        return this.isEnabled && showInOrderScreen
    }

    fun trackPriceChanges(): Boolean {
        return this.isEnabled && trackPriceChanges
    }

    fun maxOrdersToTrack(): Int {
        return 3
    }

    fun checkForChanges(newProduct: BazaarProduct): TrackChanges? {
        if (lastTime == null) {
            lastTime = newProduct
            return null
        }
        val oldProduct = lastTime
        lastTime = newProduct
        return TrackChanges(oldProduct!!, newProduct)
    }

    val displayName: String
        get() {
            val name = itemId
            if (!name.contains(":")) return name.replace("_", " ").uppercase()
            return SBCollections.Companion.getNameFromID(name).replace("_", " ").uppercase()
        }

    inner class TrackChanges(private val oldProduct: BazaarProduct, private val newProduct: BazaarProduct) {
        /**
         * @return Return the Text that should be displayed in the notification. `null` if no notification should be sent.
         */
        var notificationText: String? = null
            private set

        init {
            proccessChanges()
        }

        private fun proccessChanges() {
            val notificationTextBuilder = StringBuilder()
            var priceChange = newProduct.getBestPrice(trackType)!! - oldProduct.getBestPrice(trackType)!!
            if (abs(priceChange) < 0.04) priceChange = 0.0
            if (trackType == OfferType.INSTANT_BUY) {
                if (priceChange > 0) {
                    if (isNotifyGoodChanges) {
                        notificationTextBuilder.append("(good) Order got filled / canceled.\n")
                    }
                } else if (priceChange < 0) {
                    notificationTextBuilder.append("(bad) You got underbid!\n")
                }
            } else if (trackType == OfferType.INSTANT_SELL) {
                if (priceChange > 0) {
                    notificationTextBuilder.append("(bad) You got overbid!\n")
                } else if (priceChange < 0) {
                    if (isNotifyGoodChanges) {
                        notificationTextBuilder.append("(good) Order got filled / canceled.\n")
                    }
                }
            }

            if (priceChange == 0.0 && isInformAboutOrderAttachments) {
                val oldOffers = oldProduct.getOfferType(trackType)
                val newOffers = newProduct.getOfferType(trackType)
                if (!oldOffers.isEmpty() && !newOffers.isEmpty()) {
                    val oldBest = oldOffers.get(0)
                    val newBest = newOffers.get(0)
                    if (oldBest.amount < newBest.amount || oldBest.orders < newBest.orders) {
                        notificationTextBuilder.append(
                            "Someone attached an order to the best offer!\n (%s -> %s = %s items)\n".formatted(
                                amountFormat.format(oldBest.pricePerUnit),
                                amountFormat.format(newBest.pricePerUnit),
                                amountFormat.format((newBest.amount - oldBest.amount).toLong())
                            )
                        )
                    }
                }
            }
            if (!notificationTextBuilder.isEmpty()) {
                val offers = newProduct.getOfferType(trackType)
                var bestOffer: Offer? = null
                if (!offers.isEmpty()) bestOffer = offers.first()
                val bestOrderStatus =
                    if (bestOffer == null) "No orders" else "%s for %s each".formatted(
                        amountFormat.format(bestOffer.amount.toLong()),
                        priceFormat.format(bestOffer.pricePerUnit)
                    )
                this.notificationText = """
                        Item: %s
                        %s
                        Best Order Status:
                        %s
                        
                        """.trimIndent().formatted(newProduct.displayName, notificationTextBuilder.toString(), bestOrderStatus)
            }
        }

        val offerTableValues: MutableList<Offer>
            /**
             * Return the list of orders that shall be displayed in the table. null means not shown. if list is empty it is shown that there are no orders!
             */
            get() {
                val offers = newProduct.getOfferType(trackType)
                offers.subList(0, min(offers.size, maxOrdersToTrack()))
                return offers
            }
    }

    fun setTrackPriceChanges(trackPriceChanges: Boolean) {
        this.trackPriceChanges = trackPriceChanges
    }

    fun setShowInOrderScreen(showInOrderScreen: Boolean) {
        this.showInOrderScreen = showInOrderScreen
    }

    companion object {
        val amountFormat: NumberFormat
        val priceFormat: NumberFormat

        init {
            amountFormat = NumberFormat.getInstance(Locale.US)
            priceFormat = NumberFormat.getInstance(Locale.US)
            priceFormat.setMinimumFractionDigits(1)
            priceFormat.setMaximumFractionDigits(1)
        }
    }
}
