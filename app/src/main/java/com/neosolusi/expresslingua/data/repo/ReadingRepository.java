package com.neosolusi.expresslingua.data.repo;

import android.arch.lifecycle.LiveData;
import android.content.Context;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;

import com.neosolusi.expresslingua.AppConstants;
import com.neosolusi.expresslingua.AppExecutors;
import com.neosolusi.expresslingua.AppInjectors;
import com.neosolusi.expresslingua.AppUtils;
import com.neosolusi.expresslingua.algorithm.SM2;
import com.neosolusi.expresslingua.algorithm.Sentence;
import com.neosolusi.expresslingua.algorithm.Word;
import com.neosolusi.expresslingua.data.dao.DictionaryDao;
import com.neosolusi.expresslingua.data.dao.FlashcardDao;
import com.neosolusi.expresslingua.data.dao.ReadingDao;
import com.neosolusi.expresslingua.data.dao.ReadingInfoDao;
import com.neosolusi.expresslingua.data.dao.UserDao;
import com.neosolusi.expresslingua.data.entity.Dictionary;
import com.neosolusi.expresslingua.data.entity.Flashcard;
import com.neosolusi.expresslingua.data.entity.Reading;
import com.neosolusi.expresslingua.data.entity.ReadingInfo;
import com.neosolusi.expresslingua.data.entity.ReadingInfoMeta;
import com.neosolusi.expresslingua.data.entity.ReadingParcel;
import com.neosolusi.expresslingua.data.network.NetworkDataSource;

import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import io.realm.Case;
import io.realm.Realm;
import io.realm.RealmResults;

import static com.neosolusi.expresslingua.features.flashcard.FlashcardActivity.SELECT_CARD;
import static com.neosolusi.expresslingua.features.flashcard.FlashcardActivity.SELECT_WORDS;

public class ReadingRepository extends BaseRepository<Reading> {

    private static final String TAG = ReadingRepository.class.getSimpleName();

    // Singleton Instantiation
    private static final Object LOCK = new Object();
    private static ReadingRepository mInstance;
    private final ReadingDao mDao;
    private final UserDao mUserDao;
    private final AppExecutors mExecutors;
    private final NetworkDataSource mNetworkSource;
    private boolean mInitialized = false;

    private ReadingRepository(ReadingDao dao, UserDao userDao, NetworkDataSource networkDataSource, AppExecutors executors) {
        super(dao);
        this.mDao = dao;
        this.mUserDao = userDao;
        this.mExecutors = executors;
        this.mNetworkSource = networkDataSource;

        LiveData<List<Reading>> networkData = mNetworkSource.getReadings();
        networkData.observeForever(readings -> {
            if (readings == null) return;
            if (mDao.count() == 0) {
                mDao.insertAsync(readings);
            } else {
                mDao.copyOrUpdateAsync(readings);
            }
        });

        LiveData<List<Reading>> networkUserData = mNetworkSource.getReadingsUser();
        networkUserData.observeForever(readings -> {
            if (readings == null) return;
            mDao.updateFromUserData(readings);
        });

        LiveData<Reading> networkUploadedReading = mNetworkSource.getUploadedReading();
        networkUploadedReading.observeForever(reading -> {
            if (reading == null) return;
            Log.d(TAG, "Upload Reading Success");
            reading.setUploaded(true);
            mDao.copyOrUpdate(reading);
            mDao.refresh();
            upload();
        });
    }

    public synchronized static ReadingRepository getInstance(ReadingDao dao, UserDao userDao, NetworkDataSource networkDataSource, AppExecutors executors) {
        if (mInstance == null) {
            synchronized (LOCK) {
                mInstance = new ReadingRepository(dao, userDao, networkDataSource, executors);
            }
        }

        return mInstance;
    }

    private synchronized void initializeData() {
        if (mInitialized) return;
        mInitialized = true;

        boolean fetchNeed = isFetchNeeded();
        mExecutors.networkIO().execute(() -> {
            if (fetchNeed) mNetworkSource.startNetworkService("reading");
        });
    }

