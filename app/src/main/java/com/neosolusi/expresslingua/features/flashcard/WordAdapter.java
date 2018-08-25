package com.neosolusi.expresslingua.features.flashcard;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v7.widget.RecyclerView;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.BackgroundColorSpan;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.neosolusi.expresslingua.AppConstants;
import com.neosolusi.expresslingua.AppUtils;
import com.neosolusi.expresslingua.R;
import com.neosolusi.expresslingua.data.entity.Flashcard;

import io.realm.Case;
import io.realm.OrderedCollectionChangeSet;
import io.realm.Realm;
import io.realm.RealmResults;

public class WordAdapter extends RecyclerView.Adapter<WordAdapter.ViewHolder> {

    private Context context;
    private RealmResults<Flashcard> mDataset;
    private Realm mRealm;
    private SharedPreferences mPreference;
    private OnInteractionListener listener;

    public WordAdapter(Context context, RealmResults<Flashcard> flashcards, OnInteractionListener listener) {
        setHasStableIds(true);
        this.context = context;
        this.listener = listener;
        this.mRealm = Realm.getDefaultInstance();
        this.mPreference = PreferenceManager.getDefaultSharedPreferences(context);
        this.update(flashcards);
    }

    @NonNull @Override public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_row_flashcard_word, parent, false);

        return new ViewHolder(view);
    }

    @Override public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bindTo(mDataset.get(position), listener);
    }

    @Override public int getItemCount() {
        if (mDataset == null) return 0;
        int fluencySize = mPreference.getInt(AppConstants.PREFERENCE_MAX_DAILY_FLUENCY_CARD, 5);
        return mDataset.size() > fluencySize ? fluencySize : mDataset.size();
    }

    @Override public long getItemId(int position) {
        return mDataset.get(position).getId();
    }

    private void update(RealmResults<Flashcard> entities) {
        mDataset = entities;
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

    protected class ViewHolder extends RecyclerView.ViewHolder {

        ConstraintLayout content;
        TextView textWord;
        TextView textTranslation;
        ImageButton buttonSpeak;
        ImageButton buttonMore;

        Flashcard wordFlashcard, copyFlashcard;
        OnInteractionListener wordListener;

        public ViewHolder(View itemView) {
            super(itemView);

            content = itemView.findViewById(R.id.item_content_flashcard_word);
            textWord = itemView.findViewById(R.id.item_text_flashcard_card);
            textTranslation = itemView.findViewById(R.id.item_text_flashcard_translation);
            buttonSpeak = itemView.findViewById(R.id.item_button_flashcard_speak);
            buttonMore = itemView.findViewById(R.id.item_button_flashcard_more);

            ViewGroup.LayoutParams layoutParams = content.getLayoutParams();
            layoutParams.width = AppUtils.screenWidth() - (20 * Math.round(AppUtils.screenDensity())); // paddingLeft: 10, paddingRight: 10
            content.setLayoutParams(layoutParams);
        }

        public void bindTo(Flashcard flashcard, OnInteractionListener listener) {
            copyFlashcard = mRealm.copyFromRealm(flashcard);
            wordFlashcard = flashcard;
            wordListener = listener;

            textWord.setText(wordFlashcard.getCard());
//            textTranslation.setText(wordFlashcard.getTranslation());
//            textTranslation.setText(String.valueOf(wordFlashcard.getRepeat()));
            textTranslation.setText(String.valueOf(wordFlashcard.getRead_repeat()));

            switchColor(wordFlashcard);
            coloringWords(flashcard, textWord);

            buttonSpeak.setOnClickListener(view -> wordListener.onSpeak(view, wordFlashcard, getAdapterPosition(), false));
            buttonMore.setOnClickListener(view -> wordListener.onMoreOptionClick(view, copyFlashcard));

            /*
            item onClick has handled from CardFragment
            it's not Adapter job anymore, so sad....
            itemView.setOnClickListener(v -> wordListener.onClick(v, wordFlashcard, getAdapterPosition(), SELECT_CARD));
             */
        }

        protected void coloringWords(Flashcard entity, TextView textView) {
            String sentence = entity.getCard();

            if (TextUtils.isEmpty(sentence)) return;

            String[] words = sentence.split(" ");
            Spannable wordSpan = new SpannableString(sentence);
            int wordsCount = 0;
            int color;

            for (String word : words) {
                Flashcard flashcard = mRealm.where(Flashcard.class)
                        .equalTo("card", AppUtils.normalizeString(word), Case.INSENSITIVE)
                        .equalTo("already_read", 1)
                        .findFirst();

                wordsCount += word.length();

                if (flashcard != null) {
                    color = indicator(flashcard.getMastering_level());
                    if (flashcard.getMastering_level() == 3) {
                        wordSpan.setSpan(
                                new BackgroundColorSpan(color),
                                wordsCount - word.length(),
                                wordsCount,
                                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                                        );
                    } else {
                        wordSpan.setSpan(
                                new ForegroundColorSpan(color),
                                wordsCount - word.length(),
                                wordsCount,
                                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                                        );
                    }
                }

                wordsCount += 1; // add space character
            }

            textView.setText(wordSpan);
            if (sentence.toUpperCase().equals(textView.getText().toString())) {
                textView.setTypeface(null, Typeface.BOLD);
            } else {
                textView.setTypeface(null, Typeface.NORMAL);
            }
        }

        protected int indicator(int level) {
            switch (level) {
                case 1:
                    return ActivityCompat.getColor(context, R.color.indikator_red);
                case 2:
                    return ActivityCompat.getColor(context, R.color.indikator_orange);
                case 3:
                    return ActivityCompat.getColor(context, R.color.indikator_yellow);
                case 4:
                    return ActivityCompat.getColor(context, R.color.indikator_green);
                case 5:
                    return ActivityCompat.getColor(context, R.color.indikator_blue);
                default:
                    return ActivityCompat.getColor(context, R.color.white_gray2);
            }
        }

        protected void switchColor(Flashcard entity) {
            ColorDrawable colorDrawable = (ColorDrawable) content.getBackground();
            int colorFrom = colorDrawable.getColor();
            int colorTo;

            switch (entity.getSelected()) {
                case 1:
                    colorTo = ActivityCompat.getColor(context, R.color.colorListSecondSelection);
                    break;
//                case 2:
//                    colorTo = ActivityCompat.getColor(context, R.color.colorListSecondSelection);
//                    break;
                default:
                    colorTo = ActivityCompat.getColor(context, R.color.white);
            }

            ValueAnimator colorAnimation = ValueAnimator.ofObject(new ArgbEvaluator(), colorFrom, colorTo);
            colorAnimation.setDuration(AppConstants.DEFAULT_ANIMATION_LENGTH);
            colorAnimation.addUpdateListener(animator -> content.setBackgroundColor((int) animator.getAnimatedValue()));
            colorAnimation.start();
        }
    }

}
