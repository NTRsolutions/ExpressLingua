package com.neosolusi.expresslingua.features.lesson;

import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProvider;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;

import com.neosolusi.expresslingua.data.repo.ChallengeHardRepository;
import com.neosolusi.expresslingua.data.repo.ChallengeRepository;
import com.neosolusi.expresslingua.data.repo.DictionaryRepository;
import com.neosolusi.expresslingua.data.repo.FlashcardRepository;
import com.neosolusi.expresslingua.data.repo.ReadingInfoRepository;
import com.neosolusi.expresslingua.data.repo.ReadingRepository;

public class LessonViewModelFactory extends ViewModelProvider.NewInstanceFactory {

    private final SharedPreferences mPref;
    private final ReadingRepository mReadingRepo;
    private final ReadingInfoRepository mReadingInfoRepo;
    private final FlashcardRepository mFlashcardRepo;
    private final DictionaryRepository mDictionaryRepo;
    private final ChallengeRepository mChallengeRepo;
    private final ChallengeHardRepository mChallengeHardRepo;

    public LessonViewModelFactory(
            SharedPreferences pref,
            ReadingRepository readingRepository,
            ReadingInfoRepository readingInfoRepository,
            DictionaryRepository dictionaryRepository,
            FlashcardRepository flashcardRepository,
            ChallengeRepository challengeRepository,
            ChallengeHardRepository challengeHardRepository) {
        this.mPref = pref;
        this.mReadingRepo = readingRepository;
        this.mReadingInfoRepo = readingInfoRepository;
        this.mFlashcardRepo = flashcardRepository;
        this.mDictionaryRepo = dictionaryRepository;
        this.mChallengeRepo = challengeRepository;
        this.mChallengeHardRepo = challengeHardRepository;
    }

    @NonNull @Override public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        return (T) new LessonViewModel(mPref, mReadingRepo, mReadingInfoRepo, mDictionaryRepo, mFlashcardRepo, mChallengeRepo, mChallengeHardRepo);
    }
}
