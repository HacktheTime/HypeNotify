package de.hype.hypenotify.app.tools.bazaar

import com.google.gson.annotations.SerializedName
import de.hype.hypenotify.PrivateConfig
import de.hype.hypenotify.app.skyblockconstants.NeuRepoManager
import de.hype.hypenotify.app.skyblockconstants.SBCollections
import io.github.moulberry.repo.data.NEUItem
import io.github.moulberry.repo.data.NEURecipe

class BazaarProduct {
    @SerializedName("product_id")
    val productId: String? = null

    @SerializedName("quick_status")
    val quickStatus: QuickStatus? = null

    @SerializedName("sell_summary")
    private val sellOffers: MutableList<Offer>? = null

    @SerializedName("buy_summary")
    private val buyOffers: MutableList<Offer>? = null

    fun getOfferType(type: OfferType): MutableList<Offer> {
        if (type == OfferType.INSTANT_BUY) {
            return buyOffers!!
        } else {
            return sellOffers!!
        }
    }

    /**
     * @param type The type of offer you want to get the best price for.
     * @return The best price for the given offer type, taxed.
     */
    fun getBestPriceTaxed(type: OfferType): Double? {
        val price = getBestPrice(type)
        if (price == null) return null
        return price * (PrivateConfig.BAZAAR_TAX_RATE + 1)
    }

    /**
     * @param type The type of offer you want to get the best price for.
     * @return The best price for the given offer type. Null if no order.
     */
    fun getBestPrice(type: OfferType): Double? {
        val offers = getOfferType(type)
        if (offers == null || offers.isEmpty()) return null
        return offers.first()?.pricePerUnit
    }

    /**
     * Sell Price and Buy Price are Deprecated because they use a double value which is causing the stuff to be inaccurate. Use the [.getBestPrice] method instead.
     */
    data class QuickStatus(
        val sellPrice: Double?,
        val sellOrders: Long?,
        val buyPrice: Double?,
        val buyOrders: Long?,
        val buyMovingWeek: Long?,
        val sellMovingWeek: Long?,
        val buyVolume: Long?,
        val sellVolume: Long?
    )

    data class Offer(@JvmField val amount: Int, @JvmField val pricePerUnit: Double?, @JvmField val orders: Int)

    enum class OfferType {
        INSTANT_SELL,
        INSTANT_BUY
    }

    val displayName: String
        get() {
            var name = this.productId
            if (name!!.contains(":")) {
                val sbCollection = SBCollections.getNameFromID(name)
                if (sbCollection != null) name = sbCollection
            }
            return name.replace("_", " ").lowercase()
        }


    val maxAmountPerStack by lazy {
        return@lazy 64 //TODO Ill add an impl later
    }

    val canGoIntoSacks: Boolean by lazy {
        return@lazy NeuRepoManager.sackableItems.contains(neuItem)
    }

    val neuItem: NEUItem by lazy {
        return@lazy NeuRepoManager.getItemByProductId(productId!!)
            ?: error("NEUItem not found for productId: $productId")
    }

    val recipies: List<NEURecipe> by lazy {
        neuItem.recipes
    }

    override fun toString(): String {
        return productId ?: "BazaarProduct(productId=null)"
    }
}

