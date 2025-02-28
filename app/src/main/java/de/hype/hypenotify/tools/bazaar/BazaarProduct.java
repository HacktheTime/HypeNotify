package de.hype.hypenotify.tools.bazaar;

import com.google.gson.annotations.SerializedName;
import de.hype.hypenotify.PrivateConfig;
import de.hype.hypenotify.skyblockconstants.SBCollections;
import org.jetbrains.annotations.NotNull;

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
    public double getBestPriceTaxed(@NotNull OfferType type) {
        return getBestPrice(type) * (PrivateConfig.BAZAAR_TAX_RATE + 1);
    }

    public double getBestPrice(@NotNull OfferType type) {
        double price = 0D;
        if (type == OfferType.INSTANT_BUY) {
            price = quickStatus.buyPrice;
        } else if (type == OfferType.INSTANT_SELL) {
            price = quickStatus.sellPrice;
        }
        return price;
    }

    public String getProductId() {
        return productId;
    }

    public QuickStatus getQuickStatus() {
        return quickStatus;
    }

    public record QuickStatus(
            double sellPrice,
            Long sellOrders,
            double buyPrice,
            Long buyOrders,
            Long buyMovingWeek,
            Long sellMovingWeek,
            Long buyVolume,
            Long sellVolume) {
    }

    public record Offer(int amount, double pricePerUnit, int orders) {
    }

    public enum OfferType {
        INSTANT_SELL,
        INSTANT_BUY
    }

    public String getDisplayName(){
        String name = getProductId();
        if (!name.contains(":")) return name.replace("_", " ").toLowerCase(Locale.US);
        return SBCollections.getNameFromID(name).replace("_", " ").toLowerCase(Locale.US);
    }
}

