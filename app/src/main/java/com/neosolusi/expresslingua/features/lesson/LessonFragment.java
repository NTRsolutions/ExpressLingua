package com.neosolusi.expresslingua.features.lesson;

import android.app.Activity;
import android.app.Dialog;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.speech.tts.TextToSpeech;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.CardView;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.ActionMode;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;

import com.neosolusi.expresslingua.AppConstants;
import com.neosolusi.expresslingua.AppInjectors;
import com.neosolusi.expresslingua.AppUtils;
import com.neosolusi.expresslingua.R;
import com.neosolusi.expresslingua.algorithm.SM2;
import com.neosolusi.expresslingua.algorithm.Sentence;
import com.neosolusi.expresslingua.algorithm.Word;
import com.neosolusi.expresslingua.data.entity.Challenge;
import com.neosolusi.expresslingua.data.entity.Dictionary;
import com.neosolusi.expresslingua.data.entity.Flashcard;
import com.neosolusi.expresslingua.data.entity.Reading;
import com.neosolusi.expresslingua.data.entity.ReadingInfo;
import com.neosolusi.expresslingua.features.tutorial.TutorialLessonActivity;
import com.neosolusi.expresslingua.util.RecyclerItemClickListener;
import com.neosolusi.expresslingua.util.RecyclerItemDoubleClickListener;
import com.neosolusi.expresslingua.util.RecyclerItemLongClickListener;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import io.realm.Case;
import io.realm.RealmResults;

import static com.neosolusi.expresslingua.features.flashcard.FlashcardActivity.SELECT_CARD;
import static com.neosolusi.expresslingua.features.flashcard.FlashcardActivity.SELECT_NONE;
import static com.neosolusi.expresslingua.features.flashcard.FlashcardActivity.SELECT_WORDS;

public class LessonFragment extends Fragment implements LessonAdapter.OnItemClickListener {

    private static final String ARG_ID = "lesson_id";
    private static final String ARG_AUDIO = "lesson_audio";
    private static final String ARG_PROCESS_TEXT = "lesson_process_text";

    private int mLessonId;
    private String mProcessText;
    private String mPlayAs;
    private Context mContext;
    private Activity mActivity;
    private Sentence mSentenceAlgorithm;
    private Word mWordAlgorithm;
    private SharedPreferences mPref;
    private SharedPreferences.Editor mPrefEdit;

    // Component property
    private ActionMode.Callback mActionModeCallback;
    private LinearLayoutManager mLayoutManager;
    private RecyclerView mListLesson;
    private CardView mLayoutMastering;
    private TextView mTextHeader;
    private TextView mTextTranslation;
    private TextView mCurrentSelectedTextViewSentence;
    private Button mButtonRed, mButtonOrange, mButtonYellow, mButtonGreen, mButtonBreak;
    private LessonAdapter mAdapter;
    private LessonViewModel mViewModel;
    private TextToSpeech mSpeech;
    private Dialog mDialogSelection;

    // Data property
    private RealmResults<Dictionary> mDictionaries;
    private RealmResults<Reading> mDataLessons, mDataLessonsHolder;
    private Map<Integer, Reading> mSelectedItemReading;
    private boolean mHasInitializeData = false;
    private Reading mSelectedReadingForBookmark;
    private ReadingInfo mReadingInfo;
    private int mSelectedRowForMasteringWord;
    private boolean mSelectAllToggle = false;
    private boolean mNotReadToggle = false;

    private OnFragmentInteractionListener mListener;
    private GestureDetector mTapListener;
    private MediaPlayer mRecordedPlayer;

    public LessonFragment() {
        // Required empty public constructor
    }

