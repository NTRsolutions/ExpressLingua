package com.neosolusi.expresslingua.features.lesson;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.ViewModel;
import android.content.Context;
import android.content.SharedPreferences;

import com.neosolusi.expresslingua.AppConstants;
import com.neosolusi.expresslingua.AppExecutors;
import com.neosolusi.expresslingua.AppUtils;
import com.neosolusi.expresslingua.algorithm.SM2;
import com.neosolusi.expresslingua.algorithm.Sentence;
import com.neosolusi.expresslingua.algorithm.Word;
import com.neosolusi.expresslingua.data.entity.Challenge;
import com.neosolusi.expresslingua.data.entity.ChallengeHard;
import com.neosolusi.expresslingua.data.entity.Dictionary;
import com.neosolusi.expresslingua.data.entity.Flashcard;
import com.neosolusi.expresslingua.data.entity.Reading;
import com.neosolusi.expresslingua.data.entity.ReadingInfo;
import com.neosolusi.expresslingua.data.entity.ReadingInfoMeta;
import com.neosolusi.expresslingua.data.repo.ChallengeHardRepository;
import com.neosolusi.expresslingua.data.repo.ChallengeRepository;
import com.neosolusi.expresslingua.data.repo.DictionaryRepository;
import com.neosolusi.expresslingua.data.repo.FlashcardRepository;
import com.neosolusi.expresslingua.data.repo.ReadingInfoRepository;
import com.neosolusi.expresslingua.data.repo.ReadingRepository;

import java.util.Date;
import java.util.List;
import java.util.UUID;

import io.realm.Case;
import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.Sort;

import static com.neosolusi.expresslingua.features.flashcard.FlashcardActivity.SELECT_CARD;
import static com.neosolusi.expresslingua.features.flashcard.FlashcardActivity.SELECT_WORDS;

public class LessonViewModel extends ViewModel {

    private Realm mDatabase;
    private SharedPreferences mPref;
    private ReadingRepository mReadingRepo;
    private ReadingInfoRepository mReadingInfoRepo;
    private FlashcardRepository mFlashcardRepo;
    private DictionaryRepository mDictionaryRepo;
    private ChallengeRepository mChallengeRepo;
    private ChallengeHardRepository mChallengeHardRepo;
    private LiveData<RealmResults<Reading>> mReadings;
    private LiveData<RealmResults<Flashcard>> mFlashcards;
    private LiveData<RealmResults<Dictionary>> mDictionaries;

    public LessonViewModel(
            SharedPreferences pref,
            ReadingRepository readingRepository,
            ReadingInfoRepository readingInfoRepository,
            DictionaryRepository dictionaryRepository,
            FlashcardRepository flashcardRepository,
            ChallengeRepository challengeRepository,
            ChallengeHardRepository challengeHardRepository) {
        mDatabase = Realm.getDefaultInstance();
        mPref = pref;
        mReadingRepo = readingRepository;
        mReadingInfoRepo = readingInfoRepository;
        mFlashcardRepo = flashcardRepository;
        mDictionaryRepo = dictionaryRepository;
        mChallengeRepo = challengeRepository;
        mChallengeHardRepo = challengeHardRepository;
        mDictionaries = mDictionaryRepo.findAllAsync();
    }

    @Override protected void onCleared() {
        mDatabase.close();
    }

    public ReadingInfo getReadingInfo(int readingInfoId) {
        return mReadingInfoRepo.findFirstEqualTo("file_id", readingInfoId);
    }

    public LiveData<RealmResults<Dictionary>> getDictionaries() {
        return mDictionaries;
    }

    public LiveData<RealmResults<Reading>> getReadings(int readingInfoId) {
        return mReadings = mReadingRepo.findAllEqualToAsync("file_id", readingInfoId, "sequence_no", Sort.ASCENDING);
    }

    public LiveData<RealmResults<Flashcard>> getFlashcards() {
        return mFlashcards = mFlashcardRepo.findAllAsync();
    }

    public void resetSelection(RealmResults<Reading> readings) {
        mReadingRepo.resetSelectedReading(readings);
    }

    public void resetSelection(Reading reading) {
        mReadingRepo.resetSelectedReading(reading);
    }

    public void selectReading(Reading reading, int selectType) {
        Reading copyReading = mDatabase.copyFromRealm(reading);
        copyReading.setSelected(copyReading.getSelected() == 0 ? selectType : 0);
        mReadingRepo.copyOrUpdate(copyReading);
    }

    public void selectAllReadings(RealmResults<Reading> readings, boolean toggle) {
        mReadingRepo.selectAllReadings(readings, toggle);
    }

    public void setBookmark(RealmResults<Reading> readings, Reading selectedReading) {
        mDatabase.beginTransaction();

        for (Reading reading : readings) {
            reading.setBookmarked(false);
        }

        selectedReading.setSelected(0);
        selectedReading.setBookmarked(true);
        mDatabase.commitTransaction();
    }

    public boolean isAllowToAddFlashcard(String type) {
        return mFlashcardRepo.isAllowToAddFlashcard(type);
    }

    public Flashcard findFirstFlashcardCopyEqualTo(String column, String criteria) {
        return mFlashcardRepo.findFirstCopyEqualTo(column, criteria);
    }

    public Flashcard findFirstFlashcardCopyEqualTo(String column, long criteria) {
        return mFlashcardRepo.findFirstCopyEqualTo(column, criteria);
    }

