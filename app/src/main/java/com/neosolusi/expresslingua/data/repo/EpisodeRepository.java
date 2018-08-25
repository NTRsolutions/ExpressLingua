package com.neosolusi.expresslingua.data.repo;

import android.arch.lifecycle.LiveData;
import android.util.Log;

import com.neosolusi.expresslingua.AppExecutors;
import com.neosolusi.expresslingua.data.dao.EpisodeDao;
import com.neosolusi.expresslingua.data.entity.Episode;
import com.neosolusi.expresslingua.data.network.NetworkDataSource;

import java.util.List;

public class EpisodeRepository extends BaseRepository<Episode> {

    private static final String TAG = EpisodeRepository.class.getSimpleName();

    // Singleton Instantiation
    private static final Object LOCK = new Object();
    private static EpisodeRepository mInstance;
    private final EpisodeDao mDao;
    private final AppExecutors mExecutors;
    private final NetworkDataSource mNetworkSource;
    private boolean mInitialized = false;

    private EpisodeRepository(EpisodeDao dao, NetworkDataSource networkDataSource, AppExecutors executors) {
        super(dao);
        this.mDao = dao;
        this.mExecutors = executors;
        this.mNetworkSource = networkDataSource;

        LiveData<List<Episode>> networkData = mNetworkSource.getEpisodes();
        networkData.observeForever(episodes -> {
            if (episodes == null) return;
            if (mDao.count() == 0) {
                mDao.insertAsync(episodes);
            } else {
                mDao.copyOrUpdateAsync(episodes);
            }
        });
    }

    public synchronized static EpisodeRepository getInstance(EpisodeDao dao, NetworkDataSource networkDataSource, AppExecutors executors) {
        if (mInstance == null) {
            synchronized (LOCK) {
                mInstance = new EpisodeRepository(dao, networkDataSource, executors);
            }
        }

        return mInstance;
    }

    private synchronized void initializeData() {
        if (mInitialized) return;
        mInitialized = true;

        boolean fetchNeed = isFetchNeeded();
        mExecutors.networkIO().execute(() -> {
            if (fetchNeed) mNetworkSource.startNetworkService("episode");
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
