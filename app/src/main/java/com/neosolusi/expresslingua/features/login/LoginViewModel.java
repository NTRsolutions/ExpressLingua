package com.neosolusi.expresslingua.features.login;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.ViewModel;

import com.neosolusi.expresslingua.data.entity.User;
import com.neosolusi.expresslingua.data.repo.UserRepository;

import io.realm.Realm;
import io.realm.RealmResults;

public class LoginViewModel extends ViewModel {

    private Realm database;
    private UserRepository mUserRepo;
    private LiveData<RealmResults<User>> mUsers;

    public LoginViewModel(UserRepository userRepository) {
        database = Realm.getDefaultInstance();
        mUserRepo = userRepository;
        mUsers = mUserRepo.findAllAsync();
    }

    @Override protected void onCleared() {
        database.close();
    }

    public LiveData<RealmResults<User>> getUsers() {
        return mUsers;
    }

    public void login(String userid, String password) {
        mUserRepo.startFetchService(userid, password);
    }
}
