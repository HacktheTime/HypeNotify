package de.hype.bingonet.server

import ColorSerializer
import android.graphics.Color
import com.google.gson.*
import com.google.gson.annotations.Expose
import de.hype.hypenotify.core.interfaces.Core
import de.hype.hypenotify.shared.notification.NotificationContent
import de.hype.hypenotify.shared.utils.FormattingUtils
import java.lang.reflect.Type
import java.time.Duration
import java.time.Instant
import java.time.ZoneId
import java.util.concurrent.ScheduledFuture

object CustomGson {
    var ownSerializer: Gson = GsonBuilder().create()
    var pretty: Gson? = null
    var nonPretty: Gson? = null

    fun create(): Gson {
        if (pretty == null) {
            pretty = base.setPrettyPrinting().create()
        }
        return pretty!!
    }

    fun createNotPrettyPrinting(): Gson {
        if (nonPretty == null) {
            nonPretty = base.create()
        }
        return nonPretty!!
    }

    private val base: GsonBuilder
        get() = GsonBuilder()
            .registerTypeAdapter(Color::class.java, ColorSerializer())
            .registerTypeAdapter(Instant::class.java, InstantSerializer())
            .registerTypeAdapter(NotificationContent::class.java, NotificationContentSerializer())
            .registerTypeAdapter(Duration::class.java, DurationSerializer())
            .addDeserializationExclusionStrategy(ExclusionStrategy(false))
            .addSerializationExclusionStrategy(ExclusionStrategy(true))

    class ExclusionStrategy(private val forSerialization: Boolean) : com.google.gson.ExclusionStrategy {
        override fun shouldSkipField(f: FieldAttributes): Boolean {
            val expose = f.getAnnotation<Expose?>(Expose::class.java)
            if (expose != null) {
                // Check the context and the @Expose parameters
                if (forSerialization) {
                    return !expose.serialize // Skip if serialize is false
                } else {
                    return !expose.deserialize // Skip if deserialize is false
                }
            }
            return false // Skip if no @Expose annotation
        }

        override fun shouldSkipClass(clazz: Class<*>): Boolean {
            return (forSerialization && clazz == Core::class.java) || clazz.isNestmateOf(ScheduledFuture::class.java) || clazz.isNestmateOf(
                Runnable::class.java
            )
        }
    }



    private class InstantSerializer : JsonSerializer<Instant>, JsonDeserializer<Instant> {
        @Throws(JsonParseException::class)
        override fun deserialize(json: JsonElement, typeOfT: Type?, context: JsonDeserializationContext?): Instant? {
            if (json.isJsonObject()) {
                val jsonObject = json.getAsJsonObject()
                val epochSecond = jsonObject.get("epochSecond").getAsLong()
                val nanoAdjustment = jsonObject.get("nanoAdjustment").getAsInt()
                return Instant.ofEpochSecond(epochSecond, nanoAdjustment.toLong())
            } else {
                val epochMilli = json.getAsLong()
                return Instant.ofEpochMilli(epochMilli)
            }
        }

        override fun serialize(src: Instant, typeOfSrc: Type?, context: JsonSerializationContext?): JsonElement {
            val jsonObject = JsonObject()
            jsonObject.addProperty("epochSecond", src.getEpochSecond())
            jsonObject.addProperty("nanoAdjustment", src.getNano())
            jsonObject.addProperty("toString", src.atZone(ZoneId.of("UTC")).toString())
            return jsonObject
        }
    }

    private class DurationSerializer : JsonSerializer<Duration>, JsonDeserializer<Duration> {
        @Throws(JsonParseException::class)
        override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): Duration {
            if (json.isJsonObject()) {
                val jsonObject = json.getAsJsonObject()
                val seconds = jsonObject.get("seconds").getAsLong()
                val nanoAdjustment = jsonObject.get("nanoAdjustment").getAsInt()
                return Duration.ofSeconds(seconds, nanoAdjustment.toLong())
            } else {
                val millis = json.getAsLong()
                return Duration.ofMillis(millis)
            }
        }

        override fun serialize(src: Duration, typeOfSrc: Type, context: JsonSerializationContext): JsonElement {
            val jsonObject = JsonObject()
            jsonObject.addProperty("seconds", src.getSeconds())
            jsonObject.addProperty("nanoAdjustment", src.getNano())
            jsonObject.addProperty("toString", FormattingUtils.formatTime(src))
            return jsonObject
        }
    }
}