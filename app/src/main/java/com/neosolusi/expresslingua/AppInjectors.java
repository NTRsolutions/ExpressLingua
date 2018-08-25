package com.neosolusi.expresslingua;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.neosolusi.expresslingua.algorithm.Sentence;
import com.neosolusi.expresslingua.algorithm.Word;
import com.neosolusi.expresslingua.data.dao.ChallengeDao;
import com.neosolusi.expresslingua.data.dao.ChallengeHardDao;
import com.neosolusi.expresslingua.data.dao.ContactDao;
import com.neosolusi.expresslingua.data.dao.DictionaryDao;
import com.neosolusi.expresslingua.data.dao.EpisodeDao;
import com.neosolusi.expresslingua.data.dao.FlashcardDao;
import com.neosolusi.expresslingua.data.dao.GroupDao;
import com.neosolusi.expresslingua.data.dao.MemberDao;
import com.neosolusi.expresslingua.data.dao.NotificationDao;
import com.neosolusi.expresslingua.data.dao.ReadingDao;
import com.neosolusi.expresslingua.data.dao.ReadingInfoDao;
import com.neosolusi.expresslingua.data.dao.UserDao;
import com.neosolusi.expresslingua.data.network.ExpressLinguaApi;
import com.neosolusi.expresslingua.data.network.NetworkDataSource;
import com.neosolusi.expresslingua.data.network.NetworkUtils;
import com.neosolusi.expresslingua.data.repo.ChallengeHardRepository;
import com.neosolusi.expresslingua.data.repo.ChallengeRepository;
import com.neosolusi.expresslingua.data.repo.ContactRepository;
import com.neosolusi.expresslingua.data.repo.DictionaryRepository;
import com.neosolusi.expresslingua.data.repo.EpisodeRepository;
import com.neosolusi.expresslingua.data.repo.FlashcardRepository;
import com.neosolusi.expresslingua.data.repo.GroupRepository;
import com.neosolusi.expresslingua.data.repo.MemberRepository;
import com.neosolusi.expresslingua.data.repo.NotificationRepository;
import com.neosolusi.expresslingua.data.repo.ReadingInfoRepository;
import com.neosolusi.expresslingua.data.repo.ReadingRepository;
import com.neosolusi.expresslingua.data.repo.UserRepository;
import com.neosolusi.expresslingua.features.challenge.ChallengeViewModelFactory;
import com.neosolusi.expresslingua.features.flashcard.CardViewModelFactory;
import com.neosolusi.expresslingua.features.group.listcontact.ListContactViewModelFactory;
import com.neosolusi.expresslingua.features.group.listgroup.ListGroupViewModelFactory;
import com.neosolusi.expresslingua.features.group.group.GroupViewModelFactory;
import com.neosolusi.expresslingua.features.home.HomeViewModelFactory;
import com.neosolusi.expresslingua.features.lesson.LessonViewModelFactory;
import com.neosolusi.expresslingua.features.lessons.LessonsViewModelFactory;
import com.neosolusi.expresslingua.features.login.LoginViewModelFactory;
import com.neosolusi.expresslingua.features.register.RegisterViewModelFactory;
import com.neosolusi.expresslingua.features.stories.StoriesViewModelFactory;

import io.realm.Realm;
import retrofit2.Retrofit;

public class AppInjectors {

    public static SharedPreferences provideSharedPreferences(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
    }

    public static SharedPreferences.Editor provideSharedPreferencesEditor(Context context) {
        return provideSharedPreferences(context.getApplicationContext()).edit();
    }

    //***********************************************************************
    // CLASS INJECTORS
    //***********************************************************************
    public static NetworkDataSource provideNetworkDataSource(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
        Retrofit retrofit = NetworkUtils.getWorker(context.getApplicationContext(), preferences);
        ExpressLinguaApi expressLinguaApi = retrofit.create(ExpressLinguaApi.class);
        return NetworkDataSource.getInstance(context.getApplicationContext(), expressLinguaApi, preferences, preferences.edit());
    }

