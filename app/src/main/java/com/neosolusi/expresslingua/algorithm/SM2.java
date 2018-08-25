package com.neosolusi.expresslingua.algorithm;

import android.content.SharedPreferences;
import android.support.annotation.NonNull;

import com.neosolusi.expresslingua.AppConstants;
import com.neosolusi.expresslingua.AppUtils;
import com.neosolusi.expresslingua.data.entity.Dictionary;
import com.neosolusi.expresslingua.data.entity.Flashcard;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.Sort;

import static com.neosolusi.expresslingua.algorithm.SM2.State.LEARN_ONE;
import static com.neosolusi.expresslingua.algorithm.SM2.State.LEARN_TWO;
import static com.neosolusi.expresslingua.algorithm.SM2.State.RELEARNING;
import static com.neosolusi.expresslingua.algorithm.SM2.State.REVIEW;
import static com.neosolusi.expresslingua.features.flashcard.FlashcardActivity.SELECT_NONE;
import static com.neosolusi.expresslingua.features.flashcard.FlashcardActivity.SELECT_WORDS;

public abstract class SM2 {

    protected SharedPreferences mPreferences;
    protected SharedPreferences.Editor mPreferencesEdit;
    protected Set<Flashcard> mReviewCards;
    protected Set<Flashcard> mNewCards;
    protected int mSelectType;

    public SM2(SharedPreferences preferences, SharedPreferences.Editor preferencesEdit) {
        this.mPreferences = preferences;
        this.mPreferencesEdit = preferencesEdit;

        long lastReviewDate = mPreferences.getLong(AppConstants.PREFERENCE_LAST_REVIEW_DATE, new Date().getTime());
        if (AppUtils.isBeforeToday(lastReviewDate)) {
            mPreferencesEdit.putInt(AppConstants.PREFERENCE_REVIEWED_CARD, 0).apply();
            mPreferencesEdit.putInt(AppConstants.PREFERENCE_NEWED_CARD, 0).apply();
        }

        mNewCards = newCards();
        mReviewCards = reviewCards();
    }

    protected abstract String flashcardType();

    public Flashcard getFlashcard() {
        Realm realm = Realm.getDefaultInstance();

        Set<Flashcard> news = newCards();
        Set<Flashcard> reviews = reviewCards();

        List<Flashcard> mDisplayCards = new ArrayList<>();
        if (news != null) mDisplayCards.addAll(news);
        if (reviews != null) mDisplayCards.addAll(reviews);
        if (mDisplayCards.isEmpty()) return null;

        Collections.shuffle(mDisplayCards);
        Flashcard flashcard = mDisplayCards.get(0);
        realm.close();

        return flashcard;
    }

    public synchronized void calculate(@NonNull Flashcard flashcard, int level, int selectType) {
        mSelectType = selectType;

        int reviewedCard = mPreferences.getInt(AppConstants.PREFERENCE_REVIEWED_CARD, 0);
        int newedCard = mPreferences.getInt(AppConstants.PREFERENCE_NEWED_CARD, 0);

        if (flashcard.getState() == State.NEW) {
            mPreferencesEdit.putInt(AppConstants.PREFERENCE_NEWED_CARD, newedCard + 1).apply();
        }

        graduate(flashcard, level);

        switch (flashcard.getState()) {
            case NEW:
                flashcard.setState(LEARN_ONE);
                break;
            case REVIEW:
                mPreferencesEdit.putInt(AppConstants.PREFERENCE_REVIEWED_CARD, reviewedCard + 1).apply();
                break;
        }

        mPreferencesEdit.putLong(AppConstants.PREFERENCE_LAST_REVIEW_DATE, new Date().getTime()).apply();
    }

