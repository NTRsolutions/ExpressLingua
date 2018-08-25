package com.neosolusi.expresslingua.data.network;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.firebase.jobdispatcher.Constraint;
import com.firebase.jobdispatcher.Driver;
import com.firebase.jobdispatcher.FirebaseJobDispatcher;
import com.firebase.jobdispatcher.GooglePlayDriver;
import com.firebase.jobdispatcher.Job;
import com.firebase.jobdispatcher.Lifetime;
import com.firebase.jobdispatcher.Trigger;
import com.neosolusi.expresslingua.AppConstants;
import com.neosolusi.expresslingua.R;
import com.neosolusi.expresslingua.data.entity.Challenge;
import com.neosolusi.expresslingua.data.entity.Contact;
import com.neosolusi.expresslingua.data.entity.Dictionary;
import com.neosolusi.expresslingua.data.entity.Episode;
import com.neosolusi.expresslingua.data.entity.Flashcard;
import com.neosolusi.expresslingua.data.entity.FlashcardParcel;
import com.neosolusi.expresslingua.data.entity.Group;
import com.neosolusi.expresslingua.data.entity.Member;
import com.neosolusi.expresslingua.data.entity.MemberProgress;
import com.neosolusi.expresslingua.data.entity.Notification;
import com.neosolusi.expresslingua.data.entity.Reading;
import com.neosolusi.expresslingua.data.entity.ReadingInfo;
import com.neosolusi.expresslingua.data.entity.ReadingParcel;
import com.neosolusi.expresslingua.data.entity.ServiceConfig;
import com.neosolusi.expresslingua.data.entity.User;
import com.neosolusi.expresslingua.data.entity.UserParcel;

import java.io.File;
import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.neosolusi.expresslingua.AppConstants.PREFERENCE_MAX_ALLOWED_FLASHCARD;

public class NetworkDataSource {

    private static final String TAG = NetworkDataSource.class.getSimpleName();

    // Firebase property
    private static final String SYNC_TAG = "expresslingua-sync";
    private static final int SYNC_INTERVAL_HOURS = 3;
    private static final int SYNC_INTERVAL_MINUTES = (int) TimeUnit.HOURS.toMinutes(SYNC_INTERVAL_HOURS);
    private static final int SYNC_INTERVAL_SECONDS = (int) TimeUnit.MINUTES.toSeconds(SYNC_INTERVAL_MINUTES); //SYNC_INTERVAL_MINUTES
    private static final int SYNC_FLEXTIME_SECONDS = SYNC_INTERVAL_SECONDS / 3;

    // Singleton Instantiation
    private static final Object LOCK = new Object();
    private static NetworkDataSource mInstance;
    private final SharedPreferences mPref;
    private final SharedPreferences.Editor mPrefEdit;
    private final MutableLiveData<User> mUser, mFindUser;
    private final MutableLiveData<User> mUploadedUser;
    private final MutableLiveData<Reading> mUploadedReading;
    private final MutableLiveData<Flashcard> mUploadedFlashcard;
    private final MutableLiveData<Group> mUploadedGroup;
    private final MutableLiveData<Member> mUploadedMember;
    private final MutableLiveData<MemberProgress> mMemberProgress;
    private final MutableLiveData<List<Contact>> mUploadedContact;
    private final MutableLiveData<List<Episode>> mDownloadedEpisodes;
    private final MutableLiveData<List<Reading>> mDownloadedReadings;
    private final MutableLiveData<List<Reading>> mDownloadedReadingUser;
    private final MutableLiveData<List<Flashcard>> mDownloadedFlashcards;
    private final MutableLiveData<List<Dictionary>> mDownloadedDictionaries;
    private final MutableLiveData<List<ReadingInfo>> mDownloadedReadingInfos;
    private final MutableLiveData<List<Notification>> mDownloadedNotifications;
    private final MutableLiveData<List<Group>> mDownloadedGroups;
    private final MutableLiveData<List<Member>> mDownloadedMembers;
    private final ExpressLinguaApi mService;
    private final SimpleDateFormat mDateFormat;
    private final WeakReference<Context> mContext;