    public Reading findFirstReadingCopyEqualTo(String column, String criteria) {
        return mReadingRepo.findFirstCopyEqualTo(column, criteria);
    }

    public Reading findFirstReadingCopyEqualTo(String column, long criteria) {
        return mDatabase.copyFromRealm(mReadingRepo.findFirstEqualTo(column, criteria));
    }

    public Challenge findChallengeFromReading(long reference) {
        Challenge challenge = mChallengeRepo.findFirstEqualTo("reference", reference);
        return challenge == null ? null : mDatabase.copyFromRealm(challenge);
    }

    public void updateReadingInfoDownloadComplete(String fileName) {
        ReadingInfo info = mReadingInfoRepo.findFirstCopyEqualTo("audio_file_name", fileName);
        info.setDownload_complete(true);
        mReadingInfoRepo.copyOrUpdate(info);
    }

    public void updateReading(Reading reading) {
        mReadingRepo.copyOrUpdateAsync(reading);
    }

    public void copyOrUpdateFlashcard(Flashcard flashcard) {
        mFlashcardRepo.copyOrUpdate(flashcard);
    }

    public void updateFlashcard(Flashcard flashcard) {
        mFlashcardRepo.update(flashcard);
    }

    public void updateMetadata(ReadingInfo infoToUpdate, String card) {
        ReadingInfo readingInfo = mDatabase.copyFromRealm(infoToUpdate);
        mDatabase.executeTransactionAsync(db -> {
            ReadingInfoMeta meta = db.where(ReadingInfoMeta.class).equalTo("menu_id", readingInfo.getMenu_id()).findFirst();
            if (meta != null) {
                if (AppUtils.isWord(card)) {
                    meta.setWordMarked(meta.getWordMarked() + 1);
                } else {
                    meta.setSentenceMarked(meta.getSentenceMarked() + 1);
                }
            }
        });

        AppExecutors.getInstance().diskIO().execute(() -> {
            boolean wordFound;
            Realm db = Realm.getDefaultInstance();
            RealmResults<ReadingInfo> readingInfos = db.where(ReadingInfo.class).equalTo("episode_id", readingInfo.getEpisode_id()).findAll();
            for (ReadingInfo info : readingInfos) {
                wordFound = false;
                if (info.getFile_id() == readingInfo.getFile_id()) continue;

                RealmResults<Reading> readings = db.where(Reading.class).equalTo("file_id", info.getFile_id()).findAll();
                for (Reading reading : readings) {
                    if (reading.getSentence() == null || reading.getSentence().isEmpty()) continue;

                    String[] words = reading.getSentence().split(" ");
                    for (String word : words) {
                        if (!word.trim().replaceAll("[^a-zA-Z_0-9\\s]", "").isEmpty()
                                && !word.trim().equals("")
                                && AppUtils.normalizeString(word).toLowerCase().equals(card.toLowerCase())) {
                            final long menuId = info.getMenu_id();
                            db.executeTransactionAsync(localDb -> {
                                ReadingInfoMeta meta = localDb.where(ReadingInfoMeta.class).equalTo("menu_id", menuId).findFirst();
                                if (meta != null) {
                                    if (AppUtils.isWord(card)) {
                                        meta.setWordMarked(meta.getWordMarked() + 1);
                                    } else {
                                        meta.setSentenceMarked(meta.getSentenceMarked() + 1);
                                    }
                                }
                            });
                            wordFound = true;
                            break;
                        }
                    }

                    if (wordFound) break;
                }
            }
            db.close();
        });

    }

    public ReadingInfo getCurrentLesson(int fileId) {
        return mReadingInfoRepo.findFirstEqualTo("file_id", fileId);
    }

    public ReadingInfo getNextLesson(int fileId) {
        return mReadingInfoRepo.findFirstEqualTo("file_id", fileId + 1);
    }

    public ReadingInfo getPrevLesson(int fileId) {
        return mReadingInfoRepo.findFirstEqualTo("file_id", fileId - 1);
    }

    public void createChallenge(Reading reading, int level) {
        if (level < 2) return;
        if (reading.getTranslation() == null || reading.getTranslation().trim().isEmpty()) return;

        List<Challenge> challenges = mChallengeRepo.findAll();
        double maxOrangeLevel = mPref.getInt(AppConstants.PREFERENCE_MAX_CHALLENGE_ORANGE, 5);

        if (level == 2 && !challenges.isEmpty()) {
            double foundCriteria = 0;
            for (Challenge challenge : challenges) {
                Reading read = mReadingRepo.findFirstEqualTo("id", challenge.getReference());
                if (read != null && read.getMastering_level() == 2) foundCriteria++;
            }

            double currentMinLevelSize = 0;
            if (foundCriteria > 0)
                currentMinLevelSize = foundCriteria / mChallengeRepo.findAll().size();
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

    public long makeNewId() {
        return mFlashcardRepo.makeNewId();
    }

    public RealmResults<Flashcard> findWordsInFlashcard(String[] words) {
        return mDatabase.where(Flashcard.class).in("card", words).equalTo("type", "word").findAll();
    }

    public void updateReadRepeat(RealmResults<Reading> readings, Word wordAlgo, Sentence sentenceAlgo, Context context) {
        mReadingRepo.updateReadRepeat(mDatabase.copyFromRealm(readings), wordAlgo, sentenceAlgo, context);
    }

}
