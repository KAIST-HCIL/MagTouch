package kaist.hcil.magtouch;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.wear.widget.WearableRecyclerView;

import java.util.ArrayList;

/**
 * References:
 *
 * [recycle view] https://developer.android.com/guide/topics/ui/layout/recyclerview
 * [callback trick] https://stackoverflow.com/questions/51716173/wearablerecyclerview-android-wear-os
 */

public class MainMenuAdapter extends WearableRecyclerView.Adapter<MainMenuAdapter.MainMenuViewHolder> {
    private ArrayList<String> textData;
    private AdapterCallback callback;

    public interface AdapterCallback
    {
        void onItemClicked(int idx);
    }


    public MainMenuAdapter(ArrayList<String> data, AdapterCallback callback)
    {
        textData = data;
        this.callback = callback;
    }

    @Override
    public MainMenuViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RelativeLayout v = (RelativeLayout) LayoutInflater.from(parent.getContext()).inflate(R.layout.menu_item_layout, parent, false);
        MainMenuViewHolder vh = new MainMenuViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(MainMenuViewHolder holder, final int position) {
        holder.textView.setText(textData.get(position));
        holder.itemLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(callback != null)
                {
                    callback.onItemClicked(position);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return textData.size();
    }

    public static class MainMenuViewHolder extends WearableRecyclerView.ViewHolder
    {
        public RelativeLayout itemLayout;
        public TextView textView;
        public MainMenuViewHolder(RelativeLayout itemView) {
            super(itemView);
            itemLayout = itemView;
            textView = itemLayout.findViewById(R.id.menu_item_text_view);
        }
    }
}
