package com.neosolusi.expresslingua.features.home;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.app.ActivityOptions;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.neosolusi.expresslingua.AppConstants;
import com.neosolusi.expresslingua.AppInjectors;
import com.neosolusi.expresslingua.AppUtils;
import com.neosolusi.expresslingua.OnNavigationListener;
import com.neosolusi.expresslingua.R;
import com.neosolusi.expresslingua.data.entity.Challenge;
import com.neosolusi.expresslingua.data.entity.Flashcard;
import com.neosolusi.expresslingua.data.entity.Notification;
import com.neosolusi.expresslingua.data.entity.Reading;
import com.neosolusi.expresslingua.features.challenge.ChallengeActivity;
import com.neosolusi.expresslingua.features.flashcard.CardFragment;
import com.neosolusi.expresslingua.features.flashcard.FlashcardActivity;
import com.neosolusi.expresslingua.features.stories.StoriesActivity;

import java.text.DecimalFormat;
import java.util.Timer;
import java.util.TimerTask;

import io.realm.RealmResults;

import static android.view.View.GONE;
import static com.neosolusi.expresslingua.features.challenge.ChallengeActivity.LIST_CHALLENGES;
import static com.neosolusi.expresslingua.features.challenge.ChallengeActivity.LIST_CHALLENGE_TYPE;

public class HomeFragment extends Fragment implements View.OnClickListener {
    public static final String TAG = "com.neosolusi.expresslingua.features.home.HomeFragment";

    private TextView mIndicatorGreen;
    private TextView mIndicatorYellow;
    private TextView mIndicatorOrange;
    private TextView mIndicatorRed;
    private TextView mIndicatorBlue;

    private TextView mSentenceIndicatorGreen;
    private TextView mSentenceIndicatorYellow;
    private TextView mSentenceIndicatorOrange;
    private TextView mSentenceIndicatorRed;
    private TextView mSentenceIndicatorBlue;

    private TextView mTextEpisode;
    private TextView mTextWiki;
    private TextView mTextNotification;

    private TextView mTextNotSeen, mTextSkipped, mTextIncorrect, mTextCorrect;
    private TextView mTextNotSeenProgress, mTextSkippedProgress, mTextIncorrectProgress, mTextCorrectProgress;

    private CardView mCardImageDashboard;

    private boolean mColorSwitch;
    private int mDefaultFontColor, mFontColorColorAnimateValue;

    private Timer mTimer;
    private Context mContext;
    private Handler mHandler;
    private String mUrl;
    private HomeViewModel mHomeViewModel;
    private OnNavigationListener mListener;

    private boolean isFlashcardButtonEnable = false;
    private boolean isChallengeButtonEnable = false;

    public HomeFragment() {
        // Required empty public constructor
    }

    public static HomeFragment getInstance() {
        return new HomeFragment();
    }