    public synchronized void upload() {
        String userid = mUserDao.findActiveUser().getUserid();
        Reading reading = mDao.findFirstEqualTo("uploaded", false);

        if (reading == null) {
            Log.d(TAG, "Does'n have pending upload");
            return;
        }

        ReadingParcel parcel = new ReadingParcel(reading);
        mExecutors.networkIO().execute(() -> {
            Log.d(TAG, "Upload Reading Begin");
            Bundle bundle = new Bundle();
            bundle.putString("userid", userid);
            bundle.putParcelable("reading", parcel);
            mNetworkSource.startNetworkServiceWithExtra("upload_reading", bundle);
        });
    }

    @Override public boolean isFetchNeeded() {
        return 0 == mDao.count() || mDao.findFirstNotEqualTo("start_duration", null) == null;
    }

    public void housekeeping() {
        String userid = mUserDao.findActiveUser().getUserid();
        Bundle bundle = new Bundle();
        bundle.putString("userid", userid);
        mNetworkSource.startNetworkServiceWithExtra("readingUser", bundle);
    }

    public void resetSelectedReading(RealmResults<Reading> readings) {
        mDao.resetSelectedReadings(readings);
    }

    public void resetSelectedReading(Reading reading) {
        mDao.resetSelectedReading(reading);
    }

    public void selectAllReadings(RealmResults<Reading> readings, boolean toggle) {
        mDao.selectAllReadings(readings, toggle);
    }

    public void updateReadRepeat(List<Reading> readings, Word wordAlgo, Sentence sentenceAlgo, Context context) {
        mExecutors.diskIO().execute(() -> {
            Realm database = Realm.getDefaultInstance();
            ReadingDao readingDao = new ReadingDao(database);
            DictionaryDao dictionaryDao = new DictionaryDao(database);
            FlashcardDao flashcardDao = new FlashcardDao(database);
            ReadingInfoDao readingInfoDao = new ReadingInfoDao(database);
            ChallengeRepository challengeRepository = AppInjectors.provideChallengeRepository(context);

            boolean isAllowToAddFlashcardWord = flashcardDao.isAllowToAddFlashcard("word");
            boolean isAllowToAddFlashcardSentence = flashcardDao.isAllowToAddFlashcard("sentence");
            RealmResults<Dictionary> dictionaries = dictionaryDao.findAll();

            Set<String> wordSet = new HashSet<>();
            for (Reading reading : readings) {
                if (reading.getSentence() == null) reading.setSentence("");

                String[] words = reading.getSentence().split(" ");
                for (String word : words) {
                    if (!word.trim().replaceAll("[^a-zA-Z_0-9\\s]", "").isEmpty()
                            && !word.trim().equals("")
                            && isAllowToAddFlashcardWord && !wordSet.contains(AppUtils.normalizeString(word))) {
                        wordSet.add(AppUtils.normalizeString(word));
                        addOrUpdateFlashcardWord(flashcardDao, readingInfoDao, dictionaries, reading.getFile_id(), wordAlgo, word);
                    }
                }

                if (isAllowToAddFlashcardSentence) {
                    int toLevel = reading.getMastering_level() < 4 ? reading.getMastering_level() + 1 : reading.getMastering_level();

                    reading.setAlready_read(1);
                    reading.setMastering_level(toLevel);
                    reading.setUploaded(false);

                    addOrUpdateFlashcardSentence(challengeRepository, flashcardDao, readingInfoDao, reading, sentenceAlgo, toLevel);
                }

                reading.setSelected(0);
                reading.setDatemodified(new Date());
                readingDao.copyOrUpdate(reading);
            }

            // Cleanup
            database.close();
        });
    }

