package com.neosolusi.expresslingua.features.register;

import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProvider;
import android.support.annotation.NonNull;

import com.neosolusi.expresslingua.data.repo.UserRepository;

public class RegisterViewModelFactory extends ViewModelProvider.NewInstanceFactory {

    private final UserRepository mUserRepository;

    public RegisterViewModelFactory(UserRepository mUserRepository) {
        this.mUserRepository = mUserRepository;
    }

    @NonNull @Override public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        return (T) new RegisterViewModel(mUserRepository);
    }
}