    private NetworkDataSource(Context context, ExpressLinguaApi service, SharedPreferences pref, SharedPreferences.Editor prefEdit) {
        this.mContext = new WeakReference<>(context);
        this.mService = service;
        this.mUser = new MutableLiveData<>();
        this.mFindUser = new MutableLiveData<>();
        this.mUploadedUser = new MutableLiveData<>();
        this.mUploadedReading = new MutableLiveData<>();
        this.mUploadedFlashcard = new MutableLiveData<>();
        this.mUploadedGroup = new MutableLiveData<>();
        this.mUploadedMember = new MutableLiveData<>();
        this.mUploadedContact = new MutableLiveData<>();
        this.mDownloadedEpisodes = new MutableLiveData<>();
        this.mDownloadedReadings = new MutableLiveData<>();
        this.mDownloadedFlashcards = new MutableLiveData<>();
        this.mDownloadedReadingUser = new MutableLiveData<>();
        this.mDownloadedDictionaries = new MutableLiveData<>();
        this.mDownloadedReadingInfos = new MutableLiveData<>();
        this.mDownloadedNotifications = new MutableLiveData<>();
        this.mDownloadedGroups = new MutableLiveData<>();
        this.mDownloadedMembers = new MutableLiveData<>();
        this.mMemberProgress = new MutableLiveData<>();
        this.mPref = pref;
        this.mPrefEdit = prefEdit;
        this.mDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    }

    public static NetworkDataSource getInstance(Context context, ExpressLinguaApi service, SharedPreferences pref, SharedPreferences.Editor prefEdit) {
        if (mInstance == null) {
            synchronized (LOCK) {
                mInstance = new NetworkDataSource(context, service, pref, prefEdit);
            }
        }

        return mInstance;
    }

    public LiveData<User> getUser() {
        return mUser;
    }

    public LiveData<User> findUser(String phone) {
        return mFindUser;
    }

    public LiveData<User> getUploadedUser() {
        return mUploadedUser;
    }

    public LiveData<Reading> getUploadedReading() {
        return mUploadedReading;
    }

    public LiveData<Flashcard> getUploadedFlashcard() {
        return mUploadedFlashcard;
    }

    public LiveData<Group> getUploadedGroup() {
        return mUploadedGroup;
    }

    public LiveData<Member> getUploadedMember() {
        return mUploadedMember;
    }

    public LiveData<MemberProgress> getMemberProgress() {
        return mMemberProgress;
    }

    public LiveData<List<Contact>> getUploadedContact() {
        return mUploadedContact;
    }

    public LiveData<List<Episode>> getEpisodes() {
        return mDownloadedEpisodes;
    }

    public LiveData<List<Reading>> getReadings() {
        return mDownloadedReadings;
    }

    public LiveData<List<Reading>> getReadingsUser() {
        return mDownloadedReadingUser;
    }

    public LiveData<List<Flashcard>> getFlashcards() {
        return mDownloadedFlashcards;
    }

    public LiveData<List<Dictionary>> getDictionaries() {
        return mDownloadedDictionaries;
    }

    public LiveData<List<ReadingInfo>> getReadingInfos() {
        return mDownloadedReadingInfos;
    }

    public LiveData<List<Notification>> getNotifications() {
        return mDownloadedNotifications;
    }

    public LiveData<List<Group>> getGroups() {
        return mDownloadedGroups;
    }

    public LiveData<List<Member>> getMembers() {
        return mDownloadedMembers;
    }

    public void startNetworkService(String service) {
        Intent intent = new Intent(mContext.get(), NetworkSyncIntentService.class);
        intent.putExtra("service", service);

        mContext.get().startService(intent);
    }

    public void startNetworkServiceWithExtra(String service, Bundle extra) {
        Intent intent = new Intent(mContext.get(), NetworkSyncIntentService.class);
        intent.putExtra("service", service);
        intent.putExtra("extra", extra);

        mContext.get().startService(intent);
    }