    private void addOrUpdateFlashcardSentence(ChallengeRepository challengeRepository, FlashcardDao flashcardDao, ReadingInfoDao readingInfoDao, Reading reading, Sentence sentenceAlgo, int level) {
        if (reading.getSentence().isEmpty()) return;

        // CreateIfNotExists Challenge
        challengeRepository.createChallenge(reading, level);

        // Update flashcard
        // **********************************************************************************
        HashMap<String, Object> params = new HashMap<>();
        params.put("reference", reading.getId());
        params.put("type", "sentence");
        Flashcard card = flashcardDao.findFirstCopyEqualTo(params);
        if (card != null) {
            sentenceAlgo.calculate(card, level, SELECT_CARD);

            // Update reading level
            reading.setMastering_level(card.getMastering_level());

            card.setAlready_read(1);
            card.setUploaded(false);
            card.setDatemodified(new Date());
            card.setRead_repeat(card.getRead_repeat() + 1);

            // Commit changes
            flashcardDao.update(card);
            flashcardDao.refresh();
            return;
        }

        // Add new flashcard
        // **********************************************************************************
        Flashcard newCard = new Flashcard();
        long id = flashcardDao.makeNewId();
        newCard.setId(id);
        newCard.setCard(reading.getSentence());

        newCard.setReference(reading.getId());
        newCard.setTranslation(reading.getTranslation());
        newCard.setCategory(Reading.class.getSimpleName());

        newCard.setUploaded(false);
        newCard.setSelected(0);
        newCard.setMastering_level(level);
        newCard.setAlready_read(1);
        newCard.setType("sentence");
        newCard.setDatecreated(new Date());
        newCard.setDatemodified(new Date());
        newCard.setRepeat(0);
        newCard.setReviewed(true);
        newCard.setState(SM2.State.NEW);
        newCard.setE_factor(2.5);
        newCard.setInterval(1);
        newCard.setNext_show(new Date());
        newCard.setEasy_counter(level == 4 ? 1 : 0);
        newCard.setRead_repeat(0);

        // Commit changes
        updateMetadata(readingInfoDao.findFirstEqualTo("file_id", reading.getFile_id()), reading.getSentence(), "sentence");
        flashcardDao.copyOrUpdate(newCard);
    }

    private void addOrUpdateFlashcardWord(FlashcardDao flashcardDao, ReadingInfoDao readingInfoDao, RealmResults<Dictionary> dictionaries, int lessonId, Word wordAlgo, String word) {
        if (dictionaries.isEmpty()) return;

        int toLevel = 1;

        // Local Word is forbidden
        // **********************************************************************************
        Dictionary dictionary = dictionaries.where().equalTo("word", AppUtils.normalizeString(word), Case.INSENSITIVE).findFirst();
        if (dictionary != null && dictionary.getLocal_word() > AppConstants.DEFAULT_LOCAL_WORD_CONSTANT) {
            return;
        }

        // Update flashcard
        // **********************************************************************************
        Flashcard card = flashcardDao.copyFromDb(flashcardDao.where()
                .equalTo("card", AppUtils.normalizeString(word), Case.INSENSITIVE)
                .equalTo("type", "word").findFirst());
        if (card != null) {
            toLevel = card.getMastering_level() < 4 ? card.getMastering_level() + 1 : card.getMastering_level();

            wordAlgo.calculate(card, toLevel, SELECT_WORDS);

            card.setAlready_read(1);
            card.setUploaded(false);
            card.setDatemodified(new Date());
            card.setRead_repeat(card.getRead_repeat() + 1);

            // Commit changes
            flashcardDao.update(card);
            flashcardDao.refresh();
            return;
        }

        // Add new flashcard
        // **********************************************************************************
        Flashcard newCard = new Flashcard();
        long id = flashcardDao.makeNewId();
        newCard.setId(id);
        newCard.setCard(AppUtils.normalizeString(word));

        dictionary = dictionaries.where().equalTo("word", AppUtils.normalizeString(word), Case.INSENSITIVE).findFirst();
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
        newCard.setMastering_level(toLevel);
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
        newCard.setEasy_counter(0);
        newCard.setRead_repeat(0);

        // Commit changes
        flashcardDao.copyOrUpdate(newCard);
        updateMetadata(readingInfoDao.findFirstEqualTo("file_id", lessonId), word, "word");
    }

