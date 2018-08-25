package com.neosolusi.expresslingua.features.lessons;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;

import com.neosolusi.expresslingua.AppExecutors;
import com.neosolusi.expresslingua.AppUtils;
import com.neosolusi.expresslingua.data.entity.Episode;
import com.neosolusi.expresslingua.data.entity.Flashcard;
import com.neosolusi.expresslingua.data.entity.Reading;
import com.neosolusi.expresslingua.data.entity.ReadingInfo;
import com.neosolusi.expresslingua.data.entity.ReadingInfoMeta;
import com.neosolusi.expresslingua.data.repo.EpisodeRepository;
import com.neosolusi.expresslingua.data.repo.ReadingInfoRepository;
import com.neosolusi.expresslingua.data.repo.ReadingRepository;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import io.realm.Case;
import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.Sort;

public class LessonsViewModel extends ViewModel {

    private Realm mDatabase;
    private ReadingInfoRepository mReadingInfoRepo;
    private EpisodeRepository mEpisodeRepo;
    private ReadingRepository mReadingRepo;
    private LiveData<RealmResults<ReadingInfo>> mReadingInfos;
    private MutableLiveData<Integer> mFinishCheckMetadata;

    public LessonsViewModel(ReadingInfoRepository readingInfoRepository, EpisodeRepository episodeRepository, ReadingRepository readingRepository) {
        mDatabase = Realm.getDefaultInstance();
        mReadingInfoRepo = readingInfoRepository;
        mEpisodeRepo = episodeRepository;
        mReadingRepo = readingRepository;
        mFinishCheckMetadata = new MutableLiveData<>();
    }

    @Override protected void onCleared() {
        mDatabase.close();
    }

    public Episode getEpisode(long episodeId) {
        return mEpisodeRepo.findFirstEqualTo("episode_id", episodeId);
    }

    public LiveData<RealmResults<ReadingInfo>> getReadingInfos(long episodeId) {
        mReadingInfos = mReadingInfoRepo.findAllEqualToAsync("episode_id", episodeId, "menu_id", Sort.ASCENDING);
        return mReadingInfos;
    }

    public LiveData<Integer> checkMetaDataObserver() {
        return mFinishCheckMetadata;
    }

    public boolean hasMetadata() {
        return mDatabase.where(ReadingInfoMeta.class).findFirst() != null;
    }

    public boolean hasFlashcard() {
        return mDatabase.where(Flashcard.class).findFirst() != null;
    }

    public void checkMetaData(long episodeId) {
        if (mDatabase.where(Flashcard.class).findFirst() == null) {
            List<ReadingInfo> infos = mReadingInfoRepo.findAllEqualTo("episode_id", episodeId);
            for (ReadingInfo info : infos) {
                mDatabase.executeTransaction(db -> {
                    ReadingInfoMeta meta = db.createObject(ReadingInfoMeta.class, UUID.randomUUID().getMostSignificantBits() & Long.MAX_VALUE);
                    meta.setMenu_id(info.getMenu_id());
                    meta.setSentenceCount(info.getSentences_count());
                    meta.setSentenceMarked(0);
                    meta.setWordCount(info.getWords_count());
                    meta.setWordMarked(0);
                });
            }
            mFinishCheckMetadata.postValue(1);
            return;
        }

        AppExecutors.getInstance().diskIO().execute(() -> {
            int wordsMarked, sentencesMarked;
            Set<String> listWord = new HashSet<>();

            Realm db = Realm.getDefaultInstance();
            RealmResults<ReadingInfo> infos = db.where(ReadingInfo.class).equalTo("episode_id", episodeId).findAll();
            for (ReadingInfo info : infos) {
                if (db.where(ReadingInfoMeta.class).equalTo("menu_id", info.getMenu_id()).findFirst() != null)
                    continue;

                wordsMarked = 0;
                sentencesMarked = 0;
                listWord.clear();

                RealmResults<Reading> readings = db.where(Reading.class).equalTo("file_id", info.getFile_id()).findAll();
                for (Reading reading : readings) {
                    if (reading.getSentence() == null || reading.getSentence().isEmpty()) continue;

                    // Words
                    String[] words = reading.getSentence().split(" ");
                    for (String word : words) {
                        if (!word.trim().replaceAll("[^a-zA-Z_0-9\\s]", "").isEmpty() && !word.trim().equals("")) {
                            // Words count
                            if (!listWord.contains(AppUtils.normalizeString(word).toLowerCase())) {
                                listWord.add(AppUtils.normalizeString(word).toLowerCase());

                                // Marked word count
                                Flashcard flashcard = db.where(Flashcard.class).equalTo("card", AppUtils.normalizeString(word), Case.INSENSITIVE).findFirst();
                                if (flashcard != null) wordsMarked++;
                            }
                        }
                    }

                    // Sentences
                    Flashcard flashcard = db.where(Flashcard.class)
                            .equalTo("category", Reading.class.getSimpleName())
                            .equalTo("type", "sentence").equalTo("reference", reading.getId())
                            .findFirst();
                    if (flashcard != null) sentencesMarked++;
                }

                final int sentencesCount = readings.size();
                final int markedWord = wordsMarked;
                final int markedSentence = sentencesMarked;
                db.executeTransaction(localDb -> {
                    ReadingInfoMeta meta = localDb.createObject(ReadingInfoMeta.class, UUID.randomUUID().getMostSignificantBits() & Long.MAX_VALUE);
                    meta.setMenu_id(info.getMenu_id());
                    meta.setSentenceCount(sentencesCount);
                    meta.setSentenceMarked(markedSentence);
                    meta.setWordCount(listWord.size());
                    meta.setWordMarked(markedWord);
                });
            }
            mFinishCheckMetadata.postValue(1);
            db.close();
        });
    }

}
