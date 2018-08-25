package com.neosolusi.expresslingua.features.flashcard;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;

import com.neosolusi.expresslingua.AppConstants;
import com.neosolusi.expresslingua.AppUtils;
import com.neosolusi.expresslingua.algorithm.SM2;
import com.neosolusi.expresslingua.algorithm.Sentence;
import com.neosolusi.expresslingua.algorithm.Word;
import com.neosolusi.expresslingua.data.entity.Challenge;
import com.neosolusi.expresslingua.data.entity.ChallengeHard;
import com.neosolusi.expresslingua.data.entity.Dictionary;
import com.neosolusi.expresslingua.data.entity.Flashcard;
import com.neosolusi.expresslingua.data.entity.Reading;
import com.neosolusi.expresslingua.data.repo.ChallengeHardRepository;
import com.neosolusi.expresslingua.data.repo.ChallengeRepository;
import com.neosolusi.expresslingua.data.repo.DictionaryRepository;
import com.neosolusi.expresslingua.data.repo.FlashcardRepository;
import com.neosolusi.expresslingua.data.repo.ReadingRepository;
import com.neosolusi.expresslingua.features.flashcard.MainAdapter.CardType;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import io.realm.Case;
import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.Sort;

import static com.neosolusi.expresslingua.features.flashcard.FlashcardActivity.SELECT_CARD;
import static com.neosolusi.expresslingua.features.flashcard.FlashcardActivity.SELECT_WORDS;

public class CardViewModel extends ViewModel {

    private Realm mDatabase;
    private SharedPreferences mPref;
    private FlashcardRepository mFlashcardRepo;
    private DictionaryRepository mDictionaryRepo;
    private ReadingRepository mReadingRepo;
    private ChallengeRepository mChallengeRepo;
    private ChallengeHardRepository mChallengeHardRepo;
    private LiveData<RealmResults<Flashcard>> mFlashcards;
    private RealmResults<Dictionary> mDictionaries;
    private MutableLiveData<Flashcard> mFlashcard;
    private HashMap<Integer, Flashcard> mSelectedCards;
    private SM2 mAlgorithm;
    private Word mWordAlgo;
    private Sentence mSentenceAlgo;

    // Deck
    private String mDeckText;

    public CardViewModel(SharedPreferences pref, FlashcardRepository flashcardRepository, DictionaryRepository dictionaryRepository, ReadingRepository readingRepository, ChallengeRepository challengeRepository, ChallengeHardRepository challengeHardRepository, Word wordAlgorithm, Sentence sentenceAlgorithm) {
        mDatabase = Realm.getDefaultInstance();
        mPref = pref;
        mFlashcardRepo = flashcardRepository;
        mDictionaryRepo = dictionaryRepository;
        mReadingRepo = readingRepository;
        mChallengeRepo = challengeRepository;
        mSelectedCards = new HashMap<>();
        mFlashcard = new MutableLiveData<>();
        mWordAlgo = wordAlgorithm;
        mSentenceAlgo = sentenceAlgorithm;
        mDictionaries = mDictionaryRepo.findAll();
    }

    @Override protected void onCleared() {
        mDatabase.close();
    }

    public LiveData<RealmResults<Flashcard>> getFlashcards(int level, CardType cardType, boolean ignoreDate) {
        String type;
        switch (cardType.name()) {
            case "MULTIPLE_SENTENCES":
                type = "sentence";
                mAlgorithm = mSentenceAlgo;
                break;
            default:
                type = "word";
                mAlgorithm = mWordAlgo;
                break;
        }

        if (ignoreDate) {
            HashMap<String, Object> params = new HashMap<>();
            params.put("mastering_level", level);
            params.put("already_read", 1);
            params.put("type", type);
            mFlashcards = mFlashcardRepo.findAllEqualToAsync(params, "datemodified", Sort.ASCENDING);
        } else {
            mFlashcards = mFlashcardRepo.getFlashcards(type);
        }

        return mFlashcards;
    }

