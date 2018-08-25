package com.neosolusi.expresslingua.features.challenge;

import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProvider;
import android.support.annotation.NonNull;

import com.neosolusi.expresslingua.algorithm.Sentence;
import com.neosolusi.expresslingua.algorithm.Word;
import com.neosolusi.expresslingua.data.repo.ChallengeRepository;
import com.neosolusi.expresslingua.data.repo.DictionaryRepository;
import com.neosolusi.expresslingua.data.repo.FlashcardRepository;
import com.neosolusi.expresslingua.data.repo.ReadingInfoRepository;
import com.neosolusi.expresslingua.data.repo.ReadingRepository;

public class ChallengeViewModelFactory extends ViewModelProvider.NewInstanceFactory {

    private final ReadingRepository mReadingRepo;
    private final ReadingInfoRepository mReadingInfoRepo;
    private final FlashcardRepository mFlashcardRepo;
    private final ChallengeRepository mChallengeRepo;
    private final DictionaryRepository mDictionaryRepo;
    private final Word mWordAlgorithm;
    private final Sentence mSentenceAlgorithm;

    public ChallengeViewModelFactory(ReadingRepository readingRepository, ReadingInfoRepository readingInfoRepository, FlashcardRepository flashcardRepository, ChallengeRepository challengeRepository, DictionaryRepository dictionaryRepository,
                                     Word wordAlgorithm, Sentence sentenceAlgorithm) {
        this.mReadingRepo = readingRepository;
        this.mReadingInfoRepo = readingInfoRepository;
        this.mFlashcardRepo = flashcardRepository;
        this.mChallengeRepo = challengeRepository;
        this.mDictionaryRepo = dictionaryRepository;
        this.mWordAlgorithm = wordAlgorithm;
        this.mSentenceAlgorithm = sentenceAlgorithm;
    }

    @NonNull @Override public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        return (T) new ChallengeViewModel(mReadingRepo, mReadingInfoRepo, mFlashcardRepo, mChallengeRepo, mDictionaryRepo, mWordAlgorithm, mSentenceAlgorithm);
    }

}