    private void updateMetadata(ReadingInfo infoToUpdate, String card, String type) {
        if (Looper.myLooper() == Looper.getMainLooper())
            throw new IllegalThreadStateException("Cannot update metadata on main thread");

        Realm database = Realm.getDefaultInstance();

        /* TODO
           Improvement
           Cari semua reading yang ada/contains kata {card} lalu update metadata-nya
         */
        RealmResults<Reading> readings;
        if (AppUtils.isWord(card) && type.equalsIgnoreCase("word")) {
            readings = database.where(Reading.class)
                    .contains("sentence", " " + card + " ", Case.INSENSITIVE).or()
                    .contains("sentence", " " + card, Case.INSENSITIVE).or()
                    .contains("sentence", card + " ", Case.INSENSITIVE)
                    .findAll();
        } else {
            ReadingInfoMeta infoMeta = database.where(ReadingInfoMeta.class).equalTo("menu_id", infoToUpdate.getMenu_id()).findFirst();
            if (infoMeta != null) {
                database.beginTransaction();
                infoMeta.setSentenceMarked(infoMeta.getSentenceMarked() + 1);
                database.commitTransaction();
                database.close();
            }
            return;
        }

        ReadingInfo info = null;
        for (Reading reading : readings) {
            if (info != null && reading.getFile_id() == info.getFile_id()) continue;

            String sentence = reading.getSentence().replace("*", "");
            String findWord = card.replace("*", "");
            if (!sentence.matches(".*(?<=\\W" + findWord + "|^" + findWord + ")(?=\\W|" + findWord + "$).*")) {
                info = null;
                continue;
            }

            info = database.where(ReadingInfo.class).equalTo("file_id", reading.getFile_id()).findFirst();
            ReadingInfoMeta infoMeta = database.where(ReadingInfoMeta.class).equalTo("menu_id", info.getMenu_id()).findFirst();
            if (infoMeta != null) {
                database.beginTransaction();
                if (AppUtils.isWord(card)) {
                    infoMeta.setWordMarked(infoMeta.getWordMarked() + 1);
                } else {
                    infoMeta.setSentenceMarked(infoMeta.getSentenceMarked() + 1);
                }
                database.commitTransaction();
                database.refresh();
            }
        }
        database.close();

        // =====================


//        ReadingInfo readingInfo = database.copyFromRealm(infoToUpdate);
//
//        database.beginTransaction();
//        ReadingInfoMeta meta = database.where(ReadingInfoMeta.class).equalTo("menu_id", readingInfo.getMenu_id()).findFirst();
//        if (meta != null) {
//            if (AppUtils.isWord(card)) {
//                meta.setWordMarked(meta.getWordMarked() + 1);
//            } else {
//                meta.setSentenceMarked(meta.getSentenceMarked() + 1);
//            }
//        }
//        database.commitTransaction();

//        boolean wordFound;
//        RealmResults<ReadingInfo> readingInfos = database.where(ReadingInfo.class).equalTo("episode_id", readingInfo.getEpisode_id()).findAll();
//        for (ReadingInfo info : readingInfos) {
//            wordFound = false;
//            if (info.getFile_id() == readingInfo.getFile_id()) continue;
//
//            RealmResults<Reading> readings = database.where(Reading.class).equalTo("file_id", info.getFile_id()).findAll();
//            for (Reading reading : readings) {
//                if (reading.getSentence() == null || reading.getSentence().isEmpty()) continue;
//
//                String[] words = reading.getSentence().split(" ");
//                for (String word : words) {
//                    if (!word.trim().replaceAll("[^a-zA-Z_0-9\\s]", "").isEmpty() && !word.trim().equals("") && AppUtils.normalizeString(word).toLowerCase().equals(card.toLowerCase())) {
//                        final long menuId = info.getMenu_id();
//                        database.executeTransaction(db -> {
//                            ReadingInfoMeta infoMeta = db.where(ReadingInfoMeta.class).equalTo("menu_id", menuId).findFirst();
//                            if (infoMeta != null) {
//                                if (AppUtils.isWord(card)) {
//                                    infoMeta.setWordMarked(infoMeta.getWordMarked() + 1);
//                                } else {
//                                    infoMeta.setSentenceMarked(infoMeta.getSentenceMarked() + 1);
//                                }
//                            }
//                        });
//                        wordFound = true;
//                        break;
//                    }
//                }
//                if (wordFound) break;
//            }
//        }

//        database.close();
    }

    @Override public void wakeup() {
        initializeData();
        Log.d(TAG, "wakeup");
    }
}
