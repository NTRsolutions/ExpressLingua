package com.neosolusi.expresslingua.features.challenge;

import android.Manifest;
import android.arch.lifecycle.ViewModelProviders;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.neosolusi.expresslingua.AppInjectors;
import com.neosolusi.expresslingua.AppServices;
import com.neosolusi.expresslingua.AppUtils;
import com.neosolusi.expresslingua.MainActivity;
import com.neosolusi.expresslingua.R;
import com.neosolusi.expresslingua.data.entity.Challenge;
import com.neosolusi.expresslingua.data.entity.Reading;
import com.neosolusi.expresslingua.data.entity.ReadingInfo;
import com.neosolusi.expresslingua.features.BaseActivity;

import java.util.Random;

public class ChallengeActivity extends BaseActivity implements OnInteractionListener, OnShowTutorialListener {

    public static final String LIST_CHALLENGES = "list_challenges";
    public static final String LIST_CHALLENGE_TYPE = "challenge_type";

    // Component property
    private AppServices mAppService;
    private ChallengeViewModel mViewModel;
    private Challenge mChallenge;
    private Challenge mLastChallenge;
    private boolean mChallengeSwitch = true;
    private boolean mBound;
    private boolean isListChallenge = false;

    private ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            AppServices.LocalBinder binder = (AppServices.LocalBinder) iBinder;
            mAppService = binder.getService();
            mBound = true;
            initialFragment();
        }

        @Override public void onServiceDisconnected(ComponentName componentName) {
            mBound = false;
        }
    };

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_challenge);

//        findViewById(R.id.app_bar).setVisibility(View.VISIBLE);
//        Toolbar toolbar = findViewById(R.id.toolbar);

