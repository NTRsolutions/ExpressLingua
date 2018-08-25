package com.neosolusi.expresslingua.features.home;

import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProvider;
import android.support.annotation.NonNull;

import com.neosolusi.expresslingua.data.dao.FlashcardDao;
import com.neosolusi.expresslingua.data.dao.NotificationDao;
import com.neosolusi.expresslingua.data.repo.ChallengeRepository;
import com.neosolusi.expresslingua.data.repo.FlashcardRepository;
import com.neosolusi.expresslingua.data.repo.NotificationRepository;
import com.neosolusi.expresslingua.data.repo.ReadingRepository;

public class HomeViewModelFactory extends ViewModelProvider.NewInstanceFactory {

    private final FlashcardRepository mFlashcardRepo;
    private final NotificationRepository mNotificationRepo;
    private final ChallengeRepository mChallengeRepo;
    private final ReadingRepository mReadingRepo;

    public HomeViewModelFactory(FlashcardRepository flashcardRepository, NotificationRepository notificationRepository, ChallengeRepository challengeRepository, ReadingRepository readingRepository) {
        this.mFlashcardRepo = flashcardRepository;
        this.mNotificationRepo = notificationRepository;
        this.mChallengeRepo = challengeRepository;
        this.mReadingRepo = readingRepository;
    }

    @NonNull @Override public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        return (T) new HomeViewModel(mFlashcardRepo, mNotificationRepo, mChallengeRepo, mReadingRepo);
    }
}
