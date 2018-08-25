package com.neosolusi.expresslingua.features.challenge;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.arch.lifecycle.ViewModelProviders;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.neosolusi.expresslingua.AppConstants;
import com.neosolusi.expresslingua.AppInjectors;
import com.neosolusi.expresslingua.AppUtils;
import com.neosolusi.expresslingua.R;
import com.neosolusi.expresslingua.data.entity.Challenge;
import com.neosolusi.expresslingua.data.entity.Reading;
import com.neosolusi.expresslingua.data.entity.ReadingInfo;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

import static android.view.View.GONE;

public class SpeakFragment extends Fragment {

    private static final String TAG = SpeakFragment.class.getSimpleName();
    private static final int SPEECH_CODE = 155;

    // Data property
    public Challenge mPredefinedChallenge, mChallenge;
    public Reading mReadingForChallenge;

    // References property
    private SpeechRecognizer mSpeechRecognizer;
    private SpeechRecognitionListener mSpeechListener;
    private ChallengeViewModel mViewModel;
    private OnInteractionListener mListener;
    private OnShowTutorialListener mTutorial;
    private Context mContext;
    private Activity mActivity;
    private Intent mSpeechRecognizerIntent;
    private MediaPlayer mSoundPlayerCorrect, mSoundPlayerWrong, mSoundPlayerRecorded;
    private MediaRecorder mAudioRecorder;
    private File mRecordedAudioFile;
    private BroadcastReceiver mAudioPauseReceiver;
    private boolean isCountDownProgressRunning = false;
    private boolean mIslistening;
    private int mWrongAnswerCount;
    private String mAnswer;
    private ObjectAnimator mAnimator;

    // UI property
    private TextView mTextAnswer, mTextLabel1, mTextLabel2;
    private ImageButton mImageSpeak;
    private ImageView mImageMic;
    private Button mButtonCheck, mButtonPass;
    private ProgressBar mProgressTimer;

    public SpeakFragment() {
        // Required empty public constructor
    }

    public static SpeakFragment newInstance(Challenge challenge) {
        SpeakFragment fragment = new SpeakFragment();
        fragment.mPredefinedChallenge = challenge;
        return fragment;
    }

    @Override public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_speak, container, false);

        configureLayout(root);
        endListen();
        blockUI(true);

//        mSpeechRecognizer = SpeechRecognizer.createSpeechRecognizer(getContext());
//        mSpeechRecognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
//        mSpeechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
//        mSpeechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, mContext.getPackageName());
//        mSpeechRecognizerIntent.putExtra("android.speech.extra.GET_AUDIO_FORMAT", "audio/AMR");
//        mSpeechRecognizerIntent.putExtra("android.speech.extra.GET_AUDIO", true);

        mSpeechListener = new SpeechRecognitionListener();
        mSoundPlayerCorrect = MediaPlayer.create(mActivity, R.raw.clap);
        mSoundPlayerWrong = MediaPlayer.create(mActivity, R.raw.buzzer);
//        mSoundPlayerRecorded = new MediaPlayer();

        mAudioPauseReceiver = new BroadcastReceiver() {
            @Override public void onReceive(Context context, Intent intent) {
                blockUI(false);
                if (!isCountDownProgressRunning) startCountDown();
            }
        };

        getQuestion();

        return root;
    }

    @Override public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (data == null) return;
        if (data.getExtras() == null) return;
        if (data.getData() == null) return;

        Bundle bundle = data.getExtras();

        ArrayList<String> matches = bundle.getStringArrayList(RecognizerIntent.EXTRA_RESULTS);
        endListen();
        blockUI(false);
        mIslistening = false;
        mButtonCheck.setEnabled(true);

//        mSpeechRecognizer.cancel();
//        mSpeechRecognizer.stopListening();
//        mSpeechRecognizer.destroy();

        if (matches == null) return;

        mAnswer = matches.get(0);
        mTextAnswer.setText(mAnswer);
        check(data.getData());
