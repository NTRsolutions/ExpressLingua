package com.neosolusi.expresslingua.features.flashcard;

import android.app.Activity;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.speech.tts.TextToSpeech;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetBehavior;
import android.support.transition.TransitionManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.neosolusi.expresslingua.AppConstants;
import com.neosolusi.expresslingua.AppInjectors;
import com.neosolusi.expresslingua.AppUtils;
import com.neosolusi.expresslingua.R;
import com.neosolusi.expresslingua.data.entity.Flashcard;
import com.neosolusi.expresslingua.data.entity.Reading;
import com.neosolusi.expresslingua.features.flashcard.MainAdapter.CardType;
import com.neosolusi.expresslingua.features.tutorial.TutorialFlashcardMultipleActivity;
import com.neosolusi.expresslingua.features.tutorial.TutorialFlashcardSingleActivity;
import com.neosolusi.expresslingua.util.RecyclerItemClickListener;
import com.neosolusi.expresslingua.util.RecyclerItemDoubleClickListener;

import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import io.realm.RealmResults;

import static android.view.View.GONE;
import static com.neosolusi.expresslingua.features.flashcard.FlashcardActivity.SELECT_CARD;
import static com.neosolusi.expresslingua.features.flashcard.FlashcardActivity.SELECT_NONE;
import static com.neosolusi.expresslingua.features.flashcard.FlashcardActivity.SELECT_WORDS;

public class CardFragment extends Fragment implements OnInteractionListener {

    public static final String ARG_LEVEL = "flashcard_level";
    public static final String ARG_TYPE = "flashcard_type";
    public static final String ARG_IGNORE_DATE = "flashcard_ignore_date";
    private static final String ARG_SEARCH = "flashcard_search";

    // Components
    private SharedPreferences mPreferences;
    private LinearLayoutManager mLayoutManager;
    private RecyclerView mListFlashMultiple;
    private LinearLayout mBottomSheetLayout;
    private View mIndicatorView;
    private Button mButtonGreen, mButtonYellow, mButtonOrange, mButtonRed;
    private TextToSpeech mSpeech;
    private SearchView mSearchView;
    private CardViewModel mViewModel;
    private RecyclerView.Adapter mAdapter;
    private RecyclerItemDoubleClickListener mItemDoubleClickListener;
    private RecyclerItemClickListener mItemClickListener;
    private CardView mLayoutMastering;

    // Components BottomSheet
    private BottomSheetBehavior mBottomSheetBehavior;
    private Button mSheetButtonSave;
    private ImageButton mSheetButtonSpeak;
    private EditText mSheetEditTranslation;
    private TextView mSheetTextTitle;
    private TextView mSheetTextTranslation;
    private LinearLayout mSheetContentSampleOne, mSheetContentSampleTwo, mSheetContentSampleThree;
    private TextView mSheetTextSampleOne, mSheetTextSampleTwo, mSheetTextSampleThree;

    // Arguments
    private CardType mCardTypeArguments;
    private String mFlashSearchArguments;
    private int mLevelArguments;

    // Single Flashcard
    private Timer mTimer;
    private Handler mHandler;
    private boolean hasShowAnswer = true;

    private Context mContext;
    private Activity mActivity;
    private OnFragmentInteractionListener mListener;
    private RealmResults<Flashcard> mDataFlashcards;
    private boolean mHasInitializeData = false;
    private boolean mSelectCardToggle = false;
    private boolean mIgnoreDate = false;
    private int mSelectedRowForMasteringWord;

    public CardFragment() {
        // Required empty public constructor
    }

