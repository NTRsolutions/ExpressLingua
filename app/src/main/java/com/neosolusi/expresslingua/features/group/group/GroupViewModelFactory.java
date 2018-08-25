package com.neosolusi.expresslingua.features.group.group;

import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProvider;
import android.support.annotation.NonNull;

import com.neosolusi.expresslingua.data.network.NetworkDataSource;
import com.neosolusi.expresslingua.data.repo.GroupRepository;
import com.neosolusi.expresslingua.data.repo.MemberRepository;
import com.neosolusi.expresslingua.data.repo.UserRepository;

public class GroupViewModelFactory extends ViewModelProvider.NewInstanceFactory {

    private final UserRepository mUserRepo;
    private final GroupRepository mGroupRepo;
    private final MemberRepository mMemberRepo;
    private final NetworkDataSource mDataSource;

    public GroupViewModelFactory(GroupRepository groupRepository, MemberRepository memberRepository, UserRepository userRepository, NetworkDataSource dataSource) {
        this.mUserRepo = userRepository;
        this.mGroupRepo = groupRepository;
        this.mMemberRepo = memberRepository;
        this.mDataSource = dataSource;
    }

    @NonNull @Override public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        return (T) new GroupViewModel(mGroupRepo, mMemberRepo, mUserRepo, mDataSource);
    }

}