    public LiveData<Flashcard> getFlashcard(CardType cardType) {
        switch (cardType.name()) {
            case "SINGLE_WORD":
                mAlgorithm = mWordAlgo;
                break;
            default:
                mAlgorithm = mSentenceAlgo;
        }

        Flashcard flashcard = mAlgorithm.getFlashcard();
        if (flashcard == null) {
            mFlashcard.setValue(null);
            return mFlashcard;
        }

        // For debugging purpose
        int newCardCount = mAlgorithm.newCards() == null ? 0 : mAlgorithm.newCards().size();
        int reviewCardCount = mAlgorithm.reviewCards() == null ? 0 : mAlgorithm.reviewCards().size();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        mDeckText = flashcard.getState() + " : " + String.valueOf(newCardCount) + " - 0 - " + String.valueOf(reviewCardCount) + " | " + dateFormat.format(flashcard.getNext_show());
        // *******************************************************************************************************************

        mFlashcard.setValue(flashcard);
        return mFlashcard;
    }

    public Flashcard findFirstFlashcardCopyEqualTo(String column, String criteria) {
        return mFlashcardRepo.findFirstCopyEqualTo(column, criteria);
    }

    public Reading findFirstReadingCopyEqualTo(String column, long criteria) {
        return mReadingRepo.findFirstCopyEqualTo(column, criteria);
    }

    public String getDeck() {
        return mDeckText;
    }

    public String whenFlashcardWillShow() {
        RealmResults<Flashcard> flashcards = mDatabase.where(Flashcard.class)
                .equalTo("already_read", 1)
                .equalTo("type", "word")
                .greaterThan("repeat", 0)
                .lessThan("mastering_level", 5)
                .greaterThan("next_show", new Date())
                .findAllSorted("next_show", Sort.ASCENDING);

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());

        if (flashcards.isEmpty()) return "";