    public static CardFragment newInstance(CardType cardType, String search, int level, boolean ignoreDate) {
        CardFragment fragment = new CardFragment();
        Bundle args = new Bundle();

        switch (cardType) {
            case SINGLE_WORD:
                args.putString(ARG_TYPE, "SINGLE_WORD");
                break;
            case SINGLE_SENTENCE:
                args.putString(ARG_TYPE, "SINGLE_SENTENCE");
                break;
            case MULTIPLE_SENTENCES:
                args.putString(ARG_TYPE, "MULTIPLE_SENTENCES");
                break;
            case CHALLENGES:
                args.putString(ARG_TYPE, "CHALLENGES");
                break;
            default:
                args.putString(ARG_TYPE, "MULTIPLE_WORDS");
                break;
        }

        args.putString(ARG_SEARCH, search);
        args.putInt(ARG_LEVEL, level);
        args.putBoolean(ARG_IGNORE_DATE, ignoreDate);
        fragment.setArguments(args);

        return fragment;
    }

    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        if (getArguments() != null) {
            String cardType = getArguments().getString(ARG_TYPE);

            if (cardType == null) return;
            switch (cardType) {
                case "SINGLE_WORD":
                    mCardTypeArguments = CardType.SINGLE_WORD;
                    break;
                case "SINGLE_SENTENCE":
                    mCardTypeArguments = CardType.SINGLE_SENTENCE;
                    break;
                case "MULTIPLE_SENTENCES":
                    mCardTypeArguments = CardType.MULTIPLE_SENTENCES;
                    break;
                case "CHALLENGES":
                    mCardTypeArguments = CardType.CHALLENGES;
                    break;
                default:
                    mCardTypeArguments = CardType.MULTIPLE_WORDS;
                    break;
            }

            mLevelArguments = getArguments().getInt(ARG_LEVEL);
            mFlashSearchArguments = getArguments().getString(ARG_SEARCH, "");
            mIgnoreDate = getArguments().getBoolean(ARG_IGNORE_DATE);
        }

