package com.neosolusi.expresslingua.features.group.member;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;
import android.content.Context;

import com.neosolusi.expresslingua.AppInjectors;
import com.neosolusi.expresslingua.data.entity.Member;
import com.neosolusi.expresslingua.data.entity.MemberProgress;
import com.neosolusi.expresslingua.data.network.NetworkDataSource;

import io.realm.Realm;

public class MemberViewModel extends ViewModel {

    private Realm database;
    private NetworkDataSource mDataSource;
    private MutableLiveData<MemberProgress> mMemberProgress;

    public MemberViewModel(Context context) {
        database = Realm.getDefaultInstance();
        mDataSource = AppInjectors.provideNetworkDataSource(context);
    }

    @Override protected void onCleared() {
        database.close();
    }

}