    public void startNetworkServiceWithExtraFile(String service, File file, Bundle extra) {
        if (extra == null) return;

        long id;
        RequestBody reqUser;
        if (service.equalsIgnoreCase("group")) {
            id = extra.getLong("groupId");
            reqUser = RequestBody.create(MultipartBody.FORM, String.valueOf(id));
        } else {
            String username = extra.getString("userName");
            reqUser = RequestBody.create(MultipartBody.FORM, username == null ? "" : username);
        }

        RequestBody reqFile = RequestBody.create(MediaType.parse("image/*"), file);
        RequestBody reqName = RequestBody.create(MultipartBody.FORM, file.getName());
        MultipartBody.Part body = MultipartBody.Part.createFormData("image", file.getName(), reqFile);

        if (service.equalsIgnoreCase("group")) {
            mService.uploadGroupImage(body, reqName, reqUser).enqueue(new Callback<Wrapper<String>>() {
                @Override public void onResponse(Call<Wrapper<String>> call, Response<Wrapper<String>> response) {
                    if (response.isSuccessful()) {
                        sendBroadcast(AppConstants.BROADCAST_UPLOAD_GROUP_IMAGE_SUCCESS, response.body().data);
                        Log.i(TAG, "Upload group image success");
                    } else {
                        Log.e(TAG, "Upload group image failed: " + response.message());
                    }
                }

                @Override public void onFailure(Call<Wrapper<String>> call, Throwable t) {
                    Log.e(TAG, t.getMessage());
                }
            });
        } else {
            mService.uploadUserImage(body, reqName, reqUser).enqueue(new Callback<Wrapper<String>>() {
                @Override public void onResponse(Call<Wrapper<String>> call, Response<Wrapper<String>> response) {
                    if (response.isSuccessful()) {
                        sendBroadcast(AppConstants.BROADCAST_UPLOAD_USER_IMAGE_SUCCESS, response.body().data);
                        Log.i(TAG, "Upload user image success");
                    } else {
                        Log.e(TAG, "Upload user image failed: " + response.message());
                    }
                }

                @Override public void onFailure(Call<Wrapper<String>> call, Throwable t) {
                    Log.e(TAG, t.getMessage());
                }
            });
        }
    }

    public void scheduleFetchService(Bundle extra) {
        Driver driver = new GooglePlayDriver(mContext.get());
        FirebaseJobDispatcher dispatcher = new FirebaseJobDispatcher(driver);
        Job syncJob = dispatcher.newJobBuilder()
                .setExtras(extra)
                .setService(ExpressLinguaJobService.class)
                .setTag(SYNC_TAG)
                .setConstraints(Constraint.ON_ANY_NETWORK)
                .setLifetime(Lifetime.FOREVER)
                .setRecurring(true)
                .setTrigger(Trigger.executionWindow(SYNC_INTERVAL_SECONDS, SYNC_INTERVAL_SECONDS + SYNC_FLEXTIME_SECONDS))
                .setReplaceCurrent(true)
                .build();

        dispatcher.schedule(syncJob);
        Log.d(TAG, "Job Scheduled");
    }

    public void registerUser(Bundle extra) {
        if (extra == null) return;

        mService.register(
                extra.getString("userid"),
                extra.getString("email"),
                extra.getString("password"),
                extra.getInt("commercial_status"),
                extra.getString("cell_no"),
                extra.getString("address"),
                extra.getString("city"),
                extra.getString("province"),
                extra.getString("country"),
                extra.getDouble("gps_latitude"),
                extra.getDouble("gps_longitude")
                         ).enqueue(new Callback<Wrapper<User>>() {
            @Override public void onResponse(Call<Wrapper<User>> call, Response<Wrapper<User>> response) {
                if (response.isSuccessful()) {
                    User user = response.body().data;

                    if (Integer.valueOf(response.body().status) >= 1 && user != null) {
                        mUser.postValue(user);
                    } else {
                        sendBroadcast(AppConstants.BROADCAST_REGISTER_FAILED, response.body().message);
                    }
                } else {
                    Log.e(TAG, "Fetch user failed: " + response.message());
                    sendBroadcast(AppConstants.BROADCAST_REGISTER_FAILED, mContext.get().getString(R.string.error_network_access));
                }
            }

            @Override public void onFailure(Call<Wrapper<User>> call, Throwable t) {
                Log.e(TAG, t.getMessage());
                sendBroadcast(AppConstants.BROADCAST_REGISTER_FAILED, mContext.get().getString(R.string.error_connection));
            }
        });
    }