        mContext = getContext();
        mHandler = new Handler();
        mSpeech = new TextToSpeech(getActivity(), i -> {
            if (i != TextToSpeech.ERROR) {
                mSpeech.setLanguage(Locale.UK);
            }
        });
    }

    @Override public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (mContext == null) return null;

        View view = inflater.inflate(R.layout.fragment_flashcard, container, false);

        CardViewModelFactory factory = AppInjectors.provideCardViewModelFactory(getContext());
        mViewModel = ViewModelProviders.of(this, factory).get(CardViewModel.class);

        mItemDoubleClickListener = new RecyclerItemDoubleClickListener(getContext(), (v, position) -> CardFragment.this.onDoubleClick(mDataFlashcards, position));
        mItemClickListener = new RecyclerItemClickListener(getContext(), (v, position) -> CardFragment.this.onClick(v, mDataFlashcards.get(position), position, SELECT_CARD));

        initComponent(view);
        configureLayout(view);
        configureBottomSheet();
        initListener(view);
        setupModel();

        return view;
    }

    private void initComponent(View view) {
        if (mCardTypeArguments == CardType.SINGLE_WORD || mCardTypeArguments == CardType.SINGLE_SENTENCE) {
            mIndicatorView = view.findViewById(R.id.view_single_indicator);
        } else {
            mIndicatorView = view.findViewById(R.id.view_indicator);
        }

        mLayoutMastering = view.findViewById(R.id.layout_mastering);
        mListFlashMultiple = view.findViewById(R.id.recycler_flashcard_word);
        mBottomSheetLayout = view.findViewById(R.id.bottom_sheet_flashcard_word);
        mButtonGreen = view.findViewById(R.id.button_green);
        mButtonYellow = view.findViewById(R.id.button_yellow);
        mButtonOrange = view.findViewById(R.id.button_orange);
        mButtonRed = view.findViewById(R.id.button_red);
    }

    private void configureLayout(View view) {
        if (mCardTypeArguments == CardType.SINGLE_WORD || mCardTypeArguments == CardType.SINGLE_SENTENCE) {
            view.findViewById(R.id.layout_single).setVisibility(View.VISIBLE);
            view.findViewById(R.id.layout_multiple).setVisibility(View.GONE);
            view.findViewById(R.id.text_goto_sentence).setVisibility(GONE);
        } else {
            view.findViewById(R.id.layout_single).setVisibility(GONE);
            view.findViewById(R.id.text_goto_sentence).setVisibility(View.VISIBLE);
            view.findViewById(R.id.layout_multiple).setVisibility(View.VISIBLE);
        }

        Drawable dividerImage = ContextCompat.getDrawable(mContext, R.drawable.divider);
        DividerItemDecoration divider = new DividerItemDecoration(mContext, DividerItemDecoration.VERTICAL);
        if (dividerImage != null) divider.setDrawable(dividerImage);

        mLayoutManager = new LinearLayoutManager(getContext());
        mListFlashMultiple.setLayoutManager(mLayoutManager);
        mListFlashMultiple.addItemDecoration(divider);
        mListFlashMultiple.setHasFixedSize(true);

        //******************************************
        mListFlashMultiple.removeOnItemTouchListener(mItemDoubleClickListener);
        mListFlashMultiple.removeOnItemTouchListener(mItemClickListener);
        if (mCardTypeArguments == CardType.MULTIPLE_WORDS || mCardTypeArguments == CardType.MULTIPLE_SENTENCES) {
            mListFlashMultiple.addOnItemTouchListener(mItemDoubleClickListener);
            mListFlashMultiple.addOnItemTouchListener(mItemClickListener);
        }
        //******************************************

        showIndicator(mLevelArguments);

        if (mCardTypeArguments != CardType.SINGLE_SENTENCE || mCardTypeArguments != CardType.SINGLE_WORD) {
            view.findViewById(R.id.text_goto_sentence).setVisibility(GONE);
            view.findViewById(R.id.view_indicator).setVisibility(GONE);
            view.findViewById(R.id.text_header).setVisibility(GONE);
        }

        Toolbar toolbar = mActivity.findViewById(R.id.toolbar);
        ImageView imageUp = toolbar.findViewById(R.id.image_arrow_up);
        ImageView imageDown = toolbar.findViewById(R.id.image_arrow_down);
        ImageView imageHelp = toolbar.findViewById(R.id.image_help);
        ImageView imageNotSet = toolbar.findViewById(R.id.image_not_set);
        if (mCardTypeArguments == CardType.MULTIPLE_SENTENCES || mCardTypeArguments == CardType.MULTIPLE_WORDS) {
            imageHelp.setOnClickListener(v -> AppUtils.startActivity(mContext, TutorialFlashcardMultipleActivity.class));
            imageUp.setOnClickListener(v -> mLayoutManager.scrollToPosition(0));
            imageDown.setOnClickListener(v -> {
                if (mDataFlashcards == null) return;
                int fluencySize = mPreferences.getInt(AppConstants.PREFERENCE_MAX_DAILY_FLUENCY_CARD, 5);
                int dataSize = mDataFlashcards.size() > fluencySize ? fluencySize : mDataFlashcards.size();
                mLayoutManager.scrollToPosition(dataSize - 1);
            });
            imageNotSet.setVisibility(GONE);
        } else {
            imageHelp.setOnClickListener(v -> AppUtils.startActivity(mContext, TutorialFlashcardSingleActivity.class));
            imageDown.setVisibility(GONE);
            imageUp.setVisibility(GONE);
            imageNotSet.setVisibility(GONE);
        }
    }

    private void configureBottomSheet() {
        mBottomSheetBehavior = BottomSheetBehavior.from(mBottomSheetLayout);

        mSheetButtonSave = mBottomSheetLayout.findViewById(R.id.btnSave);
        mSheetEditTranslation = mBottomSheetLayout.findViewById(R.id.editTextTranslation);
        mSheetTextTitle = mBottomSheetLayout.findViewById(R.id.itemFlashCard);
        mSheetTextTranslation = mBottomSheetLayout.findViewById(R.id.itemFlashCardTranslation);
        mSheetContentSampleOne = mBottomSheetLayout.findViewById(R.id.itemFlashCardSample1Layout);
        mSheetContentSampleTwo = mBottomSheetLayout.findViewById(R.id.itemFlashCardSample2Layout);
        mSheetContentSampleThree = mBottomSheetLayout.findViewById(R.id.itemFlashCardSample3Layout);
        mSheetTextSampleOne = mBottomSheetLayout.findViewById(R.id.itemFlashCardSample1);
        mSheetTextSampleTwo = mBottomSheetLayout.findViewById(R.id.itemFlashCardSample2);
        mSheetTextSampleThree = mBottomSheetLayout.findViewById(R.id.itemFlashCardSample3);
        mSheetButtonSpeak = mBottomSheetLayout.findViewById(R.id.itemFlashCardSpeak);
    }

    private void showBottomSheet(Flashcard flashcard) {
        mSheetTextTitle.setText(AppUtils.leftTrim(flashcard.getCard()));
        mSheetTextTranslation.setText(AppUtils.leftTrim(flashcard.getTranslation()));
        if (mSheetTextTranslation.getText().toString().trim().equals("")) {
            mSheetTextTranslation.setVisibility(GONE);
            mSheetEditTranslation.setVisibility(View.VISIBLE);
            mSheetButtonSave.setVisibility(View.VISIBLE);
        } else {
            mSheetTextTranslation.setVisibility(View.VISIBLE);
            mSheetEditTranslation.setVisibility(View.GONE);
            mSheetButtonSave.setVisibility(View.GONE);
        }

        String[] samples = AppUtils.getSamples(flashcard);
        mSheetContentSampleOne.setVisibility(View.VISIBLE);
        mSheetContentSampleTwo.setVisibility(View.VISIBLE);
        mSheetContentSampleThree.setVisibility(View.VISIBLE);
        if (samples[0] == null) mSheetContentSampleOne.setVisibility(GONE);
        if (samples[1] == null) mSheetContentSampleTwo.setVisibility(GONE);
        if (samples[2] == null) mSheetContentSampleThree.setVisibility(GONE);
        mSheetTextSampleOne.setText(samples[0]);
        mSheetTextSampleTwo.setText(samples[1]);
        mSheetTextSampleThree.setText(samples[2]);

        mSheetButtonSpeak.setOnClickListener(view -> mSpeech.speak(flashcard.getCard(), TextToSpeech.QUEUE_FLUSH, null));
        mSheetButtonSave.setOnClickListener(view -> {
            AppUtils.dismissKeyboard(getActivity(), view.getWindowToken());

            Handler handler = new Handler();
            handler.postDelayed(() -> {
                mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);

                flashcard.setTranslation(mSheetEditTranslation.getText().toString());
                flashcard.setUploaded(false);

                mViewModel.copyOrUpdate(flashcard);

                mSheetTextTranslation.setVisibility(View.VISIBLE);
                mSheetEditTranslation.setVisibility(GONE);
                mSheetButtonSave.setVisibility(GONE);
            }, 500);
        });

        mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
    }

    private void initListener(View view) {
        mButtonGreen.setOnClickListener(v -> {
            if (getSelectedText() != null && !getSelectedText().trim().isEmpty()) {
                mViewModel.addOrUpdateFlashcardWord(getSelectedText(), 4, SELECT_NONE);
                mAdapter.notifyDataSetChanged();
            } else {
                mViewModel.updateLevel(mCardTypeArguments, 4);
            }
        });
        mButtonYellow.setOnClickListener(v -> {
            if (getSelectedText() != null && !getSelectedText().trim().isEmpty()) {
                mViewModel.addOrUpdateFlashcardWord(getSelectedText(), 3, SELECT_NONE);
                mAdapter.notifyDataSetChanged();
            } else {
                mViewModel.updateLevel(mCardTypeArguments, 3);
            }
        });
        mButtonOrange.setOnClickListener(v -> {
            if (getSelectedText() != null && !getSelectedText().trim().isEmpty()) {
                mViewModel.addOrUpdateFlashcardWord(getSelectedText(), 2, SELECT_NONE);
                mAdapter.notifyDataSetChanged();
            } else {
                mViewModel.updateLevel(mCardTypeArguments, 2);
            }
        });
        mButtonRed.setOnClickListener(v -> {
            if (getSelectedText() != null && !getSelectedText().trim().isEmpty()) {
                mViewModel.addOrUpdateFlashcardWord(getSelectedText(), 1, SELECT_NONE);
                mAdapter.notifyDataSetChanged();
            } else {
                mViewModel.updateLevel(mCardTypeArguments, 1);
            }
        });

        view.findViewById(R.id.layout_top).setOnClickListener(view1 -> {
            if (mBottomSheetBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED) {
                mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
            }
        });
        view.findViewById(R.id.text_goto_sentence).setOnClickListener(view1 -> {
            showFlashcards(CardType.MULTIPLE_SENTENCES);
            view1.setVisibility(GONE);
        });
    }

    private void setupModel() {
        if (mCardTypeArguments == CardType.MULTIPLE_WORDS || mCardTypeArguments == CardType.MULTIPLE_SENTENCES) {
            mViewModel.getFlashcards(mLevelArguments, mCardTypeArguments, mIgnoreDate).observe(this, flashcards -> {
                mDataFlashcards = flashcards;

                if (flashcards == null || flashcards.isEmpty()) {
                    showNoFlashcard();
                    return;
                }
                if (mHasInitializeData) return;

                switch (mCardTypeArguments) {
                    case MULTIPLE_SENTENCES:
                        mAdapter = new SentenceAdapter(getContext(), mListener.useTranslate(), mDataFlashcards, this);
                        break;
                    case MULTIPLE_WORDS:
                        mAdapter = new WordAdapter(getContext(), mDataFlashcards, this);
                }

                mViewModel.resetSelection(mDataFlashcards);
                mHasInitializeData = true;
                mListFlashMultiple.setAdapter(mAdapter);
            });
        } else {
            mViewModel.getFlashcard(mCardTypeArguments).observe(this, flashcard -> {
                if (flashcard == null) {
                    showNoFlashcard();

                    /*
                      There is no need to refresh flashcard word with timer
                      if flashcard word is empty, flashcard multiple will show
                      startTimer();
                     */

                    return;
                }

                if (hasShowAnswer) {
                    if (mTimer != null) mTimer.cancel();
                    mTimer = null;
                    mViewModel.selectFlashcard(0, flashcard, SELECT_NONE);
                    showIndicator(flashcard.getMastering_level());
                    showFlashcard(flashcard, mViewModel.getDeck());
                    hasShowAnswer = false;
                } else {
                    showAnswer(flashcard);
                    mHandler.postDelayed(() -> mViewModel.getFlashcard(mCardTypeArguments), 2000);
                    hasShowAnswer = true;
                }
            });
        }
    }

    private String getSelectedText() {
        if (mLayoutManager.findViewByPosition(mSelectedRowForMasteringWord) == null) return null;

        if (mCardTypeArguments == CardType.MULTIPLE_SENTENCES) {
            TextView textview = mLayoutManager.findViewByPosition(mSelectedRowForMasteringWord).findViewById(R.id.item_text_lesson_sentence);
            int start = textview.getSelectionStart();
            int end = textview.getSelectionEnd();
            return textview.getText().subSequence(start, end).toString();
        }

        return null;
    }

    @Override public void onAttach(Context context) {
        super.onAttach(context);
        mActivity = getActivity();
        mPreferences = AppInjectors.provideSharedPreferences(context);
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
            mSpeech.shutdown();
            mSpeech = null;
        }
    }