//        Bitmap bitmap = ((BitmapDrawable) getResources().getDrawable(R.drawable.icon_left_home)).getBitmap();
//        Drawable drawable = new BitmapDrawable(getResources(), Bitmap.createScaledBitmap(bitmap, 50, 50, true));
//        drawable.setColorFilter(getResources().getColor(R.color.colorFooterPrimary), PorterDuff.Mode.MULTIPLY);
//        toolbar.setNavigationIcon(drawable);

        setSupportActionBar(findViewById(R.id.toolbar));
        if (getSupportActionBar() != null) {
            getSupportActionBar().setCustomView(R.layout.app_bar_lesson);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        Bundle bundle = getIntent().getBundleExtra("extra");
        if (bundle != null) isListChallenge = bundle.getBoolean(LIST_CHALLENGES, false);
    }

    @Override protected void onDestroy() {
        if (mBound) {
            mAppService.releasePlayer();
            unbindService(mServiceConnection);
            mBound = false;
        }
        super.onDestroy();
    }

    @Override protected void onPause() {
        super.onPause();
    }

    @Override protected void onStop() {
        if (mBound) {
            mAppService.releasePlayer();
            unbindService(mServiceConnection);
            mBound = false;
        }
        super.onStop();
    }

    @Override protected void onStart() {
        super.onStart();
        if (!mBound) boundToService();
        requestPermissions();
    }

    @Override protected void onResume() {
        super.onResume();
    }

    @Override public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    @Override public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case 50:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d("Permission", "Granted");
                } else {
                    new AlertDialog.Builder(this).setTitle("Permissions").setIcon(R.mipmap.ic_launcher)
                            .setMessage("Aplikasi membutuhkan beberapa akses untuk dapat bekerja")
                            .setNegativeButton("Tutup", (dialog, which) -> {
                                dialog.dismiss();
                                finish();
                            })
                            .show();
                }
        }
    }

    @Override public boolean onCreateOptionsMenu(Menu menu) {
        if (isListChallenge) getMenuInflater().inflate(R.menu.lesson_menu, menu);
        return true;
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

    private void getQuestion() {
        mChallenge = mViewModel.getQuestions();
        mChallengeSwitch = false;

        if (mChallenge == null) {
            if (mViewModel.isHasFinishChallenges()) {
                showFinishChallenges();
                return;
            }
            showNoChallenges();
            return;
        }

        if (mViewModel.findReadingForChallenge(mChallenge) == null) getQuestion();
    }

    private void boundToService() {
        Intent service = new Intent(this, AppServices.class);
        bindService(service, mServiceConnection, Context.BIND_AUTO_CREATE);
    }

    private void requestPermissions() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, 50);
        }
    }

    private boolean isShouldRepeatChallenge() {
        Reading reading = mViewModel.findReadingForChallenge(mChallenge);

        return reading.getMastering_level() == 3 && mChallenge == mLastChallenge;
    }

    private void initialFragment() {
        Fragment fragment;

        if (isListChallenge) {
            configureToolbarLesson();

            Toolbar toolbar = findViewById(R.id.toolbar);
            toolbar.findViewById(R.id.image_arrow_up).setVisibility(View.GONE);
            toolbar.findViewById(R.id.image_arrow_down).setVisibility(View.GONE);
            toolbar.findViewById(R.id.image_help).setVisibility(View.GONE);

            fragment = new ListFragment();
            fragment.setArguments(getIntent().getBundleExtra("extra"));
        } else {
            ChallengeViewModelFactory factory = AppInjectors.provideChallengeViewModelFactory(this);
            mViewModel = ViewModelProviders.of(this, factory).get(ChallengeViewModel.class);

            if (mChallenge == null) getQuestion();
            mLastChallenge = mChallenge;
            Random random = new Random();
            int rnd = random.nextInt(2) + 1;
            if (rnd == 1) {
                fragment = PuzzleFragment.newInstance(mChallenge);
            } else if (rnd == 2) {
                fragment = SpeakFragment.newInstance(mChallenge);
            } else {
                fragment = WrittenFragment.newInstance(mChallenge);
            }
        }

        getSupportFragmentManager()
                .beginTransaction()
                .add(R.id.content_fragment, fragment)
                .commitAllowingStateLoss();
    }

    private void loadFragment() {
        Fragment previousFragment = getSupportFragmentManager().findFragmentById(R.id.content_fragment);
        Fragment nextFragment;

        if (isShouldRepeatChallenge()) {
            mLastChallenge = null;
            if (previousFragment instanceof WrittenFragment) {
                nextFragment = PuzzleFragment.newInstance(mChallenge);
            } else {
                nextFragment = WrittenFragment.newInstance(mChallenge);
            }
        } else {
            getQuestion();
            if (mChallenge == null) return;
            mLastChallenge = mChallenge;

            Random random = new Random();
            int rnd = random.nextInt(2) + 1;
            if (rnd == 1) {
                nextFragment = PuzzleFragment.newInstance(mChallenge);
            } else if (rnd == 2) {
                nextFragment = SpeakFragment.newInstance(mChallenge);
            } else {
                nextFragment = WrittenFragment.newInstance(mChallenge);
            }
        }

        getSupportFragmentManager().popBackStackImmediate();
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.content_fragment, nextFragment);
        fragmentTransaction.commitAllowingStateLoss();
    }

    @Override public void speak(Reading reading, boolean isSlow) {
        if (mBound && reading != null) mAppService.speech(reading, isSlow);
    }

    @Override public void resetPlayer() {
        if (mBound) {
            mAppService.releasePlayer();
            mAppService.initializePlayer();
        }
    }

    @Override public void setPlayerInfo(ReadingInfo info) {
        if (mBound && info != null) {
            mAppService.setPlayerInfo(info);
            mAppService.initializePlayer();
        }
    }

    @Override public void correctAnswers(Challenge challenge) {
        mViewModel.correctAnswers(challenge);
    }

    @Override public void wrongAnswers(Challenge challenge) {
        mViewModel.wrongAnswers(challenge);
    }

    @Override public void nextChallenge() {
        new Handler().postDelayed(this::loadFragment, 100);
    }

    @Override public void showNoChallenges() {
        new AlertDialog.Builder(this).setTitle("Challenges").setIcon(R.mipmap.ic_launcher_ealing)
                .setMessage("Challenge tidak tersedia. Untuk melakukan ini Anda harus sudah punya kalimat yang agak sulit, agak mudah atau sangat mudah")
                .setPositiveButton("Kembali", (dialog, which) -> this.onBackPressed())
                .setCancelable(false)
                .show();
    }

    @Override public boolean useTranslate() {
        return mBound && mAppService.useTranslate();
    }

    @Override public void showFinishChallenges() {
        new AlertDialog.Builder(this).setTitle("Challenges").setIcon(R.mipmap.ic_launcher_ealing)
                .setMessage("Selamat Anda telah menyelesaikan challenge")
                .setPositiveButton("Home", (dialog, which) -> this.onBackPressed())
                .setNegativeButton("Ulangi", (dialog, which) -> {
                    mViewModel.repeatQuestions();
                    getQuestion();
                })
                .setCancelable(false)
                .show();
    }

    @Override public void showTutorial(String title, String description, int image) {
        Fragment previousFragment = getSupportFragmentManager().findFragmentById(R.id.content_fragment);
        Fragment nextFragment = HelpChallengeFragment.newInstance(title, description, image);
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.add(R.id.content_fragment, nextFragment);
        fragmentTransaction.commitAllowingStateLoss();
    }

    public enum TYPE {
        NOT_SEEN, SKIPPED, INCORRECT, CORRECT
    }

}
