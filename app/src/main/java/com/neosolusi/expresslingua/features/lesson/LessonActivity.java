package com.neosolusi.expresslingua.features.lesson;

import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.arch.lifecycle.ViewModelProviders;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;

import com.dinuscxj.progressbar.CircleProgressBar;
import com.google.android.exoplayer2.ui.PlayerControlView;
import com.google.android.exoplayer2.ui.PlayerView;
import com.neosolusi.expresslingua.AppConstants;
import com.neosolusi.expresslingua.AppInjectors;
import com.neosolusi.expresslingua.AppServices;
import com.neosolusi.expresslingua.AppServices.AudioDownloadCallback;
import com.neosolusi.expresslingua.AppUtils;
import com.neosolusi.expresslingua.MainActivity;
import com.neosolusi.expresslingua.R;
import com.neosolusi.expresslingua.data.entity.Reading;
import com.neosolusi.expresslingua.data.entity.ReadingInfo;
import com.neosolusi.expresslingua.data.network.DownloadReceiver;
import com.neosolusi.expresslingua.data.network.NetworkDownloadIntentService;
import com.neosolusi.expresslingua.features.BaseActivity;

import java.util.Timer;
import java.util.TimerTask;

public class LessonActivity extends BaseActivity implements LessonFragment.OnFragmentInteractionListener {

    public static final String LESSONS_ID = "lessons_id";
    public static final String LESSONS_AUDIO = "lessons_audio";
    public static final String LESSONS_PROCESS_TEXT = "lessons_process_text";

    // Component property
    private Timer mTimer;
    private Handler mHandler;
    private AppServices mAppService;
    private LessonViewModel mViewModel;
    private AudioDownloadCallback mDownloadCallback;
    private SharedPreferences mPref;
    private SharedPreferences.Editor mPrefEdit;
    private ImageButton mButtonDownload;
    private CircleProgressBar mProgressDownload;

    // Player property
    private PlayerView mPlayerView;

    // Data property
    private int mLessonsId;
    private String mLessonsAudio;
    private String mProcessText;
    private boolean mBound = false;
    private boolean mColorSwitch = false;

    private ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            AppServices.LocalBinder binder = (AppServices.LocalBinder) iBinder;
            mAppService = binder.getService();
            mAppService.setDownloadListener(mDownloadCallback);

