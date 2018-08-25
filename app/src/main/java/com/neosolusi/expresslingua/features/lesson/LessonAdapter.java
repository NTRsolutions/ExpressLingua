package com.neosolusi.expresslingua.features.lesson;

import android.content.Context;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v7.widget.RecyclerView;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.BackgroundColorSpan;
import android.text.style.ForegroundColorSpan;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.neosolusi.expresslingua.AppConstants;
import com.neosolusi.expresslingua.AppUtils;
import com.neosolusi.expresslingua.R;
import com.neosolusi.expresslingua.data.entity.Dictionary;
import com.neosolusi.expresslingua.data.entity.Flashcard;
import com.neosolusi.expresslingua.data.entity.Reading;

import java.util.UUID;

import io.realm.Case;
import io.realm.OrderedCollectionChangeSet;
import io.realm.Realm;
import io.realm.RealmResults;

public class LessonAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int VIEW_TYPE_READING = 0;
    private static final int VIEW_TYPE_FOOTER = 1;

    private final OnItemClickListener mListener;
    private RealmResults<Reading> mDataset;
    private Realm mRealm;
    private Context mContext;
    private Typeface mTypeface;
    private boolean useTranslate = true;

    public LessonAdapter(Context context, boolean useTranslate, @NonNull OnItemClickListener listener) {
        setHasStableIds(true);
        mContext = context;
        mListener = listener;
        mRealm = Realm.getDefaultInstance();
        mTypeface = null;
        this.useTranslate = useTranslate;
    }

    public void update(RealmResults<Reading> readings) {
        if (mDataset != null) mDataset.removeAllChangeListeners();

        mDataset = readings;
        mDataset.addChangeListener((updates, changeSet) -> {
            // `null`  means the async query returns the first time.
            if (changeSet == null) {
                notifyDataSetChanged();
                return;
            }

            // For deletions, the adapter has to be notified in reverse order.
            OrderedCollectionChangeSet.Range[] deletions = changeSet.getDeletionRanges();
            for (int i = deletions.length - 1; i >= 0; i--) {
                OrderedCollectionChangeSet.Range range = deletions[i];
                notifyItemRangeRemoved(range.startIndex, range.length);
            }

            OrderedCollectionChangeSet.Range[] insertions = changeSet.getInsertionRanges();
            for (OrderedCollectionChangeSet.Range range : insertions) {
                notifyItemRangeInserted(range.startIndex, range.length);
            }

            OrderedCollectionChangeSet.Range[] modification = changeSet.getChangeRanges();
            for (OrderedCollectionChangeSet.Range range : modification) {
                notifyItemRangeChanged(range.startIndex, range.length);
            }
        });
        notifyDataSetChanged();
    }

    @NonNull @Override public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        int layoutId = getLayoutIdByType(viewType);
        View view = LayoutInflater.from(parent.getContext()).inflate(layoutId, parent, false);

        if (viewType == VIEW_TYPE_READING) {
            return new ViewHolder(view);
        } else {
            return new FooterViewHolder(view);
        }
    }

    @Override public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        switch (holder.getItemViewType()) {
            case VIEW_TYPE_READING:
                ViewHolder viewHolder = (ViewHolder) holder;
                viewHolder.bindTo(mDataset.get(position), mListener);
                break;
            case VIEW_TYPE_FOOTER:
                FooterViewHolder footerHolder = (FooterViewHolder) holder;
                footerHolder.bindTo();
                break;
        }
    }

    @Override public int getItemCount() {
        return mDataset == null ? 0 : mDataset.size() + 1;
    }

    @Override public long getItemId(int position) {
        if (mDataset == null) {
            return 0;
        } else if (position == mDataset.size()) {
            return mDataset.size();
        } else {
            return position;
        }
    }

    @Override public int getItemViewType(int position) {
        if (position == mDataset.size()) {
            return VIEW_TYPE_FOOTER;
        }
        return VIEW_TYPE_READING;
    }

    private int getLayoutIdByType(int viewType) {
        switch (viewType) {
            case VIEW_TYPE_READING: {
                return R.layout.item_row_lesson;
            }
            case VIEW_TYPE_FOOTER: {
                return R.layout.item_row_lesson_footer;
            }
            default:
                throw new IllegalArgumentException("Invalid view type, value of " + viewType);
        }
    }

    public interface OnItemClickListener {
        void onClick(View view, Reading reading, int position, int selectType);

        void onSpeak(View view, Reading reading, int position, boolean isSlow);

        void onTextSelection(Reading reading, String text, int position);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        ConstraintLayout content;
        View viewLevel;
        TextView textActor;
        ImageButton imageSpeak;
        TextView textSentence;
        TextView textTranslation;
        ActionMode.Callback mActionModeCallback;

        public ViewHolder(View itemView) {
            super(itemView);

            content = itemView.findViewById(R.id.item_lesson_content);
            viewLevel = itemView.findViewById(R.id.item_view_lesson_level);
            textActor = itemView.findViewById(R.id.item_text_lesson_actor);
            imageSpeak = itemView.findViewById(R.id.item_image_lesson_speak);
            textSentence = itemView.findViewById(R.id.item_text_lesson_sentence);
            textTranslation = itemView.findViewById(R.id.item_text_lesson_translation);

            if (!useTranslate) textTranslation.setVisibility(View.GONE);
        }

        protected void bindTo(final Reading reading, final OnItemClickListener listener) {
            textActor.setText(reading.getActor() == null ? "Cinta" : reading.getActor());

            if (reading.getTranslation() != null && !reading.getTranslation().trim().equalsIgnoreCase("")) {
                textTranslation.setText(String.format("(%s)", reading.getTranslation()));
            }

            textSentence.setText(reading.getSentence());

            if (reading.getSentence() == null || reading.getSentence().trim().equalsIgnoreCase("")) {
                imageSpeak.setVisibility(View.GONE);
            } else {
                imageSpeak.setVisibility(View.VISIBLE);
            }

            if (reading.getAlready_read() == 1) {
                viewLevel.setBackgroundColor(indicator(reading.getMastering_level()));
            } else {
                viewLevel.setBackgroundColor(ActivityCompat.getColor(mContext, android.R.color.transparent));
            }

            coloringWords(reading);

            switchColor(reading);

            if (reading.getSec() != 0 && reading.getSec() != 1) {
                textSentence.setTextSize(reading.getSec());
            } else {
                textSentence.setTextSize(14f);
            }

            imageSpeak.setOnClickListener(view -> listener.onSpeak(view, reading, getAdapterPosition(), false));
            imageSpeak.setOnLongClickListener(view -> {
                listener.onSpeak(view, reading, getAdapterPosition(), true);
                return true;
            });

            mActionModeCallback = new ActionMode.Callback() {
                @Override public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                    listener.onTextSelection(reading, "", getAdapterPosition());
                    mode.setTitle("Words selection");
                    if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
                        menu.clear();
                        menu.close();
                    }
                    return true;
                }

                @Override public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                    if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
                        menu.clear();
                        menu.close();
                    }
                    return true;
                }

                @Override public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                    int start = textSentence.getSelectionStart();
                    int end = textSentence.getSelectionEnd();
                    listener.onTextSelection(reading, textSentence.getText().subSequence(start, end).toString(), getAdapterPosition());
                    mode.finish();
                    return false;
                }

                @Override public void onDestroyActionMode(ActionMode mode) {

                }
            };
            textSentence.setCustomSelectionActionModeCallback(mActionModeCallback);
        }

        protected void switchColor(Reading entity) {
            ColorDrawable colorDrawable = (ColorDrawable) content.getBackground();
            int colorFrom = colorDrawable.getColor();
            int colorTo;

            switch (entity.getSec()) {
                case 1:
                    colorTo = ActivityCompat.getColor(mContext, R.color.colorListSection);
                    break;
                default:
                    colorTo = ActivityCompat.getColor(mContext, R.color.white);
            }

            switch (entity.getSelected()) {
                case 1:
                    colorTo = ActivityCompat.getColor(mContext, R.color.colorListSelection);
                    break;
                case 2:
                    colorTo = ActivityCompat.getColor(mContext, R.color.colorListSecondSelection);
                    break;
//                default:
//                    colorTo = ActivityCompat.getColor(mContext, R.color.white);
            }

            content.setBackgroundColor(colorTo);
//            ValueAnimator colorAnimation = ValueAnimator.ofObject(new ArgbEvaluator(), colorFrom, colorTo);
//            colorAnimation.setDuration(AppConstants.DEFAULT_ANIMATION_LENGTH);
//            colorAnimation.addUpdateListener(animator -> content.setBackgroundColor((int) animator.getAnimatedValue()));
//            colorAnimation.start();
        }

        protected int indicator(int level) {
            switch (level) {
                case 1:
                    return ActivityCompat.getColor(mContext, R.color.selection_red);
                case 2:
                    return ActivityCompat.getColor(mContext, R.color.selection_orange);
                case 3:
                    return ActivityCompat.getColor(mContext, R.color.selection_yellow);
                case 4:
                    return ActivityCompat.getColor(mContext, R.color.selection_green);
                case 5:
                    return ActivityCompat.getColor(mContext, R.color.selection_blue);
                default:
                    return ActivityCompat.getColor(mContext, R.color.white_gray2);
            }
        }

        protected void coloringWords(@NonNull Reading reading) {
            if (reading.getSentence() == null) return;

            String[] words = reading.getSentence().split(" ");
            Spannable wordSpan = new SpannableString(reading.getSentence());
            int wordsCount = 0;
            int color;

            for (String word : words) {
                Flashcard flashcard = mRealm.where(Flashcard.class).equalTo("card", AppUtils.normalizeString(word), Case.INSENSITIVE).equalTo("already_read", 1).findFirst();
                wordsCount += word.length();
                if (flashcard != null) {
                    color = indicator(flashcard.getMastering_level());
                    if (flashcard.getMastering_level() == 3) {
                        wordSpan.setSpan(new BackgroundColorSpan(color), wordsCount - word.length(), wordsCount, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    } else {
                        wordSpan.setSpan(new ForegroundColorSpan(color), wordsCount - word.length(), wordsCount, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    }
                } else {
                    Dictionary dictionary = mRealm.where(Dictionary.class).equalTo("word", AppUtils.normalizeString(word), Case.INSENSITIVE).findFirst();
                    if (dictionary != null && dictionary.getLocal_word() > AppConstants.DEFAULT_LOCAL_WORD_CONSTANT) {
                        wordSpan.setSpan(new ForegroundColorSpan(indicator(5)), wordsCount - word.length(), wordsCount, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    }
                }

                wordsCount += 1; // add space character
            }

            textSentence.setText(wordSpan);
            if (reading.getSentence().toUpperCase().equals(textSentence.getText().toString())) {
                textSentence.setTypeface(mTypeface, Typeface.BOLD);
            } else {
                textSentence.setTypeface(mTypeface, Typeface.NORMAL);
            }
        }

    }

    public class FooterViewHolder extends RecyclerView.ViewHolder {

        TextView textUpdate;

        public FooterViewHolder(View itemView) {
            super(itemView);

            textUpdate = itemView.findViewById(R.id.item_text_lesson_footer);
        }

        protected void bindTo() {
            // Empty implementation
        }
    }

}
