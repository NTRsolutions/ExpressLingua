package com.neosolusi.expresslingua.features.stories;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.ViewModel;

import com.neosolusi.expresslingua.data.entity.Episode;
import com.neosolusi.expresslingua.data.entity.Reading;
import com.neosolusi.expresslingua.data.entity.ReadingInfo;
import com.neosolusi.expresslingua.data.repo.EpisodeRepository;
import com.neosolusi.expresslingua.data.repo.ReadingInfoRepository;
import com.neosolusi.expresslingua.data.repo.ReadingRepository;

import java.util.HashMap;

import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.Sort;

public class StoriesViewModel extends ViewModel {

    private Realm mDatabase;
    private EpisodeRepository mEpisodeRepo;
    private ReadingInfoRepository mReadingInfoRepo;
    private ReadingRepository mReadingRepo;
    private LiveData<RealmResults<Episode>> mEpisodes;

    public StoriesViewModel(EpisodeRepository episodeRepository, ReadingInfoRepository readingInfoRepository, ReadingRepository readingRepository) {
        mDatabase = Realm.getDefaultInstance();
        mEpisodeRepo = episodeRepository;
        mReadingInfoRepo = readingInfoRepository;
        mReadingRepo = readingRepository;
        mEpisodes = mEpisodeRepo.findAllAsync("sequence_no", Sort.ASCENDING);
    }

    @Override protected void onCleared() {
        mDatabase.close();
    }

    public LiveData<RealmResults<Episode>> getEpisodes() {
        return mEpisodes;
    }

    public boolean hasFinishReadStory(Episode episode) {
        if (episode.getSequence_no() == 1) return true;

        Episode beforeEpisode = mEpisodeRepo.findFirstEqualTo("sequence_no", episode.getSequence_no() - 1);
        RealmResults<ReadingInfo> infos = mReadingInfoRepo.findAllEqualTo("episode_id", beforeEpisode.getEpisode_id());
        if (infos.isEmpty()) return false;

        boolean isFinish = true;
        for (ReadingInfo info : infos) {
            HashMap<String, Object> criterias = new HashMap<>();
            criterias.put("already_read", 0);
            criterias.put("file_id", info.getFile_id());

            if (mReadingRepo.findFirstEqualTo(criterias) != null) {
                isFinish = false;
                break;
            }
        }

        return isFinish;
    }

}
