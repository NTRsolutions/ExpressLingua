package com.neosolusi.expresslingua.features.lessons;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.neosolusi.expresslingua.AppInjectors;
import com.neosolusi.expresslingua.R;
import com.neosolusi.expresslingua.data.entity.ReadingInfo;
import com.neosolusi.expresslingua.data.entity.ReadingInfoMeta;
import com.neosolusi.expresslingua.data.repo.ReadingRepository;

import java.util.Locale;

import io.realm.Realm;
import io.realm.RealmResults;

public class LessonsAdapter extends RecyclerView.Adapter<LessonsAdapter.ViewHolder> {

    private static final int VIEW_TYPE_ENABLE = 0;
    private static final int VIEW_TYPE_DISABLE = 1;
    private final OnItemClickListener mListener;
    private Realm mDb;
    private RealmResults<ReadingInfo> mDataset;
    private ReadingRepository mReadingRepo;

    public LessonsAdapter(Context context, @NonNull OnItemClickListener listener) {
        setHasStableIds(true);
        this.mListener = listener;
        this.mDb = Realm.getDefaultInstance();

        mReadingRepo = AppInjectors.provideReadingRepository(context);
    }

    public void update(RealmResults<ReadingInfo> readingInfos) {
        this.mDataset = readingInfos;

        mDataset.addChangeListener((update, changeSet) -> notifyDataSetChanged());

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
        return mDataset == null ? 0 : mDataset.get(position).getMenu_id();
    }

    @Override public int getItemViewType(int position) {
        if (isEnable(position)) {
            return VIEW_TYPE_ENABLE;
        } else {
            return VIEW_TYPE_ENABLE;
        }
    }

    private int getLayoutByType(int viewType) {
        switch (viewType) {
            case VIEW_TYPE_ENABLE:
                return R.layout.item_row_lessons;
            case VIEW_TYPE_DISABLE:
                return R.layout.item_row_lessons_disable;
            default:
                throw new IllegalArgumentException("Invalid view type " + viewType);
        }
    }

    private boolean isEnable(int position) {
//        if (position == 0) return true;
//
//        HashMap<String, Object> criterias = new HashMap<>();
//        criterias.put("file_id", mDataset.get(position - 1).getFile_id());
//        criterias.put("already_read", 0);
//
//        return mReadingRepo.findFirstEqualTo(criterias) == null;
        return true;
    }

    public interface OnItemClickListener {
        void onClick(View view, ReadingInfo readingInfo, int position);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView textShortTitle;
        TextView textTitle;
        TextView textWordProgress;
        TextView textSentenceProgress;

        public ViewHolder(View itemView) {
            super(itemView);

            textShortTitle = itemView.findViewById(R.id.item_text_lessons_short_title);
            textTitle = itemView.findViewById(R.id.item_text_lessons_title);
            textWordProgress = itemView.findViewById(R.id.item_text_lessons_word_progress);
            textSentenceProgress = itemView.findViewById(R.id.item_text_lessons_sentence_progress);
        }

        public void bindTo(final ReadingInfo readingInfo, final OnItemClickListener listener) {
            textShortTitle.setText(readingInfo.getShort_title());
            textTitle.setText(readingInfo.getTitle());

            if (isEnable(getAdapterPosition())) {
                itemView.setOnClickListener(v -> listener.onClick(v, readingInfo, getAdapterPosition()));
            }

            ReadingInfoMeta metadata = mDb.where(ReadingInfoMeta.class).equalTo("menu_id", readingInfo.getMenu_id()).findFirst();
            if (metadata != null) {
                textWordProgress.setText(String.format(Locale.US, "Words: %d/%d", metadata.getWordMarked(), readingInfo.getWords_count()));
                textSentenceProgress.setText(String.format(Locale.US, "Sentences: %d/%d", metadata.getSentenceMarked(), readingInfo.getSentences_count()));
            } else {
                textWordProgress.setText(String.format(Locale.US, "Words: %d/%d", 0, 0));
                textSentenceProgress.setText(String.format(Locale.US, "Sentences: %d/%d", 0, 0));
            }
        }
    }

}
