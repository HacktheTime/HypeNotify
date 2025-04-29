import android.graphics.Color
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonParseException
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import java.lang.reflect.Type

class ColorSerializer : JsonSerializer<Color>, JsonDeserializer<Color> {
    @Throws(JsonParseException::class)
    override fun deserialize(
        json: JsonElement,
        typeOfT: Type?,
        context: JsonDeserializationContext?
    ): Color {
        val jsonObject = json.getAsJsonObject()
        val red = jsonObject.getAsJsonPrimitive("r").getAsInt().toFloat()
        val green = jsonObject.getAsJsonPrimitive("g").getAsInt().toFloat()
        val blue = jsonObject.getAsJsonPrimitive("b").getAsInt().toFloat()
        val alpha = jsonObject.getAsJsonPrimitive("a").getAsInt().toFloat()

        return Color.valueOf(red, green, blue, alpha)
    }

    override fun serialize(src: Color, typeOfSrc: Type?, context: JsonSerializationContext?): JsonElement {
        val jsonObject = JsonObject()
        jsonObject.addProperty("r", src.red())
        jsonObject.addProperty("g", src.green())
        jsonObject.addProperty("b", src.blue())
        jsonObject.addProperty("a", src.alpha())

        return jsonObject
    }
}