    public void fetchUser(Bundle extra) {
        if (extra == null) return;

        String userid = extra.getString("userid");
        String password = extra.getString("password");

        mService.login(userid, password).enqueue(new Callback<Wrapper<User>>() {
            @Override public void onResponse(Call<Wrapper<User>> call, Response<Wrapper<User>> response) {
                if (response.isSuccessful()) {
                    User user = response.body().data;

                    if (Integer.valueOf(response.body().status) >= 1 && user != null) {
                        mUser.postValue(user);
                    } else {
                        sendBroadcast(AppConstants.BROADCAST_LOGIN_FAILED, response.body().message);
                    }
                } else {
                    Log.e(TAG, "Fetch user failed: " + response.message());
                    sendBroadcast(AppConstants.BROADCAST_LOGIN_FAILED, mContext.get().getString(R.string.error_network_access));
                }
            }

            @Override public void onFailure(Call<Wrapper<User>> call, Throwable t) {
                Log.e(TAG, t.getMessage());
                sendBroadcast(AppConstants.BROADCAST_LOGIN_FAILED, mContext.get().getString(R.string.error_connection));
            }
        });
    }

    public void fetchConfig() {
        mService.config().enqueue(new Callback<WrapperList<ServiceConfig>>() {
            @Override public void onResponse(Call<WrapperList<ServiceConfig>> call, Response<WrapperList<ServiceConfig>> response) {
                if (response.isSuccessful()) {
                    List<ServiceConfig> config = response.body().data;

                    if (Integer.valueOf(response.body().status) >= 1) {
                        // Save max allowed flashcard
                        mPrefEdit.putInt(PREFERENCE_MAX_ALLOWED_FLASHCARD, config.get(0).getMaks_kata()).apply();

                        // Broadcast current server version to subscriber
                        sendBroadcast(AppConstants.BROADCAST_SERVICE_CONFIG, config.get(0).getCurrent_version());
                    }
                } else {
                    Log.e(TAG, "Fetch ServiceConfig failed: " + response.message());
                }
            }

            @Override public void onFailure(Call<WrapperList<ServiceConfig>> call, Throwable t) {
                Log.e(TAG, t.getMessage());
            }
        });
    }

    public void fetchEpisode() {
        String dateSync = mPref.getString(AppConstants.PREFERENCE_LAST_SYNC_EPISODE, "0000-00-00");
        mService.getEpisodes(dateSync).enqueue(new Callback<WrapperList<Episode>>() {
            @Override public void onResponse(Call<WrapperList<Episode>> call, Response<WrapperList<Episode>> response) {
                if (response.isSuccessful()) {
                    List<Episode> episodes = response.body().data;

                    if (Integer.valueOf(response.body().status) >= 1 && !episodes.isEmpty()) {
                        mDownloadedEpisodes.postValue(episodes);
                    }

                    mPrefEdit.putString(AppConstants.PREFERENCE_LAST_SYNC_EPISODE, mDateFormat.format(new Date())).apply();
                } else {
                    Log.e(TAG, "Fetch episode failed: " + response.message());
                }
            }

            @Override public void onFailure(Call<WrapperList<Episode>> call, Throwable t) {
                Log.e(TAG, t.getMessage());
            }
        });
    }

    public void fetchReading() {
        String dateSync = mPref.getString(AppConstants.PREFERENCE_LAST_SYNC_READING, "0000-00-00");
        mService.getReading(dateSync).enqueue(new Callback<WrapperList<Reading>>() {
            @Override public void onResponse(Call<WrapperList<Reading>> call, Response<WrapperList<Reading>> response) {
                if (response.isSuccessful()) {
                    List<Reading> readings = response.body().data;

                    if (Integer.valueOf(response.body().status) >= 1 && !readings.isEmpty()) {
                        mDownloadedReadings.postValue(readings);
                    }

                    mPrefEdit.putString(AppConstants.PREFERENCE_LAST_SYNC_READING, mDateFormat.format(new Date())).apply();
                } else {
                    Log.e(TAG, "Fetch reading failed: " + response.message());
                }
            }

            @Override public void onFailure(Call<WrapperList<Reading>> call, Throwable t) {
                Log.e(TAG, t.getMessage());
            }
        });
    }

