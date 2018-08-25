package com.neosolusi.expresslingua;

import android.Manifest;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.provider.ContactsContract;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.android.exoplayer2.SimpleExoPlayer;
import com.neosolusi.expresslingua.data.entity.Contact;
import com.neosolusi.expresslingua.data.entity.Group;
import com.neosolusi.expresslingua.data.entity.Member;
import com.neosolusi.expresslingua.data.entity.Reading;
import com.neosolusi.expresslingua.data.entity.ReadingInfo;
import com.neosolusi.expresslingua.data.entity.User;
import com.neosolusi.expresslingua.data.network.ExpressLinguaApi;
import com.neosolusi.expresslingua.data.network.NetworkDataSource;
import com.neosolusi.expresslingua.data.network.NetworkUtils;
import com.neosolusi.expresslingua.data.repo.BaseRepository;
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
import com.neosolusi.expresslingua.features.lesson.LessonActivity;
import com.neosolusi.expresslingua.receiver.ContactReceiver;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import io.realm.Realm;
import okhttp3.ResponseBody;
import retrofit2.Response;
import retrofit2.Retrofit;

public class AppServices extends Service {

    public static final String ACTION_SYNC = "action_sync";
    public static final String DOWNLOAD_ACTION = "download";
    public static final int FOREGROUND_SERVICE = 102;

    private static final int INTERVAL_MINUTES = 1;
    private static final long INTERVAL_MILLIS = TimeUnit.MINUTES.toMillis(INTERVAL_MINUTES);

    // Repositories, service, and player
    private static UserRepository mUserRepo;
    private static EpisodeRepository mEpisodeRepo;
    private static ReadingRepository mReadingRepo;
    private static FlashcardRepository mFlashcardRepo;
    private static ChallengeRepository mChallengeRepo;
    private static DictionaryRepository mDictionaryRepo;
    private static ReadingInfoRepository mReadingInfoRepo;
    private static GroupRepository mGroupRepo;
    private static MemberRepository mMemberRepo;
    private static ContactRepository mContactRepo;
    private static ExpressLinguaApi mNetwork;
    private static AppPlayer mPlayer;

    // Downloader
    private static NotificationRepository mNotificationRepo;
    private final IBinder mBinder = new LocalBinder();

    // References
    private Realm mDatabase;
    private ContactReceiver mContactReceiver;

    // Data Property
    private boolean mUseTranslate = true;
    private NotificationManager mNotificationManager;
    private NotificationCompat.Builder mNotification;
    private AudioDownloadCallback mListener;
    private Handler mHandler;
    private Timer mTimerUpload, mTimerWaitingSync;

    @Override public void onCreate() {
        super.onCreate();

        mUserRepo = AppInjectors.provideUserRepository(this);
        mEpisodeRepo = AppInjectors.provideEpisodeRepository(this);
        mReadingRepo = AppInjectors.provideReadingRepository(this);
        mFlashcardRepo = AppInjectors.provideFlashcardRepository(this);
        mChallengeRepo = AppInjectors.provideChallengeRepository(this);
        mDictionaryRepo = AppInjectors.provideDictionaryRepository(this);
        mReadingInfoRepo = AppInjectors.provideReadingInfoRepository(this);
        mNotificationRepo = AppInjectors.provideNotificationRepository(this);
        mGroupRepo = AppInjectors.provideGroupRepository(this);
        mMemberRepo = AppInjectors.provideMemberRepository(this);
        mContactRepo = AppInjectors.provideContactRepository(this);

        Retrofit retrofit = NetworkUtils.getWorker(this, AppInjectors.provideSharedPreferences(this));
        mNetwork = retrofit.create(ExpressLinguaApi.class);

        mPlayer = AppPlayer.getInstance(this);

        housekeeping();

        mDatabase = Realm.getDefaultInstance();
        mHandler = new Handler(Looper.getMainLooper());
        mTimerUpload = new Timer();
        mTimerWaitingSync = new Timer();

        // Register contact change observer
        mContactReceiver = new ContactReceiver(this, mContactRepo);
        getApplicationContext().getContentResolver().registerContentObserver(ContactsContract.Contacts.CONTENT_URI, true, mContactReceiver);
    }

