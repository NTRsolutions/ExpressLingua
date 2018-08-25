package com.neosolusi.expresslingua.data.repo;

import android.util.Log;

import com.neosolusi.expresslingua.data.dao.ChallengeHardDao;
import com.neosolusi.expresslingua.data.entity.ChallengeHard;

public class ChallengeHardRepository extends BaseRepository<ChallengeHard> {

    private static final String TAG = ChallengeHardRepository.class.getSimpleName();

    // Singleton Instantiation
    private static final Object LOCK = new Object();
    private static ChallengeHardRepository mInstance;
    private final ChallengeHardDao mDao;
    private boolean mInitialized = false;

    private ChallengeHardRepository(ChallengeHardDao dao) {
        super(dao);
        this.mDao = dao;
    }

    public synchronized static ChallengeHardRepository getInstance(ChallengeHardDao dao) {
        if (mInstance == null) {
            synchronized (LOCK) {
                mInstance = new ChallengeHardRepository(dao);
            }
        }

        return mInstance;
    }

    public synchronized void initializeData() {
        if (mInitialized) return;
        mInitialized = true;
    }

    @Override public boolean isFetchNeeded() {
        return mDao.count() == 0;
    }

    @Override public void wakeup() {
        Log.d(TAG, "wakeup");
    }

}