    public static Word provideWordAlgorithm(Context context) {
        return Word.getInstance(
                provideSharedPreferences(context.getApplicationContext()),
                provideSharedPreferencesEditor(context.getApplicationContext()));
    }

    public static Sentence provideSentenceAlgorithm(Context context) {
        return Sentence.getInstance(
                provideSharedPreferences(context.getApplicationContext()),
                provideSharedPreferencesEditor(context.getApplicationContext()));
    }

    //***********************************************************************
    // REPOSITORY INJECTORS
    //***********************************************************************
    public static UserRepository provideUserRepository(Context context) {
        Realm realm = Realm.getDefaultInstance();
        AppExecutors executors = AppExecutors.getInstance();
        return UserRepository.getInstance(new UserDao(realm), provideNetworkDataSource(context), executors);
    }

    public static FlashcardRepository provideFlashcardRepository(Context context) {
        Realm realm = Realm.getDefaultInstance();
        AppExecutors executors = AppExecutors.getInstance();
        return FlashcardRepository.getInstance(new FlashcardDao(realm), new UserDao(realm), provideNetworkDataSource(context), executors);
    }

    public static NotificationRepository provideNotificationRepository(Context context) {
        Realm realm = Realm.getDefaultInstance();
        AppExecutors executors = AppExecutors.getInstance();
        return NotificationRepository.getInstance(new NotificationDao(realm), provideNetworkDataSource(context), executors);
    }

    public static EpisodeRepository provideEpisodeRepository(Context context) {
        Realm realm = Realm.getDefaultInstance();
        AppExecutors executors = AppExecutors.getInstance();
        return EpisodeRepository.getInstance(new EpisodeDao(realm), provideNetworkDataSource(context), executors);
    }

    public static DictionaryRepository provideDictionaryRepository(Context context) {
        Realm realm = Realm.getDefaultInstance();
        AppExecutors executors = AppExecutors.getInstance();
        return DictionaryRepository.getInstance(new DictionaryDao(realm), provideNetworkDataSource(context), executors);
    }

    public static ReadingInfoRepository provideReadingInfoRepository(Context context) {
        Realm realm = Realm.getDefaultInstance();
        AppExecutors executors = AppExecutors.getInstance();
        return ReadingInfoRepository.getInstance(new ReadingInfoDao(realm), provideNetworkDataSource(context), executors);
    }

    public static ReadingRepository provideReadingRepository(Context context) {
        Realm realm = Realm.getDefaultInstance();
        AppExecutors executors = AppExecutors.getInstance();
        return ReadingRepository.getInstance(new ReadingDao(realm), new UserDao(realm), provideNetworkDataSource(context), executors);
    }

    public static ChallengeRepository provideChallengeRepository(Context context) {
        Realm realm = Realm.getDefaultInstance();
        AppExecutors executors = AppExecutors.getInstance();
        return ChallengeRepository.getInstance(new ChallengeDao(realm), new UserDao(realm), new FlashcardDao(realm), provideSharedPreferences(context), provideNetworkDataSource(context), executors);
    }

    public static ChallengeHardRepository provideChallengeHardRepository(Context context) {
        Realm realm = Realm.getDefaultInstance();
        return ChallengeHardRepository.getInstance(new ChallengeHardDao(realm));
    }

    public static ContactRepository provideContactRepository(Context context) {
        Realm realm = Realm.getDefaultInstance();
        AppExecutors executors = AppExecutors.getInstance();
        return ContactRepository.getInstance(new ContactDao(realm), provideNetworkDataSource(context), executors);
    }

    public static MemberRepository provideMemberRepository(Context context) {
        Realm realm = Realm.getDefaultInstance();
        AppExecutors executors = AppExecutors.getInstance();
        return MemberRepository.getInstance(new MemberDao(realm), provideNetworkDataSource(context), executors);
    }

    public static GroupRepository provideGroupRepository(Context context) {
        Realm realm = Realm.getDefaultInstance();
        AppExecutors executors = AppExecutors.getInstance();
        return GroupRepository.getInstance(new GroupDao(realm), provideNetworkDataSource(context), executors);
    }

