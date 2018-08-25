package com.neosolusi.expresslingua.features.stories;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.neosolusi.expresslingua.AppInjectors;
import com.neosolusi.expresslingua.R;
import com.neosolusi.expresslingua.data.entity.Episode;
import com.neosolusi.expresslingua.data.repo.ReadingInfoRepository;

import io.realm.RealmResults;

public class StoriesAdapter extends RecyclerView.Adapter<StoriesAdapter.ViewHolder> {

    private static final int VIEW_TYPE_ENABLE = 0;
    private static final int VIEW_TYPE_DISABLE = 1;
    private final OnItemClickListener mListener;
    private RealmResults<Episode> mDataset;
    private ReadingInfoRepository mReadingInfoRepo;

    public StoriesAdapter(Context context, @NonNull OnItemClickListener listener) {
        setHasStableIds(true);
        this.mListener = listener;

        mReadingInfoRepo = AppInjectors.provideReadingInfoRepository(context);
    }

    public void update(RealmResults<Episode> episodes) {
        this.mDataset = episodes;

        mDataset.addChangeListener((episodes1, changeSet) -> notifyDataSetChanged());

        notifyDataSetChanged();
    }

    @Override public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        int layoutId = getLayoutByType(viewType);
        View view = LayoutInflater.from(parent.getContext()).inflate(layoutId, parent, false);

        return new ViewHolder(view);
    }

    @Override public void onBindViewHolder(ViewHolder holder, int position) {
        holder.bindTo(mDataset.get(position), mListener);
    }

    @Override public int getItemCount() {
        return mDataset == null ? 0 : mDataset.size();
    }

    @Override public long getItemId(int position) {
        return mDataset == null ? 0 : mDataset.get(position).getEpisode_id();
    }

    @Override public int getItemViewType(int position) {
        if (position == 0) {
            return VIEW_TYPE_ENABLE;
        } else {
            return VIEW_TYPE_ENABLE;
        }
    }

    private int getLayoutByType(int viewType) {
        switch (viewType) {
            case VIEW_TYPE_ENABLE:
                return R.layout.item_row_episode;
            case VIEW_TYPE_DISABLE:
                return R.layout.item_row_episode_disable;
            default:
                throw new IllegalArgumentException("Invalid view type " + viewType);
        }
    }

    public interface OnItemClickListener {
        void onClick(View view, Episode episode, int position);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView textName;
        TextView textTitle;

        public ViewHolder(View itemView) {
            super(itemView);

            textName = itemView.findViewById(R.id.item_text_episode_name);
            textTitle = itemView.findViewById(R.id.item_text_episode_title);
        }

        public void bindTo(final Episode episode, final OnItemClickListener listener) {
            textName.setText(episode.getName_episode());
            textTitle.setText(episode.getTitle());

            itemView.setOnClickListener(v -> listener.onClick(v, episode, getAdapterPosition()));
        }
    }
}
