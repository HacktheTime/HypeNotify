package de.hype.hypenotify.app.screen.features.bazaar

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import de.hype.hypenotify.R
import de.hype.hypenotify.app.skyblockconstants.NeuRepoManager.getItem
import java.security.AccessController.getContext

internal class BazaarSuggestionItem(itemId: String?, displayName: String?) {
    val itemId: String?
    val displayName: String?

    init {
        this.itemId = itemId
        this.displayName = displayName
    }
}

internal class BazaarSuggestionAdapter(context: Context, items: MutableList<BazaarSuggestionItem>) : ArrayAdapter<BazaarSuggestionItem>
(context, 0, items) {
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var convertView = convertView
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_bazaar_suggestion, parent, false)
        }

        val item: BazaarSuggestionItem = getItem(position)!!

        val tvItemName = convertView.findViewById<TextView>(R.id.tv_item_name)
        val tvItemId = convertView.findViewById<TextView>(R.id.tv_item_id)

        tvItemName.setText(item.displayName)
        tvItemId.setText(item.itemId)

        return convertView
    }
}