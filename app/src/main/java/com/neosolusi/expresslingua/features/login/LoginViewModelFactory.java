package com.neosolusi.expresslingua.features.login;

import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProvider;
import android.support.annotation.NonNull;

import com.neosolusi.expresslingua.data.repo.UserRepository;

public class LoginViewModelFactory extends ViewModelProvider.NewInstanceFactory {

    private final UserRepository mUserRepo;

    public LoginViewModelFactory(UserRepository userRepository) {
        this.mUserRepo = userRepository;
    }

    @NonNull @Override public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        return (T) new LoginViewModel(mUserRepo);
    }
}