    public void fetchReadingUser(Bundle extra) {
        if (extra == null) return;

        String userid = extra.getString("userid");
        String dateSync = mPref.getString(AppConstants.PREFERENCE_LAST_SYNC_READING_USER, "0000-00-00");
        mService.getReadingUser(userid, dateSync).enqueue(new Callback<WrapperList<Reading>>() {
            @Override public void onResponse(Call<WrapperList<Reading>> call, Response<WrapperList<Reading>> response) {
                if (response.isSuccessful()) {
                    List<Reading> readings = response.body().data;
                    mDownloadedReadingUser.postValue(readings);
                    mPrefEdit.putString(AppConstants.PREFERENCE_LAST_SYNC_READING_USER, mDateFormat.format(new Date())).apply();
                } else {
                    Log.e(TAG, "Fetch reading user failed: " + response.message());
                }
            }

            @Override public void onFailure(Call<WrapperList<Reading>> call, Throwable t) {
                Log.e(TAG, t.getMessage());
            }
        });
    }

    public void fetchFlashcard(Bundle extra) {
        if (extra == null) return;

        String userid = extra.getString("userid");
        String dateSync = mPref.getString(AppConstants.PREFERENCE_LAST_SYNC_FLASHCARD, "0000-00-00");
        mService.getFlashcardUser(userid, dateSync).enqueue(new Callback<WrapperList<Flashcard>>() {
            @Override public void onResponse(Call<WrapperList<Flashcard>> call, Response<WrapperList<Flashcard>> response) {
                if (response.isSuccessful()) {
                    List<Flashcard> flashcards = response.body().data;
                    mDownloadedFlashcards.postValue(flashcards);
                    mPrefEdit.putString(AppConstants.PREFERENCE_LAST_SYNC_FLASHCARD, mDateFormat.format(new Date())).apply();
                } else {
                    Log.e(TAG, "Fetch flashcard failed: " + response.message());
                }
            }

            @Override public void onFailure(Call<WrapperList<Flashcard>> call, Throwable t) {
                Log.e(TAG, t.getMessage());
            }
        });
    }

    public void fetchDictionary() {
        String dateSync = mPref.getString(AppConstants.PREFERENCE_LAST_SYNC_DICTIONARY, "0000-00-00");
        mService.getDictionaries(dateSync).enqueue(new Callback<WrapperList<Dictionary>>() {
            @Override public void onResponse(Call<WrapperList<Dictionary>> call, Response<WrapperList<Dictionary>> response) {
                if (response.isSuccessful()) {
                    List<Dictionary> dictionaries = response.body().data;

                    if (Integer.valueOf(response.body().status) >= 1 && !dictionaries.isEmpty()) {
                        mDownloadedDictionaries.postValue(dictionaries);
                    }

                    mPrefEdit.putString(AppConstants.PREFERENCE_LAST_SYNC_DICTIONARY, mDateFormat.format(new Date())).apply();
                } else {
                    Log.e(TAG, "Fetch dictionary failed: " + response.message());
                }
            }

            @Override public void onFailure(Call<WrapperList<Dictionary>> call, Throwable t) {
                Log.e(TAG, "Fetch dictionary failed: " + t.getMessage());
            }
        });
    }

    public void fetchReadingInfo() {
        String dateSync = mPref.getString(AppConstants.PREFERENCE_LAST_SYNC_READING_INFO, "0000-00-00");
        mService.getReadingInfo(dateSync).enqueue(new Callback<WrapperList<ReadingInfo>>() {
            @Override public void onResponse(Call<WrapperList<ReadingInfo>> call, Response<WrapperList<ReadingInfo>> response) {
                if (response.isSuccessful()) {
                    List<ReadingInfo> readingInfos = response.body().data;

                    if (Integer.valueOf(response.body().status) >= 1 && !readingInfos.isEmpty()) {
                        mDownloadedReadingInfos.postValue(readingInfos);
                    }

                    mPrefEdit.putString(AppConstants.PREFERENCE_LAST_SYNC_READING_INFO, mDateFormat.format(new Date())).apply();
                } else {
                    Log.e(TAG, "Fetch reading info failed: " + response.message());
                }
            }

            @Override public void onFailure(Call<WrapperList<ReadingInfo>> call, Throwable t) {
                Log.e(TAG, t.getMessage());
            }
        });
    }

