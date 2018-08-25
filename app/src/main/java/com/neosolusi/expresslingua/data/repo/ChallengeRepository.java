package com.neosolusi.expresslingua.data.repo;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;

import com.neosolusi.expresslingua.AppConstants;
import com.neosolusi.expresslingua.AppExecutors;
import com.neosolusi.expresslingua.data.dao.ChallengeDao;
import com.neosolusi.expresslingua.data.dao.ChallengeHardDao;
import com.neosolusi.expresslingua.data.dao.FlashcardDao;
import com.neosolusi.expresslingua.data.dao.ReadingDao;
import com.neosolusi.expresslingua.data.dao.UserDao;
import com.neosolusi.expresslingua.data.entity.Challenge;
import com.neosolusi.expresslingua.data.entity.ChallengeHard;
import com.neosolusi.expresslingua.data.entity.Flashcard;
import com.neosolusi.expresslingua.data.entity.Reading;
import com.neosolusi.expresslingua.data.network.NetworkDataSource;

import java.util.Date;
import java.util.List;
import java.util.UUID;

import io.realm.Realm;
import io.realm.RealmResults;

public class ChallengeRepository extends BaseRepository<Challenge> {

    private static final String TAG = ChallengeRepository.class.getSimpleName();

    // Singleton Instantiation
    private static final Object LOCK = new Object();
    private static ChallengeRepository mInstance;
    private final ChallengeDao mDao;
    private final UserDao mUserDao;
    private final FlashcardDao mFlashcardDao;
    private final AppExecutors mExecutors;
    private final NetworkDataSource mNetworkSource;
    private final SharedPreferences mPref;

    private ChallengeRepository(ChallengeDao dao, UserDao userDao, FlashcardDao flashcardDao, SharedPreferences sharedPreferences, NetworkDataSource networkDataSource, AppExecutors executors) {
        super(dao);
        this.mDao = dao;
        this.mUserDao = userDao;
        this.mFlashcardDao = flashcardDao;
        this.mExecutors = executors;
        this.mNetworkSource = networkDataSource;
        this.mPref = sharedPreferences;
    }

    public synchronized static ChallengeRepository getInstance(ChallengeDao dao, UserDao userDao, FlashcardDao flashcardDao, SharedPreferences sharedPreferences, NetworkDataSource networkDataSource, AppExecutors executors) {
        if (mInstance == null) {
            synchronized (LOCK) {
                mInstance = new ChallengeRepository(dao, userDao, flashcardDao, sharedPreferences, networkDataSource, executors);
            }
        }

        return mInstance;
    }

    public synchronized void initializeData() {
        if (! isFetchNeeded()) {
            mDao.housekeeping();
            return;
        }

        mDao.autoCreateChallenges();
    }

    public synchronized void upload() {
        int notSeen = mDao.findAllEqualTo("seen", false).size();
        int skipped = mDao.findAllEqualTo("skip", true).size();
        int incorrect = ((Long) mDao.findAllEqualTo("correct", false).where().equalTo("seen", true).equalTo("skip", false).count()).intValue();
        int correct = mDao.findAllEqualTo("correct", true).size();

        RealmResults<Flashcard> flashcards = mFlashcardDao.findAll();
        Long greenCount = flashcards.where().equalTo("type", "word").equalTo("mastering_level", 4).equalTo("already_read", 1).count();
        Long yellowCount = flashcards.where().equalTo("type", "word").equalTo("mastering_level", 3).equalTo("already_read", 1).count();
        Long orangeCount = flashcards.where().equalTo("type", "word").equalTo("mastering_level", 2).equalTo("already_read", 1).count();
        Long redCount = flashcards.where().equalTo("type", "word").equalTo("mastering_level", 1).equalTo("already_read", 1).count();
        Long blueCount = flashcards.where().equalTo("type", "word").equalTo("mastering_level", 5).equalTo("already_read", 1).count();

        Long greenSentenceCount = flashcards.where().equalTo("type", "sentence").equalTo("category", Reading.class.getSimpleName()).equalTo("mastering_level", 4).equalTo("already_read", 1).count();
        Long yellowSentenceCount = flashcards.where().equalTo("type", "sentence").equalTo("category", Reading.class.getSimpleName()).equalTo("mastering_level", 3).equalTo("already_read", 1).count();
        Long orangeSentenceCount = flashcards.where().equalTo("type", "sentence").equalTo("category", Reading.class.getSimpleName()).equalTo("mastering_level", 2).equalTo("already_read", 1).count();
        Long redSentenceCount = flashcards.where().equalTo("type", "sentence").equalTo("category", Reading.class.getSimpleName()).equalTo("mastering_level", 1).equalTo("already_read", 1).count();
        Long blueSentenceCount = flashcards.where().equalTo("type", "sentence").equalTo("category", Reading.class.getSimpleName()).equalTo("mastering_level", 5).equalTo("already_read", 1).count();

        String userid = mUserDao.findActiveUser().getUserid();

        if (notSeen == 0 && skipped == 0 && incorrect == 0 && correct == 0) return;

        mExecutors.networkIO().execute(() -> {
            Log.d(TAG, "Upload Challenge Begin");
            Bundle bundle = new Bundle();
            bundle.putString("userid", userid);
            bundle.putInt("not_seen", notSeen);
            bundle.putInt("skipped", skipped);
            bundle.putInt("incorrect", incorrect);
            bundle.putInt("correct", correct);

            bundle.putInt("w_red", redCount.intValue());
            bundle.putInt("w_orange", orangeCount.intValue());
            bundle.putInt("w_yellow", yellowCount.intValue());
            bundle.putInt("w_green", greenCount.intValue());
            bundle.putInt("w_blue", blueCount.intValue());

            bundle.putInt("s_red", redSentenceCount.intValue());
            bundle.putInt("s_orange", orangeSentenceCount.intValue());
            bundle.putInt("s_yellow", yellowSentenceCount.intValue());
            bundle.putInt("s_green", greenSentenceCount.intValue());
            bundle.putInt("s_blue", blueSentenceCount.intValue());

            mNetworkSource.startNetworkServiceWithExtra("upload_challenge", bundle);
        });
    }

