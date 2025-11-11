package de.hype.hypenotify.app.tools.bazaar

import android.util.Log
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import de.hype.hypenotify.app.core.StaticIntents
import de.hype.hypenotify.app.core.interfaces.MiniCore
import de.hype.hypenotify.app.tools.bazaar.BazaarProduct.OfferType
import de.hype.hypenotify.app.tools.notification.GroupBehaviour
import de.hype.hypenotify.app.tools.notification.NotificationBuilder
import de.hype.hypenotify.app.tools.notification.NotificationChannels
import de.hype.hypenotify.app.tools.notification.NotificationVisibility
import java.io.EOFException
import java.io.IOException
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.SocketTimeoutException
import java.net.URL
import java.net.UnknownHostException
import java.time.Duration
import java.time.Instant
import java.util.List
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit
import kotlin.concurrent.Volatile

class BazaarService(private val core: MiniCore) {
    var trackedItems: MutableList<TrackedBazaarItem> = ArrayList<TrackedBazaarItem>()

    // Flag to suppress the very next notification that would be sent after a manual update
    @Volatile
    private var suppressNextNotification = false

    init {
        initTrackedItems()
        orderTracker = OrderTrackingService(core, this)
    }

    val checkInterval: Int
        get() {
            val isInFreeNetwork = core.isInFreeNetwork
            if (isInFreeNetwork) {
                return 15
            } else {
                return core.config().bazaarCheckerNoWlanDelaySeconds
            }
        }

    private fun initTrackedItems() {
        val addToTrack = listOf(
            TrackedBazaarItem("ENCHANTED_REDSTONE_LAMP", OfferType.INSTANT_BUY)
        )
        for (item in addToTrack) {
            trackedItems.add(item)
        }
    }

    @Throws(IOException::class)
    fun update() {
        fetchBazaar()
    }

    @Throws(IOException::class)
    fun getMaxAgeResponse(maxAge: Duration): BazaarResponse? {
        if (lastResponse == null || lastUpdate!!.plusSeconds(maxAge.getSeconds()).isBefore(Instant.now())) fetchBazaar()
        return lastResponse
    }

    /**
     * Returns the last fetched Bazaar response if it is not older than the given maxAge.
     *
     *
     * / **
     * Manual immediate update. If suppressNotification is true, the next notification that would be
     * generated because of this update will be suppressed (no sound/alert).
     */
    @Throws(IOException::class)
    fun updateNow(suppressNotification: Boolean) {
        // set suppression for this manual run
        this.suppressNextNotification = suppressNotification
        fetchBazaar()
        // run a check immediately so any notifications related to this update happen now (and will be suppressed if requested)
        if (orderTracker != null) orderTracker.checkPrice()
    }

    @get:Throws(IOException::class) val maxAgeResponse: BazaarResponse?
        get() {
            val delay = this.checkInterval
            if (delay == null) {
                return null
            }
            return getMaxAgeResponse(Duration.ofSeconds((delay - 1).toLong()))
        }

    /**
     * Pause any active tracking (called when low battery detected). This cancels scheduled checks but keeps the service alive.
     */
    fun pauseTrackingForLowBattery() {
        if (orderTracker != null) orderTracker!!.stop()
    }

    /**
     * Resume tracking after battery recovered.
     */
    fun resumeTrackingAfterBatteryOK() {
        if (orderTracker == null) {
            orderTracker = OrderTrackingService(core, this)
        } else {
            orderTracker!!.start()
        }
    }

    class OrderTrackingService(private val core: MiniCore, private val bazaarService: BazaarService) {
        private var checkWifiStateCounter = 0
        private var nextCheck: ScheduledFuture<*>? = null

        init {
            // Bazaar Service is needed since this is called from within ints constructor meaning that it is still null in core
            start()
        }

        fun stop() {
            if (nextCheck != null) {
                nextCheck!!.cancel(false)
            }
        }

        fun start() {
            core.executionService().execute(Runnable {
                checkPrice()
                registerNextCheck()
            })
        }

