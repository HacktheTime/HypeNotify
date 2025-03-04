package de.hype.hypenotify.tools.bazaar;

import de.hype.hypenotify.skyblockconstants.SBCollections;
import org.jetbrains.annotations.Nullable;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class TrackedBazaarItem {
    public final String itemId;
    public final BazaarProduct.OfferType trackType;
    private BazaarProduct lastTime;
    /**
     * inform about changes that are beneficial to you. for example if a order gets filled or canceled.
     */
    private boolean notifyGoodChanges = true;
    private boolean informAboutOrderAttachments = false;
    private boolean trackPriceChanges = true;
    private boolean showInOrderScreen = true;
    private boolean enabled = true;
    public static final NumberFormat amountFormat;
    public static final NumberFormat priceFormat;

    static {
        amountFormat = NumberFormat.getInstance(java.util.Locale.US);
        priceFormat = NumberFormat.getInstance(java.util.Locale.US);
        priceFormat.setMinimumFractionDigits(1);
        priceFormat.setMaximumFractionDigits(1);
    }

    public boolean showInOrderScreen() {
        return enabled && showInOrderScreen;
    }

    public boolean trackPriceChanges() {
        return enabled && trackPriceChanges;
    }

    public TrackedBazaarItem(String itemId, BazaarProduct.OfferType trackType) {
        this.itemId = itemId;
        this.trackType = trackType;
    }

    public int maxOrdersToTrack() {
        return 3;
    }

    public TrackChanges checkForChanges(BazaarProduct newProduct) {
        if (lastTime == null) {
            lastTime = newProduct;
            return null;
        }
        BazaarProduct oldProduct = lastTime;
        lastTime = newProduct;
        return new TrackChanges(oldProduct, newProduct);
    }

    public String getDisplayName() {
        String name = itemId;
        if (!name.contains(":")) return name.replace("_", " ").toUpperCase(Locale.US);
        return SBCollections.getNameFromID(name).replace("_", " ").toUpperCase(Locale.US);
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean newState) {
        enabled = newState;
    }

    public class TrackChanges {
        private final BazaarProduct oldProduct, newProduct;
        private String notificationText;

        public TrackChanges(BazaarProduct oldProduct, BazaarProduct newProduct) {
            this.oldProduct = oldProduct;
            this.newProduct = newProduct;
            proccessChanges();
        }

        private void proccessChanges() {
            StringBuilder notificationTextBuilder = new StringBuilder();
            double priceChange = newProduct.getBestPrice(trackType) - oldProduct.getBestPrice(trackType);
            if (Math.abs(priceChange) < 0.04) priceChange = 0;
            if (trackType == BazaarProduct.OfferType.INSTANT_BUY) {
                if (priceChange > 0) {
                    if (notifyGoodChanges) {
                        notificationTextBuilder.append("(good) Order got filled / canceled.\n");
                    }
                } else if (priceChange < 0) {
                    notificationTextBuilder.append("(bad) You got underbid!\n");
                }
            } else if (trackType == BazaarProduct.OfferType.INSTANT_SELL) {
                if (priceChange > 0) {
                    notificationTextBuilder.append("(bad) You got overbid!\n");
                } else if (priceChange < 0) {
                    if (notifyGoodChanges) {
                        notificationTextBuilder.append("(good) Order got filled / canceled.\n");
                    }
                }
            }

            if (priceChange == 0 && informAboutOrderAttachments) {
                List<BazaarProduct.Offer> oldOffers = oldProduct.getOfferType(trackType);
                List<BazaarProduct.Offer> newOffers = newProduct.getOfferType(trackType);
                if (!oldOffers.isEmpty() && !newOffers.isEmpty()) {
                    BazaarProduct.Offer oldBest = oldOffers.get(0);
                    BazaarProduct.Offer newBest = newOffers.get(0);
                    if (oldBest.amount() < newBest.amount() || oldBest.orders() < newBest.orders()) {
                        notificationTextBuilder.append("Someone attached an order to the best offer!\n (%s -> %s = %s items)\n".formatted(amountFormat.format(oldBest), amountFormat.format(newBest), amountFormat.format(newBest.amount() - oldBest.amount())));
                    }
                }
            }
            if (!notificationTextBuilder.isEmpty()) {
                List<BazaarProduct.Offer> offers = newProduct.getOfferType(trackType);
                BazaarProduct.Offer bestOffer = null;
                if (!offers.isEmpty()) bestOffer = offers.getFirst();
                String bestOrderStatus = bestOffer == null ? "No orders" : "%s for %s each".formatted(amountFormat.format(bestOffer.amount()), priceFormat.format(bestOffer.pricePerUnit()));
                this.notificationText = """
                        Item: %s
                        %s
                        Best Order Status:
                        %s
                        """.formatted(newProduct.getDisplayName(), notificationTextBuilder.toString(), bestOrderStatus);
            }
        }

        /**
         * Return the list of orders that shall be displayed in the table. null means not shown. if list is empty it is shown that there are no orders!
         */
        @Nullable
        public List<BazaarProduct.Offer> getOfferTableValues() {
            List<BazaarProduct.Offer> offers = newProduct.getOfferType(trackType);
            offers.subList(0, Math.min(offers.size(), maxOrdersToTrack()));
            return offers;
        }

        /**
         * @return Return the Text that should be displayed in the notification. {@code null} if no notification should be sent.
         */
        @Nullable
        public String getNotificationText() {
            return notificationText;
        }
    }
}