    public synchronized Set<Flashcard> newCards() {
        int maxNewCards = mPreferences.getInt(AppConstants.PREFERENCE_MAX_DAILY_NEW_CARD, 0);
        int newedCards = mPreferences.getInt(AppConstants.PREFERENCE_NEWED_CARD, 0);

        if (mNewCards == null || mNewCards.isEmpty() || (mNewCards.size() + newedCards < maxNewCards)) {
            if (mNewCards == null) mNewCards = new HashSet<>();
            Realm realm = Realm.getDefaultInstance();

            int maxLevel = 5;
            if (flashcardType().equalsIgnoreCase("word")) maxLevel = 2;

            RealmResults<Flashcard> newFlashcards = realm.where(Flashcard.class)
                    .equalTo("already_read", 1)
                    .equalTo("type", flashcardType())
                    .equalTo("state", State.NEW.name())
                    .equalTo("repeat", 0)
                    .lessThan("next_show", new Date())
                    .lessThanOrEqualTo("mastering_level", maxLevel)
                    .findAllSorted("next_show", Sort.DESCENDING);
            for (Flashcard flashcard : newFlashcards) {
                // Filter local word on Dictionary. 9 == local word
                if (flashcard.getCategory().equalsIgnoreCase(Dictionary.class.getSimpleName())) {
                    Dictionary dictionary = realm.where(Dictionary.class).equalTo("id", flashcard.getReference()).findFirst();
                    if (dictionary.getLocal_word() == 9) continue;
                }

                if (Math.abs(mNewCards.size() + newedCards) >= maxNewCards) break;
                mNewCards.add(flashcard);
            }
            realm.close();
        }

        Set<Flashcard> flashcards = new HashSet<>();
        for (Flashcard flashcard : mNewCards) {
            if (flashcard.getRepeat() == 0) flashcards.add(flashcard);
        }

        if (newedCards == maxNewCards) return null;
        return flashcards;
    }

    public synchronized Set<Flashcard> reviewCards() {
        int maxReviewCards = mPreferences.getInt(AppConstants.PREFERENCE_MAX_DAILY_REVIEW_CARD, 0);
        int reviewedCards = mPreferences.getInt(AppConstants.PREFERENCE_REVIEWED_CARD, 0);

        if (mReviewCards == null || mReviewCards.isEmpty() || (mReviewCards.size() + reviewedCards < maxReviewCards)) {
            if (mReviewCards == null) mReviewCards = new HashSet<>();
            Realm realm = Realm.getDefaultInstance();

            int maxLevel = 4;
            if (flashcardType().equalsIgnoreCase("word")) maxLevel = 2;

            RealmResults<Flashcard> flashcards = realm.where(Flashcard.class)
                    .greaterThan("repeat", 0)
                    .equalTo("already_read", 1)
                    .equalTo("type", flashcardType())
                    .lessThan("next_show", new Date())
                    .lessThanOrEqualTo("mastering_level", maxLevel)
                    .findAllSorted("next_show", Sort.DESCENDING);
            for (Flashcard flashcard : flashcards) {
                // Filter local word on Dictionary. 9 == local word
                if (flashcard.getCategory().equalsIgnoreCase(Dictionary.class.getSimpleName())) {
                    Dictionary dictionary = realm.where(Dictionary.class).equalTo("id", flashcard.getReference()).findFirst();
                    if (dictionary.getLocal_word() == 9) continue;
                }

                if (Math.abs(mReviewCards.size() + reviewedCards) >= maxReviewCards) break;
                mReviewCards.add(flashcard);
            }
            realm.close();
        }

        Set<Flashcard> reviews = new HashSet<>();
        for (Flashcard flashcard : mReviewCards) {
            if (flashcard.getNext_show().before(new Date())) reviews.add(flashcard);
        }

        if (reviewedCards >= maxReviewCards) return null;
        return reviews;
    }

    private void graduate(Flashcard flashcard, int level) {
        switch (flashcard.getState()) {
            case NEW:
                fromNew(flashcard, level);
                break;
            case LEARN_ONE:
                fromLearnOne(flashcard, level);
                break;
            case LEARN_TWO:
                fromLearnTwo(flashcard, level);
                break;
            case REVIEW:
                fromReview(flashcard, level);
                break;
            case RELEARNING:
                fromReLearning(flashcard, level);
        }
    }

    private void fromNew(Flashcard flashcard, int level) {
        switch (level) {
            case Difficulty.AGAIN:
                toLearnOne(flashcard, level);
                break;
            case Difficulty.HARD:
                toLearnTwo(flashcard, level);
                break;
            case Difficulty.GOOD:
            case Difficulty.EASY:
                toReview(flashcard, level);
                break;
        }
    }

    private void fromLearnOne(Flashcard flashcard, int level) {
        switch (level) {
            case Difficulty.AGAIN:
                toLearnOne(flashcard, level);
                break;
            case Difficulty.HARD:
            case Difficulty.GOOD:
                toLearnTwo(flashcard, level);
                break;
            case Difficulty.EASY:
                toReview(flashcard, level);
                break;
        }
    }

