package com.neosolusi.expresslingua.data.repo;

import android.arch.lifecycle.LiveData;
import android.util.Log;

import com.neosolusi.expresslingua.AppExecutors;
import com.neosolusi.expresslingua.data.dao.ReadingInfoDao;
import com.neosolusi.expresslingua.data.entity.ReadingInfo;
import com.neosolusi.expresslingua.data.network.NetworkDataSource;

import java.util.List;

public class ReadingInfoRepository extends BaseRepository<ReadingInfo> {

    private static final String TAG = ReadingInfoRepository.class.getSimpleName();

    // Singleton Instantiation
    private static final Object LOCK = new Object();
    private static ReadingInfoRepository mInstance;
    private final ReadingInfoDao mDao;
    private final AppExecutors mExecutors;
    private final NetworkDataSource mNetworkSource;
    private boolean mInitialized = false;

    private ReadingInfoRepository(ReadingInfoDao dao, NetworkDataSource networkDataSource, AppExecutors executors) {
        super(dao);
        this.mDao = dao;
        this.mExecutors = executors;
        this.mNetworkSource = networkDataSource;

        LiveData<List<ReadingInfo>> networkData = mNetworkSource.getReadingInfos();
        networkData.observeForever(readingInfos -> {
            if (readingInfos == null) return;
            if (mDao.count() == 0) {
                mDao.insertAsync(readingInfos);
            } else {
                mDao.copyOrUpdateAsync(readingInfos);
            }
        });
    }

    public synchronized static ReadingInfoRepository getInstance(ReadingInfoDao dao, NetworkDataSource networkDataSource, AppExecutors executors) {
        if (mInstance == null) {
            synchronized (LOCK) {
                mInstance = new ReadingInfoRepository(dao, networkDataSource, executors);
            }
        }

        return mInstance;
    }

    private synchronized void initializeData() {
        if (mInitialized) return;
        mInitialized = true;

        boolean fetchNeed = isFetchNeeded();
        mExecutors.networkIO().execute(() -> {
            if (fetchNeed) mNetworkSource.startNetworkService("readingInfo");
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