    public void resetSelectedChallenges(RealmResults<Challenge> challenges) {
        mDao.resetSelectedChallenges(challenges);
    }

    public void update(Challenge challenge) {
        mDao.update(challenge);
        mDao.refresh();
    }

    public void createChallenge(Reading reading, int level) {
        Realm db = Realm.getDefaultInstance();
        ChallengeDao dao;
        ChallengeHardDao challengeHardDao = new ChallengeHardDao(db);
        ReadingDao readingDao = new ReadingDao(db);

        if (Looper.myLooper() != Looper.getMainLooper()) {
            dao = new ChallengeDao(db);
        } else {
            dao = mDao;
        }

        if (level < 2) return;
        if (reading.getTranslation() == null || reading.getTranslation().trim().isEmpty()) return;

        List<Challenge> challenges = dao.findAll();
        double maxOrangeLevel = mPref.getInt(AppConstants.PREFERENCE_MAX_CHALLENGE_ORANGE, 5);

        if (level == 2 && !challenges.isEmpty()) {
            double foundCriteria = 0;
            for (Challenge challenge : challenges) {
                Reading read = readingDao.findFirstEqualTo("id", challenge.getReference());
                if (read != null && read.getMastering_level() == 2) foundCriteria++;
            }

            double currentMinLevelSize = 0;
            if (foundCriteria > 0)
                currentMinLevelSize = foundCriteria / dao.findAll().size();
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

            ChallengeHard challenge = challengeHardDao.findFirstEqualTo("reference", reading.getId());
            if (challenge == null) {
                ChallengeHard newChallenge = new ChallengeHard();
                newChallenge.setId(UUID.randomUUID().getMostSignificantBits() & Long.MAX_VALUE);
                newChallenge.setCategory("Listening");
                newChallenge.setReference(reading.getId());
                newChallenge.setSeen(false);
                newChallenge.setSkip(false);
                newChallenge.setCorrect(false);
                newChallenge.setDatecreated(new Date());
                challengeHardDao.copyOrUpdate(newChallenge);
            }
        }
    }

    private void createEasyChallenge(Reading reading) {
        Realm db = Realm.getDefaultInstance();
        ChallengeDao dao;

        if (Looper.myLooper() != Looper.getMainLooper()) {
            dao = new ChallengeDao(db);
        } else {
            dao = mDao;
        }

        Challenge challenge = dao.findFirstEqualTo("reference", reading.getId());
        if (challenge == null) {
            Challenge newChallenge = new Challenge();
            newChallenge.setId(UUID.randomUUID().getMostSignificantBits() & Long.MAX_VALUE);
            newChallenge.setCategory("Listening");
            newChallenge.setReference(reading.getId());
            newChallenge.setSeen(false);
            newChallenge.setSkip(false);
            newChallenge.setCorrect(false);
            newChallenge.setDatecreated(new Date());
            dao.copyOrUpdate(newChallenge);
        }

        db.close();
    }

    private void moveHardChallengeToEasy() {
        Realm db = Realm.getDefaultInstance();
        ChallengeHardDao dao = new ChallengeHardDao(db);
        ReadingDao readingDao = new ReadingDao(db);

        RealmResults<ChallengeHard> challengeHards = dao.findAll();
        for (ChallengeHard challenge : challengeHards) {
            createEasyChallenge(readingDao.findFirstEqualTo("id", challenge.getReference()));
        }
        dao.deleteAll();

        db.close();
    }

    private boolean isHardChallenge(Reading reading) {
        return reading.getKal_panjang() > 0;
    }

    @Override public boolean isFetchNeeded() {
        return mDao.count() == 0;
    }

    @Override public void wakeup() {
        Log.d(TAG, "wakeup");
    }
}
