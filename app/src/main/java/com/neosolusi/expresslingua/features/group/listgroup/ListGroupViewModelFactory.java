package com.neosolusi.expresslingua.features.group.listgroup;

import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProvider;
import android.support.annotation.NonNull;

import com.neosolusi.expresslingua.data.repo.GroupRepository;

public class ListGroupViewModelFactory extends ViewModelProvider.NewInstanceFactory {

    private final GroupRepository mGroupRepo;

    public ListGroupViewModelFactory(GroupRepository repository) {
        this.mGroupRepo = repository;
    }

    @NonNull @Override public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        return (T) new ListGroupViewModel(mGroupRepo);
    }

}

