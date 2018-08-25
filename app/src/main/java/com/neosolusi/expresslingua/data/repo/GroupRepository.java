package com.neosolusi.expresslingua.data.repo;

import android.arch.lifecycle.LiveData;
import android.os.Bundle;
import android.util.Log;

import com.neosolusi.expresslingua.AppExecutors;
import com.neosolusi.expresslingua.AppInjectors;
import com.neosolusi.expresslingua.ExpressLingua;
import com.neosolusi.expresslingua.data.dao.GroupDao;
import com.neosolusi.expresslingua.data.entity.Group;
import com.neosolusi.expresslingua.data.network.NetworkDataSource;

import java.io.File;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;

public class GroupRepository extends BaseRepository<Group> {

    private static final String TAG = GroupRepository.class.getSimpleName();

    // Singleton Instantiation
    private static final Object LOCK = new Object();
    private static GroupRepository mInstance;
    private final GroupDao mDao;
    private final AppExecutors mExecutors;
    private final NetworkDataSource mNetworkSource;
    private boolean mInitialized = false;

    private GroupRepository(GroupDao dao, NetworkDataSource networkDataSource, AppExecutors executors) {
        super(dao);
        this.mDao = dao;
        this.mExecutors = executors;
        this.mNetworkSource = networkDataSource;

        LiveData<List<Group>> networkData = mNetworkSource.getGroups();
        networkData.observeForever(groups -> {
            if (groups == null) return;
            if (mDao.count() == 0) {
                mDao.insertAsync(groups);
            } else {
                mDao.copyOrUpdateAsync(groups);
            }
        });

        LiveData<Group> networkUploadedGroup = mNetworkSource.getUploadedGroup();
        networkUploadedGroup.observeForever(group -> {
            if (group == null) return;
            Log.d(TAG, "Upload group Success");
            mDao.copyOrUpdateAsync(group);
            mDao.refresh();
        });
    }

    public synchronized static GroupRepository getInstance(GroupDao dao, NetworkDataSource networkDataSource, AppExecutors executors) {
        if (mInstance == null) {
            synchronized (LOCK) {
                mInstance = new GroupRepository(dao, networkDataSource, executors);
            }
        }

        return mInstance;
    }

    private synchronized void initializeData() {
        if (mInitialized) return;
        mInitialized = true;

        boolean fetchNeed = isFetchNeeded();
        mExecutors.networkIO().execute(() -> {
            if (fetchNeed) mNetworkSource.startNetworkService("show_groups");
        });
    }

    public synchronized void upload(Group group, boolean isEdit) {
        if (group == null) {
            Log.d(TAG, "Does'n have pending upload");
            return;
        }

        mExecutors.networkIO().execute(() -> {
            Log.d(TAG, "Upload Group Begin");
            Bundle bundle = new Bundle();
            bundle.putLong("id", group.getId());
            bundle.putString("name", group.getName());
            bundle.putString("owner", group.getAdmin());
            bundle.putInt("privacy", group.getPrivacy());
            bundle.putInt("translate", group.getTranslate());
            bundle.putString("remarks", group.getDescription());
            bundle.putString("url", group.getUrl());

            if (isEdit) {
                mNetworkSource.startNetworkServiceWithExtra("update_group", bundle);
            } else {
                mNetworkSource.startNetworkServiceWithExtra("upload_group", bundle);
            }
        });
    }

    public synchronized void uploadImage(File file, Group group) {
        mExecutors.networkIO().execute(() -> {
            Bundle bundle = new Bundle();
            bundle.putLong("groupId", group.getId());
            mNetworkSource.startNetworkServiceWithExtraFile("group", file, bundle);
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
