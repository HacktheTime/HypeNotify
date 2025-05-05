package de.hype.hypenotify.environment

import com.google.gson.GsonBuilder
import de.hype.hypenotify.shared.utils.BaseCustomGson
import java.awt.Color
import kotlin.jvm.java

object CustomGson : BaseCustomGson() {
    override fun getBase(): GsonBuilder {
        return super.getBase().registerTypeAdapter(Color::class.java, ColorSerializer())
    }
}