    public void fetchNotification() {
        mService.getNotification().enqueue(new Callback<WrapperList<Notification>>() {
            @Override public void onResponse(Call<WrapperList<Notification>> call, Response<WrapperList<Notification>> response) {
                if (response.isSuccessful()) {
                    List<Notification> notifications = response.body().data;

                    if (Integer.valueOf(response.body().status) >= 1 && !notifications.isEmpty()) {
                        mDownloadedNotifications.postValue(notifications);
                    }
                } else {
                    Log.e(TAG, "Fetch FlashcardActivity failed: " + response.message());
                }
            }

            @Override public void onFailure(Call<WrapperList<Notification>> call, Throwable t) {
                Log.e(TAG, t.getMessage());
            }
        });
    }

    public void fetchGroups() {
        mService.getGroups().enqueue(new Callback<WrapperList<Group>>() {
            @Override public void onResponse(Call<WrapperList<Group>> call, Response<WrapperList<Group>> response) {
                if (response.isSuccessful()) {
                    List<Group> groups = response.body().data;

                    if (Integer.valueOf(response.body().status) >= 1) {
                        mDownloadedGroups.postValue(groups);
                    }
                } else {
                    Log.e(TAG, "Fetch Groups failed: " + response.message());
                }
            }

            @Override public void onFailure(Call<WrapperList<Group>> call, Throwable t) {
                Log.e(TAG, t.getMessage());
            }
        });
    }

    public void fetchMembers() {
        mService.getMembers().enqueue(new Callback<WrapperList<Member>>() {
            @Override public void onResponse(Call<WrapperList<Member>> call, Response<WrapperList<Member>> response) {
                if (response.isSuccessful()) {
                    List<Member> members = response.body().data;

                    if (Integer.valueOf(response.body().status) >= 1) {
                        mDownloadedMembers.postValue(members);
                    }
                } else {
                    Log.e(TAG, "Fetch Members failed: " + response.message());
                }
            }

            @Override public void onFailure(Call<WrapperList<Member>> call, Throwable t) {
                Log.e(TAG, t.getMessage());
            }
        });
    }

    public void fetchMemberProgress(Bundle extra) {
        if (extra == null) return;

        String memberId = extra.getString("memberId");

        mService.getMemberProgress(memberId).enqueue(new Callback<Wrapper<MemberProgress>>() {
            @Override public void onResponse(Call<Wrapper<MemberProgress>> call, Response<Wrapper<MemberProgress>> response) {
                if (response.isSuccessful()) {
                    MemberProgress members = response.body().data;

                    if (Integer.valueOf(response.body().status) >= 1) {
                        mMemberProgress.postValue(members);
                    } else {
                        mMemberProgress.postValue(new MemberProgress());
                    }
                } else {
                    Log.e(TAG, "Fetch member progress failed: " + response.message());
                }
            }

            @Override public void onFailure(Call<Wrapper<MemberProgress>> call, Throwable t) {
                Log.e(TAG, t.getMessage());
            }
        });
    }

    public void fetchUserByPhone(Bundle extra) {

    }

    public void compareContacts(List<Contact> contacts) {
        mService.uploadContacts(contacts).enqueue(new Callback<WrapperList<Contact>>() {
            @Override public void onResponse(Call<WrapperList<Contact>> call, Response<WrapperList<Contact>> response) {
                if (response.isSuccessful() && Integer.valueOf(response.body().status) > 0) {
                    List<Contact> serverContacts = response.body().data;
                    mUploadedContact.postValue(serverContacts);
                } else {
                    Log.e(TAG, "Upload user failed: " + response.message());
                }

//                Intent intent = new Intent(AppConstants.BROADCAST_INITIAL_DATA_COMPLETE);
//                LocalBroadcastManager.getInstance(mContext.get()).sendBroadcast(intent);
            }

            @Override public void onFailure(Call<WrapperList<Contact>> call, Throwable t) {
                Log.e(TAG, t.getMessage());
            }
        });
    }