//    @Override public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
//        inflater.inflate(R.menu.app_menu, menu);
//
//        MenuItem item = menu.findItem(R.id.menu_search);
//        mSearchView = (SearchView) item.getActionView();
//
//        super.onCreateOptionsMenu(menu, inflater);
//    }
//
//    @Override public boolean onOptionsItemSelected(MenuItem item) {
//        return super.onOptionsItemSelected(item);
//    }

//    @Override public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
//        MenuItem item = menu.findItem(R.id.menu_search);
//        SearchView searchView = (SearchView) item.getActionView();
//        searchView.setQueryHint("Search Challenge...");
//        searchView.setIconifiedByDefault(true);
//        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
//            @Override public boolean onQueryTextSubmit(String query) {
//                if (query.trim().isEmpty()) {
////                    mAdapter.update(mDataFlashcards);
//                } else {
////                    mAdapter.update(mDataFlashcards.where().contains("sentence", query, Case.INSENSITIVE).findAll());
//                }
//                return true;
//            }
//
//            @Override public boolean onQueryTextChange(String newText) {
////                if (newText.trim().isEmpty()) mAdapter.update(mDataLessons);
//                return true;
//            }
//        });
//
//        EditText editTextSearch = searchView.findViewById(android.support.v7.appcompat.R.id.search_src_text);
//        editTextSearch.setTextColor(ContextCompat.getColor(mContext, R.color.colorFooterPrimary));
//        editTextSearch.setHintTextColor(ContextCompat.getColor(mContext, R.color.colorFooterPrimary));
//        super.onCreateOptionsMenu(menu, inflater);
//    }
//
//    @Override public boolean onOptionsItemSelected(MenuItem item) {
//        switch (item.getItemId()) {
//            case R.id.menu_select_all:
//                onDoubleClick(mDataFlashcards, 0);
//                break;
//            case R.id.menu_help:
//                if (mCardTypeArguments == CardType.MULTIPLE_WORDS || mCardTypeArguments == CardType.MULTIPLE_SENTENCES) {
//                    AppUtils.startActivity(mContext, TutorialFlashcardMultipleActivity.class);
//                } else {
//                    AppUtils.startActivity(mContext, TutorialFlashcardSingleActivity.class);
//                }
//                break;
//        }
//        return false;
//    }

    @Override public void onClick(View view, Flashcard flashcard, int position, int selectType) {
        if (mCardTypeArguments == CardType.MULTIPLE_SENTENCES) {
            mViewModel.selectFlashcard(position, flashcard, SELECT_WORDS);
        } else {
            mViewModel.selectFlashcard(position, flashcard, selectType);
        }
    }

    @Override public void onDoubleClick(RealmResults<Flashcard> entities, int position) {
        if (mCardTypeArguments == CardType.MULTIPLE_SENTENCES) {
            mViewModel.selectFlashcard(position, mDataFlashcards.get(position), SELECT_CARD);
        } else {
            mViewModel.selectAllFlashcards(entities, position, mSelectCardToggle);
        }
        mSelectCardToggle = !mSelectCardToggle;
    }

    @Override public void onTextSelected(Flashcard entity, String text, int position) {
        mSelectedRowForMasteringWord = position;
    }

    @Override public void onSpeak(View view, Flashcard flashcard, int position, boolean isSlow) {
        if (mCardTypeArguments == CardType.MULTIPLE_SENTENCES || mCardTypeArguments == CardType.SINGLE_SENTENCE) {
            Reading reading = mViewModel.findFirstReadingCopyEqualTo("id", flashcard.getReference());
            if (reading != null && !reading.getStart_duration().contains("00:00:00")) {
                mListener.onSpeech(flashcard, isSlow);
                return;
            }
        }
        mSpeech.speak(flashcard.getCard(), TextToSpeech.QUEUE_FLUSH, null);
    }

    @Override public void onMoreOptionClick(View view, Flashcard flashcard) {
        showBottomSheet(flashcard);
    }

    public void showFlashcard(@NonNull Flashcard flashcard, @NonNull String deck) {
        deck = "";
        if (getActivity() == null) return;

        if (getView() == null) return;
        getView().findViewById(R.id.layout_single).setVisibility(View.VISIBLE);
        getView().findViewById(R.id.text_no_card).setVisibility(GONE);
        getView().findViewById(R.id.layout_multiple).setVisibility(GONE);

        getView().findViewById(R.id.button_single_more).setOnClickListener(view -> onMoreOptionClick(view, flashcard));

        TransitionManager.beginDelayedTransition(getView().findViewById(R.id.layout_single));

        getView().findViewById(R.id.text_single_translation).setVisibility(GONE);
        ((TextView) getView().findViewById(R.id.text_single_card)).setText(flashcard.getCard());
        ((TextView) getView().findViewById(R.id.text_single_sample)).setText(deck);
    }

    public void showAnswer(@NonNull Flashcard flashcard) {
        if (getView() == null) return;

        // Disabled animation
        // TransitionManager.beginDelayedTransition(getView().findViewById(R.id.layout_single));

        getView().findViewById(R.id.text_single_translation).setVisibility(View.VISIBLE);
        ((TextView) getView().findViewById(R.id.text_single_translation)).setText(flashcard.getTranslation());

        // Only for debugging purpose
        // SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        // TextView textSample = getView().findViewById(R.id.text_single_sample);
        // textSample.setText(mViewModel.getDeck());
        // textSample.setText(textSample.getText() + "\n" + dateFormat.format(flashcard.getNext_show()));
    }

    public void showNoFlashcard() {
        if (getView() == null) return;

        if (mCardTypeArguments == CardType.SINGLE_WORD) {
            showFlashcards(CardType.MULTIPLE_WORDS);
            return;
        } else if (mCardTypeArguments == CardType.MULTIPLE_WORDS) {
            showFlashcards(CardType.MULTIPLE_SENTENCES);
            return;
        }

        // Debug only
        TextView textNoCard = getView().findViewById(R.id.text_no_card);
        textNoCard.setText("You have no card!" + "\n" + mViewModel.whenFlashcardWillShow());
        // *********************************************************************

        getView().findViewById(R.id.text_no_card).setVisibility(View.VISIBLE);
        getView().findViewById(R.id.layout_multiple).setVisibility(GONE);
        getView().findViewById(R.id.layout_single).setVisibility(GONE);
    }

    private void showFlashcards(CardType cardType) {
        if (getView() == null) return;

        getView().findViewById(R.id.text_no_card).setVisibility(View.GONE);
        getView().findViewById(R.id.layout_multiple).setVisibility(View.VISIBLE);
        getView().findViewById(R.id.layout_single).setVisibility(GONE);

        mHasInitializeData = false;
        mCardTypeArguments = cardType;
        mLevelArguments = 3;
        if (mTimer != null) {
            mTimer.cancel();
            mTimer.purge();
            mTimer = null;
        }

        configureLayout(getView());

        mViewModel.getFlashcard(mCardTypeArguments).removeObservers(this);
        mViewModel.getFlashcards(mLevelArguments, mCardTypeArguments, mIgnoreDate).removeObservers(this);
        mListFlashMultiple.removeOnItemTouchListener(mItemDoubleClickListener);
        mListFlashMultiple.removeOnItemTouchListener(mItemClickListener);

        setupModel();

        if (mCardTypeArguments == CardType.MULTIPLE_WORDS || mCardTypeArguments == CardType.MULTIPLE_SENTENCES) {
            mListFlashMultiple.addOnItemTouchListener(mItemDoubleClickListener);
            mListFlashMultiple.addOnItemTouchListener(mItemClickListener);
        }
        //******************************************
    }

    private void showIndicator(int level) {
        if (getContext() == null || getView() == null) return;

        mIndicatorView.setVisibility(View.VISIBLE);

        CardView contentSingle = getView().findViewById(R.id.layout_single);

        switch (level) {
            case 1:
                mIndicatorView.setBackground(ActivityCompat.getDrawable(getContext(), R.drawable.shape_oval_red));
                contentSingle.setCardBackgroundColor(ContextCompat.getColor(getContext(), R.color.indikator_red));
                break;
            case 2:
                mIndicatorView.setBackground(ActivityCompat.getDrawable(getContext(), R.drawable.shape_oval_orange));
                contentSingle.setCardBackgroundColor(ContextCompat.getColor(getContext(), R.color.indikator_orange));
                break;
            case 3:
                mIndicatorView.setBackground(ActivityCompat.getDrawable(getContext(), R.drawable.shape_oval_yellow));
                contentSingle.setCardBackgroundColor(ContextCompat.getColor(getContext(), R.color.indikator_yellow));
                break;
            case 4:
                mIndicatorView.setBackground(ActivityCompat.getDrawable(getContext(), R.drawable.shape_oval_green));
                contentSingle.setCardBackgroundColor(ContextCompat.getColor(getContext(), R.color.indikator_green));
                break;
            case 5:
                mIndicatorView.setBackground(ActivityCompat.getDrawable(getContext(), R.drawable.shape_oval_blue));
                contentSingle.setCardBackgroundColor(ContextCompat.getColor(getContext(), R.color.indikator_blue));
                break;
            default:
                mIndicatorView.setVisibility(GONE);
        }
    }

    private void startTimer() {
        if (mTimer == null) mTimer = new Timer();
        else return;

        mTimer.schedule(new TimerTask() {
            @Override public void run() {
                mHandler.post(() -> mViewModel.getFlashcard(mCardTypeArguments));
            }
        }, 10000, 10000);
    }

    public interface OnFragmentInteractionListener {
        void onSpeech(Flashcard flashcard, boolean isSlow);
        boolean useTranslate();
    }
}
