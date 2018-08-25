package com.neosolusi.expresslingua.features.challenge;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.ViewModel;
import android.support.annotation.NonNull;

import com.neosolusi.expresslingua.AppConstants;
import com.neosolusi.expresslingua.AppUtils;
import com.neosolusi.expresslingua.algorithm.SM2;
import com.neosolusi.expresslingua.algorithm.Sentence;
import com.neosolusi.expresslingua.algorithm.Word;
import com.neosolusi.expresslingua.data.entity.Challenge;
import com.neosolusi.expresslingua.data.entity.Dictionary;
import com.neosolusi.expresslingua.data.entity.Flashcard;
import com.neosolusi.expresslingua.data.entity.Reading;
import com.neosolusi.expresslingua.data.entity.ReadingInfo;
import com.neosolusi.expresslingua.data.repo.ChallengeRepository;
import com.neosolusi.expresslingua.data.repo.DictionaryRepository;
import com.neosolusi.expresslingua.data.repo.FlashcardRepository;
import com.neosolusi.expresslingua.data.repo.ReadingInfoRepository;
import com.neosolusi.expresslingua.data.repo.ReadingRepository;
import com.neosolusi.expresslingua.features.challenge.ChallengeActivity.TYPE;
import com.neosolusi.expresslingua.features.flashcard.MainAdapter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import io.realm.Case;
import io.realm.Realm;
import io.realm.RealmQuery;
import io.realm.RealmResults;
import io.realm.Sort;

import static com.neosolusi.expresslingua.features.flashcard.FlashcardActivity.SELECT_CARD;
import static com.neosolusi.expresslingua.features.flashcard.FlashcardActivity.SELECT_WORDS;

public class ChallengeViewModel extends ViewModel {

    private Realm mDatabase;
    private ReadingRepository mReadingRepo;
    private ReadingInfoRepository mReadingInfoRepo;
    private FlashcardRepository mFlashcardRepo;
    private ChallengeRepository mChallengeRepo;
    private DictionaryRepository mDictionaryRepo;
    private List<Challenge> mQuestions;
    private List<Challenge> mCorrectAnswer;
    private RealmResults<Dictionary> mDictionaries;
    private LiveData<RealmResults<Challenge>> mChallenges;
    private HashMap<Integer, Challenge> mSelectedCards;
    private boolean hasFinishChallenges = false;
    private SM2 mAlgorithm;
    private Word mWordAlgo;
    private Sentence mSentenceAlgo;

    public ChallengeViewModel(
            ReadingRepository readingRepository,
            ReadingInfoRepository readingInfoRepository,
            FlashcardRepository flashcardRepository,
            ChallengeRepository challengeRepository,
            DictionaryRepository dictionaryRepository,
            Word wordAlgorithm, Sentence sentenceAlgorithm) {
        mDatabase = Realm.getDefaultInstance();
        mReadingRepo = readingRepository;
        mReadingInfoRepo = readingInfoRepository;
        mFlashcardRepo = flashcardRepository;
        mChallengeRepo = challengeRepository;
        mDictionaryRepo = dictionaryRepository;
        mWordAlgo = wordAlgorithm;
        mSentenceAlgo = sentenceAlgorithm;
        mDictionaries = mDictionaryRepo.findAll();
        mCorrectAnswer = new ArrayList<>();
        mSelectedCards = new HashMap<>();
    }

    @Override protected void onCleared() {
        mDatabase.close();
    }

    public Flashcard getFlashcard() {
        return mDatabase.where(Flashcard.class).equalTo("type", "sentence")
                .greaterThan("mastering_level", 2)
                .equalTo("already_read", 1)
                .findFirst();
    }

    public Challenge getQuestions() {
        if (mQuestions == null || mQuestions.isEmpty()) {
            mQuestions = new ArrayList<>();
            RealmResults<Challenge> challenges = mDatabase.where(Challenge.class).equalTo("correct", false).findAll();
            mQuestions.addAll(challenges);
        }

        if (mQuestions != null && mQuestions.size() > 0) {
            if (mCorrectAnswer.size() > 0) mQuestions.removeAll(mCorrectAnswer);
            if (mQuestions.size() == 0) {
                hasFinishChallenges = true;
                return null;
            }
            Collections.shuffle(mQuestions);
            Collections.shuffle(mQuestions);
            Collections.shuffle(mQuestions);
            return mQuestions.get(0);
        }

        return null;
    }

    public LiveData<RealmResults<Challenge>> getChallenges(TYPE type) {
        switch (type) {
            case CORRECT:
                mChallenges = mChallengeRepo.findAllEqualToAsync("correct", true);
                break;
            case INCORRECT:
                HashMap<String, Object> criterias = new HashMap<>();
                criterias.put("correct", false);
                criterias.put("seen", true);
                criterias.put("skip", false);
                mChallenges = mChallengeRepo.findAllEqualToAsync(criterias, "id", Sort.ASCENDING);
                break;
            case SKIPPED:
                mChallenges = mChallengeRepo.findAllEqualToAsync("skip", true);
                break;
            default:
                mChallenges = mChallengeRepo.findAllEqualToAsync("seen", false);
        }

        return mChallenges;
    }

