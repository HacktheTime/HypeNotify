package de.hype.hypenotify.app.screen

import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import de.hype.hypenotify.R
import de.hype.hypenotify.app.core.interfaces.Core
import de.hype.hypenotify.layouts.autodetection.LayoutRegistry
import java.util.*

class OverviewScreen(core: Core) : Screen(core, null) {
    private var searchBox: EditText? = null
    private var layoutList: LinearLayout? = null

    init {
        updateScreen()
    }

    private fun updateLayoutList(filter: String) {
        // Clear only dynamic items, preserving the search box.
        val layouts = LayoutRegistry.getAllLayouts()
        for (entry in layouts.entries) {
            if (filter.isEmpty() || entry.key!!.lowercase(Locale.getDefault()).contains(filter.lowercase(Locale.getDefault()))) {
                val layoutItem = TextView(getContext())
                layoutItem.setText(entry.key)
                layoutItem.setOnClickListener(OnClickListener { v: View? -> switchLayout(entry.value!!) })
                layoutItem.setTextSize(20f)
                layoutList!!.addView(layoutItem)
            }
        }
    }

    private fun switchLayout(layoutClass: Class<out Screen>) {
        try {
            val layoutInstance: View = layoutClass.getConstructor(Core::class.java, View::class.java).newInstance(core, this)
            // Create a container for the back button and the dynamic content.
            val container = LinearLayout(context)
            container.setOrientation(VERTICAL)

            // Add the dynamic content to the container.
            container.addView(layoutInstance)

            // Set the container as the content view.
            (getContext() as AppCompatActivity).setContentView(container)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun inflateLayouts() {
        LayoutInflater.from(core.context()).inflate(R.layout.sidebar, this, true)
    }

    override fun updateScreen(dynamicScreen: LinearLayout) {
        searchBox = findViewById<EditText>(R.id.search_box)
        layoutList = findViewById<LinearLayout>(R.id.layout_list)

        searchBox!!.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                updateLayoutList(s.toString())
            }

            override fun afterTextChanged(s: Editable?) {
            }
        })
        updateLayoutList("")
    }

    override fun onPause() {
    }

    override fun onResume() {
    }

    override fun getDynamicScreen(): LinearLayout? {
        return null
    }
}