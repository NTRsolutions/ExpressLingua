package com.neosolusi.expresslingua.data.repo;

import android.arch.lifecycle.LiveData;
import android.os.Bundle;
import android.util.Log;

import com.neosolusi.expresslingua.AppExecutors;
import com.neosolusi.expresslingua.data.dao.UserDao;
import com.neosolusi.expresslingua.data.entity.User;
import com.neosolusi.expresslingua.data.entity.UserParcel;
import com.neosolusi.expresslingua.data.network.NetworkDataSource;

import java.io.File;

public class UserRepository extends BaseRepository<User> {

    private static final String TAG = UserRepository.class.getSimpleName();

    // Singleton Instantiation
    private static final Object LOCK = new Object();
    private static UserRepository mInstance;
    private final UserDao mDao;
    private final NetworkDataSource mNetworkSource;
    private final AppExecutors mExecutors;
    private boolean mInitialized = false;

    private UserRepository(UserDao dao, NetworkDataSource networkDataSource, AppExecutors executors) {
        super(dao);
        mDao = dao;
        mNetworkSource = networkDataSource;
        mExecutors = executors;

        LiveData<User> networkData = mNetworkSource.getUser();
        networkData.observeForever(user -> {
            if (user == null) return;
            mDao.copyOrUpdateAsync(user);
            mDao.refresh();
        });

        LiveData<User> networkUploadedUser = mNetworkSource.getUploadedUser();
        networkUploadedUser.observeForever(user -> {
            if (user == null) return;
            Log.d(TAG, "Upload User Success");
        });
    }

    public synchronized static UserRepository getInstance(UserDao dao, NetworkDataSource networkDataSource, AppExecutors executors) {
        if (mInstance == null) {
            synchronized (LOCK) {
                mInstance = new UserRepository(dao, networkDataSource, executors);
            }
        }

        return mInstance;
    }

    private synchronized void initializeData() {
        if (mInitialized) return;
        mInitialized = true;
    }

    public synchronized void upload() {
        User user = mDao.findActiveUser();

        if (user == null) {
            Log.d(TAG, "Does'n have pending upload");
            return;
        }

        UserParcel parcel = new UserParcel(user);
        mExecutors.networkIO().execute(() -> {
            Log.d(TAG, "Upload User Begin");
            Bundle bundle = new Bundle();
            bundle.putParcelable("user", parcel);
            mNetworkSource.startNetworkServiceWithExtra("upload_user", bundle);
        });
    }

    public synchronized void uploadImage(File file, User user) {
        mExecutors.networkIO().execute(() -> {
            Bundle bundle = new Bundle();
            bundle.putLong("userId", user.getId());
            bundle.putString("userName", user.getUserid());
            mNetworkSource.startNetworkServiceWithExtraFile("user", file, bundle);
        });
    }

    @Override public boolean isFetchNeeded() {
        return false;
    }

    @Override public void wakeup() {
        initializeData();
        Log.d(TAG, "wakeup");
    }

    public void startFetchService(String userid, String password) {
        mExecutors.networkIO().execute(() -> {
            Bundle bundle = new Bundle();
            bundle.putString("userid", userid);
            bundle.putString("password", password);
            mNetworkSource.startNetworkServiceWithExtra("login", bundle);
        });
    }

    public void startRegisterService(User user) {
        mExecutors.networkIO().execute(() -> {
            Bundle bundle = new Bundle();
            bundle.putString("userid", user.getUserid());
            bundle.putString("email", user.getEmail());
            bundle.putString("password", user.getPassword());
            bundle.putInt("commercial_status", user.getCommercial_status());
            bundle.putString("cell_no", user.getCell_no());
            bundle.putString("address", user.getAddress());
            bundle.putString("city", user.getCity());
            bundle.putString("province", user.getProvince());
            bundle.putString("country", user.getCountry());
            bundle.putDouble("gps_latitude", user.getGps_lat());
            bundle.putDouble("gps_longitude", user.getGps_lng());
            mNetworkSource.startNetworkServiceWithExtra("register", bundle);
        });
    }

    public void startFindUserByPhone(String phone) {
        mExecutors.networkIO().execute(() -> {
            Bundle bundle = new Bundle();
            bundle.putString("phone", phone);
            mNetworkSource.startNetworkServiceWithExtra("find_user", bundle);
        });
    }

    public User findActiveUser() {
        return mDao.findActiveUser();
    }

    public User findActiveUserCopy() {
        return mDao.findActiveUserCopy();
    }

}