        private fun registerNextCheck() {
            val timeBetweenChecks = bazaarService.checkInterval
            if (timeBetweenChecks == null) {
                checkWifiStateCounter++
                if (checkWifiStateCounter >= 20) {
                    if (!core.isInFreeNetwork()) {
                        val notificationBuilder =
                            NotificationBuilder(
                                core.context(),
                                "Bazaar Price Checker",
                                "You are no longer in a Wifi. Tracking stopped!",
                                NotificationChannels.BAZAAR_TRACKER
                            )
                        notificationBuilder.send()
                        return
                    }
                }
                return
            }
            checkWifiStateCounter = 0
            nextCheck = core.executionService().schedule(Runnable {
                try {
                    checkPrice()
                } catch (e: Exception) {
                    Log.e("BazaarService", "Error checking price in OrderTrackingService", e)
                } finally {
                    // Always reschedule, even if checkPrice fails
                    registerNextCheck()
                }
            }, timeBetweenChecks.toLong(), TimeUnit.SECONDS)
        }

        private fun checkPrice() {
            try {
                val response = bazaarService.maxAgeResponse
                if (response == null) return
                val items = response.products
                for (toTrackItem in bazaarService.trackedItems) {
                    if (!toTrackItem.trackPriceChanges()) continue
                    val product = items.get(toTrackItem.itemId)
                    if (product == null) {
                        continue
                    }
                    val wrappedChanges = toTrackItem.checkForChanges(product)
                    if (wrappedChanges != null) {
                        val notificationText = wrappedChanges.getNotificationText()
                        if (notificationText != null) {
                            val notificationBuilder =
                                NotificationBuilder(
                                    core.context(),
                                    "Bazaar Price Checker",
                                    notificationText,
                                    NotificationChannels.BAZAAR_TRACKER
                                )
                            notificationBuilder.setVisibility(NotificationVisibility.Companion.PUBLIC)
                            notificationBuilder.setAction(StaticIntents.LAUNCH_BAZAAR.getAsIntent(core.context()).getAsPending())
                            notificationBuilder.setAutoCancel(true)
                            notificationBuilder.setAlertOnlyOnce(false)
                            notificationBuilder.setGroupAlertBehaviour(GroupBehaviour.GROUP_ALERT_ALL)
                            notificationBuilder.send()
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    companion object {
        private val gson: Gson = GsonBuilder().registerTypeAdapter(Double::class.java, PriceDoubleAdapter()).create()
        private const val API_URL = "https://api.hypixel.net/v2/skyblock/bazaar"
        var lastResponse: BazaarResponse? = null
            private set
        private var orderTracker: OrderTrackingService?
        private var lastUpdate: Instant? = null

        fun getLastUpdate(): Instant {
            return lastUpdate!!
        }

        private fun fetchBazaar() {
            var connection: HttpURLConnection? = null
            try {
                connection = URL(API_URL).openConnection() as HttpURLConnection?
                connection!!.setRequestMethod("GET")
                connection.setConnectTimeout(5000)
                connection.setReadTimeout(5000)
                connection.getResponseCode()

                InputStreamReader(connection.getInputStream()).use { reader ->
                    lastResponse = gson.fromJson<BazaarResponse?>(reader, BazaarResponse::class.java)
                    lastUpdate = Instant.now()
                }
            } catch (e: SocketTimeoutException) {
                Log.i("BazaarService", "Hypixel BZ Connection Timeout")
            } catch (e: UnknownHostException) {
                Log.i("BazaarService", "Hypixel BZ Connection Unknown Host")
            } catch (e: EOFException) {
                Log.i("BazaarService", "Hypixel BZ Connection EOF")
            } catch (e: IOException) {
                Log.i("BazaarService", "Hypixel BZ Connection Error: " + e.message)
            } catch (e: Throwable) {
                Log.e("BazaarService", "Hypixel BZ Error: ", e)
            } finally {
                // Disconnect connection to free resources
                if (connection != null) {
                    connection.disconnect()
                }
            }
        }
    }
}
