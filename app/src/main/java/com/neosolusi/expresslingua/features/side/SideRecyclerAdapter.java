package com.neosolusi.expresslingua.features.side;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.neosolusi.expresslingua.R;

import java.util.List;

public class SideRecyclerAdapter extends RecyclerView.Adapter<SideRecyclerAdapter.ViewHolder> {

    private List<ItemHolder> mDataset;

    public SideRecyclerAdapter(List<ItemHolder> data) {
        setHasStableIds(true);
        this.mDataset = data;
    }

    @Override public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_row_side, parent, false);
        return new ViewHolder(view);
    }

    @Override public void onBindViewHolder(ViewHolder holder, int position) {
        holder.bindTo(mDataset.get(position));
    }

    @Override public int getItemCount() {
        return mDataset.size();
    }

    @Override public int getItemViewType(int position) {
        return position;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private TextView textTitle, textDescription;

        public ViewHolder(View itemView) {
            super(itemView);

            textTitle = itemView.findViewById(R.id.text_title);
            textDescription = itemView.findViewById(R.id.text_description);
        }

        public void bindTo(ItemHolder item) {
            textTitle.setText(item.title);
            textDescription.setText(item.description);
        }
    }

    public static class ItemHolder {
        public String title;
        public String description;

        public ItemHolder(String title, String description) {
            this.title = title;
            this.description = description;
        }
    }

}
