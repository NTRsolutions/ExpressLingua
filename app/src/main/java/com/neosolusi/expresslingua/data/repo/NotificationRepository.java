package com.neosolusi.expresslingua.data.repo;

import android.arch.lifecycle.LiveData;
import android.util.Log;

import com.neosolusi.expresslingua.AppExecutors;
import com.neosolusi.expresslingua.data.dao.NotificationDao;
import com.neosolusi.expresslingua.data.entity.Notification;
import com.neosolusi.expresslingua.data.network.NetworkDataSource;

import java.util.List;

public class NotificationRepository extends BaseRepository<Notification> {

    private static final String TAG = NotificationRepository.class.getSimpleName();

    // Singleton Instantiation
    private static final Object LOCK = new Object();
    private static NotificationRepository mInstance;
    private final NotificationDao mDao;
    private final NetworkDataSource mNetworkSource;
    private final AppExecutors mExecutors;
    private boolean mInitialized = false;

    private NotificationRepository(NotificationDao dao, NetworkDataSource networkDataSource, AppExecutors executors) {
        super(dao);
        mDao = dao;
        mNetworkSource = networkDataSource;
        mExecutors = executors;

        LiveData<List<Notification>> networkData = mNetworkSource.getNotifications();
        networkData.observeForever(notifications -> {
            if (notifications == null) return;
            mDao.insertAsync(notifications);
        });
    }

    public synchronized static NotificationRepository getInstance(NotificationDao dao, NetworkDataSource networkDataSource, AppExecutors executors) {
        if (mInstance == null) {
            synchronized (LOCK) {
                mInstance = new NotificationRepository(dao, networkDataSource, executors);
            }
        }

        return mInstance;
    }

    private synchronized void initializeData() {
        if (mInitialized) return;
        mInitialized = true;

        mExecutors.networkIO().execute(() -> {
            if (isFetchNeeded()) {
                mNetworkSource.startNetworkService("config");
                mNetworkSource.startNetworkService("notification");
            }
        });
    }

    @Override public boolean isFetchNeeded() {
        return true;
    }

    @Override public void wakeup() {
        initializeData();
        Log.d(TAG, "wakeup");
    }
}