    public List<String> getRandomWords(List<String> exceptWords) {
        RealmQuery<Dictionary> query = mDatabase.where(Dictionary.class);
        for (String word : exceptWords) {
            query.notEqualTo("word", word);
        }

        List<String> words = new ArrayList<>();
        List<Dictionary> dictionaries = query.findAll();
        Random random = new Random();

        for (int i = 0; i <= dictionaries.size(); i++) {
            int rnd = random.nextInt(dictionaries.size() - 1 + 1) + 1;
            words.add(dictionaries.get(rnd).getWord());
            if (i == 2) break;
        }

        return words;
    }

    public void correctAnswers(Challenge challenge) {
        if (mCorrectAnswer.contains(challenge)) return;

        mCorrectAnswer.add(challenge);
        Challenge copy = mDatabase.copyFromRealm(challenge);
        copy.setSeen(true);
        copy.setCorrect(true);
        copy.setSkip(false);
        mChallengeRepo.copyOrUpdate(copy);
    }

    public void wrongAnswers(Challenge challenge) {
        Challenge copy = mDatabase.copyFromRealm(challenge);
        copy.setSeen(true);
        copy.setCorrect(false);
        copy.setSkip(false);
        mChallengeRepo.copyOrUpdate(copy);

        // Wrong answer? set reading level to lowest challenge value
        Reading readingCopy = mDatabase.copyFromRealm(findReadingForChallenge(challenge));
        readingCopy.setMastering_level(3); // 3 is the lowest value for now
        mReadingRepo.copyOrUpdate(readingCopy);
    }

    public void skip(Challenge challenge) {
        Challenge copy = mDatabase.copyFromRealm(challenge);
        copy.setSeen(true);
        copy.setSkip(true);
        copy.setCorrect(false);
        mChallengeRepo.copyOrUpdate(copy);
    }

    public void repeatQuestions() {
        mQuestions = null;
        mCorrectAnswer = new ArrayList<>();
        hasFinishChallenges = false;
    }

    public boolean isHasFinishChallenges() {
        return hasFinishChallenges;
    }

    public Reading findReadingForChallenge(Challenge challenge) {
        return mReadingRepo.findFirstEqualTo("id", challenge.getReference());
    }

    public Challenge findChallengeFromReading(long reference) {
        Challenge challenge = mChallengeRepo.findFirstEqualTo("reference", reference);
        return challenge == null ? null : mDatabase.copyFromRealm(challenge);
    }

    public ReadingInfo getCurrentLesson(int fileId) {
        return mDatabase.where(ReadingInfo.class).equalTo("file_id", fileId).findFirst();
    }

    public void resetSelection(RealmResults<Challenge> challenges) {
        mChallengeRepo.resetSelectedChallenges(challenges);
        mSelectedCards.clear();
    }

    public void selectChallenge(int position, @NonNull Challenge challenge, int selectType) {
        Challenge copyChallenge = mDatabase.copyFromRealm(challenge);
        copyChallenge.setSelected(copyChallenge.getSelected() == 0 ? selectType : 0);
        mChallengeRepo.update(copyChallenge);

        if (mSelectedCards.containsKey(position)) {
            mSelectedCards.remove(position);
        } else {
            mSelectedCards.put(position, challenge);
        }
    }

    public Flashcard findFirstFlashcardCopyEqualTo(String column, String criteria) {
        return mFlashcardRepo.findFirstCopyEqualTo(column, criteria);
    }

    public Flashcard findFirstFlashcardCopyEqualTo(String column, long criteria) {
        HashMap<String, Object> params = new HashMap<>();
        params.put(column, criteria);
        params.put("type", "sentence");
        params.put("category", Reading.class.getSimpleName());
        return mDatabase.copyFromRealm(mFlashcardRepo.findFirstEqualTo(params));
    }

    private void addOrUpdateFlashcard(Flashcard flashcard, int level) {
        if (flashcard.getSelected() == SELECT_WORDS) {
            flashcard.setSelected(0);
            mFlashcardRepo.update(flashcard);
            return;
        }

        mAlgorithm = mSentenceAlgo;
        mAlgorithm.calculate(flashcard, level, flashcard.getSelected());

        flashcard.setSelected(0);
        flashcard.setAlready_read(1);
        flashcard.setUploaded(false);
        flashcard.setDatemodified(new Date());

        // Commit changes
        mFlashcardRepo.update(flashcard);
    }

