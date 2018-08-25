package com.neosolusi.expresslingua.features.challenge;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.flexbox.AlignSelf;
import com.google.android.flexbox.FlexboxLayoutManager;
import com.neosolusi.expresslingua.R;

import java.util.List;

public class PuzzleAdapter extends RecyclerView.Adapter<PuzzleAdapter.ViewHolder> {

    private List<PuzzleItem> mDataset;
    private OnItemClickListener mListener;
    private Context mContext;

    public PuzzleAdapter(Context context, OnItemClickListener listener) {
        setHasStableIds(true);
        this.mContext = context;
        this.mListener = listener;
    }

    public void swap(List<PuzzleItem> data) {
        Log.d("Puzzle Adapter", data.toString());
        if (mDataset == null || mDataset.isEmpty()) {
            mDataset = data;
            notifyDataSetChanged();
        } else {
            DiffUtil.DiffResult result = DiffUtil.calculateDiff(new PuzzleCallback(mDataset, data));
            mDataset = data;
            result.dispatchUpdatesTo(this);
        }
    }

    public void clear() {
        for (PuzzleItem item : mDataset) {
            item.selected = false;
        }
        notifyDataSetChanged();
    }

    public void enableItem(PuzzleItem item) {
        mDataset.get(mDataset.indexOf(item)).selected = false;
        notifyDataSetChanged();
    }

    @Override public PuzzleAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_row_puzzle, parent, false);
        return new ViewHolder(view);
    }

    @Override public void onBindViewHolder(PuzzleAdapter.ViewHolder holder, int position) {
        holder.bindTo(mDataset.get(position));
    }

    @Override public int getItemCount() {
        return mDataset == null ? 0 : mDataset.size();
    }

    @Override public int getItemViewType(int position) {
        return position;
    }

    @Override public long getItemId(int position) {
        return mDataset == null ? 0 : mDataset.get(position).id;
    }

    public interface OnItemClickListener {
        void onClick(PuzzleItem item);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private TextView textWord;

        public ViewHolder(View itemView) {
            super(itemView);
            textWord = itemView.findViewById(R.id.item_text_word);
        }

        public void bindTo(PuzzleItem item) {
            textWord.setText(item.word);

            if (!item.selected) {
                itemView.setBackground(ContextCompat.getDrawable(mContext, R.drawable.shape_button));
            } else {
                itemView.setBackground(ContextCompat.getDrawable(mContext, R.drawable.shape_button_disable));
            }

            itemView.setOnClickListener(v -> {
                if (!item.selected) {
                    mListener.onClick(item);
                    mDataset.get(getAdapterPosition()).selected = true;
                    itemView.setBackground(ContextCompat.getDrawable(mContext, R.drawable.shape_button_disable));
                }
            });

            ViewGroup.LayoutParams lp = textWord.getLayoutParams();
            if (lp instanceof FlexboxLayoutManager.LayoutParams) {
                FlexboxLayoutManager.LayoutParams flexboxLp = (FlexboxLayoutManager.LayoutParams) textWord.getLayoutParams();
                flexboxLp.setFlexGrow(1.0f);
            }
        }
    }

    public class PuzzleCallback extends DiffUtil.Callback {

        private List<PuzzleItem> oldData, newData;

        public PuzzleCallback(List<PuzzleItem> oldData, List<PuzzleItem> newData) {
            this.oldData = oldData;
            this.newData = newData;
        }

        @Override public int getOldListSize() {
            return oldData.size();
        }

        @Override public int getNewListSize() {
            return newData.size();
        }

        @Override public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
            return oldData.get(oldItemPosition).id == newData.get(newItemPosition).id
                    && oldData.get(oldItemPosition).word.equalsIgnoreCase(newData.get(newItemPosition).word);
        }

        @Override public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
            return oldData.get(oldItemPosition).selected == newData.get(newItemPosition).selected
                    && oldData.get(oldItemPosition).word.equalsIgnoreCase(newData.get(newItemPosition).word);
        }
    }
}