    //***********************************************************************
    // VIEW MODEL FACTORY INJECTORS
    //***********************************************************************

    public static HomeViewModelFactory provideHomeViewModelFactory(Context context) {
        return new HomeViewModelFactory(
                provideFlashcardRepository(context.getApplicationContext()),
                provideNotificationRepository(context.getApplicationContext()),
                provideChallengeRepository(context.getApplicationContext()),
                provideReadingRepository(context.getApplicationContext())
        );
    }

    public static LoginViewModelFactory provideLoginViewModelFactory(Context context) {
        return new LoginViewModelFactory(provideUserRepository(context.getApplicationContext()));
    }

    public static RegisterViewModelFactory provideRegisterViewModelFactory(Context context) {
        return new RegisterViewModelFactory(provideUserRepository(context.getApplicationContext()));
    }

    public static StoriesViewModelFactory provideStoriesViewModelFactory(Context context) {
        return new StoriesViewModelFactory(
                provideEpisodeRepository(context.getApplicationContext()),
                provideReadingInfoRepository(context.getApplicationContext()),
                provideReadingRepository(context.getApplicationContext())
        );
    }

    public static LessonsViewModelFactory provideLessonsViewModelFactory(Context context) {
        return new LessonsViewModelFactory(
                provideReadingInfoRepository(context.getApplicationContext()),
                provideEpisodeRepository(context.getApplicationContext()),
                provideReadingRepository(context.getApplicationContext())
        );
    }

    public static LessonViewModelFactory provideLessonViewModelFactory(Context context) {
        return new LessonViewModelFactory(
                provideSharedPreferences(context.getApplicationContext()),
                provideReadingRepository(context.getApplicationContext()),
                provideReadingInfoRepository(context.getApplicationContext()),
                provideDictionaryRepository(context.getApplicationContext()),
                provideFlashcardRepository(context.getApplicationContext()),
                provideChallengeRepository(context.getApplicationContext()),
                provideChallengeHardRepository(context.getApplicationContext())
        );
    }

    public static CardViewModelFactory provideCardViewModelFactory(Context context) {
        return new CardViewModelFactory(
                provideSharedPreferences(context.getApplicationContext()),
                provideFlashcardRepository(context.getApplicationContext()),
                provideDictionaryRepository(context.getApplicationContext()),
                provideReadingRepository(context.getApplicationContext()),
                provideChallengeRepository(context.getApplicationContext()),
                provideChallengeHardRepository(context.getApplicationContext()),
                provideWordAlgorithm(context.getApplicationContext()),
                provideSentenceAlgorithm(context.getApplicationContext())
        );
    }

    public static ChallengeViewModelFactory provideChallengeViewModelFactory(Context context) {
        return new ChallengeViewModelFactory(
                provideReadingRepository(context.getApplicationContext()),
                provideReadingInfoRepository(context.getApplicationContext()),
                provideFlashcardRepository(context.getApplicationContext()),
                provideChallengeRepository(context.getApplicationContext()),
                provideDictionaryRepository(context.getApplicationContext()),
                provideWordAlgorithm(context.getApplicationContext()),
                provideSentenceAlgorithm(context.getApplicationContext())
        );
    }

    public static ListContactViewModelFactory provideListContactViewModelFactory(Context context) {
        return new ListContactViewModelFactory(
                provideContactRepository(context.getApplicationContext()),
                provideMemberRepository(context.getApplicationContext()),
                provideGroupRepository(context.getApplicationContext())
        );
    }

    public static ListGroupViewModelFactory provideListGroupViewModelFactory(Context context) {
        return new ListGroupViewModelFactory(provideGroupRepository(context.getApplicationContext()));
    }

    public static GroupViewModelFactory provideNewGroupViewModelFactory(Context context) {
        return new GroupViewModelFactory(
                provideGroupRepository(context.getApplicationContext()),
                provideMemberRepository(context.getApplicationContext()),
                provideUserRepository(context.getApplicationContext()),
                provideNetworkDataSource(context)
        );
    }

}
