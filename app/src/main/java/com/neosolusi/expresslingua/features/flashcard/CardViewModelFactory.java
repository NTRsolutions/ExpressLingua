package com.neosolusi.expresslingua.features.flashcard;

import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProvider;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;

import com.neosolusi.expresslingua.algorithm.Sentence;
import com.neosolusi.expresslingua.algorithm.Word;
import com.neosolusi.expresslingua.data.repo.ChallengeHardRepository;
import com.neosolusi.expresslingua.data.repo.ChallengeRepository;
import com.neosolusi.expresslingua.data.repo.DictionaryRepository;
import com.neosolusi.expresslingua.data.repo.FlashcardRepository;
import com.neosolusi.expresslingua.data.repo.ReadingRepository;

public class CardViewModelFactory extends ViewModelProvider.NewInstanceFactory {

    private final SharedPreferences mPref;
    private final FlashcardRepository mFlashcardRepo;
    private final DictionaryRepository mDictionaryRepo;
    private final ReadingRepository mReadingRepo;
    private final ChallengeRepository mChallengeRepo;
    private final ChallengeHardRepository mChallengeHardRepo;
    private final Word mWordAlgorithm;
    private final Sentence mSentenceAlgorithm;

    public CardViewModelFactory(SharedPreferences pref, FlashcardRepository flashcardRepository, DictionaryRepository dictionaryRepository, ReadingRepository readingRepository, ChallengeRepository challengeRepository, ChallengeHardRepository challengeHardRepository, Word wordAlgorithm, Sentence sentenceAlgorithm) {
        this.mPref = pref;
        this.mFlashcardRepo = flashcardRepository;
        this.mDictionaryRepo = dictionaryRepository;
        this.mReadingRepo = readingRepository;
        this.mChallengeRepo = challengeRepository;
        this.mChallengeHardRepo = challengeHardRepository;
        this.mWordAlgorithm = wordAlgorithm;
        this.mSentenceAlgorithm = sentenceAlgorithm;
    }

    @NonNull @Override public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        return (T) new CardViewModel(mPref, mFlashcardRepo, mDictionaryRepo, mReadingRepo, mChallengeRepo, mChallengeHardRepo, mWordAlgorithm, mSentenceAlgorithm);
    }

}
