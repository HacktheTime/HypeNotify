package de.hype.hypenotify.tools.bazaar;

import com.google.gson.annotations.SerializedName;
import de.hype.hypenotify.PrivateConfig;
import de.hype.hypenotify.skyblockconstants.SBCollections;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Locale;

public class BazaarProduct {
    @SerializedName("product_id")
    private String productId;
    @SerializedName("quick_status")
    private QuickStatus quickStatus;
    @SerializedName("sell_summary")
    private List<Offer> sellOffers;
    @SerializedName("buy_summary")
    private List<Offer> buyOffers;

    public List<Offer> getOfferType(@NotNull OfferType type) {
        if (type == OfferType.INSTANT_BUY) {
            return buyOffers;
        } else {
            return sellOffers;
        }
    }

    /**
     * @param type The type of offer you want to get the best price for.
     * @return The best price for the given offer type, taxed.
     */
    public Double getBestPriceTaxed(@NotNull OfferType type) {
        Double price = getBestPrice(type);
        if (price == null) return null;
        return price * (PrivateConfig.BAZAAR_TAX_RATE + 1);
    }

    /**
     * @param type The type of offer you want to get the best price for.
     * @return The best price for the given offer type. Null if no order.
     */
    @Nullable
    public Double getBestPrice(@NotNull OfferType type) {
        List<Offer> offers = getOfferType(type);
        if (offers == null || offers.isEmpty()) return null;
        return offers.getFirst().pricePerUnit;
    }

    public String getProductId() {
        return productId;
    }

    public QuickStatus getQuickStatus() {
        return quickStatus;
    }

    /**
     * Sell Price and Buy Price are Deprecated because they use a double value which is causing the stuff to be inaccurate. Use the {@link #getBestPrice(OfferType)} method instead.
     */
    public record QuickStatus(
            @Deprecated
            Double sellPrice,
            Long sellOrders,
            @Deprecated
            Double buyPrice,
            Long buyOrders,
            Long buyMovingWeek,
            Long sellMovingWeek,
            Long buyVolume,
            Long sellVolume) {
    }

    public record Offer(int amount, Double pricePerUnit, int orders) {
    }

    public enum OfferType {
        INSTANT_SELL,
        INSTANT_BUY
    }

    public String getDisplayName() {
        String name = getProductId();
        if (name.contains(":")) {
            String sbCollection = SBCollections.getNameFromID(name);
            if (sbCollection != null) name = sbCollection;
        }
        return name.replace("_", " ").toLowerCase(Locale.US);
    }
}