            mBound = true;
            initialFragment();
            playerReady();
        }

        @Override public void onServiceDisconnected(ComponentName componentName) {
            mBound = false;
        }
    };

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lesson);

        mHandler = new Handler(Looper.getMainLooper());
        mTimer = new Timer();

        initComponent();
        configureLayout();
        initListener();

        LessonViewModelFactory factory = AppInjectors.provideLessonViewModelFactory(this);
        mViewModel = ViewModelProviders.of(this, factory).get(LessonViewModel.class);

        Intent service = new Intent(this, AppServices.class);
        bindService(service, mServiceConnection, Context.BIND_AUTO_CREATE);
    }

    private void configureLayout() {
        configureToolbarLesson();

        // Animate download button color for first time app launch
        mTimer.scheduleAtFixedRate(new TimerTask() {
            @Override public void run() {
                mHandler.post(() -> {
                    ObjectAnimator animator;
                    if (mColorSwitch) {
                        animator = ObjectAnimator.ofPropertyValuesHolder(mButtonDownload, PropertyValuesHolder.ofInt("alpha", 0, 255));
                    } else {
                        animator = ObjectAnimator.ofPropertyValuesHolder(mButtonDownload, PropertyValuesHolder.ofInt("alpha", 255, 0));
                    }
                    mColorSwitch = !mColorSwitch;
                    animator.setDuration(AppConstants.DEFAULT_ANIMATION_LENGTH);
                    animator.start();
                });
            }
        }, 1000, 1000);
    }

    private void initComponent() {
        mPlayerView = findViewById(R.id.player_view);
        mPref = AppInjectors.provideSharedPreferences(this);
        mPrefEdit = AppInjectors.provideSharedPreferencesEditor(this);
    }

    private void initListener() {
        PlayerControlView controlView = mPlayerView.findViewById(R.id.exo_controller);
        ImageButton btnNext = controlView.findViewById(R.id.button_next);
        btnNext.setOnClickListener(v -> {
            if (!canGoToNextLesson(mViewModel.getCurrentLesson(mLessonsId + 1))) return;

            ReadingInfo info = mViewModel.getNextLesson(mLessonsId);
            if (mBound && info != null) {
                mLessonsId = info.getFile_id();
                mLessonsAudio = info.getAudio_file_name();
                mAppService.releasePlayer();
                saveLastLesson();
                playerReady();
                loadFragment();
            } else {
                Snackbar.make(v, "End of lesson", Snackbar.LENGTH_LONG).show();
            }
        });

        ImageButton btnPrev = controlView.findViewById(R.id.button_prev);
        btnPrev.setOnClickListener(v -> onBackPressed());

        mDownloadCallback = new AudioDownloadCallback() {
            @Override public void onDownloadFinish(boolean result) {
                mButtonDownload.setVisibility(View.GONE);
                mProgressDownload.setVisibility(View.GONE);
                mButtonDownload.setImageDrawable(ContextCompat.getDrawable(LessonActivity.this, R.drawable.ic_cloud_download_white_24dp));
                if (mBound) mAppService.initializePlayer();

                mViewModel.updateReadingInfoDownloadComplete(mLessonsAudio);
            }

            @Override public void onProgressUpdate(int value) {
                mProgressDownload.setProgress(value);
                Log.d("Download Progress", "" + value);
            }
        };

        mProgressDownload = controlView.findViewById(R.id.progress_download);
        mButtonDownload = controlView.findViewById(R.id.button_download);
        mButtonDownload.setOnClickListener(v -> {
            if (mBound) {
                if (!AppUtils.isConnectedToInternet(LessonActivity.this)) {
                    showSnakeBar(mButtonDownload, "Anda sedang offline");
                    return;
                }
                mAppService.pause();
                mButtonDownload.setVisibility(View.GONE);
                mProgressDownload.setVisibility(View.VISIBLE);
                mProgressDownload.setProgress(10);
                mProgressDownload.setMax(100);
                mProgressDownload.setIndeterminate(false);
//                mAppService.downloadAudioFile(mLessonsAudio);

                Intent intent = new Intent(this, NetworkDownloadIntentService.class);
                Bundle bundle = new Bundle();
                bundle.putString("filename", mLessonsAudio);
                bundle.putParcelable("receiver", new DownloadReceiver(mDownloadCallback, new Handler()));
                intent.putExtra("extra", bundle);
                startService(intent);
            }
        });
    }

    private void initialFragment() {
        Fragment fragment;

        fragment = LessonFragment.newInstance(mLessonsId, mLessonsAudio, mProcessText);

        getSupportFragmentManager()
                .beginTransaction()
                .add(R.id.content_fragment, fragment)
                .commit();
    }

    private void loadFragment() {
        Fragment previousFragment = getSupportFragmentManager().findFragmentById(R.id.content_fragment);
        Fragment nextFragment = LessonFragment.newInstance(mLessonsId, mLessonsAudio, mProcessText);

        AppUtils.performFragmentTransition(previousFragment, nextFragment);

        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.content_fragment, nextFragment);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commitAllowingStateLoss();
    }

    private void handleIntent(Intent intent) {
        Bundle bundle = intent.getBundleExtra("extra");
        if (bundle != null) {
            mLessonsId = bundle.getInt(LESSONS_ID);
            mLessonsAudio = bundle.getString(LESSONS_AUDIO);
            intent.removeExtra("extra");
        } else {
            mLessonsId = mPref.getInt(AppConstants.PREFERENCE_LAST_LESSON_ID, 1);
            mLessonsAudio = mPref.getString(AppConstants.PREFERENCE_LAST_LESSON_AUDIO, null);
        }

        if (Build.VERSION.SDK_INT >= 23) {
            CharSequence processText = intent.getCharSequenceExtra(Intent.EXTRA_PROCESS_TEXT);
            if (processText != null) {
                mProcessText = processText.toString();
            }
        }

        saveLastLesson();
        if (mBound) initialFragment();
    }

    private void playerReady() {
        ReadingInfo info = mViewModel.getCurrentLesson(mLessonsId);
        if (info != null) {
            mAppService.setPlayerInfo(info);
            mAppService.initializePlayer();

            if (mAppService.isAudioFileExists()) {
                mButtonDownload.setVisibility(View.GONE);
                mProgressDownload.setVisibility(View.GONE);
                mButtonDownload.setImageDrawable(ContextCompat.getDrawable(LessonActivity.this, R.drawable.ic_cloud_download_white_24dp));
            } else {
                mButtonDownload.setVisibility(View.VISIBLE);
                mButtonDownload.setImageDrawable(ContextCompat.getDrawable(LessonActivity.this, R.drawable.ic_cloud_download_white_24dp));
            }
        }

        mPlayerView.setPlayer(mAppService.getPlayer());
        mPlayerView.setUseArtwork(false);
        mPlayerView.setDefaultArtwork(BitmapFactory.decodeResource(getResources(), R.drawable.album_noart));
        mPlayerView.getSubtitleView().setBottomPaddingFraction(0.49f);
        mPlayerView.getSubtitleView().setApplyEmbeddedFontSizes(true);
        mPlayerView.getSubtitleView().setApplyEmbeddedStyles(true);
    }

    private void saveLastLesson() {
        // Save LessonId & LessonAudio to SharedPreferences to tackle intent filter access
        mPrefEdit.putInt(AppConstants.PREFERENCE_LAST_LESSON_ID, mLessonsId).apply();
        mPrefEdit.putString(AppConstants.PREFERENCE_LAST_LESSON_AUDIO, mLessonsAudio).apply();
    }

    private void showSnakeBar(View view, @NonNull String message) {
        Snackbar.make(view, message, Snackbar.LENGTH_LONG).show();
    }

    @Override public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.lesson_menu, menu);
        return true;
    }

    @Override public void onBackPressed() {
        ReadingInfo info = mViewModel.getPrevLesson(mLessonsId);
        if (mBound && info != null) {
            mLessonsId = info.getFile_id();
            mLessonsAudio = info.getAudio_file_name();
            mAppService.releasePlayer();
            saveLastLesson();
            playerReady();
        }

        super.onBackPressed();

        overridePendingTransition(R.anim.animation_reverse, R.anim.animation2_reverse);
    }

    @Override public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                AppUtils.startActivity(this, MainActivity.class);
                finish();
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }

    @Override protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        handleIntent(intent);
    }

    @Override protected void onResume() {
        super.onResume();
        handleIntent(getIntent());
        if (mBound) {
            mAppService.releasePlayer();
            playerReady();
        }
    }

    @Override protected void onPause() {
        if (mBound) mAppService.releasePlayer();
        super.onPause();
    }

    @Override protected void onDestroy() {
        unbindService(mServiceConnection);
        mBound = false;
        super.onDestroy();
    }

    @Override public boolean useTranslate() {
        return mBound && mAppService.useTranslate();
    }

    @Override public void onSpeech(Reading reading, boolean isSlow) {
        if (mBound) mAppService.speech(reading, isSlow);
    }

    @Override public void onShowMasterLayout(boolean show) {
        if (show) {
            mPlayerView.setVisibility(View.GONE);
        } else {
            mPlayerView.setVisibility(View.VISIBLE);
        }
    }

}