    @Override public void onDestroy() {
        if (mTimerUpload != null) {
            mTimerUpload.cancel();
            mTimerUpload = null;
        }

        mDatabase.close();

        Intent intent = new Intent(AppConstants.BROADCAST_APP_DESTROY);
        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);

        getApplicationContext().getContentResolver().unregisterContentObserver(mContactReceiver);

        super.onDestroy();
    }

    @Override public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent == null || intent.getAction() == null) return START_NOT_STICKY;

        User user = mUserRepo.findActiveUser();
        Bundle bundle = new Bundle();
        bundle.putString("userid", user.getUserid());

        intent.putExtra("extra", bundle);

        switch (intent.getAction()) {
            case ACTION_SYNC:
                performSync(intent.getBundleExtra("extra"));
                break;
            default:
                performSync(intent.getBundleExtra("extra"));
        }

        return START_STICKY;
    }

    @Nullable @Override public IBinder onBind(Intent intent) {
        return mBinder;
    }

    private void performSync(Bundle extra) {
        // Read Contact
        // readContact(); Remark dulu belum ready

        // Wakeup all the worker
        List<BaseRepository> repositories = new ArrayList<>();
        repositories.add(mEpisodeRepo);
        repositories.add(mReadingRepo);
        repositories.add(mDictionaryRepo);
        repositories.add(mReadingInfoRepo);
        repositories.add(mNotificationRepo);
        repositories.add(mGroupRepo);
        repositories.add(mMemberRepo);
        repositories.add(mContactRepo);
        repositories.add(mChallengeRepo);
        repositories.add(mFlashcardRepo);

        for (BaseRepository repo : repositories) {
            repo.wakeup();
        }

        NetworkDataSource networkDataSource = AppInjectors.provideNetworkDataSource(this);
        networkDataSource.scheduleFetchService(extra);

        mTimerUpload.scheduleAtFixedRate(new TimerTask() {
            @Override public void run() {
                mHandler.post(() -> {
                    mFlashcardRepo.upload();
                    mReadingRepo.upload();
                    mChallengeRepo.upload();
                    mMemberRepo.upload();
                });
            }
        }, INTERVAL_MILLIS, INTERVAL_MILLIS);

        mTimerWaitingSync.schedule(new TimerTask() {
            @Override public void run() {
                mHandler.post(() -> {
                    Log.d("TimerWaitingSync", "Waiting");
                    repositories.remove(mNotificationRepo);
                    repositories.remove(mFlashcardRepo);
                    repositories.remove(mChallengeRepo);
                    repositories.remove(mGroupRepo);
                    repositories.remove(mMemberRepo);
                    repositories.remove(mContactRepo);
                    boolean ready = true;
                    for (BaseRepository repo : repositories) {
                        if (repo.isFetchNeeded()) {
                            ready = false;
                            break;
                        }
                    }
                    if (ready) initialDataFinished();
                });
            }
        }, 500, 1000);
    }

    private void showNotification() {
        Intent notificationIntent = new Intent(this, LessonActivity.class);
        notificationIntent.setAction(DOWNLOAD_ACTION);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Bitmap icon = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher);

        mNotification = new NotificationCompat.Builder(this, AppServices.class.getSimpleName())
                .setContentTitle("ExpressLingua Audio Download")
                .setContentText("Download in progress")
                .setContentIntent(pendingIntent)
                .setSmallIcon(R.drawable.ic_notif_icon)
                .setLargeIcon(Bitmap.createScaledBitmap(icon, 128, 128, false))
                .setOngoing(false);

        mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
    }

    private void initialDataFinished() {
        if (mTimerWaitingSync != null) {
            mTimerWaitingSync.cancel();
            mTimerWaitingSync.purge();
            mTimerWaitingSync = null;

            // Begin process only when other worker has finish initializeData
            mFlashcardRepo.initializeData();

            // Begin fetch reading user only when fresh install/reinstall
            SharedPreferences pref = AppInjectors.provideSharedPreferences(this);
            String readingUserLastSync = pref.getString(AppConstants.PREFERENCE_LAST_SYNC_READING_USER, "0000-00-00");
            if (readingUserLastSync.equalsIgnoreCase("0000-00-00")) {
                mReadingRepo.housekeeping();
            }

            // Begin populate challenge from Reading
            mChallengeRepo.initializeData();

            // Upload contact to server & send notify initial data isCompleted
            // uploadContact(); Remark dulu belum ready

            Intent intent = new Intent(AppConstants.BROADCAST_INITIAL_DATA_COMPLETE);
            LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
        }
    }

    private void housekeeping() {
        // Get data from server after migration schema version 2
        SharedPreferences.Editor editor = AppInjectors.provideSharedPreferencesEditor(this);
        if (mDictionaryRepo.isFetchNeeded()) {
            editor.putString(AppConstants.PREFERENCE_LAST_SYNC_DICTIONARY, "0000-00-00").apply();
        }

        if (mReadingRepo.isFetchNeeded()) {
            mReadingRepo.deleteAll();
            editor.putString(AppConstants.PREFERENCE_LAST_SYNC_READING, "0000-00-00").apply();
        }

        if (mFlashcardRepo.isFetchNeeded()) {
            editor.putString(AppConstants.PREFERENCE_LAST_SYNC_FLASHCARD, "0000-00-00").apply();
        }
    }

    private void readContact() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        ContentResolver cr = getContentResolver();
        Cursor cursor = cr.query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                new String[]{
                        ContactsContract.Contacts._ID,
                        ContactsContract.Contacts.DISPLAY_NAME,
                        ContactsContract.CommonDataKinds.Phone.NUMBER,
                        ContactsContract.RawContacts.ACCOUNT_TYPE
                },
                ContactsContract.RawContacts.ACCOUNT_TYPE + " <> 'google' ",
                null, null);

        if (cursor == null) return;

        if (cursor.getCount() > 0) {
            List<Contact> contacts = new ArrayList<>();
            while (cursor.moveToNext()) {
                String Phone_number = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                String name = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));

                Contact contact = new Contact();
                contact.setId(UUID.randomUUID().getMostSignificantBits() & Long.MAX_VALUE);
                contact.setPhone(AppUtils.normalizePhone(Phone_number));
                contact.setName(name);
                contact.setActive(false);
                contacts.add(contact);
            }

            List<Contact> filteredContact = new ArrayList<>();
            for (Contact contact : contacts) {
                if (mContactRepo.findFirstCopyEqualTo("phone", AppUtils.normalizePhone(contact.getPhone())) == null) {
                    filteredContact.add(contact);
                }
            }
            mContactRepo.copyOrUpdate(filteredContact);
        }

        cursor.close();
    }

    private void uploadContact() {
        mContactRepo.compareToService(mDatabase.copyFromRealm(mDatabase.where(Contact.class).equalTo("isActive", false).findAll()));
    }

    // *******************************************************************************
    // Public Functions
    // *******************************************************************************
    public boolean useTranslate() {
        String user = mUserRepo.findActiveUser().getUserid();

        for (Member member : mMemberRepo.findAllEqualTo("user_id", user)) {
            Group group = mGroupRepo.findFirstEqualTo("id", member.getGroup_id());
            if (group == null) continue;

            if (group.getTranslate() == 0) {
                mUseTranslate = false;
                break;
            }
        }

        return mUseTranslate;
    }

    // *******************************************************************************
    // Player
    // *******************************************************************************
    public SimpleExoPlayer getPlayer() {
        return mPlayer.getPlayer();
    }

    public void setPlayerInfo(ReadingInfo info) {
        mPlayer.setInfo(info);
    }

    public boolean isAudioFileExists() {
        return mPlayer.isAudioExists();
    }

    public void initializePlayer() {
        mPlayer.initializePlayer();
    }

    public void releasePlayer() {
        mPlayer.releasePlayer();
    }

    public void speech(Reading reading, boolean isSlow) {
        mPlayer.speech(reading, isSlow);
    }

    public void pause() {
        mPlayer.pause();
    }

    // *******************************************************************************
    // Audio download
    // *******************************************************************************
    public void downloadAudioFile(String fileName) {
        showNotification();
        AudioDownload download = new AudioDownload(this, fileName, mNotification, mNotificationManager, mListener);
        download.execute();
    }

    public void setDownloadListener(AudioDownloadCallback listener) {
        mListener = listener;
    }

    public interface AudioDownloadCallback {
        void onDownloadFinish(boolean result);

        void onProgressUpdate(int value);
    }

    public static class AudioDownload extends AsyncTask<Void, Integer, Boolean> {
        private AudioDownloadCallback mListener;
        private NotificationManager mNotificationManager;
        private NotificationCompat.Builder mNotification;
        private String mLessonsAudio;

        private File audioPath;
        private File audioFile;
        private byte[] fileReader = new byte[1024];
        private long fileSize, fileSizeDownloaded;
        private int read, done = 0;

        public AudioDownload(Context context, String fileName, NotificationCompat.Builder notification, NotificationManager notificationManager, AudioDownloadCallback cb) {
            mLessonsAudio = fileName;
            mNotificationManager = notificationManager;
            mNotification = notification;
            mListener = cb;
            audioPath = new File(context.getFilesDir(), "audio");
            audioFile = new File(audioPath, mLessonsAudio.split("\\.")[0] + ".m4a");
        }

        @Override protected Boolean doInBackground(Void... voids) {
            try {
                Response<ResponseBody> response = mNetwork.downloadAudioFile(mLessonsAudio.split("\\.")[0] + ".m4a").execute();

                if (response.isSuccessful()) {
                    fileSize = response.body().contentLength();
                    fileSizeDownloaded = 0;

                    if (!audioPath.isDirectory() && !audioPath.mkdir()) return false;
                    if (!audioFile.exists() && !audioFile.createNewFile()) return false;

                    InputStream input = response.body().byteStream();
                    OutputStream output = new FileOutputStream(audioFile);

                    while ((read = input.read(fileReader)) != -1) {
                        output.write(fileReader, 0, read);
                        fileSizeDownloaded += read;

                        Long d = fileSizeDownloaded * 100 / fileSize;
                        if (d.intValue() != done) {
                            done = d.intValue();
                            publishProgress(d.intValue());
                        }
                    }
                } else {
                    return false;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            return true;
        }

        @Override protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            mNotification.setProgress(100, values[0], false);
            mNotificationManager.notify(FOREGROUND_SERVICE, mNotification.build());
            if (mListener != null) mListener.onProgressUpdate(values[0]);
        }

        @Override protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);

            ReadingInfo info = mReadingInfoRepo.findFirstCopyEqualTo("audio_file_name", mLessonsAudio);
            info.setDownload_complete(true);
            mReadingInfoRepo.copyOrUpdate(info);

            if (result) {
                mNotification.setContentText("Download complete").setProgress(0, 0, false);
                mNotificationManager.notify(FOREGROUND_SERVICE, mNotification.build());
                if (mListener != null) mListener.onDownloadFinish(true);
            } else {
                mNotification.setContentText("Download failed").setProgress(0, 0, false);
                mNotificationManager.notify(FOREGROUND_SERVICE, mNotification.build());
                if (mListener != null) mListener.onDownloadFinish(false);
            }
        }
    }

    public class LocalBinder extends Binder {
        public AppServices getService() {
            return AppServices.this;
        }
    }

}
