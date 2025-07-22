package de.hype.hypenotify.environment
//
//import ColorSerializer
//import android.graphics.Color
//import com.google.gson.GsonBuilder
//import de.hype.hypenotify.app.core.interfaces.Core
//
//object CustomGson : BaseCustomGson() {
//    override fun getBase(): GsonBuilder {
//        return super.getBase().registerTypeAdapter(Color::class.java, ColorSerializer())
//    }
//
//    override fun shouldSkipClassExtendable(clazz: Class<*>, forSerialization: Boolean): Boolean {
//        return super.shouldSkipClassExtendable(
//            clazz,
//            forSerialization
//        ) || (forSerialization && clazz == Core::class.java)
//    }
//}