    public void uploadUser(Bundle extra) {
        if (extra == null) return;

        UserParcel user = extra.getParcelable("user");

        if (user == null) return;

        mService.updateUser(
                user.getUserid(),
                user.getGps_lat(),
                user.getGps_lng(),
                user.getManufacture(),
                user.getApi_version(),
                user.getApp_version()).enqueue(new Callback<Wrapper<User>>() {
            @Override public void onResponse(Call<Wrapper<User>> call, Response<Wrapper<User>> response) {
                if (response.isSuccessful()) {
                    User result = response.body().data;
                    if (result != null && Integer.valueOf(response.body().status) >= 1)
                        mUploadedUser.postValue(new User(user));
                } else {
                    Log.e(TAG, "Upload user failed: " + response.message());
                }
            }

            @Override public void onFailure(Call<Wrapper<User>> call, Throwable t) {
                Log.e(TAG, t.getMessage());
            }
        });
    }

    public void uploadReading(Bundle extra) {
        if (extra == null) return;

        ReadingParcel reading = extra.getParcelable("reading");

        if (reading == null) return;

        mService.uploadReading(
                reading.getFile_id(),
                reading.getSequence_no(),
                reading.getSentence(),
                reading.getMastering_level(),
                extra.getString("userid")).enqueue(new Callback<Wrapper<Reading>>() {
            @Override public void onResponse(Call<Wrapper<Reading>> call, Response<Wrapper<Reading>> response) {
                if (response.isSuccessful()) {
                    Reading read = response.body().data;
                    if (read != null && Integer.valueOf(response.body().status) >= 1)
                        mUploadedReading.postValue(new Reading(reading));
                } else {
                    Log.e(TAG, "Upload reading failed: " + response.message());
                }
            }

            @Override public void onFailure(Call<Wrapper<Reading>> call, Throwable t) {
                Log.e(TAG, t.getMessage());
            }
        });
    }

    public void uploadChallenge(Bundle extra) {
        if (extra == null) return;

        mService.uploadChallenge(
                extra.getString("userid"),
                extra.getInt("not_seen"),
                extra.getInt("skipped"),
                extra.getInt("incorrect"),
                extra.getInt("correct"),
                extra.getInt("w_red"),
                extra.getInt("w_orange"),
                extra.getInt("w_yellow"),
                extra.getInt("w_green"),
                extra.getInt("w_blue"),
                extra.getInt("s_red"),
                extra.getInt("s_orange"),
                extra.getInt("s_yellow"),
                extra.getInt("s_green"),
                extra.getInt("s_blue")).enqueue(new Callback<Wrapper<Challenge>>() {
            @Override public void onResponse(Call<Wrapper<Challenge>> call, Response<Wrapper<Challenge>> response) {
                if (response.isSuccessful()) {
                    if (Integer.valueOf(response.body().status) < 1) {
                        Log.e(TAG, "Upload reading failed: " + response.message());
                    }
                } else {
                    Log.e(TAG, "Upload reading failed: " + response.message());
                }
            }

            @Override public void onFailure(Call<Wrapper<Challenge>> call, Throwable t) {
                Log.e(TAG, t.getMessage());
            }
        });
    }

    public void uploadFlashcard(Bundle extra) {
        if (extra == null) return;

        FlashcardParcel flashcard = extra.getParcelable("flashcard");

        if (flashcard == null) return;

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        String dateCreate = dateFormat.format(flashcard.getDatecreated());
        String dateUpdate = dateFormat.format(flashcard.getDatemodified());
        mService.uploadFlashcard(
                flashcard.getCard(),
                flashcard.getTranslation(),
                flashcard.getCategory(),
                null, //flashcard.getDefinition()
                null, //flashcard.getAudio()
                null, //flashcard.getPicture()
                null, //flashcard.getNotes()
                flashcard.getMastering_level(),
                null, //flashcard.getFile_id()
                dateCreate,
                dateUpdate,
                extra.getString("userid"),
                1).enqueue(new Callback<Wrapper<Flashcard>>() {
            @Override public void onResponse(Call<Wrapper<Flashcard>> call, Response<Wrapper<Flashcard>> response) {
                if (response.isSuccessful()) {
                    Flashcard card = response.body().data;
                    if (card != null && Integer.valueOf(response.body().status) >= 1)
                        mUploadedFlashcard.postValue(new Flashcard(flashcard));
                } else {
                    Log.e(TAG, "Upload flashcard failed: " + response.message());
                }
            }

            @Override public void onFailure(Call<Wrapper<Flashcard>> call, Throwable t) {
                Log.e(TAG, t.getMessage());
            }
        });
    }

