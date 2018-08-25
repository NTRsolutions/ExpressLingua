package com.neosolusi.expresslingua.data.repo;

import android.arch.lifecycle.LiveData;
import android.util.Log;

import com.neosolusi.expresslingua.AppExecutors;
import com.neosolusi.expresslingua.data.dao.DictionaryDao;
import com.neosolusi.expresslingua.data.entity.Dictionary;
import com.neosolusi.expresslingua.data.network.NetworkDataSource;

import java.util.List;

public class DictionaryRepository extends BaseRepository<Dictionary> {

    private static final String TAG = DictionaryRepository.class.getSimpleName();

    // Singleton Instantiation
    private static final Object LOCK = new Object();
    private static DictionaryRepository mInstance;
    private final DictionaryDao mDao;
    private final AppExecutors mExecutors;
    private final NetworkDataSource mNetworkSource;
    private boolean mInitialized = false;

    private DictionaryRepository(DictionaryDao dao, NetworkDataSource networkDataSource, AppExecutors executors) {
        super(dao);
        this.mDao = dao;
        this.mExecutors = executors;
        this.mNetworkSource = networkDataSource;

        LiveData<List<Dictionary>> networkData = mNetworkSource.getDictionaries();
        networkData.observeForever(dictionaries -> {
            if (dictionaries == null) return;
            if (mDao.count() == 0) {
                mDao.insertAsync(dictionaries);
            } else {
                mDao.copyOrUpdateAsync(dictionaries);
            }
        });
    }

    public synchronized static DictionaryRepository getInstance(DictionaryDao dao, NetworkDataSource networkDataSource, AppExecutors executors) {
        if (mInstance == null) {
            synchronized (LOCK) {
                mInstance = new DictionaryRepository(dao, networkDataSource, executors);
            }
        }

        return mInstance;
    }

    private synchronized void initializeData() {
        if (mInitialized) return;
        mInitialized = true;

        boolean fetchNeed = isFetchNeeded();
        mExecutors.networkIO().execute(() -> {
            if (fetchNeed) mNetworkSource.startNetworkService("dictionary");
        });
    }

    @Override public boolean isFetchNeeded() {
        return 0 == mDao.count();
    }

    @Override public void wakeup() {
        initializeData();
        Log.d(TAG, "wakeup");
    }
}