    public void updateLevel(MainAdapter.CardType cardType, int level) {
        Realm mRealm = Realm.getDefaultInstance();

        String mCategory = "single";
        if (cardType == MainAdapter.CardType.MULTIPLE_WORDS || cardType == MainAdapter.CardType.MULTIPLE_SENTENCES) {
            mCategory = "multiple";
        }

        List<Challenge> challenges = new ArrayList<>();

        if (mCategory.equalsIgnoreCase("multiple")) {
            challenges = mRealm.copyFromRealm(mChallenges.getValue());
        } else {
            if (mSelectedCards.get(0) == null) return;
            Challenge challenge = mRealm.copyFromRealm(mSelectedCards.get(0));
            challenges.add(challenge);
        }

        for (int i = 0; i <= challenges.size(); i++) {
            if (mSelectedCards.isEmpty()) break;
            if (mSelectedCards.containsKey(i)) {
                Reading reading = mDatabase.copyFromRealm(findReadingForChallenge(mSelectedCards.get(i)));
                if (reading == null) continue;

                Flashcard selectedCard = findFirstFlashcardCopyEqualTo("reference", reading.getId());

                if (cardType == MainAdapter.CardType.MULTIPLE_SENTENCES || cardType == MainAdapter.CardType.SINGLE_SENTENCE) {
                    String[] words = selectedCard.getCard().split(" ");
                    for (String word : words) {
                        if (!word.trim().replaceAll("[^a-zA-Z_0-9\\s]", "").isEmpty()
                                && !word.trim().equals("")) {
                            addOrUpdateFlashcardWord(word, level, mSelectedCards.get(i).getSelected());
                        }
                    }
                }

                addOrUpdateFlashcard(selectedCard, level);

                // Update Reading associated with this flashcard
                if ((cardType == MainAdapter.CardType.MULTIPLE_SENTENCES || cardType == MainAdapter.CardType.SINGLE_SENTENCE)
                        && mSelectedCards.get(i).getSelected() != SELECT_WORDS) {
                    reading.setUploaded(false);
                    reading.setAlready_read(1);

                    // Use mastering_level from calculated algorithm result on selectedCard
                    reading.setMastering_level(selectedCard.getMastering_level());
                    mReadingRepo.copyOrUpdate(reading);
                }

                Challenge challenge = mDatabase.copyFromRealm(mSelectedCards.get(i));
                challenge.setSelected(0);
                mChallengeRepo.update(challenge);
            }
        }

        mSelectedCards.clear();
    }

    public void addOrUpdateFlashcardWord(String word, int level, int selectType) {
        mAlgorithm = mWordAlgo;
        if (selectType == SELECT_CARD) return;
        if (mDictionaries == null || mDictionaries.isEmpty()) return;

        // Local Word is forbidden
        // **********************************************************************************
        Dictionary dictionary = mDictionaries.where().equalTo("word", AppUtils.normalizeString(word), Case.INSENSITIVE).findFirst();
        if (dictionary != null && dictionary.getLocal_word() > AppConstants.DEFAULT_LOCAL_WORD_CONSTANT) {
            return;
        }

        // Update flashcard
        // **********************************************************************************
        Flashcard card = findFirstFlashcardCopyEqualTo("card", AppUtils.normalizeString(word));
        if (card != null) {
            mAlgorithm.calculate(card, level, selectType);

            card.setAlready_read(1);
            card.setUploaded(false);
            card.setDatemodified(new Date());

            // Commit changes
            mFlashcardRepo.update(card);
            return;
        }

        // Add new flashcard
        // **********************************************************************************
        Flashcard newCard = new Flashcard();
        long id = mFlashcardRepo.makeNewId();
        newCard.setId(id);
        newCard.setCard(AppUtils.normalizeString(word));

        dictionary = mDictionaries.where().equalTo("word", AppUtils.normalizeString(word), Case.INSENSITIVE).findFirst();
        if (dictionary != null) {
            newCard.setReference(dictionary.getId());
            newCard.setTranslation(dictionary.getTranslation());
            newCard.setCategory(Dictionary.class.getSimpleName());
        } else {
            newCard.setReference(UUID.randomUUID().getMostSignificantBits() & Long.MAX_VALUE);
            newCard.setTranslation("");
            newCard.setCategory("User");
        }

        newCard.setUploaded(false);
        newCard.setSelected(0);
        newCard.setMastering_level(level);
        newCard.setAlready_read(1);
        newCard.setType("word");
        newCard.setDatecreated(new Date());
        newCard.setDatemodified(new Date());
        newCard.setRepeat(0);
        newCard.setReviewed(true);
        newCard.setState(SM2.State.NEW);
        newCard.setE_factor(2.5);
        newCard.setInterval(1);
        newCard.setNext_show(new Date());
        newCard.setEasy_counter(level == 4 ? 1 : 0);

        // Commit changes
        copyOrUpdate(newCard);
    }

    public void copyOrUpdate(Flashcard flashcard) {
        mFlashcardRepo.copyOrUpdate(flashcard);
    }
}
