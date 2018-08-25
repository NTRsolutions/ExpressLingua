package com.neosolusi.expresslingua.features.stories;

import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProvider;
import android.support.annotation.NonNull;

import com.neosolusi.expresslingua.data.repo.EpisodeRepository;
import com.neosolusi.expresslingua.data.repo.ReadingInfoRepository;
import com.neosolusi.expresslingua.data.repo.ReadingRepository;

public class StoriesViewModelFactory extends ViewModelProvider.NewInstanceFactory {

    private final EpisodeRepository mEpisodeRepo;
    private final ReadingInfoRepository mReadingInfoRepo;
    private final ReadingRepository mReadingRepo;

    public StoriesViewModelFactory(EpisodeRepository repository, ReadingInfoRepository readingInfoRepository, ReadingRepository readingRepository) {
        this.mEpisodeRepo = repository;
        this.mReadingInfoRepo = readingInfoRepository;
        this.mReadingRepo = readingRepository;
    }

    @NonNull @Override public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        return (T) new StoriesViewModel(mEpisodeRepo, mReadingInfoRepo, mReadingRepo);
    }
}
