package com.neosolusi.expresslingua.features.lessons;

import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProvider;
import android.support.annotation.NonNull;

import com.neosolusi.expresslingua.data.repo.EpisodeRepository;
import com.neosolusi.expresslingua.data.repo.ReadingInfoRepository;
import com.neosolusi.expresslingua.data.repo.ReadingRepository;

public class LessonsViewModelFactory extends ViewModelProvider.NewInstanceFactory {

    private final ReadingInfoRepository mReadingInfoRepo;
    private final EpisodeRepository mEpisodeRepo;
    private final ReadingRepository mReadingRepo;

    public LessonsViewModelFactory(ReadingInfoRepository repository, EpisodeRepository episodeRepository, ReadingRepository readingRepository) {
        this.mReadingInfoRepo = repository;
        this.mEpisodeRepo = episodeRepository;
        this.mReadingRepo = readingRepository;
    }

    @NonNull @Override public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        return (T) new LessonsViewModel(mReadingInfoRepo, mEpisodeRepo, mReadingRepo);
    }

}
