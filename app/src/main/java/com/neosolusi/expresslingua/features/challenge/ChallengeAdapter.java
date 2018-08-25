package com.neosolusi.expresslingua.features.challenge;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v7.widget.RecyclerView;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
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
import com.neosolusi.expresslingua.data.entity.Challenge;
import com.neosolusi.expresslingua.data.entity.Dictionary;
import com.neosolusi.expresslingua.data.entity.Flashcard;
import com.neosolusi.expresslingua.data.entity.Reading;

import io.realm.Case;
import io.realm.OrderedCollectionChangeSet;
import io.realm.Realm;
import io.realm.RealmResults;

public class ChallengeAdapter extends RecyclerView.Adapter<ChallengeAdapter.ViewHolder> {

    private Context context;
    private RealmResults<Challenge> mDataset;
    private Realm mRealm;
    private OnItemClickListener mListener;
    private boolean useTranslate = true;

    public ChallengeAdapter(Context context, boolean useTranslate, OnItemClickListener listener, RealmResults<Challenge> challenges) {
        setHasStableIds(true);
        this.context = context;
        this.mRealm = Realm.getDefaultInstance();
        this.mListener = listener;
        this.useTranslate = useTranslate;
        this.update(challenges);
    }

    @Override public ChallengeAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_row_lesson, parent, false);

        return new ViewHolder(view);
    }

    @Override public void onBindViewHolder(ViewHolder holder, int position) {
        holder.bindTo(mDataset.get(position));
    }

    @Override public int getItemCount() {
        return mDataset == null ? 0 : mDataset.size();
    }

    @Override public long getItemId(int position) {
        return mDataset.get(position).getId();
    }

    public void update(RealmResults<Challenge> entities) {
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

    public interface OnItemClickListener {
        void onClick(View view, Challenge entity, int position, int selectType);

        void onDoubleClick(RealmResults<Challenge> entities, int position);

        void onTextSelected(Reading entity, String text, int position);

        void onSpeak(View view, Challenge entity, int position, boolean isSlow);

        void onMoreOptionClick(View view, Challenge entity);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        ConstraintLayout content;
        View viewLevel;
        TextView textActor;
        ImageButton imageSpeak;
        TextView textSentence;
        TextView textTranslation;

        Reading reading;
        Flashcard flashcard;
        ActionMode.Callback actionModeCallback;

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

        protected void bindTo(Challenge challenge) {
            reading = findReadingById(challenge.getReference());
            if (reading == null) return;

            flashcard = findFlashcardByReference(reading.getId());
            if (flashcard == null) return;

            textActor.setText(reading.getActor() == null ? "Cinta" : reading.getActor());
            textTranslation.setText(reading.getTranslation());
            viewLevel.setBackgroundColor(indicator(reading.getMastering_level()));

            if (reading.getSentence() == null || reading.getSentence().trim().equalsIgnoreCase("")) {
                imageSpeak.setVisibility(View.GONE);
            } else {
                imageSpeak.setVisibility(View.VISIBLE);
            }

            coloringWords(flashcard, textSentence);
            switchColor(challenge);

            imageSpeak.setOnClickListener(view -> mListener.onSpeak(view, challenge, getAdapterPosition(), false));
            imageSpeak.setOnLongClickListener(view -> {
                mListener.onSpeak(view, challenge, getAdapterPosition(), true);
                return true;
            });

            actionModeCallback = new ActionMode.Callback() {
                @Override public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                    mListener.onTextSelected(reading, "", getAdapterPosition());
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
                    mListener.onTextSelected(reading, textSentence.getText().subSequence(start, end).toString(), getAdapterPosition());
                    mode.finish();
                    return false;
                }

                @Override public void onDestroyActionMode(ActionMode mode) {

                }
            };
            textSentence.setCustomSelectionActionModeCallback(actionModeCallback);
        }

        protected Flashcard findFlashcardByReference(long id) {
            return mRealm.where(Flashcard.class).equalTo("reference", id).equalTo("category", Reading.class.getSimpleName()).findFirst();
        }

        protected Reading findReadingById(long id) {
            return mRealm.where(Reading.class).equalTo("id", id).findFirst();
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
                } else {
                    Dictionary dictionary = mRealm.where(Dictionary.class).equalTo("word", AppUtils.normalizeString(word), Case.INSENSITIVE).findFirst();
                    if (dictionary != null && dictionary.getLocal_word() > AppConstants.DEFAULT_LOCAL_WORD_CONSTANT) {
                        wordSpan.setSpan(new ForegroundColorSpan(indicator(5)), wordsCount - word.length(), wordsCount, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
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

        protected void switchColor(Challenge entity) {
            ColorDrawable colorDrawable = (ColorDrawable) content.getBackground();
            int colorFrom = colorDrawable.getColor();
            int colorTo;

            switch (entity.getSelected()) {
                case 1:
                    colorTo = ActivityCompat.getColor(context, R.color.colorListSelection);
                    break;
                case 2:
                    colorTo = ActivityCompat.getColor(context, R.color.colorListSecondSelection);
                    break;
                default:
                    colorTo = ActivityCompat.getColor(context, R.color.white);
            }

            content.setBackgroundColor(colorTo);
//            ValueAnimator colorAnimation = ValueAnimator.ofObject(new ArgbEvaluator(), colorFrom, colorTo);
//            colorAnimation.setDuration(AppConstants.DEFAULT_ANIMATION_LENGTH);
//            colorAnimation.addUpdateListener(animator -> content.setBackgroundColor((int) animator.getAnimatedValue()));
//            colorAnimation.start();
        }
    }

}