//        check();
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
        AppUtils.dismissKeyboard(mContext, mTextAnswer.getWindowToken());
    }

    @Override public void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(mContext).unregisterReceiver(mAudioPauseReceiver);
        if (mSpeechRecognizer != null) mSpeechRecognizer.destroy();
    }

    @Override public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_help:
                mTutorial.showTutorial("Gambar 10", "", R.drawable.tutorial_10);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void configureLayout(View view) {
        if (getContext() == null) return;

        ChallengeViewModelFactory factory = AppInjectors.provideChallengeViewModelFactory(getContext());
        mViewModel = ViewModelProviders.of(this, factory).get(ChallengeViewModel.class);

        mTextAnswer = view.findViewById(R.id.text_challenge_answer);
        mTextLabel1 = view.findViewById(R.id.text_challenge_1);
        mTextLabel2 = view.findViewById(R.id.text_challenge_2);

        mImageMic = view.findViewById(R.id.image_challenge_mic);
        mImageMic.setOnClickListener(v -> {
//            mSpeechRecognizer.setRecognitionListener(mSpeechListener);
//            mSpeechRecognizer.startListening(mSpeechRecognizerIntent);
            startSpeechRecognition(mReadingForChallenge.getSentence());
            beginListen();
        });

        mImageSpeak = view.findViewById(R.id.button_challenge_speak);
        mImageSpeak.setOnClickListener(v -> speak(false));
        mImageSpeak.setOnLongClickListener(v -> {
            speak(true);
            return true;
        });

        mButtonCheck = view.findViewById(R.id.button_challenge_check);
        mButtonCheck.setEnabled(false);
        mButtonCheck.setOnClickListener(v -> check(null));

        mButtonPass = view.findViewById(R.id.button_challenge_pass);
        mButtonPass.setOnClickListener(v -> {
            if (mAnimator != null) mAnimator.removeAllListeners();
            if (mIslistening) {
                mIslistening = false;
                mSpeechRecognizer.stopListening();
                mSpeechRecognizer.destroy();
            }
            mListener.resetPlayer();
            mTextAnswer.setText("");
            mWrongAnswerCount = 0;
            mViewModel.skip(mChallenge);
            mListener.nextChallenge();
        });

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
        imageHelp.setOnClickListener(v -> mTutorial.showTutorial("Gambar 10", "", R.drawable.tutorial_10));
    }

    private void speak(boolean isSlow) {
        if (mIslistening) {
            mIslistening = false;
            mSpeechRecognizer.stopListening();
            mSpeechRecognizer.destroy();
        }
        Reading reading = mViewModel.findReadingForChallenge(mChallenge);
        mListener.speak(reading, isSlow);
        blockUI(true);
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

        ReadingInfo info = mViewModel.getCurrentLesson(reading.getFile_id());
        if (info != null && mListener != null) {
            mListener.resetPlayer();
            mListener.setPlayerInfo(info);
        }

        mReadingForChallenge = mViewModel.findReadingForChallenge(mChallenge);
        if (mReadingForChallenge != null) {
            mTextAnswer.setText(mReadingForChallenge.getSentence());
        }

        // Set audio file output
//        File audioPath = new File(mContext.getFilesDir(), "record");
//        mRecordedAudioFile = new File(audioPath, mReadingForChallenge.getSequence_no() + ".3gp");
//        mAudioRecorder = new MediaRecorder();
//        mAudioRecorder.setOutputFile(mRecordedAudioFile.getAbsolutePath());
//        mAudioRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
//        mAudioRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
//        mAudioRecorder.setAudioEncoder(MediaRecorder.OutputFormat.AMR_NB);

        new Handler().postDelayed(() -> {
            if (mListener == null) return;
            mAnswer = "";
            mListener.speak(reading, false);
            mTextAnswer.setText(reading.getSentence());
        }, 2000);
    }

    private void beginListen() {
        mImageMic.setBackground(ContextCompat.getDrawable(mContext, R.drawable.shape_button));
        mTextLabel1.setVisibility(View.GONE);
        mTextLabel2.setVisibility(View.VISIBLE);
    }

    private void endListen() {
        mImageMic.setBackground(null);
        mTextLabel1.setVisibility(View.VISIBLE);
        mTextLabel2.setVisibility(View.GONE);
    }

    private void blockUI(boolean block) {
        if (block) {
            mActivity.getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        } else {
            mActivity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        }
    }

    private void check(Uri uri) {
        if (getView() == null) return;

        blockUI(true);

        Reading reading = mViewModel.findReadingForChallenge(mChallenge);
        if (reading == null) {
            showReadingFailed();
            return;
        }

        String answer = AppUtils.normalizeStringWithoutThickMark(mAnswer);
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
            mTextAnswer.setText("");
            mSoundPlayerCorrect.start();
            mListener.correctAnswers(mChallenge);
            recordSpeechToFile(uri);
        } else {
//            if (mRecordedAudioFile.exists()) mRecordedAudioFile.delete();
            mImageSpeak.setImageResource(R.drawable.ic_wrong_48dp);
            mWrongAnswerCount++;
            mSoundPlayerWrong.start();
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

    private void startSpeechRecognition(String question) {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, mContext.getPackageName());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, question);
        intent.putExtra("android.speech.extra.GET_AUDIO_FORMAT", "audio/AMR");
        intent.putExtra("android.speech.extra.GET_AUDIO", true);
        startActivityForResult(intent, SPEECH_CODE);
        mIslistening = true;
    }

    private void recordSpeechToFile(Uri uri) {
        try {
            if (mReadingForChallenge == null || uri == null) return;

            File audioPath = new File(mContext.getFilesDir(), "record");
            File audioFile = new File(audioPath, mReadingForChallenge.getSequence_no() + ".amr");

            byte[] fileReader = new byte[1024];
            int read;

            if (!audioPath.isDirectory() && !audioPath.mkdir()) return;
            if (!audioFile.exists() && !audioFile.createNewFile()) return;

            ContentResolver contentResolver = mContext.getContentResolver();
            InputStream input = contentResolver.openInputStream(uri);
            OutputStream output = new FileOutputStream(audioFile);

            if (input == null) return;

            while ((read = input.read(fileReader)) != -1) {
                output.write(fileReader, 0, read);
            }

            // Testing sound only
//            mSoundPlayerRecorded.setDataSource(audioFile.getAbsolutePath());
//            mSoundPlayerRecorded.prepare();
//            mSoundPlayerRecorded.start();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void startCountDown() {
        isCountDownProgressRunning = true;

        Reading reading = mViewModel.findReadingForChallenge(mChallenge);
        if (reading == null) return;

        int charCount = reading.getSentence().length();
        double duration = (charCount * 0.2) + (30 - reading.getFile_id());

        mAnimator = ObjectAnimator.ofInt(mProgressTimer, "progress", 100, 0);
        mAnimator.setDuration((int) duration * 1000);
        mAnimator.addListener(new Animator.AnimatorListener() {
            @Override public void onAnimationStart(Animator animator) {

            }

            @Override public void onAnimationEnd(Animator animator) {
                if (mIslistening) {
                    mIslistening = false;
                    if (mSpeechRecognizer != null) {
                        mSpeechRecognizer.stopListening();
                        mSpeechRecognizer.destroy();
                    }
                }

                mTextAnswer.setText("");
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

    protected class SpeechRecognitionListener implements RecognitionListener {

        int message;

        @Override public void onReadyForSpeech(Bundle bundle) {
            Log.d(TAG, "Ready for speech");
            mIslistening = true;
        }

        @Override public void onBeginningOfSpeech() {
//            try {
//                mAudioRecorder.prepare();
//                mAudioRecorder.start();
//            } catch (IllegalStateException ise) {
//                // make something ...
//            } catch (IOException ioe) {
//                // make something
//            }
        }

        @Override public void onRmsChanged(float v) {

        }

        @Override public void onBufferReceived(byte[] bytes) {

        }

        @Override public void onEndOfSpeech() {
            endListen();
        }

        @Override public void onError(int i) {
            switch (i) {
                case SpeechRecognizer.ERROR_AUDIO:
                    message = R.string.error_audio_error;
                    break;
                case SpeechRecognizer.ERROR_CLIENT:
                    message = R.string.error_client;
                    break;
                case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS:
                    message = R.string.error_permission;
                    break;
                case SpeechRecognizer.ERROR_NETWORK:
                    message = R.string.error_network;
                    break;
                case SpeechRecognizer.ERROR_NETWORK_TIMEOUT:
                    message = R.string.error_timeout;
                    break;
                case SpeechRecognizer.ERROR_NO_MATCH:
                    message = R.string.error_no_match;
                    break;
                case SpeechRecognizer.ERROR_RECOGNIZER_BUSY:
                    message = R.string.error_busy;
                    break;
                case SpeechRecognizer.ERROR_SERVER:
                    message = R.string.error_server;
                    break;
                case SpeechRecognizer.ERROR_SPEECH_TIMEOUT:
                    message = R.string.error_timeout;
                    break;
                default:
                    message = R.string.error_understand;
                    break;
            }
            Log.d(TAG, getString(message));
        }

        @Override public void onResults(Bundle bundle) {
            ArrayList<String> matches = bundle.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
            endListen();
            blockUI(false);
            mIslistening = false;
            mButtonCheck.setEnabled(true);
            mSpeechRecognizer.cancel();
            mSpeechRecognizer.stopListening();
            mSpeechRecognizer.destroy();
//            mAudioRecorder.stop();
//            mAudioRecorder.release();
//            mAudioRecorder = null;

            if (matches == null) return;

            mAnswer = matches.get(0);
            mTextAnswer.setText(mAnswer);
            check(null);
        }

        @Override public void onPartialResults(Bundle bundle) {
            Log.d(TAG, "Google Speech PartialResults");
        }

        @Override public void onEvent(int i, Bundle bundle) {
            Log.d(TAG, "Google Speech onEvent");
        }
    }
}
