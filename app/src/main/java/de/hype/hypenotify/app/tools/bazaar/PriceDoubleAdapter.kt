package de.hype.hypenotify.app.tools.bazaar

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonParseException
import java.lang.reflect.Type

class PriceDoubleAdapter : JsonDeserializer<Double?> {
    @Throws(JsonParseException::class)
    override fun deserialize(json: JsonElement, typeOfT: Type?, context: JsonDeserializationContext?): Double {
        return json.getAsJsonPrimitive().getAsString().toDouble()
    }
}
