package de.hype.hypenotify.app.tools.timers;

import com.google.gson.*;

import java.lang.reflect.Type;
import java.time.Instant;

/**
 * Gson Adapter for handling Instant serialization & deserialization.
 */
class InstantAdapter implements JsonSerializer<Instant>, JsonDeserializer<Instant> {
    @Override
    public JsonElement serialize(Instant src, Type typeOfSrc, JsonSerializationContext context) {
        return new JsonPrimitive(src.getEpochSecond());
    }

    @Override
    public Instant deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        return Instant.ofEpochSecond(json.getAsLong());
    }
}