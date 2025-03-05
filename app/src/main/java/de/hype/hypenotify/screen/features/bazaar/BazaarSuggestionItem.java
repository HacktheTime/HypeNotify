package de.hype.hypenotify.screen.features.bazaar;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import androidx.annotation.NonNull;
import de.hype.hypenotify.R;
import org.jetbrains.annotations.Nullable;

import java.util.List;

class BazaarSuggestionItem {
    final String itemId;
    final String displayName;

    public BazaarSuggestionItem(String itemId, String displayName) {
        this.itemId = itemId;
        this.displayName = displayName;
    }
}

class BazaarSuggestionAdapter extends ArrayAdapter<BazaarSuggestionItem> {

    public BazaarSuggestionAdapter(Context context, List<BazaarSuggestionItem> items) {
        super(context, 0, items);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_bazaar_suggestion, parent, false);
        }

        BazaarSuggestionItem item = getItem(position);

        TextView tvItemName = convertView.findViewById(R.id.tv_item_name);
        TextView tvItemId = convertView.findViewById(R.id.tv_item_id);

        tvItemName.setText(item.displayName);
        tvItemId.setText(item.itemId);

        return convertView;
    }
}