        return dateFormat.format(flashcards.get(0).getNext_show());
    }

    public void resetSelection(RealmResults<Flashcard> flashcards) {
        mFlashcardRepo.resetSelectedFlashcards(flashcards);
        mSelectedCards.clear();
    }

    public void selectFlashcard(int position, @NonNull Flashcard flashcard, int selectType) {
        Flashcard copyFlashcard = mDatabase.copyFromRealm(flashcard);
        copyFlashcard.setSelected(copyFlashcard.getSelected() == 0 ? selectType : 0);
        mFlashcardRepo.update(copyFlashcard);

        if (mSelectedCards.containsKey(position)) {
            mSelectedCards.remove(position);
        } else {
            mSelectedCards.put(position, flashcard);
        }
    }

    public void selectAllFlashcards(RealmResults<Flashcard> flashcards, int position, boolean toggle) {
        if (toggle) {
            resetSelection(flashcards);
            return;
        }

        mSelectedCards.clear();

        int loop = 0;
        int level = flashcards.get(position).getMastering_level();
        for (Flashcard flashcard : flashcards) {
            // Only select flashcard when mastering_level same with selected card
            if (flashcard.getMastering_level() == level) {
                selectFlashcard(loop, flashcard, SELECT_CARD);
            }
            loop++;
        }
    }

    public void updateLevel(CardType cardType, int level) {
        Realm mRealm = Realm.getDefaultInstance();

        String mCategory = "single";
        if (cardType == CardType.MULTIPLE_WORDS || cardType == CardType.MULTIPLE_SENTENCES) {
            mCategory = "multiple";
        }

        List<Flashcard> flashcards = new ArrayList<>();

        if (mCategory.equalsIgnoreCase("multiple")) {
            flashcards = mRealm.copyFromRealm(mFlashcards.getValue());
        } else {
            if (mSelectedCards.get(0) == null) return;
            Flashcard flashcard = mRealm.copyFromRealm(mSelectedCards.get(0));
            flashcards.add(flashcard);
        }

        for (int i = 0; i <= flashcards.size(); i++) {
            if (mSelectedCards.isEmpty()) break;
            if (mSelectedCards.containsKey(i)) {
                Flashcard selectedCard = mRealm.copyFromRealm(mSelectedCards.get(i));

                if (cardType == CardType.MULTIPLE_SENTENCES || cardType == CardType.SINGLE_SENTENCE) {
                    String[] words = selectedCard.getCard().split(" ");
                    for (String word : words) {
                        if (!word.trim().replaceAll("[^a-zA-Z_0-9\\s]", "").isEmpty()
                                && !word.trim().equals("")) {
                            addOrUpdateFlashcardWord(word, level, selectedCard.getSelected());
                        }
                    }
                }

                addOrUpdateFlashcard(selectedCard, level);

                // Update Reading associated with this flashcard
                if ((cardType == CardType.MULTIPLE_SENTENCES || cardType == CardType.SINGLE_SENTENCE)
                        && selectedCard.getSelected() != SELECT_WORDS) {
                    Reading reading = mReadingRepo.findFirstCopyEqualTo("id", selectedCard.getReference());
                    if (reading != null) {
                        reading.setUploaded(false);
                        reading.setAlready_read(1);

                        // Use mastering_level from calculated algorithm result on selectedCard
                        reading.setMastering_level(selectedCard.getMastering_level());

                        // Create challenge from this reading
                        createChallenge(reading, level);
                    }
                    mReadingRepo.copyOrUpdateAsync(reading);
                }

                mFlashcard.setValue(selectedCard);
            }
        }

        mSelectedCards.clear();
    }

    private void addOrUpdateFlashcard(Flashcard flashcard, int level) {
        if (flashcard.getSelected() == SELECT_WORDS) {
            flashcard.setSelected(0);
            mFlashcardRepo.update(flashcard);
            return;
        }

        mAlgorithm.calculate(flashcard, level, flashcard.getSelected());

        flashcard.setSelected(0);
        flashcard.setAlready_read(1);
        flashcard.setUploaded(false);
        flashcard.setDatemodified(new Date());

        // Commit changes
        mFlashcardRepo.update(flashcard);
    }

    public void addOrUpdateFlashcardWord(String word, int level, int selectType) {
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

    public void createChallenge(Reading reading, int level) {
        if (level < 2) return;
        if (reading.getTranslation().trim().isEmpty()) return;

        List<Challenge> challenges = mChallengeRepo.findAll();
        double maxOrangeLevel = mPref.getInt(AppConstants.PREFERENCE_MAX_CHALLENGE_ORANGE, 5);

        if (level == 2 && !challenges.isEmpty()) {
            double foundCriteria = 0;
            for (Challenge challenge : challenges) {
                Reading read = mReadingRepo.findFirstEqualTo("id", challenge.getReference());
                if (read != null && read.getMastering_level() == 2) foundCriteria++;
            }

            double currentMinLevelSize = 0;
            if (foundCriteria > 0) currentMinLevelSize = foundCriteria / mChallengeRepo.findAll().size();
            if (maxOrangeLevel / 100 < currentMinLevelSize) return;
        }

        if (!isHardChallenge(reading)) {
            createEasyChallenge(reading);
        } else {
            if (reading.getFile_id() >= AppConstants.DEFAULT_LESSON_TO_SHOW_HARD_CHALLENGE) {
                moveHardChallengeToEasy();
                createEasyChallenge(reading);
                return;
            }

            ChallengeHard challenge = mChallengeHardRepo.findFirstEqualTo("reference", reading.getId());
            if (challenge == null) {
                ChallengeHard newChallenge = new ChallengeHard();
                newChallenge.setId(UUID.randomUUID().getMostSignificantBits() & Long.MAX_VALUE);
                newChallenge.setCategory("Listening");
                newChallenge.setReference(reading.getId());
                newChallenge.setSeen(false);
                newChallenge.setSkip(false);
                newChallenge.setCorrect(false);
                newChallenge.setDatecreated(new Date());
                mChallengeHardRepo.copyOrUpdate(newChallenge);
            }
        }
    }

    private void createEasyChallenge(Reading reading) {
        Challenge challenge = mChallengeRepo.findFirstEqualTo("reference", reading.getId());
        if (challenge == null) {
            Challenge newChallenge = new Challenge();
            newChallenge.setId(UUID.randomUUID().getMostSignificantBits() & Long.MAX_VALUE);
            newChallenge.setCategory("Listening");
            newChallenge.setReference(reading.getId());
            newChallenge.setSeen(false);
            newChallenge.setSkip(false);
            newChallenge.setCorrect(false);
            newChallenge.setDatecreated(new Date());
            mChallengeRepo.copyOrUpdate(newChallenge);
        }
    }

    private void moveHardChallengeToEasy() {
        RealmResults<ChallengeHard> challengeHards = mChallengeHardRepo.findAll();
        for (ChallengeHard challenge : challengeHards) {
            createEasyChallenge(mReadingRepo.findFirstEqualTo("id", challenge.getReference()));
        }
        mChallengeHardRepo.deleteAll();
    }

    private boolean isHardChallenge(Reading reading) {
        return reading.getKal_panjang() > 0;
    }

    public void copyOrUpdate(Flashcard flashcard) {
        mFlashcardRepo.copyOrUpdate(flashcard);
    }
}
