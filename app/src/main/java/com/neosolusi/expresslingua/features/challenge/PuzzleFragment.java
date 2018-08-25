package com.neosolusi.expresslingua.features.challenge;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.arch.lifecycle.ViewModelProviders;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.flexbox.FlexDirection;
import com.google.android.flexbox.FlexWrap;
import com.google.android.flexbox.FlexboxLayoutManager;
import com.google.android.flexbox.JustifyContent;
import com.neosolusi.expresslingua.AppConstants;
import com.neosolusi.expresslingua.AppInjectors;
import com.neosolusi.expresslingua.AppUtils;
import com.neosolusi.expresslingua.R;
import com.neosolusi.expresslingua.data.entity.Challenge;
import com.neosolusi.expresslingua.data.entity.Reading;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static android.view.View.GONE;

public class PuzzleFragment extends Fragment implements PuzzleAdapter.OnItemClickListener {

    public Challenge mPredefinedChallenge, mChallenge;
    private ChallengeViewModel mViewModel;
    private PuzzleAdapter mAdapter;
    private OnInteractionListener mListener;
    private OnShowTutorialListener mTutorial;
    private Activity mActivity;
    private List<PuzzleItem> mListSelectedItem;
    private ProgressBar mProgressTimer;
    private int mWrongAnswerCount;
    private ObjectAnimator mAnimator;
    private BroadcastReceiver mAudioPauseReceiver;
    private boolean isCountDownProgressRunning = false;

    // Components property
    private TextView mTextQuestion, mTextAnswer;

    public PuzzleFragment() {
        // Required empty public constructor
    }

    public static PuzzleFragment newInstance(Challenge challenge) {
        PuzzleFragment fragment = new PuzzleFragment();
        fragment.mPredefinedChallenge = challenge;
        return fragment;
    }

