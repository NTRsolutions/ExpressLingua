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
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import com.neosolusi.expresslingua.AppConstants;
import com.neosolusi.expresslingua.AppInjectors;
import com.neosolusi.expresslingua.AppUtils;
import com.neosolusi.expresslingua.R;
import com.neosolusi.expresslingua.data.entity.Challenge;
import com.neosolusi.expresslingua.data.entity.Reading;
import com.neosolusi.expresslingua.data.entity.ReadingInfo;

import static android.view.View.GONE;

public class WrittenFragment extends Fragment {

    // Data property
    public Challenge mPredefinedChallenge, mChallenge;

    // Component property
    private WrittenFragment mInstance;
    private ChallengeViewModel mViewModel;
    private ImageButton mImageSpeak;
    private OnInteractionListener mListener;
    private OnShowTutorialListener mTutorial;
    private Context mContext;
    private Activity mActivity;
    private EditText mEditAnswer;
    private ProgressBar mProgressTimer;
    private int mWrongAnswerCount;
    private ObjectAnimator mAnimator;
    private BroadcastReceiver mAudioPauseReceiver;
    private boolean isCountDownProgressRunning = false;

    public WrittenFragment() {
        // Required empty public constructor
    }

    public static WrittenFragment newInstance(Challenge challenge) {
        WrittenFragment fragment = new WrittenFragment();
        fragment.mPredefinedChallenge = challenge;
        return fragment;
    }

    @Override public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_written, container, false);
        configureLayout(root);
        return root;
    }

    @Override public void onAttach(Context context) {
        super.onAttach(context);
        mActivity = getActivity();
        mContext = context;
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
        mContext = null;
        mTutorial = null;
    }

    @Override public void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(mContext).registerReceiver(mAudioPauseReceiver, new IntentFilter(AppConstants.BROADCAST_PLAYER_PAUSE));
        InputMethodManager imm = (InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) imm.showSoftInput(mEditAnswer, InputMethodManager.SHOW_IMPLICIT);
        getQuestion();
    }

    @Override public void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(mContext).unregisterReceiver(mAudioPauseReceiver);
    }

    @Override public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_help:
                mTutorial.showTutorial("Gambar 9", getString(R.string.tutorial_step_nine), R.drawable.tutorial_9);
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
                blockUI(false);
                if (!isCountDownProgressRunning) startCountDown();
            }
        };

        mImageSpeak = view.findViewById(R.id.button_challenge_speak);
        mImageSpeak.setOnClickListener(v -> speak(false));
        mImageSpeak.setOnLongClickListener(v -> {
            speak(true);
            return true;
        });

        view.findViewById(R.id.button_challenge_check).setEnabled(false);
        view.findViewById(R.id.button_challenge_check).setOnClickListener(v -> check());
        view.findViewById(R.id.button_challenge_pass).setOnClickListener(v -> {
            if (mAnimator != null) mAnimator.removeAllListeners();
            mListener.resetPlayer();
            mEditAnswer.setText("");
            mWrongAnswerCount = 0;
            mViewModel.skip(mChallenge);
            mListener.nextChallenge();
        });

        mEditAnswer = view.findViewById(R.id.edit_challenge_answer);
        mEditAnswer.addTextChangedListener(new TextWatcher() {
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
        mEditAnswer.requestFocus();

        mProgressTimer = view.findViewById(R.id.progress_timer);

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
        imageHelp.setOnClickListener(v -> mTutorial.showTutorial("Gambar 9", getString(R.string.tutorial_step_nine), R.drawable.tutorial_9));
    }

    private void speak(boolean isSlow) {
        Reading reading = mViewModel.findReadingForChallenge(mChallenge);
        mListener.speak(reading, isSlow);
    }

    private void getQuestion() {
        blockUI(true);

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

        ReadingInfo info = mViewModel.getCurrentLesson(reading.getFile_id());
        if (info != null && mListener != null) {
            mListener.resetPlayer();
            mListener.setPlayerInfo(info);
        }

        new Handler().postDelayed(() -> {
            if (mListener == null) return;
            mListener.speak(reading, false);
            blockUI(false);
        }, 2000);
    }

    private void check() {
        if (getView() == null) return;

        blockUI(true);

        Reading reading = mViewModel.findReadingForChallenge(mChallenge);
        if (reading == null) {
            showReadingFailed();
            return;
        }

        String answer = AppUtils.normalizeStringWithoutThickMark(mEditAnswer.getText().toString());
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
            mImageSpeak.setImageResource(R.drawable.ic_correct_48dp);
            mEditAnswer.setText("");
            MediaPlayer mp = MediaPlayer.create(mActivity, R.raw.clap);
            mp.start();
            mListener.correctAnswers(mChallenge);
        } else {
            mImageSpeak.setImageResource(R.drawable.ic_wrong_48dp);
            mWrongAnswerCount++;
            MediaPlayer mp = MediaPlayer.create(mActivity, R.raw.buzzer);
            mp.start();
            mListener.wrongAnswers(mChallenge);
        }

        new Handler().postDelayed(() -> {
            mImageSpeak.setImageResource(R.drawable.ic_volume_up_48dp);
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
            blockUI(false);
        }, 2000);
    }

    private void blockUI(boolean block) {
        if (block) {
            mActivity.getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        } else {
            mActivity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        }
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
        double duration = (charCount * 0.3) + (30 - reading.getFile_id());

        mAnimator = ObjectAnimator.ofInt(mProgressTimer, "progress", 100, 0);
        mAnimator.setDuration((int) duration * 1000);
        mAnimator.addListener(new Animator.AnimatorListener() {
            @Override public void onAnimationStart(Animator animator) {

            }

            @Override public void onAnimationEnd(Animator animator) {
                mEditAnswer.setText("");
                mWrongAnswerCount = 0;
                mViewModel.skip(mChallenge);
                if (mListener != null) {
                    mListener.resetPlayer();
                    mListener.nextChallenge();
                }
            }

            @Override public void onAnimationCancel(Animator animator) {

            }

            @Override public void onAnimationRepeat(Animator animator) {

            }
        });
        mAnimator.start();
    }

}
