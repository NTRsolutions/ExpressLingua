package com.neosolusi.expresslingua.features.home;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.ViewModel;

import com.neosolusi.expresslingua.data.entity.Challenge;
import com.neosolusi.expresslingua.data.entity.Flashcard;
import com.neosolusi.expresslingua.data.entity.Notification;
import com.neosolusi.expresslingua.data.entity.Reading;
import com.neosolusi.expresslingua.data.entity.ReadingInfo;
import com.neosolusi.expresslingua.data.repo.ChallengeRepository;
import com.neosolusi.expresslingua.data.repo.FlashcardRepository;
import com.neosolusi.expresslingua.data.repo.NotificationRepository;
import com.neosolusi.expresslingua.data.repo.ReadingRepository;

import io.realm.Realm;
import io.realm.RealmResults;

public class HomeViewModel extends ViewModel {

    private Realm mDatabase;
    private FlashcardRepository mFlashcardRepo;
    private NotificationRepository mNotificationRepo;
    private ChallengeRepository mChallengeRepo;
    private ReadingRepository mReadingRepo;
    private LiveData<RealmResults<Flashcard>> mFlashcards;
    private LiveData<RealmResults<Notification>> mNotifications;
    private LiveData<RealmResults<Challenge>> mChallenges;
    private LiveData<RealmResults<Reading>> mReadings;

    public HomeViewModel(
            FlashcardRepository flashcardRepository,
            NotificationRepository notificationRepository,
            ChallengeRepository challengeRepository,
            ReadingRepository readingRepository) {
        mDatabase = Realm.getDefaultInstance();
        mFlashcardRepo = flashcardRepository;
        mNotificationRepo = notificationRepository;
        mChallengeRepo = challengeRepository;
        mReadingRepo = readingRepository;
        mFlashcards = mFlashcardRepo.findAllAsync();
        mNotifications = mNotificationRepo.findAllAsync();
        mChallenges = mChallengeRepo.findAllAsync();
        mReadings = mReadingRepo.findAllAsync();
    }

    @Override protected void onCleared() {
        mDatabase.close();
    }

    public LiveData<RealmResults<Flashcard>> getFlashcards() {
        return mFlashcards;
    }

    public LiveData<RealmResults<Notification>> getNotifications() {
        return mNotifications;
    }

    public LiveData<RealmResults<Challenge>> getChallenges() {
        return mChallenges;
    }

    public LiveData<RealmResults<Reading>> getReadings() {
        return mReadings;
    }

    public void rePopulateChallenge() {
        mChallengeRepo.initializeData();
    }

    public int learnedLessonCount() {
        int sentencesCount = 0;
        Number maxNumber = mDatabase.where(Reading.class).equalTo("already_read", 1).max("file_id");
        int maxLessonInfo = maxNumber == null ? 0 : maxNumber.intValue();

        for (ReadingInfo info : mDatabase.where(ReadingInfo.class).lessThanOrEqualTo("file_id", maxLessonInfo).findAll()) {
            sentencesCount += info.getSentences_count();
        }

        return sentencesCount;
    }

    public void testing() {
        mDatabase.executeTransaction(db -> {
            for (Challenge challenge : db.where(Challenge.class).findAll()) {
                challenge.setCorrect(false);
            }
        });
    }
}
