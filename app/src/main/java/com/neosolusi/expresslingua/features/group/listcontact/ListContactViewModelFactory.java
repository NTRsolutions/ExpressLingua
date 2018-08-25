package com.neosolusi.expresslingua.features.group.listcontact;

import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProvider;
import android.support.annotation.NonNull;

import com.neosolusi.expresslingua.data.repo.ContactRepository;
import com.neosolusi.expresslingua.data.repo.GroupRepository;
import com.neosolusi.expresslingua.data.repo.MemberRepository;

public class ListContactViewModelFactory extends ViewModelProvider.NewInstanceFactory {

    private final ContactRepository mContactRepo;
    private final MemberRepository mMemberRepo;
    private final GroupRepository mGroupRepo;

    public ListContactViewModelFactory(ContactRepository contactRepository, MemberRepository memberRepository, GroupRepository groupRepository) {
        this.mContactRepo = contactRepository;
        this.mMemberRepo = memberRepository;
        this.mGroupRepo = groupRepository;
    }

    @NonNull @Override public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        return (T) new ListContactViewModel(mContactRepo, mMemberRepo, mGroupRepo);
    }
}