    @Override public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_puzzle, container, false);
        configureLayout(root);

        mAdapter = new PuzzleAdapter(getContext(), this);
        mListSelectedItem = new ArrayList<>();

        FlexboxLayoutManager layoutManager = new FlexboxLayoutManager(getContext());
        layoutManager.setFlexDirection(FlexDirection.ROW);
        layoutManager.setFlexWrap(FlexWrap.WRAP);
        layoutManager.setJustifyContent(JustifyContent.SPACE_AROUND);

        RecyclerView mListAnswers = root.findViewById(R.id.recycler_answers);
        mListAnswers.setItemViewCacheSize(10);
        mListAnswers.setItemAnimator(new DefaultItemAnimator());
        mListAnswers.setLayoutManager(layoutManager);
        mListAnswers.setAdapter(mAdapter);

        AppUtils.dismissKeyboard(getContext(), root.getWindowToken());

        return root;
    }

    @Override public void onAttach(Context context) {
        super.onAttach(context);
        mActivity = getActivity();
        if (context instanceof OnInteractionListener) {
            mListener = (OnInteractionListener) context;
        } else {
            throw new IllegalArgumentException("Did you forgot to implement some interface?");
        }

        if (context instanceof OnShowTutorialListener) {
            mTutorial = (OnShowTutorialListener) context;
        } else {
            throw new IllegalArgumentException("Did you forgot to implement some interface?");
        }
    }

    @Override public void onDetach() {
        super.onDetach();
        mListener = null;
        mTutorial = null;
    }

    @Override public void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(mActivity).registerReceiver(mAudioPauseReceiver, new IntentFilter(AppConstants.BROADCAST_PLAYER_PAUSE));
        getQuestion();
    }

    @Override public void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(mActivity).unregisterReceiver(mAudioPauseReceiver);
    }

    @Override public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_help:
                mTutorial.showTutorial("Gambar 12", "", R.drawable.tutorial_12);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void configureLayout(View view) {
        if (getContext() == null) return;

        ChallengeViewModelFactory factory = AppInjectors.provideChallengeViewModelFactory(getContext());
        mViewModel = ViewModelProviders.of(this, factory).get(ChallengeViewModel.class);

        mAudioPauseReceiver = new BroadcastReceiver() {
            @Override public void onReceive(Context context, Intent intent) {
                if (!isCountDownProgressRunning) startCountDown();
            }
        };

        view.findViewById(R.id.button_challenge_check).setEnabled(false);
        view.findViewById(R.id.button_challenge_check).setOnClickListener(v -> check());

        mTextQuestion = view.findViewById(R.id.text_challenge_question);
        mTextAnswer = view.findViewById(R.id.text_challenge_answer);
        mProgressTimer = view.findViewById(R.id.progress_timer);

        view.findViewById(R.id.image_challenge_clear).setOnClickListener(v -> {
            mTextAnswer.setText("");
            mAdapter.clear();
            mListSelectedItem.clear();
        });
        view.findViewById(R.id.image_challenge_backspace).setOnClickListener(v -> {
            if (mListSelectedItem.isEmpty()) return;

            Collections.reverse(mListSelectedItem);

            PuzzleItem lastItem = mListSelectedItem.get(0);
            mAdapter.enableItem(lastItem);
            mListSelectedItem.remove(lastItem);
            mTextAnswer.setText("");

            Collections.reverse(mListSelectedItem);
            for (PuzzleItem item : mListSelectedItem) {
                mTextAnswer.setText(mTextAnswer.getText() + " " + item.word);
            }
        });
        view.findViewById(R.id.button_challenge_pass).setOnClickListener(v -> {
            mAnimator.removeAllListeners();
            mTextAnswer.setText("");
            mTextQuestion.setText("");
            mWrongAnswerCount = 0;
            mViewModel.skip(mChallenge);
//            getQuestion();
            mListener.nextChallenge();
        });

        mTextAnswer.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence.length() > 0) {
                    view.findViewById(R.id.button_challenge_check).setEnabled(true);
                } else {
                    view.findViewById(R.id.button_challenge_check).setEnabled(false);
                }
            }

            @Override public void afterTextChanged(Editable editable) {

            }
        });

        Toolbar toolbar = mActivity.findViewById(R.id.toolbar);
        ImageView imageUp = toolbar.findViewById(R.id.image_arrow_up);
        ImageView imageDown = toolbar.findViewById(R.id.image_arrow_down);
        ImageView imageHelp = toolbar.findViewById(R.id.image_help);
        ImageView imageNotSet = toolbar.findViewById(R.id.image_not_set);
        imageNotSet.setVisibility(GONE);
        imageDown.setVisibility(GONE);
        imageUp.setVisibility(GONE);
        imageHelp.setVisibility(GONE);

        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(0, 0, 0, 0);
        imageHelp.setLayoutParams(params);
        imageHelp.setOnClickListener(v -> mTutorial.showTutorial("Gambar 12", "", R.drawable.tutorial_12));
    }

    private void getQuestion() {
        if (mPredefinedChallenge != null) {
            mChallenge = mPredefinedChallenge;
        } else {
            mChallenge = mViewModel.getQuestions();
        }

        if (mChallenge == null) {
            if (mViewModel.isHasFinishChallenges()) {
                mListener.showFinishChallenges();
                return;
            }
            mListener.showNoChallenges();
            return;
        }

        Reading reading = mViewModel.findReadingForChallenge(mChallenge);
        if (reading == null) {
            mListener.nextChallenge();
            return;
        }

        List<String> words = new ArrayList<>();
        words.addAll(Arrays.asList(reading.getSentence().trim().split(" ")));
        words.addAll(mViewModel.getRandomWords(words));
        Collections.shuffle(words);
        Log.d("Puzzle Words", words.toString());

        List<PuzzleItem> items = new ArrayList<>();
        for (int i = 0; i < words.size(); i++) {
            items.add(new PuzzleItem(i, words.get(i), false));
        }

        mListSelectedItem.clear();
        mTextQuestion.setText(reading.getTranslation());
        mAdapter.swap(items);

        startCountDown();
    }

    private void check() {
        if (getView() == null) return;

        Reading reading = mViewModel.findReadingForChallenge(mChallenge);
        if (reading == null) {
            showReadingFailed();
            return;
        }

        String answer = AppUtils.normalizeStringWithoutThickMark(mTextAnswer.getText().toString());
        String correct = AppUtils.normalizeStringWithoutThickMark(reading.getSentence());

        answer = AppUtils.normalizeStringForChallenge(answer);
        answer = " " + answer + " ";
        answer = AppUtils.replaceDigitWithWord(answer);

        correct = AppUtils.normalizeStringForChallenge(correct);
        correct = " " + correct + " ";
        correct = AppUtils.replaceDigitWithWord(correct);

        String filteredAnswer = answer.trim();
        String filteredCorrect = correct.trim();

        if (filteredAnswer.equalsIgnoreCase(filteredCorrect)) {
            mTextAnswer.setText("");
            mTextQuestion.setText("");
            MediaPlayer mp = MediaPlayer.create(mActivity, R.raw.clap);
            mp.start();
            mListener.correctAnswers(mChallenge);
        } else {
            mWrongAnswerCount++;
            MediaPlayer mp = MediaPlayer.create(mActivity, R.raw.buzzer);
            mp.start();
            mListener.wrongAnswers(mChallenge);
        }

        new Handler().postDelayed(() -> {
            if (filteredAnswer.equalsIgnoreCase(filteredCorrect)) {
                mAnimator.removeAllListeners();
                mListener.nextChallenge();
            } else {
                if (mWrongAnswerCount >= 3) {
                    new AlertDialog.Builder(mActivity).setTitle("Tampilkan jawaban?").setIcon(R.mipmap.ic_launcher_ealing)
                            .setNegativeButton("Tidak", (dialog, which) -> dialog.dismiss())
                            .setPositiveButton("Ya", (dialog, which) -> {
                                showAnswer(reading.getSentence());
                                mWrongAnswerCount = 0;
                            })
                            .show();
                }
            }
        }, 2000);
    }

    private void showAnswer(String answer) {
        new AlertDialog.Builder(mActivity).setTitle("Jawaban").setIcon(R.mipmap.ic_launcher_ealing)
                .setMessage(answer)
                .setNegativeButton("Tutup", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void showReadingFailed() {
        new AlertDialog.Builder(mActivity).setTitle("ExpressLingua").setIcon(R.mipmap.ic_launcher_ealing)
                .setMessage("Tidak bisa memeriksa jawaban, Lesson tidak tersedia")
                .setNegativeButton("Tutup", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void startCountDown() {
        isCountDownProgressRunning = true;

        Reading reading = mViewModel.findReadingForChallenge(mChallenge);
        if (reading == null) return;

        int charCount = reading.getSentence().length();
        double duration = (charCount * 0.5) + (30 - reading.getFile_id());

        mAnimator = ObjectAnimator.ofInt(mProgressTimer, "progress", 100, 0);
        mAnimator.setDuration((int) duration * 1000);
        mAnimator.addListener(new Animator.AnimatorListener() {
            @Override public void onAnimationStart(Animator animator) {

            }

            @Override public void onAnimationEnd(Animator animator) {
                mTextAnswer.setText("");
                mTextQuestion.setText("");
                mWrongAnswerCount = 0;
                if (mViewModel != null) mViewModel.skip(mChallenge);
                if (mListener != null) mListener.nextChallenge();
            }

            @Override public void onAnimationCancel(Animator animator) {

            }

            @Override public void onAnimationRepeat(Animator animator) {

            }
        });
        mAnimator.start();
    }

    @Override public void onClick(PuzzleItem item) {
        mListSelectedItem.add(item);
        mTextAnswer.setText(mTextAnswer.getText() + " " + item.word);
    }
}