    public static LessonFragment newInstance(int id, String audio, String processText) {
        LessonFragment fragment = new LessonFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_ID, id);
        args.putString(ARG_AUDIO, audio);
        args.putString(ARG_PROCESS_TEXT, processText);
        fragment.setArguments(args);
        return fragment;
    }

    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        if (getArguments() != null) {
            mLessonId = getArguments().getInt(ARG_ID);
            String mLessonAudio = getArguments().getString(ARG_AUDIO);
            mProcessText = getArguments().getString(ARG_PROCESS_TEXT);
        }

        mContext = getContext();
        mPref = AppInjectors.provideSharedPreferences(mContext);
        mPrefEdit = mPref.edit();
        mRecordedPlayer = new MediaPlayer();
        mTapListener = new GestureDetector(mContext, new TapListener());
        mWordAlgorithm = AppInjectors.provideWordAlgorithm(mContext);
        mSentenceAlgorithm = AppInjectors.provideSentenceAlgorithm(mContext);
        mSelectedItemReading = new HashMap<>();

        mSpeech = new TextToSpeech(getContext(), i -> {
            if (i != TextToSpeech.ERROR) {
                mSpeech.setLanguage(Locale.UK);
            }
        });

        mPlayAs = mPref.getString(AppConstants.PREFERENCE_MENU_PLAY_AS, "none");
    }

    @Override public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_lesson, container, false);

        mAdapter = new LessonAdapter(mContext, mListener.useTranslate(), this);

        initComponent(view);
        configureLayout();

        LessonViewModelFactory factory = AppInjectors.provideLessonViewModelFactory(getContext());
        mViewModel = ViewModelProviders.of(this, factory).get(LessonViewModel.class);
        mReadingInfo = mViewModel.getReadingInfo(mLessonId);
        if (mReadingInfo != null) {
            mTextHeader.setText(mReadingInfo.getTitle());
            mTextTranslation.setText(mReadingInfo.getTitle_trans());
            mViewModel.getDictionaries().observe(this, dictionaries -> mDictionaries = dictionaries);
            mViewModel.getReadings(mLessonId).observe(this, this::update);
            mViewModel.getFlashcards().observe(this, flashcards -> {
                if (flashcards != null && !flashcards.isEmpty()) {
                    mAdapter.update(mDataLessons);
                }
            });
        }

        if (mProcessText != null && !mProcessText.isEmpty()) showAddFlashDialog(mProcessText);

        return view;
    }

    private void initComponent(View view) {
        mListLesson = view.findViewById(R.id.recycler_lesson);
        mTextHeader = view.findViewById(R.id.text_header);
        mTextTranslation = view.findViewById(R.id.text_translation);
        mButtonGreen = view.findViewById(R.id.button_green);
        mButtonOrange = view.findViewById(R.id.button_orange);
        mButtonRed = view.findViewById(R.id.button_red);
        mButtonYellow = view.findViewById(R.id.button_yellow);
        mButtonBreak = view.findViewById(R.id.button_break);
        mLayoutMastering = view.findViewById(R.id.layout_mastering);

        mActionModeCallback = new ActionMode.Callback() {
            @Override public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                return true;
            }

            @Override public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                return true;
            }

            @Override public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                return false;
            }

            @Override public void onDestroyActionMode(ActionMode mode) {

            }
        };
    }

    private void configureLayout() {
        mButtonBreak.setVisibility(View.VISIBLE);

        DividerItemDecoration divider = new DividerItemDecoration(mContext, DividerItemDecoration.VERTICAL);
        Drawable dividerImage = ContextCompat.getDrawable(mContext, R.drawable.divider);
        if (dividerImage != null) divider.setDrawable(dividerImage);

        mLayoutManager = new LinearLayoutManager(getContext());
        mListLesson.addItemDecoration(divider);
        mListLesson.setHasFixedSize(true);
        mListLesson.setLayoutManager(mLayoutManager);
        mListLesson.setAdapter(mAdapter);

        //******************************************
        mListLesson.addOnItemTouchListener(new RecyclerItemDoubleClickListener(getContext(), (view, position) -> {
            if (position < mDataLessons.size()) {
                LessonFragment.this.onClick(view, mDataLessons.get(position), position, SELECT_CARD);
            }
        }));
        mListLesson.addOnItemTouchListener(new RecyclerItemClickListener(getContext(), (view, position) -> LessonFragment.this.onClick(view, position < mDataLessons.size() ? mDataLessons.get(position) : null, position, SELECT_WORDS)));
        mListLesson.addOnItemTouchListener(new RecyclerItemLongClickListener(getContext(), (view, position) -> {
            if (position >= mDataLessons.size()) return;
            if (mLayoutManager.findViewByPosition(position) == null) return;

            mSelectedRowForMasteringWord = position;

            // Remove background selection color when selecting word
            ConstraintLayout content = mLayoutManager.findViewByPosition(position).findViewById(R.id.item_lesson_content);
            int colorTo;
            switch (mDataLessons.get(position).getSec()) {
                case 1:
                    colorTo = ActivityCompat.getColor(mContext, R.color.colorListSection);
                    break;
                default:
                    colorTo = ActivityCompat.getColor(mContext, R.color.white);
            }
            content.setBackgroundColor(colorTo);

            TextView textview = mLayoutManager.findViewByPosition(position).findViewById(R.id.item_text_lesson_sentence);
            textview.setCustomSelectionActionModeCallback(mActionModeCallback);
            textview.setFocusableInTouchMode(true);
            textview.setFocusable(true);
            textview.requestFocus();
            textview.setCursorVisible(true);

            if (getSelectedText() != null && !getSelectedText().equalsIgnoreCase("")) {
                mListener.onShowMasterLayout(true);
                mLayoutMastering.setVisibility(View.VISIBLE);
            } else {
                mListener.onShowMasterLayout(false);
                mLayoutMastering.setVisibility(View.GONE);
            }
        }));
        //******************************************

        mDialogSelection = new Dialog(mContext);
        mDialogSelection.requestWindowFeature(Window.FEATURE_NO_TITLE);

        configureToolbar();

        initListener();
    }

    private void configureToolbar() {
        Toolbar toolbar = mActivity.findViewById(R.id.toolbar);
        ImageView imageUp = toolbar.findViewById(R.id.image_arrow_up);
        imageUp.setOnClickListener(v -> mLayoutManager.scrollToPosition(0));
        ImageView imageDown = toolbar.findViewById(R.id.image_arrow_down);
        imageDown.setOnClickListener(v -> mLayoutManager.scrollToPosition(mDataLessons.indexOf(mDataLessons.last()) + 1));
        ImageView imageHelp = toolbar.findViewById(R.id.image_help);
        imageHelp.setOnClickListener(v -> AppUtils.startActivity(mContext, TutorialLessonActivity.class));
        ImageView imageNotSet = toolbar.findViewById(R.id.image_not_set);
        imageNotSet.setOnClickListener(v -> {
            mNotReadToggle = !mNotReadToggle;
            if (mNotReadToggle) {
                imageNotSet.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.ic_select_all_off));
                mDataLessonsHolder = mDataLessons;
                mDataLessons = mDataLessons.where().equalTo("already_read", 0).findAll();
                mAdapter.update(mDataLessons);
            } else {
                imageNotSet.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.ic_select_all_on));
                mDataLessons = mDataLessonsHolder;
                mAdapter.update(mDataLessons);
            }
        });
    }

    private void initListener() {
        mButtonGreen.setOnClickListener(v -> {
            if (getSelectedText() != null && !getSelectedText().trim().isEmpty()) {
                addOrUpdateFlashcardWordFromSelection(getSelectedText(), 4, SELECT_NONE);
                mCurrentSelectedTextViewSentence.setText(mCurrentSelectedTextViewSentence.getText());
            } else {
                updateReadingLevel(4);
            }
            mListener.onShowMasterLayout(false);
            mLayoutMastering.setVisibility(View.GONE);
        });
        mButtonYellow.setOnClickListener(v -> {
            if (getSelectedText() != null && !getSelectedText().trim().isEmpty()) {
                addOrUpdateFlashcardWordFromSelection(getSelectedText(), 3, SELECT_NONE);
                mCurrentSelectedTextViewSentence.setText(mCurrentSelectedTextViewSentence.getText());
            } else {
                updateReadingLevel(3);
            }
            mListener.onShowMasterLayout(false);
            mLayoutMastering.setVisibility(View.GONE);
        });
        mButtonOrange.setOnClickListener(v -> {
            if (getSelectedText() != null && !getSelectedText().trim().isEmpty()) {
                addOrUpdateFlashcardWordFromSelection(getSelectedText(), 2, SELECT_NONE);
                mCurrentSelectedTextViewSentence.setText(mCurrentSelectedTextViewSentence.getText());
            } else {
                updateReadingLevel(2);
            }
            mListener.onShowMasterLayout(false);
            mLayoutMastering.setVisibility(View.GONE);
        });
        mButtonRed.setOnClickListener(v -> {
            if (getSelectedText() != null && !getSelectedText().trim().isEmpty()) {
                addOrUpdateFlashcardWordFromSelection(getSelectedText(), 1, SELECT_NONE);
                mCurrentSelectedTextViewSentence.setText(mCurrentSelectedTextViewSentence.getText());
            } else {
                updateReadingLevel(1);
            }
            mListener.onShowMasterLayout(false);
            mLayoutMastering.setVisibility(View.GONE);
        });

        mButtonBreak.setOnTouchListener((view, motionEvent) -> {
            mTapListener.onTouchEvent(motionEvent);
            return false;
        });

        mTextHeader.setOnClickListener(v -> selectAllReadings());
        mTextTranslation.setOnClickListener(v -> selectAllReadings());
    }

    @Override public void onAttach(Context context) {
        super.onAttach(context);
        mActivity = getActivity();
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString() + " must implement OnFragmentInteractionListener");
        }
    }

    @Override public void onDetach() {
        super.onDetach();
        mListener = null;
        if (mSpeech != null) {
            mSpeech.stop();
            mSpeech.shutdown();
            mSpeech = null;
        }
    }

    @Override public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        MenuItem item = menu.findItem(R.id.menu_search);
        SearchView searchView = (SearchView) item.getActionView();
        searchView.setQueryHint("Search Lesson...");
        searchView.setIconifiedByDefault(true);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override public boolean onQueryTextSubmit(String query) {
                if (query.trim().isEmpty()) {
                    mAdapter.update(mDataLessons);
                } else {
                    RealmResults<Reading> readings = mDataLessons.where().contains("sentence", query, Case.INSENSITIVE).findAll();
                    if (readings.isEmpty()) {
                        readings = mDataLessons.where().contains("translation", query, Case.INSENSITIVE).findAll();
                    }
                    mAdapter.update(readings);
                }
                return true;
            }

            @Override public boolean onQueryTextChange(String newText) {
                if (newText.trim().isEmpty()) mAdapter.update(mDataLessons);
                return true;
            }
        });

        EditText editTextSearch = searchView.findViewById(android.support.v7.appcompat.R.id.search_src_text);
        editTextSearch.setTextColor(ContextCompat.getColor(mContext, R.color.colorFooterPrimary));
        editTextSearch.setHintTextColor(ContextCompat.getColor(mContext, R.color.colorFooterPrimary));

        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_select_all:
                selectAllReadings();
                break;
            case R.id.menu_act_as:
                CharSequence[] items = {"Act as Cinta", "Act as Leo", "Act as Narator", "Act as all", "Turn it off"};

                boolean isFound = false;
                for (int i = 0; i < items.length; i++) {
                    if (items[i].toString().toLowerCase().contains(mPlayAs)) {
                        items[i] = "\u25CF " + items[i];
                        isFound = true;
                        break;
                    }
                }
                if (!isFound)
                    items[items.length - 1] = items[items.length - 1] = "\u25CF " + items[items.length - 1];

                new AlertDialog.Builder(mContext).setTitle("Set your own voice")
                        .setItems(items, (dialog, position) -> {
                            switch (position) {
                                case 0:
                                    mPlayAs = "cinta";
                                    mPrefEdit.putString(AppConstants.PREFERENCE_MENU_PLAY_AS, mPlayAs).apply();
                                    break;
                                case 1:
                                    mPlayAs = "leo";
                                    mPrefEdit.putString(AppConstants.PREFERENCE_MENU_PLAY_AS, mPlayAs).apply();
                                    break;
                                case 2:
                                    mPlayAs = "narator";
                                    mPrefEdit.putString(AppConstants.PREFERENCE_MENU_PLAY_AS, mPlayAs).apply();
                                    break;
                                case 3:
                                    mPlayAs = "all";
                                    mPrefEdit.putString(AppConstants.PREFERENCE_MENU_PLAY_AS, mPlayAs).apply();
                                    break;
                                case 4:
                                    mPlayAs = "none";
                                    mPrefEdit.putString(AppConstants.PREFERENCE_MENU_PLAY_AS, mPlayAs).apply();
                                    break;
                            }
                        }).create().show();
                break;
            case R.id.menu_word_color:
                changeAllColor(true);
                break;
            case R.id.menu_sentence_color:
                changeAllColor(false);
                break;
        }

        return false;
    }

    @Override public void onClick(View view, Reading reading, int position, int selectType) {
        if (reading != null) {
            mViewModel.selectReading(reading, selectType);

            mSelectedReadingForBookmark = reading;

            if (mSelectedItemReading.containsKey(position)) {
                mSelectedItemReading.remove(position);
            } else {
                mSelectedItemReading.put(position, reading);
            }

            if (mSelectedItemReading.size() > 0) {
                mListener.onShowMasterLayout(true);
                mLayoutMastering.setVisibility(View.VISIBLE);
            } else {
                mListener.onShowMasterLayout(false);
                mLayoutMastering.setVisibility(View.GONE);
            }
        } else {
            AlertDialog.Builder dialog = new AlertDialog.Builder(mContext)
                    .setIcon(R.mipmap.ic_launcher_ealing)
                    .setTitle("Update sudah baca ulang")
                    .setMessage("Sistem akan mengubah warna (tingkat kesulitan) seluruh kata dan kalimat di lesson ini.")
                    .setPositiveButton("Update", (dialog1, which) -> {
                        mViewModel.updateReadRepeat(mDataLessons, mWordAlgorithm, mSentenceAlgorithm, mContext);
                        dialog1.dismiss();
                    });
            dialog.show();
        }
    }

    @Override public void onSpeak(View view, Reading reading, int position, boolean isSlow) {
        if (reading == null) return;

        // Play sound from recorded audio
        if (!mPlayAs.equalsIgnoreCase("none") && (reading.getActor().toLowerCase().trim().equalsIgnoreCase(mPlayAs) || mPlayAs.equals("all"))) {
            if (mRecordedPlayer != null) {
                mRecordedPlayer.release();
                mRecordedPlayer = null;
            }
            mRecordedPlayer = new MediaPlayer();
            Challenge challenge = mViewModel.findChallengeFromReading(reading.getId());
            if (challenge != null && challenge.isCorrect()) {
                File audioPath = new File(mContext.getFilesDir(), "record");
                File audioFile = new File(audioPath, reading.getSequence_no() + ".amr");
                if (audioPath.isDirectory() && audioFile.exists()) {
                    try {
                        mRecordedPlayer.setDataSource(audioFile.getAbsolutePath());
                        mRecordedPlayer.prepare();
                        mRecordedPlayer.start();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    return;
                }
            }
        }

        // Play sound from Google Speech
        if (reading.getStart_duration().contains("00:00:00") && reading.getEnd_duration().contains("00:00:00")) {
            mSpeech.speak(reading.getSentence(), TextToSpeech.QUEUE_FLUSH, null);
            return;
        }

        // Play sound from Audio Master File
        mListener.onSpeech(reading, isSlow);
    }

    @Override public void onTextSelection(Reading reading, String text, int position) {
        mViewModel.resetSelection(reading);
        mSelectedRowForMasteringWord = position;
    }

    private String getSelectedText() {
        if (mLayoutManager.findViewByPosition(mSelectedRowForMasteringWord) == null) return null;

        TextView textSentence = mLayoutManager.findViewByPosition(mSelectedRowForMasteringWord).findViewById(R.id.item_text_lesson_sentence);
        int start = textSentence.getSelectionStart();
        int end = textSentence.getSelectionEnd();
        mCurrentSelectedTextViewSentence = textSentence;
        return textSentence.getText().subSequence(start, end).toString();
    }

    private void update(RealmResults<Reading> lessons) {
        mDataLessons = lessons;

        if (getView() == null) return;
        if (mHasInitializeData) return;
        if (lessons == null || lessons.isEmpty()) {
            showLoading();
            return;
        }

        // Auto scroll to bookmarked lesson
        Reading reading = lessons.where().equalTo("bookmarked", true).findFirst();
        if (reading != null) {
            LinearLayoutManager layoutManager = (LinearLayoutManager) mListLesson.getLayoutManager();
            layoutManager.scrollToPosition(mDataLessons.indexOf(reading));
        }

        getView().findViewById(R.id.progressbar).setVisibility(View.GONE);
        getView().findViewById(R.id.recycler_lesson).setVisibility(View.VISIBLE);
        mAdapter.update(lessons);
        mViewModel.resetSelection(mDataLessons);
        mHasInitializeData = true;
    }

    private void selectAllReadings() {
        // Disabled selectAll on lesson 1
        if (mLessonId == 1) {
            new AlertDialog.Builder(mContext).setTitle("Message").setIcon(R.mipmap.ic_launcher_ealing)
                    .setMessage("Anda tidak bisa memilih semua kata dan kalimat pada lesson 1")
                    .setPositiveButton("Tutup", (dialog, which) -> dialog.dismiss())
                    .setCancelable(true)
                    .show();
            return;
        }

        mSelectedItemReading.clear();
        mSelectAllToggle = !mSelectAllToggle;
        mViewModel.selectAllReadings(mDataLessons, mSelectAllToggle);
        if (!mSelectAllToggle) {
            mListener.onShowMasterLayout(false);
            mLayoutMastering.setVisibility(View.GONE);
            return;
        } else {
            mListener.onShowMasterLayout(true);
            mLayoutMastering.setVisibility(View.VISIBLE);
        }

        int loop = 0;
        for (Reading reading : mDataLessons) {
            mSelectedItemReading.put(loop, reading);
            loop++;
        }
    }

    private void updateReadingLevel(int toLevel) {
        for (Map.Entry<Integer, Reading> entry : mSelectedItemReading.entrySet()) {
            Reading reading = mViewModel.findFirstReadingCopyEqualTo("id", entry.getValue().getId());

            // Sentence in reading sometimes null in purpose
            // so we set to ("") empty string
            if (reading.getSentence() == null) reading.setSentence("");

            String[] words = reading.getSentence().split(" ");
            for (String word : words) {
                if (!word.trim().replaceAll("[^a-zA-Z_0-9\\s]", "").isEmpty()
                        && !word.trim().equals("")
                        && isAllowToAddFlashcard("word")) {
                    addOrUpdateFlashcardWord(word, toLevel, reading.getSelected());
                }
            }

            if (reading.getSelected() != 2 && isAllowToAddFlashcard("sentence")) {
                reading.setAlready_read(1);
                reading.setMastering_level(toLevel);
                reading.setUploaded(false);

                addOrUpdateFlashcardSentence(reading, toLevel, reading.getSelected());
            }

            reading.setSelected(0);
            reading.setDatemodified(new Date());
            mViewModel.updateReading(reading);
        }

        mSelectedItemReading.clear();
    }

    private void addOrUpdateFlashcardWordFromSelection(String selectedWord, int level, int selectType) {
        String[] words = selectedWord.split(" ");
        for (String word : words) {
            if (!word.trim().replaceAll("[^a-zA-Z_0-9\\s]", "").isEmpty()
                    && !word.trim().equals("")
                    && isAllowToAddFlashcard("word")) {
                addOrUpdateFlashcardWord(word, level, selectType);
            }
        }
    }

    private void addOrUpdateFlashcardSentence(Reading reading, int level, int selectType) {
        if (reading.getSentence().isEmpty()) return;

        // CreateIfNotExists Challenge
        mViewModel.createChallenge(reading, level);

        // Update flashcard
        // **********************************************************************************
        Flashcard card = mViewModel.findFirstFlashcardCopyEqualTo("reference", reading.getId());
        if (card != null) {
            if (reading.getSelected() == SELECT_WORDS || !isAllowToAddFlashcard("sentence")) return;
            if (mDialogSelection.isShowing()) mDialogSelection.dismiss();

            mSentenceAlgorithm.calculate(card, level, selectType);
            reading.setMastering_level(card.getMastering_level());

            card.setAlready_read(1);
            card.setUploaded(false);
            card.setDatemodified(new Date());

            // Commit changes
            mViewModel.updateFlashcard(card);

            return;
        }

        // Add new flashcard
        // **********************************************************************************
        Flashcard newCard = new Flashcard();
        long id = mViewModel.makeNewId();
        newCard.setId(id);
        newCard.setCard(reading.getSentence());

        newCard.setReference(reading.getId());
        newCard.setTranslation(reading.getTranslation());
        newCard.setCategory(Reading.class.getSimpleName());

        newCard.setUploaded(false);
        newCard.setSelected(0);
        newCard.setMastering_level(level);
        newCard.setAlready_read(1);
        newCard.setType("sentence");
        newCard.setDatecreated(new Date());
        newCard.setDatemodified(new Date());
        newCard.setRepeat(0);
        newCard.setReviewed(true);
        newCard.setState(SM2.State.NEW);
        newCard.setE_factor(2.5);
        newCard.setInterval(1);
        newCard.setNext_show(new Date());
        newCard.setEasy_counter(level == 4 ? 1 : 0);

        // Commit changes
        mViewModel.copyOrUpdateFlashcard(newCard);
        mViewModel.updateMetadata(mReadingInfo, reading.getSentence());

        if (mDialogSelection.isShowing()) {
            showSnakeBar(mListLesson, reading.getSentence() + " was added to your flashcard");
            mDialogSelection.dismiss();
        }
    }

    private void addOrUpdateFlashcardWord(String word, int level, int selectType) {
        // Local Word is forbidden
        // **********************************************************************************
        Dictionary dictionary = mDictionaries.where().equalTo("word", AppUtils.normalizeString(word), Case.INSENSITIVE).findFirst();
        if (dictionary != null && dictionary.getLocal_word() > AppConstants.DEFAULT_LOCAL_WORD_CONSTANT) {
            return;
        }

        // Update flashcard
        // **********************************************************************************
        Flashcard card = mViewModel.findFirstFlashcardCopyEqualTo("card", AppUtils.normalizeString(word));
        if (card != null) {
            if (selectType == SELECT_CARD) return;
            if (mDialogSelection.isShowing()) mDialogSelection.dismiss();

            mWordAlgorithm.calculate(card, level, selectType);

            card.setAlready_read(1);
            card.setUploaded(false);
            card.setDatemodified(new Date());

            // Commit changes
            mViewModel.updateFlashcard(card);
            return;
        }

        // Add new flashcard
        // **********************************************************************************
        Flashcard newCard = new Flashcard();
        long id = mViewModel.makeNewId();
        newCard.setId(id);
        newCard.setCard(AppUtils.normalizeString(word));

        dictionary = mDictionaries.where().equalTo("word", AppUtils.normalizeString(word), Case.INSENSITIVE).findFirst();
        if (dictionary != null) {
            newCard.setReference(dictionary.getId());
            newCard.setTranslation(dictionary.getTranslation());
            newCard.setCategory(Dictionary.class.getSimpleName());
        } else {
            newCard.setReference(UUID.randomUUID().getMostSignificantBits() & Long.MAX_VALUE);
            newCard.setTranslation("");
            newCard.setCategory("User");
        }

        newCard.setUploaded(false);
        newCard.setSelected(0);
        newCard.setMastering_level(level);
        newCard.setAlready_read(1);
        newCard.setType("word");
        newCard.setDatecreated(new Date());
        newCard.setDatemodified(new Date());
        newCard.setRepeat(0);
        newCard.setReviewed(true);
        newCard.setState(SM2.State.NEW);
        newCard.setE_factor(2.5);
        newCard.setInterval(1);
        newCard.setNext_show(new Date());
        newCard.setEasy_counter(level == 4 ? 1 : 0);

        // Commit changes
        mViewModel.copyOrUpdateFlashcard(newCard);
        mViewModel.updateMetadata(mReadingInfo, AppUtils.normalizeString(word));

        if (mDialogSelection.isShowing()) {
            mDialogSelection.dismiss();
            showSnakeBar(mListLesson, word + " was added to your flashcard");
        }
    }

    private void changeAllColor(boolean isWord) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.dialog_change_color, null);

        AlertDialog.Builder dialog = new AlertDialog.Builder(mContext);
        dialog.setView(view);
        dialog.setTitle("Ubah warna semua " + (isWord ? "kata" : "kalimat") + " di lesson ini\n(Update tingkat kesulitan)");
        dialog.setPositiveButton("Ubah", (i, which) -> {
            int from = 1, to = 1;

            if (((RadioButton) view.findViewById(R.id.radio_from_red)).isChecked()) from = 1;
            if (((RadioButton) view.findViewById(R.id.radio_from_orange)).isChecked()) from = 2;
            if (((RadioButton) view.findViewById(R.id.radio_from_yellow)).isChecked()) from = 3;
            if (((RadioButton) view.findViewById(R.id.radio_from_green)).isChecked()) from = 4;

            if (((RadioButton) view.findViewById(R.id.radio_to_red)).isChecked()) to = 1;
            if (((RadioButton) view.findViewById(R.id.radio_to_orange)).isChecked()) to = 2;
            if (((RadioButton) view.findViewById(R.id.radio_to_yellow)).isChecked()) to = 3;
            if (((RadioButton) view.findViewById(R.id.radio_to_green)).isChecked()) to = 4;

            if (isWord) {
                Set<String> words = new HashSet<>();
                for (Reading reading : mDataLessons) {
                    if (reading.getSentence() == null) continue;
                    String[] text = reading.getSentence().split(" ");
                    for (String word : text) {
                        String normalWord = word.trim().replaceAll("[^a-zA-Z_0-9\\s]", "");
                        if (!normalWord.isEmpty() && !word.trim().equals("")) {
                            if (!words.contains(normalWord)) words.add(normalWord);
                        }
                    }
                }
                RealmResults<Flashcard> flashcards = mViewModel.findWordsInFlashcard(words.toArray(new String[words.size()]));
                for (Flashcard flashcard : flashcards) {
                    if (flashcard.getMastering_level() == from) {
                        addOrUpdateFlashcardWordFromSelection(flashcard.getCard(), to, SELECT_NONE);
                    }
                }
            } else {
                for (Reading reading : mDataLessons) {
                    if (reading.getMastering_level() == from) {
                        LessonFragment.this.onClick(null, reading, mDataLessons.indexOf(reading), SELECT_CARD);
                    }
                }
                updateReadingLevel(to);
            }
        });
        dialog.setNegativeButton("Batal", (i, which) -> i.dismiss());
        dialog.show();
    }

    private void showAddFlashDialog(String text) {
        mDialogSelection.setContentView(R.layout.dialog_choose_level);

        TextView textTitle = mDialogSelection.findViewById(R.id.item_text_dialog_lesson_title);
        TextView textSelection = mDialogSelection.findViewById(R.id.item_text_dialog_lesson_selection);
        TextView textTranslation = mDialogSelection.findViewById(R.id.item_text_dialog_lesson_translation);

        textTitle.setText(R.string.add_flashcard_dialog_title);
        textSelection.setText(text);
        textTranslation.setVisibility(View.VISIBLE);

        Flashcard flashcard = mViewModel.findFirstFlashcardCopyEqualTo("card", AppUtils.normalizeString(text));
        if (flashcard != null) {
            textTranslation.setText(flashcard.getTranslation());
        } else {
            Reading read = mViewModel.findFirstReadingCopyEqualTo("sentence", AppUtils.normalizeString(text));
            if (read != null) {
                textTranslation.setText(read.getTranslation());
            } else {
                textTranslation.setVisibility(View.GONE);
            }
        }

        Button btnRed = mDialogSelection.findViewById(R.id.item_button_dialog_lesson_veryhard);
        Button btnOrange = mDialogSelection.findViewById(R.id.item_button_dialog_lesson_hard);
        Button btnYellow = mDialogSelection.findViewById(R.id.item_button_dialog_lesson_easy);
        Button btnGreen = mDialogSelection.findViewById(R.id.item_button_dialog_lesson_veryeasy);

        btnRed.setOnClickListener(v -> addOrUpdateFlashcardWord(text, 1, SELECT_NONE));
        btnOrange.setOnClickListener(v -> addOrUpdateFlashcardWord(text, 2, SELECT_NONE));
        btnYellow.setOnClickListener(v -> addOrUpdateFlashcardWord(text, 3, SELECT_NONE));
        btnGreen.setOnClickListener(v -> addOrUpdateFlashcardWord(text, 4, SELECT_NONE));

        mDialogSelection.show();
    }

    private void showLoading() {
        if (getView() == null) return;

        getView().findViewById(R.id.progressbar).setVisibility(View.VISIBLE);
        getView().findViewById(R.id.recycler_lesson).setVisibility(View.GONE);
    }

    private void showSnakeBar(View view, @NonNull String message) {
        Snackbar.make(view, message, Snackbar.LENGTH_LONG).show();
    }

    private boolean isAllowToAddFlashcard(String type) {
        return mViewModel.isAllowToAddFlashcard(type);
    }

    public interface OnFragmentInteractionListener {
        boolean useTranslate();

        void onSpeech(Reading reading, boolean isSlow);

        void onShowMasterLayout(boolean show);
    }

    private class TapListener implements GestureDetector.OnGestureListener, GestureDetector.OnDoubleTapListener {

        @Override public boolean onSingleTapConfirmed(MotionEvent motionEvent) {
            new Handler().postDelayed(() -> {
                mViewModel.setBookmark(mDataLessons, mSelectedReadingForBookmark);
                showSnakeBar(mButtonBreak, "Bookmark telah di set");
                mListener.onShowMasterLayout(false);
                mLayoutMastering.setVisibility(View.GONE);
            }, 250);

            return false;
        }

        @Override public boolean onDoubleTap(MotionEvent motionEvent) {
            new Handler().postDelayed(() -> {
                mSelectedItemReading.clear();
                mSelectAllToggle = false;
                mViewModel.resetSelection(mDataLessons);
                mListener.onShowMasterLayout(false);
                mLayoutMastering.setVisibility(View.GONE);
            }, 250);

            return false;
        }

        @Override public boolean onDoubleTapEvent(MotionEvent motionEvent) {
            return false;
        }

        @Override public boolean onDown(MotionEvent motionEvent) {
            return true;
        }

        @Override public void onShowPress(MotionEvent motionEvent) {

        }

        @Override public boolean onSingleTapUp(MotionEvent motionEvent) {
            return false;
        }

        @Override public boolean onScroll(MotionEvent motionEvent, MotionEvent motionEvent1, float v, float v1) {
            return false;
        }

        @Override public void onLongPress(MotionEvent motionEvent) {

        }

        @Override public boolean onFling(MotionEvent motionEvent, MotionEvent motionEvent1, float v, float v1) {
            return false;
        }
    }

}
