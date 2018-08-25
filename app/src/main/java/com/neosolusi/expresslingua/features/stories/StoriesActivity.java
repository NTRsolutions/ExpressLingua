package com.neosolusi.expresslingua.features.stories;

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
import com.neosolusi.expresslingua.features.BaseActivity;
import com.neosolusi.expresslingua.features.flashcard.CardFragment;
import com.neosolusi.expresslingua.features.flashcard.FlashcardActivity;
import com.neosolusi.expresslingua.features.lessons.LessonsActivity;

import java.util.Timer;
import java.util.TimerTask;

import io.realm.Case;
import io.realm.RealmResults;

public class StoriesActivity extends BaseActivity<StoriesViewModel>
        implements StoriesAdapter.OnItemClickListener, View.OnClickListener {

    private SharedPreferences mPref;
    private SharedPreferences.Editor mPrefEdit;
    private RecyclerView mListEpisode;
    private StoriesAdapter mAdapter;
    private StoriesViewModel mViewModel;
    private RealmResults<Episode> mDataEpisodes;
    private LinearLayoutManager mLayoutManager;
    private Timer mTimer;
    private Handler mHandler;
    private boolean mColorSwitch;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stories);

        initComponent();
        initListener();
        configureLayout();

        StoriesViewModelFactory factory = AppInjectors.provideStoriesViewModelFactory(this);
        mViewModel = ViewModelProviders.of(this, factory).get(StoriesViewModel.class);
        mViewModel.getEpisodes().observe(this, episodes -> {
            if (episodes == null || episodes.isEmpty()) {
                showLoading();
                return;
            }

            findViewById(R.id.progressbar).setVisibility(View.GONE);
            mListEpisode.setVisibility(View.VISIBLE);
            mDataEpisodes = episodes;
            mAdapter.update(episodes);
            if (mPref.getInt(AppConstants.PREFERENCE_FRESH_STORIES, 0) == 0) {
                showAnimation(true);
            } else {
                showAnimation(false);
            }
        });
    }

    private void initComponent() {
        mAdapter = new StoriesAdapter(this, this);
        mHandler = new Handler(Looper.getMainLooper());
        mTimer = new Timer();
        mPref = AppInjectors.provideSharedPreferences(this);
        mPrefEdit = AppInjectors.provideSharedPreferencesEditor(this);
        mListEpisode = findViewById(R.id.recycler_episode);
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
        mListEpisode.setLayoutManager(mLayoutManager);
        mListEpisode.addItemDecoration(divider);
        mListEpisode.setHasFixedSize(true);
        mListEpisode.setAdapter(mAdapter);
    }

    private void showLoading() {
        findViewById(R.id.progressbar).setVisibility(View.VISIBLE);
        mListEpisode.setVisibility(View.GONE);
    }

    private void showAnimation(boolean show) {
        // Animate font color for first time app launch
        if (show) {
            mTimer.scheduleAtFixedRate(new TimerTask() {
                @Override public void run() {
                    mHandler.post(() -> {
                        ValueAnimator colorAnimation;
                        if (mLayoutManager.findViewByPosition(0) == null) return;
                        int mDefaultFontColor = ContextCompat.getColor(StoriesActivity.this, R.color.color_text);
                        int mFontColorColorAnimateValue = ContextCompat.getColor(StoriesActivity.this, R.color.white);
                        TextView textView = mLayoutManager.findViewByPosition(0).findViewById(R.id.item_text_episode_name);
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
            int mDefaultFontColor = ContextCompat.getColor(StoriesActivity.this, R.color.color_text);
            TextView textView = mLayoutManager.findViewByPosition(0).findViewById(R.id.item_text_episode_name);
            textView.setTextColor(mDefaultFontColor);
            if (mTimer != null) {
                mTimer.cancel();
                mTimer.purge();
                mTimer = null;
            }
        }
    }

    @Override public void onClick(View view, Episode episode, int position) {
        if (!mViewModel.hasFinishReadStory(episode)) {
            showNotFinishRead();
            return;
        }

        Bundle bundle = new Bundle();
        bundle.putLong("episode_id", episode.getEpisode_id());

        // Save EpisodeId to SharedPreferences and update fresh stories to not fresh anymore
        mPrefEdit.putLong(AppConstants.PREFERENCE_LAST_EPISODE_ID, episode.getEpisode_id()).apply();
        mPrefEdit.putInt(AppConstants.PREFERENCE_FRESH_STORIES, 1).apply();

        showAnimation(false);

        AppUtils.startActivityWithExtra(this, LessonsActivity.class, bundle);
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
                onBackPressed();
                break;
        }
    }

    @Override public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.animation_reverse, R.anim.animation2_reverse);
    }

    @Override public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.app_menu, menu);

        MenuItem searchItem = menu.findItem(R.id.menu_search);
        SearchView mSearchView = (SearchView) searchItem.getActionView();
        mSearchView.setQueryHint("Search Episode...");
        mSearchView.setIconifiedByDefault(true);
        mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override public boolean onQueryTextSubmit(String s) {
                if (s.trim().isEmpty()) {
                    mAdapter.update(mDataEpisodes);
                } else {
                    mAdapter.update(mDataEpisodes.where().contains("name_episode", s, Case.INSENSITIVE).findAll());
                }
                return true;
            }

            @Override public boolean onQueryTextChange(String s) {
                if (s.trim().isEmpty()) mAdapter.update(mDataEpisodes);
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
