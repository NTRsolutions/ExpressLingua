package com.neosolusi.expresslingua.features.group.listgroup;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.ViewModel;

import com.neosolusi.expresslingua.data.entity.Group;
import com.neosolusi.expresslingua.data.repo.GroupRepository;

import io.realm.Realm;
import io.realm.RealmResults;

public class ListGroupViewModel extends ViewModel {

    private Realm mDatabase;
    private GroupRepository mGroupRepo;
    private LiveData<RealmResults<Group>> mGroups;

    public ListGroupViewModel(GroupRepository groupRepository) {
        mDatabase = Realm.getDefaultInstance();
        mGroupRepo = groupRepository;
        mGroups = mGroupRepo.findAllAsync();
    }

    @Override protected void onCleared() {
        mDatabase.close();
    }

    public LiveData<RealmResults<Group>> getGroups() {
        return mGroups;
    }

}