    private void fromLearnTwo(Flashcard flashcard, int level) {
        switch (level) {
            case Difficulty.AGAIN:
                toLearnOne(flashcard, level);
                break;
            case Difficulty.GOOD:
            case Difficulty.HARD:
            case Difficulty.EASY:

                // Reset previous interval from 10 to 1 minutes
                // and let toReview method do the job
                flashcard.setInterval(1);

                toReview(flashcard, level);
                break;
        }
    }

    private void fromReview(Flashcard flashcard, int level) {
        switch (level) {
            case Difficulty.AGAIN:
                toReLearn(flashcard, level);
                break;
            case Difficulty.GOOD:
            case Difficulty.HARD:
            case Difficulty.EASY:
                toReview(flashcard, level);
                break;
        }
    }

    private void fromReLearning(Flashcard flashcard, int level) {
        fromReview(flashcard, level);
    }

    private void toLearnOne(Flashcard flashcard, int level) {
        int timeToShow = 1;
        long delay = TimeUnit.MINUTES.toMillis(timeToShow);
        Date incrDate = new Date();
        incrDate.setTime(System.currentTimeMillis() + delay);

        if (isAllowToChangeLevel(flashcard, level)) {
            flashcard.setMastering_level(level);
            flashcard.setNext_show(incrDate);
            flashcard.setInterval(timeToShow);
            flashcard.setRepeat(flashcard.getRepeat() + 1);
            flashcard.setState(LEARN_ONE);
        }
    }

    private void toLearnTwo(Flashcard flashcard, int level) {
        int timeToShow = 10;
        long delay = TimeUnit.MINUTES.toMillis(timeToShow);
        Date incrDate = new Date();
        incrDate.setTime(System.currentTimeMillis() + delay);

        if (isAllowToChangeLevel(flashcard, level)) {
            flashcard.setMastering_level(level);
            flashcard.setNext_show(incrDate);
            flashcard.setInterval(timeToShow);
            flashcard.setRepeat(flashcard.getRepeat() + 1);
            flashcard.setState(LEARN_TWO);
        }
    }

    private void toReview(Flashcard flashcard, int level) {
        int timeToShow;

        double interval = Math.round(flashcard.getInterval() * flashcard.getE_factor());

        if (isAllowToChangeLevel(flashcard, level)) {
            if (level == 4) {
                if (flashcard.getMastering_level() >= 4) {
                    flashcard.setEasy_counter(flashcard.getEasy_counter() + 1);
                    flashcard.setMastering_level(5);
                    timeToShow = 7 * 2 ^ (flashcard.getEasy_counter() - 2);
                } else {
                    flashcard.setMastering_level(level);
                    flashcard.setEasy_counter(1);
                    timeToShow = Double.valueOf(interval).intValue();
                }
            } else {
                flashcard.setMastering_level(level);
                timeToShow = Double.valueOf(interval).intValue();
            }

            long delay = TimeUnit.DAYS.toMillis(timeToShow);
            Date incrDate = new Date();
            incrDate.setTime(System.currentTimeMillis() + delay);

            double newEF = flashcard.getE_factor() + (0.1 - (5 - level) * (0.08 + (5 - level) * 0.02));
            if (newEF >= 1.4) flashcard.setE_factor(newEF);

            flashcard.setNext_show(incrDate);
            flashcard.setInterval(timeToShow);
            flashcard.setRepeat(flashcard.getRepeat() + 1);
            flashcard.setState(REVIEW);
        }
    }

    private void toReLearn(Flashcard flashcard, int level) {
        toLearnOne(flashcard, level);
        flashcard.setState(RELEARNING);
    }

    private boolean isAllowToChangeLevel(Flashcard flashcard, int level) {
        return (flashcardType().equalsIgnoreCase("sentence") && mSelectType != SELECT_WORDS)
                || (flashcardType().equalsIgnoreCase("word") && mSelectType != SELECT_WORDS)
                || mSelectType == SELECT_NONE || level >= flashcard.getMastering_level();
    }

    public enum State {
        NEW, LEARN_ONE, LEARN_TWO, REVIEW, RELEARNING
    }

    public abstract class Difficulty {
        public static final int AGAIN = 1;
        public static final int HARD = 2;
        public static final int GOOD = 3;
        public static final int EASY = 4;
    }

}