    public void uploadGroup(Bundle extra) {
        if (extra == null) return;

        String name = extra.getString("name");
        String owner = extra.getString("owner");
        int privacy = extra.getInt("privacy");
        int translate = extra.getInt("translate");
        String remarks = extra.getString("remarks");
        String url = extra.getString("url");

        mService.createGroup(name, owner, privacy, translate, remarks, url).enqueue(new Callback<Wrapper<Group>>() {
            @Override public void onResponse(Call<Wrapper<Group>> call, Response<Wrapper<Group>> response) {
                if (response.isSuccessful()) {
                    Group group = response.body().data;
                    if (group != null && Integer.valueOf(response.body().status) >= 1) {
                        group.setDatecreated(new Date());
                        group.setDatemodified(new Date());
                        mUploadedGroup.postValue(group);
                    }
                } else {
                    Log.e(TAG, "Upload group failed: " + response.message());
                }
            }

            @Override public void onFailure(Call<Wrapper<Group>> call, Throwable t) {
                Log.e(TAG, t.getMessage());
            }
        });
    }

    public void updateGroup(Bundle extra) {
        if (extra == null) return;

        long id = extra.getLong("id");
        String name = extra.getString("name");
        String owner = extra.getString("owner");
        int privacy = extra.getInt("privacy");
        int translate = extra.getInt("translate");
        String remarks = extra.getString("remarks");
        String url = extra.getString("url");

        mService.updateGroup(id, name, owner, privacy, translate, remarks, url).enqueue(new Callback<Wrapper<Group>>() {
            @Override public void onResponse(Call<Wrapper<Group>> call, Response<Wrapper<Group>> response) {
                if (response.isSuccessful()) {
                    Group group = response.body().data;
                    if (group != null && Integer.valueOf(response.body().status) >= 1) {
                        group.setDatemodified(new Date());
                    }
                } else {
                    Log.e(TAG, "Update group failed: " + response.message());
                }
            }

            @Override public void onFailure(Call<Wrapper<Group>> call, Throwable t) {
                Log.e(TAG, t.getMessage());
            }
        });
    }

    public void uploadMember(Bundle extra) {
        if (extra == null) return;

        long groupId = extra.getLong("groupId");
        String userId = extra.getString("userId");
        int approved = extra.getInt("approved");
        int permission = extra.getInt("permission");

        mService.createMember(groupId, userId, approved, permission).enqueue(new Callback<Wrapper<Member>>() {
            @Override public void onResponse(Call<Wrapper<Member>> call, Response<Wrapper<Member>> response) {
                if (response.isSuccessful()) {
                    Member member = response.body().data;
                    mUploadedMember.postValue(member);
                } else {
                    Log.e(TAG, "Upload member failed: " + response.message());
                }
            }

            @Override public void onFailure(Call<Wrapper<Member>> call, Throwable t) {
                Log.e(TAG, t.getMessage());
            }
        });
    }

    public void uploadFCMToken(Bundle extra) {
        if (extra == null) return;

        String userId = extra.getString("userid");
        String token = extra.getString("token");

        mService.uploadMessageToken(userId, token).enqueue(new Callback<Wrapper<String>>() {
            @Override public void onResponse(Call<Wrapper<String>> call, Response<Wrapper<String>> response) {
                if (! response.isSuccessful()) {
                    Log.e(TAG, "Upload user token failed: " + response.message());
                } else {
                    Log.d(TAG, "FCM token uploaded");
                }
            }

            @Override public void onFailure(Call<Wrapper<String>> call, Throwable t) {
                Log.e(TAG, t.getMessage());
            }
        });
    }

    private void sendBroadcast(String intentString, String message) {
        Intent intent = new Intent(intentString);
        intent.putExtra(AppConstants.BROADCAST_MESSAGE, message);
        LocalBroadcastManager.getInstance(mContext.get()).sendBroadcast(intent);
    }

}
