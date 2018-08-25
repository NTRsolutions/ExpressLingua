package com.neosolusi.expresslingua.data.repo;

import android.arch.lifecycle.LiveData;
import android.os.Bundle;
import android.util.Log;

import com.neosolusi.expresslingua.AppExecutors;
import com.neosolusi.expresslingua.data.dao.FlashcardDao;
import com.neosolusi.expresslingua.data.dao.UserDao;
import com.neosolusi.expresslingua.data.entity.Flashcard;
import com.neosolusi.expresslingua.data.entity.FlashcardParcel;
import com.neosolusi.expresslingua.data.network.NetworkDataSource;

import java.util.List;

import io.realm.RealmResults;

public class FlashcardRepository extends BaseRepository<Flashcard> {

    private static final String TAG = FlashcardRepository.class.getSimpleName();

    // Singleton Instantiation
    private static final Object LOCK = new Object();
    private static FlashcardRepository mInstance;
    private final UserDao mUserDao;
    private final FlashcardDao mDao;
    private final NetworkDataSource mNetworkSource;
    private final AppExecutors mExecutors;
    private boolean mInitialized = false;
    private boolean mHasNoData = false;

    private FlashcardRepository(FlashcardDao flashcardDao, UserDao userDao, NetworkDataSource networkDataSource, AppExecutors executors) {
        super(flashcardDao);
        mDao = flashcardDao;
        mUserDao = userDao;
        mNetworkSource = networkDataSource;
        mExecutors = executors;

        LiveData<List<Flashcard>> networkFlashcard = mNetworkSource.getFlashcards();
        networkFlashcard.observeForever(flashcards -> {
            if (flashcards == null) {
                mHasNoData = true;
                return;
            }
            mDao.insertAsync(flashcards);
            housekeeping();
        });

        LiveData<Flashcard> networkUploadedFlashcard = mNetworkSource.getUploadedFlashcard();
        networkUploadedFlashcard.observeForever(flashcard -> {
            if (flashcard == null) return;
            Log.d(TAG, "Upload Flashcard Success");
            flashcard.setUploaded(true);
            mDao.copyOrUpdate(flashcard);
            mDao.refresh();
            upload();
        });
    }

    public synchronized static FlashcardRepository getInstance(FlashcardDao flashcardDao, UserDao userDao, NetworkDataSource networkDataSource, AppExecutors executors) {
        if (mInstance == null) {
            synchronized (LOCK) {
                mInstance = new FlashcardRepository(flashcardDao, userDao, networkDataSource, executors);
            }
        }

        return mInstance;
    }

    public synchronized void initializeData() {
        if (mInitialized) return;
        mInitialized = true;

        boolean fetchNeed = isFetchNeeded();
        String userid = mUserDao.findActiveUser().getUserid();
        mExecutors.networkIO().execute(() -> {
            Bundle bundle = new Bundle();
            bundle.putString("userid", userid);
            if (fetchNeed) {
                mNetworkSource.startNetworkServiceWithExtra("flashcard", bundle);
            }
        });
    }

    public synchronized void upload() {
        String userid = mUserDao.findActiveUser().getUserid();
        Flashcard flashcard = mDao.findFirstEqualTo("uploaded", false);

        if (flashcard == null) {
            Log.d(TAG, "Does'n have pending upload");
            return;
        }

        FlashcardParcel parcel = new FlashcardParcel(flashcard);
        mExecutors.networkIO().execute(() -> {
            Log.d(TAG, "Upload Flashcard Begin");
            Bundle bundle = new Bundle();
            bundle.putString("userid", userid);
            bundle.putParcelable("flashcard", parcel);
            mNetworkSource.startNetworkServiceWithExtra("upload_flashcard", bundle);
        });
    }

    @Override public boolean isFetchNeeded() {
        return 0 == mDao.count() && !mHasNoData;
    }

    public LiveData<RealmResults<Flashcard>> getFlashcards(String type) {
        return mDao.getFlashcards(type);
    }

    public boolean isAllowToAddFlashcard(String type) {
        return mDao.isAllowToAddFlashcard(type);
    }

    public void resetSelectedFlashcards(RealmResults<Flashcard> flashcards) {
        mDao.resetSelectedFlashcards(flashcards);
    }

    public void update(Flashcard flashcard) {
        mDao.update(flashcard);
        mDao.refresh();
    }

    public void update(List<Flashcard> flashcards) {
        mDao.update(flashcards);
        mDao.refresh();
    }

    private void housekeeping() {
        mDao.housekeeping();
    }

    public long makeNewId() {
        return mDao.makeNewId();
    }

    @Override public void wakeup() {
        mDao.housekeepingAgain();
        Log.d(TAG, "wakeup");
    }
}
