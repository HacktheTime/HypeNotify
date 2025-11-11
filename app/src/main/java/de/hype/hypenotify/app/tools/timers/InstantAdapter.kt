package de.hype.hypenotify.app.tools.timers

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonParseException
import com.google.gson.JsonPrimitive
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import java.lang.reflect.Type
import java.time.Instant

/**
 * Gson Adapter for handling Instant serialization & deserialization.
 */
internal class InstantAdapter : JsonSerializer<Instant?>, JsonDeserializer<Instant?> {
    override fun serialize(src: Instant, typeOfSrc: Type?, context: JsonSerializationContext?): JsonElement {
        return JsonPrimitive(src.getEpochSecond())
    }

    @Throws(JsonParseException::class)
    override fun deserialize(json: JsonElement, typeOfT: Type?, context: JsonDeserializationContext?): Instant? {
        return Instant.ofEpochSecond(json.getAsLong())
    }
}