package com.neosolusi.expresslingua.data.repo;

import android.arch.lifecycle.LiveData;
import android.os.Bundle;
import android.util.Log;

import com.neosolusi.expresslingua.AppExecutors;
import com.neosolusi.expresslingua.data.dao.MemberDao;
import com.neosolusi.expresslingua.data.entity.Member;
import com.neosolusi.expresslingua.data.network.NetworkDataSource;

import java.util.List;

public class MemberRepository extends BaseRepository<Member> {

    private static final String TAG = MemberRepository.class.getSimpleName();

    // Singleton Instantiation
    private static final Object LOCK = new Object();
    private static MemberRepository mInstance;
    private final MemberDao mDao;
    private final AppExecutors mExecutors;
    private final NetworkDataSource mNetworkSource;
    private boolean mInitialized = false;

    private MemberRepository(MemberDao dao, NetworkDataSource networkDataSource, AppExecutors executors) {
        super(dao);
        this.mDao = dao;
        this.mExecutors = executors;
        this.mNetworkSource = networkDataSource;

        LiveData<List<Member>> networkData = mNetworkSource.getMembers();
        networkData.observeForever(members -> {
            if (members == null) return;

            for (Member member : members) {
                member.setUploaded(true);
            }

            if (mDao.count() == 0) {
                mDao.insertAsync(members);
            } else {
                mDao.copyOrUpdateAsync(members);
            }
        });

        LiveData<Member> networkUploadedMember = mNetworkSource.getUploadedMember();
        networkUploadedMember.observeForever(member -> {
            if (member == null) return;
            member.setUploaded(true);
            mDao.copyOrUpdate(member);
            mDao.refresh();
            upload();
        });
    }

    public synchronized static MemberRepository getInstance(MemberDao dao, NetworkDataSource networkDataSource, AppExecutors executors) {
        if (mInstance == null) {
            synchronized (LOCK) {
                mInstance = new MemberRepository(dao, networkDataSource, executors);
            }
        }

        return mInstance;
    }

    private synchronized void initializeData() {
        if (mInitialized) return;
        mInitialized = true;

        boolean fetchNeed = isFetchNeeded();
        mExecutors.networkIO().execute(() -> {
            if (fetchNeed) mNetworkSource.startNetworkService("show_members");
        });
    }

    public synchronized void upload() {
        Member member = mDao.findFirstCopyEqualTo("uploaded", false);
        if (member == null) {
            Log.d(TAG, "Does'n have pending upload");
            return;
        }

        mExecutors.networkIO().execute(() -> {
            Log.d(TAG, "Upload Member Begin");
            Bundle bundle = new Bundle();
            bundle.putLong("groupId", member.getGroup_id());
            bundle.putString("userId", member.getUser_id());
            bundle.putInt("approved", member.getApproved());
            bundle.putInt("permission", member.getPermission());
            mNetworkSource.startNetworkServiceWithExtra("upload_member", bundle);
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