    @Override public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_home, container, false);

        mHandler = new Handler(Looper.getMainLooper());
        mTimer = new Timer();

        initComponent(rootView);
        initListener(rootView);
        configureLayout();

        return rootView;
    }

    @Override public void onResume() {
        super.onResume();
        HomeViewModelFactory factory = AppInjectors.provideHomeViewModelFactory(this.getContext());
        mHomeViewModel = ViewModelProviders.of(this, factory).get(HomeViewModel.class);
        mHomeViewModel.getFlashcards().observe(this, this::summary);
        mHomeViewModel.getReadings().observe(this, this::summaryReading);
        mHomeViewModel.getNotifications().observe(this, this::showNotifications);
        mHomeViewModel.getChallenges().observe(this, this::summaryChallenges);
    }

    @Override public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
        if (context instanceof OnNavigationListener) {
            mListener = (OnNavigationListener) context;
        } else {
            throw new RuntimeException(context.toString() + " must implement OnFragmentInteractionListener");
        }
    }

    @Override public void onDetach() {
        if (mListener != null) mListener = null;
        super.onDetach();
    }

    @Override public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button_exit:
                exitApps();
                break;
            case R.id.button_help:
                showHelp();
                break;
            case R.id.button_stories:
            case R.id.button_flashcard:
                showStories(v);
                break;
            case R.id.button_challenge:
                showChallenge();
                break;
            case R.id.text_wiki:
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(mUrl));
                startActivity(intent);
                break;
            case R.id.layout_correct:
            case R.id.layout_incorrect:
            case R.id.layout_skipped:
            case R.id.layout_not_seen:
                showChallengesList(v);
                break;
        }
    }

    private void showHelp() {
        mListener.onShowHelp();
    }

    private void showStories(View v) {
        if (getActivity() == null) return;

        switch (v.getId()) {
            case R.id.button_stories:
                if (getActivity() == null) break;
                AppUtils.startActivity(getContext(), StoriesActivity.class);
                break;
            case R.id.button_flashcard:
                if (!isFlashcardButtonEnable) {
                    mListener.onFlashcardEmpty();
                    return;
                }
                Intent intent = new Intent(getActivity(), FlashcardActivity.class);
                Bundle animation = ActivityOptions.makeCustomAnimation(getActivity(), R.anim.animation, R.anim.animation2).toBundle();
                intent.putExtra(CardFragment.ARG_LEVEL, 0);
                intent.putExtra(CardFragment.ARG_TYPE, "SINGLE_WORD");
                intent.putExtra(CardFragment.ARG_IGNORE_DATE, false);
                getActivity().startActivity(intent, animation);
                break;
        }
    }

    private void showChallenge() {
        Bundle bundle = new Bundle();
        bundle.putBoolean(LIST_CHALLENGES, false);
        AppUtils.startActivityWithExtra(getContext(), ChallengeActivity.class, bundle);
    }

    private void showChallengesList(View v) {
        if (getView() == null) return;
        TextView textChallenge;

        Bundle bundle = new Bundle();
        bundle.putBoolean(LIST_CHALLENGES, true);

        switch (v.getId()) {
            case R.id.layout_correct:
                bundle.putString(LIST_CHALLENGE_TYPE, "CORRECT");
                textChallenge = getView().findViewById(R.id.challenge_correct);
                break;
            case R.id.layout_incorrect:
                bundle.putString(LIST_CHALLENGE_TYPE, "INCORRECT");
                textChallenge = getView().findViewById(R.id.challenge_incorrect);
                break;
            case R.id.layout_skipped:
                bundle.putString(LIST_CHALLENGE_TYPE, "SKIPPED");
                textChallenge = getView().findViewById(R.id.challenge_skipped);
                break;
            default:
                bundle.putString(LIST_CHALLENGE_TYPE, "NOT_SEEN");
                textChallenge = getView().findViewById(R.id.challenge_not_seen);
                break;
        }

        String countString = textChallenge.getText().toString();
        if (countString.isEmpty() || countString.trim().equals("")) countString = "0";
        if (Integer.valueOf(countString) > 0) {
            AppUtils.startActivityWithExtra(getContext(), ChallengeActivity.class, bundle);
        }
    }

    private void exitApps() {
        mListener.onQuitApps();
    }

    private void showFlashCardMultiple(View view) {
        if (getActivity() == null) return;

        int level = 0;

        switch (view.getId()) {
            case R.id.indikator_red:
                level = 1;
                break;
            case R.id.indikator_orange:
                level = 2;
                break;
            case R.id.indikator_yellow:
                level = 3;
                break;
            case R.id.indikator_green:
                level = 4;
                break;
            case R.id.indikator_blue:
                level = 5;
                break;
        }

        Intent intent = new Intent(getActivity(), FlashcardActivity.class);
        Bundle animation = ActivityOptions.makeCustomAnimation(getActivity(), R.anim.animation, R.anim.animation2).toBundle();
        intent.putExtra(CardFragment.ARG_LEVEL, level);
        intent.putExtra(CardFragment.ARG_TYPE, "MULTIPLE_WORDS");
        intent.putExtra(CardFragment.ARG_IGNORE_DATE, true);
        getActivity().startActivity(intent, animation);
    }

    private void showFlashCardMultipleSentences(View view) {
        if (getActivity() == null) return;

        int level = 0;

        switch (view.getId()) {
            case R.id.sentence_indikator_red:
                level = 1;
                break;
            case R.id.sentence_indikator_orange:
                level = 2;
                break;
            case R.id.sentence_indikator_yellow:
                level = 3;
                break;
            case R.id.sentence_indikator_green:
                level = 4;
                break;
            case R.id.sentence_indikator_blue:
                level = 5;
                break;
        }

        Intent intent = new Intent(getActivity(), FlashcardActivity.class);
        Bundle animation = ActivityOptions.makeCustomAnimation(getActivity(), R.anim.animation, R.anim.animation2).toBundle();
        intent.putExtra(CardFragment.ARG_LEVEL, level);
        intent.putExtra(CardFragment.ARG_TYPE, "MULTIPLE_SENTENCES");
        intent.putExtra(CardFragment.ARG_IGNORE_DATE, true);
        getActivity().startActivity(intent, animation);
    }

    private void initComponent(View rootView) {
        mIndicatorGreen = rootView.findViewById(R.id.indikator_green);
        mIndicatorYellow = rootView.findViewById(R.id.indikator_yellow);
        mIndicatorOrange = rootView.findViewById(R.id.indikator_orange);
        mIndicatorRed = rootView.findViewById(R.id.indikator_red);
        mIndicatorBlue = rootView.findViewById(R.id.indikator_blue);

        mSentenceIndicatorGreen = rootView.findViewById(R.id.sentence_indikator_green);
        mSentenceIndicatorYellow = rootView.findViewById(R.id.sentence_indikator_yellow);
        mSentenceIndicatorOrange = rootView.findViewById(R.id.sentence_indikator_orange);
        mSentenceIndicatorRed = rootView.findViewById(R.id.sentence_indikator_red);
        mSentenceIndicatorBlue = rootView.findViewById(R.id.sentence_indikator_blue);

        mTextEpisode = rootView.findViewById(R.id.text_episode);
        mTextWiki = rootView.findViewById(R.id.text_wiki);
        mTextNotification = rootView.findViewById(R.id.text_notification);
        mCardImageDashboard = rootView.findViewById(R.id.content_image_dashboard);

        mTextNotSeen = rootView.findViewById(R.id.challenge_not_seen);
        mTextSkipped = rootView.findViewById(R.id.challenge_skipped);
        mTextIncorrect = rootView.findViewById(R.id.challenge_incorrect);
        mTextCorrect = rootView.findViewById(R.id.challenge_correct);
        mTextNotSeenProgress = rootView.findViewById(R.id.challenge_not_seen_progress);
        mTextSkippedProgress = rootView.findViewById(R.id.challenge_skipped_progress);
        mTextIncorrectProgress = rootView.findViewById(R.id.challenge_incorrect_progress);
        mTextCorrectProgress = rootView.findViewById(R.id.challenge_correct_progress);
    }

    private void initListener(View rootView) {
        mIndicatorGreen.setOnClickListener(this::showFlashCardMultiple);
        mIndicatorYellow.setOnClickListener(this::showFlashCardMultiple);
        mIndicatorOrange.setOnClickListener(this::showFlashCardMultiple);
        mIndicatorRed.setOnClickListener(this::showFlashCardMultiple);
        mIndicatorBlue.setOnClickListener(this::showFlashCardMultiple);

        mSentenceIndicatorGreen.setOnClickListener(this::showFlashCardMultipleSentences);
        mSentenceIndicatorYellow.setOnClickListener(this::showFlashCardMultipleSentences);
        mSentenceIndicatorOrange.setOnClickListener(this::showFlashCardMultipleSentences);
        mSentenceIndicatorRed.setOnClickListener(this::showFlashCardMultipleSentences);
        mSentenceIndicatorBlue.setOnClickListener(this::showFlashCardMultipleSentences);

        rootView.findViewById(R.id.layout_correct).setOnClickListener(this);
        rootView.findViewById(R.id.layout_incorrect).setOnClickListener(this);
        rootView.findViewById(R.id.layout_skipped).setOnClickListener(this);
        rootView.findViewById(R.id.layout_not_seen).setOnClickListener(this);
        rootView.findViewById(R.id.button_exit).setOnClickListener(this);
        rootView.findViewById(R.id.button_help).setOnClickListener(this);
        rootView.findViewById(R.id.button_stories).setOnClickListener(this);
        rootView.findViewById(R.id.button_flashcard).setOnClickListener(this);
        rootView.findViewById(R.id.button_challenge).setOnClickListener(this);
        rootView.findViewById(R.id.text_wiki).setOnClickListener(this);
    }

    private void configureLayout() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        SharedPreferences.Editor preferencesEditor = preferences.edit();

        mUrl = preferences.getString(AppConstants.PREFERENCE_SETTING_VALUES, "");
        if (mUrl.equalsIgnoreCase("")) {
            mUrl = "https://translate.google.com/";
            preferencesEditor.putString(AppConstants.PREFERENCE_SETTING_VALUES, mUrl).apply();
        }

        mTextWiki.setText("Dictionary");

        mDefaultFontColor = ContextCompat.getColor(mContext, R.color.white);
        mFontColorColorAnimateValue = ContextCompat.getColor(mContext, R.color.blue_sky);

        setHasOptionsMenu(true);
    }

    private void showAnimation(boolean show) {
        // Animate font color for first time app launch
        if (show) {
            mCardImageDashboard.setVisibility(View.VISIBLE);
            mTimer.scheduleAtFixedRate(new TimerTask() {
                @Override public void run() {
                    mHandler.post(() -> {
                        ValueAnimator colorAnimation;
                        if (mColorSwitch) {
                            colorAnimation = ValueAnimator.ofObject(new ArgbEvaluator(), mDefaultFontColor, mFontColorColorAnimateValue);
                        } else {
                            colorAnimation = ValueAnimator.ofObject(new ArgbEvaluator(), mFontColorColorAnimateValue, mDefaultFontColor);
                        }
                        mColorSwitch = !mColorSwitch;
                        colorAnimation.setDuration(AppConstants.DEFAULT_ANIMATION_LENGTH);
                        colorAnimation.addUpdateListener(animator -> mTextEpisode.setTextColor(((int) animator.getAnimatedValue())));
                        colorAnimation.start();
                    });
                }
            }, 1000, 1000);
        } else {
            mCardImageDashboard.setVisibility(GONE);
            mTextEpisode.setTextColor(mDefaultFontColor);
            if (mTimer != null) {
                mTimer.cancel();
                mTimer.purge();
                mTimer = null;
            }
        }
    }

    private void summary(RealmResults<Flashcard> flashcards) {
        long greenCount = flashcards.where().equalTo("type", "word").equalTo("mastering_level", 4).equalTo("already_read", 1).count();
        long yellowCount = flashcards.where().equalTo("type", "word").equalTo("mastering_level", 3).equalTo("already_read", 1).count();
        long orangeCount = flashcards.where().equalTo("type", "word").equalTo("mastering_level", 2).equalTo("already_read", 1).count();
        long redCount = flashcards.where().equalTo("type", "word").equalTo("mastering_level", 1).equalTo("already_read", 1).count();
        long blueCount = flashcards.where().equalTo("type", "word").equalTo("mastering_level", 5).equalTo("already_read", 1).count();

        mIndicatorGreen.setText(String.valueOf(greenCount));
        mIndicatorYellow.setText(String.valueOf(yellowCount));
        mIndicatorOrange.setText(String.valueOf(orangeCount));
        mIndicatorRed.setText(String.valueOf(redCount));
        mIndicatorBlue.setText(String.valueOf(blueCount));

        long greenSentenceCount = flashcards.where().equalTo("type", "sentence").equalTo("category", Reading.class.getSimpleName()).equalTo("mastering_level", 4).equalTo("already_read", 1).count();
        long yellowSentenceCount = flashcards.where().equalTo("type", "sentence").equalTo("category", Reading.class.getSimpleName()).equalTo("mastering_level", 3).equalTo("already_read", 1).count();
        long orangeSentenceCount = flashcards.where().equalTo("type", "sentence").equalTo("category", Reading.class.getSimpleName()).equalTo("mastering_level", 2).equalTo("already_read", 1).count();
        long redSentenceCount = flashcards.where().equalTo("type", "sentence").equalTo("category", Reading.class.getSimpleName()).equalTo("mastering_level", 1).equalTo("already_read", 1).count();
        long blueSentenceCount = flashcards.where().equalTo("type", "sentence").equalTo("category", Reading.class.getSimpleName()).equalTo("mastering_level", 5).equalTo("already_read", 1).count();

        mSentenceIndicatorGreen.setText(String.valueOf(greenSentenceCount));
        mSentenceIndicatorYellow.setText(String.valueOf(yellowSentenceCount));
        mSentenceIndicatorOrange.setText(String.valueOf(orangeSentenceCount));
        mSentenceIndicatorRed.setText(String.valueOf(redSentenceCount));
        mSentenceIndicatorBlue.setText(String.valueOf(blueSentenceCount));

        if (flashcards.isEmpty() && getView() != null && getContext() != null) {
            isFlashcardButtonEnable = false;
//            getView().findViewById(R.id.button_flashcard).setBackground(ContextCompat.getDrawable(getContext(), R.drawable.shape_button_disable));
//            ((TextView) getView().findViewById(R.id.text_challenge)).setTextColor(ContextCompat.getColor(getContext(), android.R.color.secondary_text_light_nodisable));
//            Drawable mDrawable = ContextCompat.getDrawable(getContext(), R.drawable.icon_challenge);
//            if (mDrawable != null) {
//                mDrawable.setColorFilter(new PorterDuffColorFilter(0xFFD7DCDF, PorterDuff.Mode.MULTIPLY));
//                ((ImageView) getView().findViewById(R.id.image_challenge)).setImageDrawable(mDrawable);
//            }
            showAnimation(true);
            return;
        }

        showAnimation(false);
        SharedPreferences.Editor mPrefEdit = AppInjectors.provideSharedPreferencesEditor(mContext);
        mPrefEdit.putInt(AppConstants.PREFERENCE_FRESH_STORIES, 1).apply();
        mPrefEdit.putInt(AppConstants.PREFERENCE_FRESH_LESSONS, 1).apply();
        isFlashcardButtonEnable = true;
    }

    private void summaryReading(RealmResults<Reading> readings) {
        mHomeViewModel.rePopulateChallenge();
    }

    private void summaryChallenges(RealmResults<Challenge> challenges) {
        long notSeen = challenges.where().equalTo("seen", false).count();
        long skipped = challenges.where().equalTo("skip", true).count();
        long incorrect = challenges.where().equalTo("correct", false).equalTo("seen", true).equalTo("skip", false).count();
        long correct = challenges.where().equalTo("correct", true).count();

        mTextNotSeen.setText(String.valueOf(notSeen));
        mTextSkipped.setText(String.valueOf(skipped));
        mTextIncorrect.setText(String.valueOf(incorrect));
        mTextCorrect.setText(String.valueOf(correct));

        int learnedLesson = mHomeViewModel.learnedLessonCount();
        if (learnedLesson > 0) {
            DecimalFormat df = new DecimalFormat("###");
            double notSeenVal = Double.valueOf(String.valueOf(notSeen));
            double skippedVal = Double.valueOf(String.valueOf(skipped));
            double incorrectVal = Double.valueOf(String.valueOf(incorrect));
            double correctVal = Double.valueOf(String.valueOf(correct));
            double learnedLessonVal = Double.valueOf(String.valueOf(learnedLesson));

            mTextNotSeenProgress.setText(df.format(notSeenVal / learnedLessonVal * 100) + "%");
            mTextSkippedProgress.setText(df.format(skippedVal / learnedLessonVal * 100) + "%");
            mTextIncorrectProgress.setText(df.format(incorrectVal / learnedLessonVal * 100) + "%");
            mTextCorrectProgress.setText(df.format(correctVal / learnedLessonVal * 100) + "%");
        } else {
            mTextNotSeenProgress.setText(String.valueOf(notSeen) + "%");
            mTextSkippedProgress.setText(String.valueOf(skipped) + "%");
            mTextIncorrectProgress.setText(String.valueOf(incorrect) + "%");
            mTextCorrectProgress.setText(String.valueOf(correct) + "%");
        }

        if (challenges.isEmpty() && getContext() != null) {
            isChallengeButtonEnable = false;
//            getView().findViewById(R.id.button_challenge).setBackground(ContextCompat.getDrawable(getContext(), R.drawable.shape_button_disable));
//            ((TextView) getView().findViewById(R.id.text_flashcard)).setTextColor(ContextCompat.getColor(getContext(), android.R.color.secondary_text_light_nodisable));
//            Drawable mDrawable = ContextCompat.getDrawable(getContext(), R.drawable.icon_flashcard);
//            if (mDrawable != null) {
//                mDrawable.setColorFilter(new PorterDuffColorFilter(0xFFD7DCDF, PorterDuff.Mode.MULTIPLY));
//                ((ImageView) getView().findViewById(R.id.image_flashcard)).setImageDrawable(mDrawable);
//            }
            return;
        }

        isChallengeButtonEnable = true;
    }

    private void showNotifications(RealmResults<Notification> notifications) {
        StringBuilder sb = new StringBuilder();
        if (!notifications.isEmpty()) {
            for (Notification notification : notifications) {
                sb.append(notification.getIsi_pesan()).append("\n");
            }
            mTextNotification.setText(sb.toString());
        }
    }

    public interface OnFragmentInteractionListener {
        void onShowHelp();

        void onQuitApps();
    }

}