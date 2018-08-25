package com.neosolusi.expresslingua.features.lessons;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.app.ActivityOptions;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.neosolusi.expresslingua.AppConstants;
import com.neosolusi.expresslingua.AppInjectors;
import com.neosolusi.expresslingua.AppUtils;
import com.neosolusi.expresslingua.MainActivity;
import com.neosolusi.expresslingua.R;
import com.neosolusi.expresslingua.data.entity.Episode;
import com.neosolusi.expresslingua.data.entity.ReadingInfo;
import com.neosolusi.expresslingua.features.BaseActivity;
import com.neosolusi.expresslingua.features.flashcard.CardFragment;
import com.neosolusi.expresslingua.features.flashcard.FlashcardActivity;
import com.neosolusi.expresslingua.features.lesson.LessonActivity;

import java.util.Timer;
import java.util.TimerTask;

import io.realm.Case;
import io.realm.RealmResults;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static com.neosolusi.expresslingua.features.lesson.LessonActivity.LESSONS_AUDIO;
import static com.neosolusi.expresslingua.features.lesson.LessonActivity.LESSONS_ID;

public class LessonsActivity extends BaseActivity<LessonsViewModel>
        implements LessonsAdapter.OnItemClickListener, View.OnClickListener {

    private SharedPreferences mPref;
    private SharedPreferences.Editor mPrefEdit;
    private RecyclerView mListLessons;
    private TextView mTextHeader;
    private TextView mTextTranslation;
    private LessonsAdapter mAdapter;
    private LessonsViewModel mViewModel;
    private RealmResults<ReadingInfo> mDataLessons;
    private LinearLayoutManager mLayoutManager;
    private Timer mTimer;
    private Handler mHandler;
    private boolean mColorSwitch;
    private boolean mHasFinishCheckMetadata;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lessons);

        initComponent();
        initListener();
        configureLayout();

        LessonsViewModelFactory factory = AppInjectors.provideLessonsViewModelFactory(this);
        mViewModel = ViewModelProviders.of(this, factory).get(LessonsViewModel.class);
    }

    private void initComponent() {
        mPref = AppInjectors.provideSharedPreferences(this);
        mPrefEdit = AppInjectors.provideSharedPreferencesEditor(this);
        mAdapter = new LessonsAdapter(this, this);
        mHandler = new Handler(Looper.getMainLooper());
        mTimer = new Timer();
        mListLessons = findViewById(R.id.recycler_lessons);
        mTextHeader = findViewById(R.id.text_header);
        mTextTranslation = findViewById(R.id.text_translation);
    }

    private void initListener() {
        findViewById(R.id.button_flashcard).setOnClickListener(this);
        findViewById(R.id.button_exit).setOnClickListener(this);
    }

    private void configureLayout() {
        setSupportActionBar(findViewById(R.id.toolbar));
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        DividerItemDecoration divider = new DividerItemDecoration(this, DividerItemDecoration.VERTICAL);
        Drawable dividerImage = ContextCompat.getDrawable(this, R.drawable.divider);
        if (dividerImage != null) divider.setDrawable(dividerImage);

        mLayoutManager = new LinearLayoutManager(this);
        mListLessons.setLayoutManager(mLayoutManager);
        mListLessons.addItemDecoration(divider);
        mListLessons.setHasFixedSize(true);
        mListLessons.setAdapter(mAdapter);
    }

    private void showLoading(boolean show) {
        if (show) {
            findViewById(R.id.progressbar).setVisibility(VISIBLE);
            findViewById(R.id.content_lessons).setVisibility(GONE);
        } else {
            findViewById(R.id.progressbar).setVisibility(GONE);
            findViewById(R.id.content_lessons).setVisibility(VISIBLE);
        }
    }

    private void showAnimation(boolean show) {
        // Animate font color for first time app launch
        if (show) {
            mTimer.scheduleAtFixedRate(new TimerTask() {
                @Override public void run() {
                    mHandler.post(() -> {
                        ValueAnimator colorAnimation;
                        if (mLayoutManager.findViewByPosition(0) == null) return;
                        int mDefaultFontColor = ContextCompat.getColor(LessonsActivity.this, R.color.color_text);
                        int mFontColorColorAnimateValue = ContextCompat.getColor(LessonsActivity.this, R.color.white);
                        TextView textView = mLayoutManager.findViewByPosition(0).findViewById(R.id.item_text_lessons_short_title);
                        if (mColorSwitch) {
                            colorAnimation = ValueAnimator.ofObject(new ArgbEvaluator(), mDefaultFontColor, mFontColorColorAnimateValue);
                        } else {
                            colorAnimation = ValueAnimator.ofObject(new ArgbEvaluator(), mFontColorColorAnimateValue, mDefaultFontColor);
                        }
                        mColorSwitch = !mColorSwitch;
                        colorAnimation.setDuration(AppConstants.DEFAULT_ANIMATION_LENGTH);
                        colorAnimation.addUpdateListener(animator -> textView.setTextColor(((int) animator.getAnimatedValue())));
                        colorAnimation.start();
                    });
                }
            }, 1000, 1000);
        } else {
            if (mLayoutManager.findViewByPosition(0) == null) return;
            int mDefaultFontColor = ContextCompat.getColor(LessonsActivity.this, R.color.color_text);
            TextView textView = mLayoutManager.findViewByPosition(0).findViewById(R.id.item_text_lessons_short_title);
            textView.setTextColor(mDefaultFontColor);
            if (mTimer != null) {
                mTimer.cancel();
                mTimer.purge();
                mTimer = null;
            }
        }
    }

    private void fetchReadingInfos(long episodeId) {
        mViewModel.getReadingInfos(episodeId).observe(this, lessons -> {
            if (lessons == null || lessons.isEmpty()) {
                showLoading(true);
                return;
            }

            findViewById(R.id.progressbar).setVisibility(View.GONE);
            mListLessons.setVisibility(View.VISIBLE);
            mDataLessons = lessons;
            mAdapter.update(lessons);
            if (mPref.getInt(AppConstants.PREFERENCE_FRESH_LESSONS, 0) == 0) {
                showAnimation(true);
            } else {
                showAnimation(false);
            }
        });
    }

    @Override protected void onResume() {
        super.onResume();

        long episodeId;
        if (getIntent().getBundleExtra("extra") == null) {
            episodeId = mPref.getLong(AppConstants.PREFERENCE_LAST_EPISODE_ID, 1);
        } else {
            episodeId = getIntent().getBundleExtra("extra").getLong("episode_id", 0);
        }

        Episode episode = mViewModel.getEpisode(episodeId);

        if (episode != null) {
            mTextHeader.setText(episode.getTitle());
            mTextTranslation.setText(episode.getTitle_trans());

            if (!mViewModel.hasMetadata() && mViewModel.hasFlashcard()) {
                showLoading(true);
                mViewModel.checkMetaDataObserver().observe(this, value -> {
                    if (value != null && value >= 1 && !mHasFinishCheckMetadata) {
                        showLoading(false);
                        mHasFinishCheckMetadata = true;

                        fetchReadingInfos(episodeId);
                    }
                });

                // This is heavy task
                mViewModel.checkMetaData(episodeId);
            } else {
                fetchReadingInfos(episodeId);
            }
        }
    }

    @Override public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.animation_reverse, R.anim.animation2_reverse);
    }

    @Override public void onClick(View view, ReadingInfo readingInfo, int position) {
        // Check if user has minimum requirement to go to next lesson
        if (!canGoToNextLesson(readingInfo)) return;

        // Save Fresh Lessons to not fresh anymore in SharedPreferences
        mPrefEdit.putInt(AppConstants.PREFERENCE_FRESH_LESSONS, 1).apply();

        Bundle bundle = new Bundle();
        bundle.putInt(LESSONS_ID, readingInfo.getFile_id());
        bundle.putString(LESSONS_AUDIO, readingInfo.getAudio_file_name());

        AppUtils.startActivityWithExtra(this, LessonActivity.class, bundle);
    }

    @Override public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button_flashcard:
                Intent intent = new Intent(this, FlashcardActivity.class);
                Bundle animation = ActivityOptions.makeCustomAnimation(this, R.anim.animation, R.anim.animation2).toBundle();
                intent.putExtra(CardFragment.ARG_LEVEL, 0);
                intent.putExtra(CardFragment.ARG_TYPE, "SINGLE_WORD");
                startActivity(intent, animation);
                break;
            case R.id.button_exit:
                AppUtils.startActivity(this, MainActivity.class);
                break;
        }
        finish();
    }

    @Override public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.app_menu, menu);

        MenuItem searchItem = menu.findItem(R.id.menu_search);
        SearchView mSearchView = (SearchView) searchItem.getActionView();
        mSearchView.setQueryHint("Search Lessons...");
        mSearchView.setIconifiedByDefault(true);
        mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override public boolean onQueryTextSubmit(String s) {
                if (s.trim().isEmpty()) {
                    mAdapter.update(mDataLessons);
                } else {
                    mAdapter.update(mDataLessons.where().contains("title", s, Case.INSENSITIVE).findAll());
                }
                return true;
            }

            @Override public boolean onQueryTextChange(String s) {
                if (s.trim().isEmpty()) mAdapter.update(mDataLessons);
                return true;
            }
        });

        EditText editTextSearch = mSearchView.findViewById(android.support.v7.appcompat.R.id.search_src_text);
        editTextSearch.setTextColor(ContextCompat.getColor(this, R.color.colorFooterPrimary));
        editTextSearch.setHintTextColor(ContextCompat.getColor(this, R.color.colorFooterPrimary));

        return true;
    }

    @Override public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;
            default:
                return super.onOptionsItemSelected(item);
        }

        return true;
    }

}