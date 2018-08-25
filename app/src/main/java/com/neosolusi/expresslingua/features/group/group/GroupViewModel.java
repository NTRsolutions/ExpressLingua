package com.neosolusi.expresslingua.features.group.group;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.ViewModel;

import com.neosolusi.expresslingua.data.entity.Group;
import com.neosolusi.expresslingua.data.entity.Member;
import com.neosolusi.expresslingua.data.entity.User;
import com.neosolusi.expresslingua.data.network.NetworkDataSource;
import com.neosolusi.expresslingua.data.repo.GroupRepository;
import com.neosolusi.expresslingua.data.repo.MemberRepository;
import com.neosolusi.expresslingua.data.repo.UserRepository;

import java.io.File;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmResults;

public class GroupViewModel extends ViewModel {

    private Realm database;
    private UserRepository mUserRepo;
    private GroupRepository mGroupRepo;
    private MemberRepository mMemberRepo;
    private NetworkDataSource mService;
    private LiveData<User> mFoundUser;
    private LiveData<RealmResults<Group>> mGroups;

    public GroupViewModel(GroupRepository groupRepository, MemberRepository memberRepository, UserRepository userRepository, NetworkDataSource dataSource) {
        database = Realm.getDefaultInstance();
        mUserRepo = userRepository;
        mGroupRepo = groupRepository;
        mMemberRepo = memberRepository;
        mService = dataSource;
        mGroups = mGroupRepo.findAllAsync();
    }

    @Override protected void onCleared() {
        database.close();
    }

    public LiveData<User> getUser() {
        return mFoundUser;
    }

    public LiveData<RealmResults<Group>> getGroups() {
        return mGroups;
    }

    public User getActiveUser() {
        return mUserRepo.findActiveUser();
    }

    public void createGroup(Group group) {
        mGroupRepo.upload(group, false);
    }

    public void updateGroup(Group group) {
        mGroupRepo.copyOrUpdate(group);
        mGroupRepo.upload(group, true);
    }

    public void uploadGroupImage(File file, Group group) {
        mGroupRepo.uploadImage(file, group);
    }

    public void createMembers(List<Member> members, long groupId) {
        for (Member member : members) {
            member.setGroup_id(groupId);
            mMemberRepo.copyOrUpdateAsync(member);
        }
    }

    public Group findFirstGroupCopyEqualTo(String column, long criteria) {
        return mGroupRepo.findFirstCopyEqualTo(column, criteria);
    }

    public List<Member> findAllMemberEqualTo(String column, long criteria) {
        return mMemberRepo.findAllEqualTo(column, criteria);
    }

//    public void findUser(String phone) {
//        mService.findUser(phone).enqueue(new Callback<Wrapper<User>>() {
//            @Override public void onResponse(Call<Wrapper<User>> call, Response<Wrapper<User>> response) {
//                if (response.isSuccessful()) {
//                    User user = response.body().data;
//
//                    if (Integer.valueOf(response.body().status) >= 1 && user != null) {
//                        mFoundUser.postValue(user);
//                    } else {
//                        Log.d("New group", response.message());
//                    }
//                } else {
//                    Log.d("New group", "unsuccessfull");
//                }
//            }
//
//            @Override public void onFailure(Call<Wrapper<User>> call, Throwable t) {
//                Log.d("New group", "Failed");
//            }
//        });
//    }

}
