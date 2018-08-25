package com.neosolusi.expresslingua.features.challenge;

import android.app.Activity;
import android.app.AlertDialog;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.neosolusi.expresslingua.AppConstants;
import com.neosolusi.expresslingua.AppInjectors;
import com.neosolusi.expresslingua.R;
import com.neosolusi.expresslingua.data.entity.Challenge;
import com.neosolusi.expresslingua.data.entity.Reading;
import com.neosolusi.expresslingua.data.entity.ReadingInfo;
import com.neosolusi.expresslingua.util.RecyclerItemClickListener;
import com.neosolusi.expresslingua.util.RecyclerItemDoubleClickListener;

import java.io.File;
import java.io.IOException;

import io.realm.RealmResults;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static com.neosolusi.expresslingua.features.challenge.ChallengeActivity.LIST_CHALLENGE_TYPE;
import static com.neosolusi.expresslingua.features.flashcard.FlashcardActivity.SELECT_CARD;
import static com.neosolusi.expresslingua.features.flashcard.FlashcardActivity.SELECT_NONE;
import static com.neosolusi.expresslingua.features.flashcard.FlashcardActivity.SELECT_WORDS;
import static com.neosolusi.expresslingua.features.flashcard.MainAdapter.CardType.MULTIPLE_SENTENCES;

public class ListFragment extends Fragment implements ChallengeAdapter.OnItemClickListener {

    // Component property
    private SharedPreferences mPref;
    private SharedPreferences.Editor mPrefEdit;
    private ChallengeViewModel mViewModel;
    private RecyclerView mListChallenges;
    private ChallengeAdapter mAdapter;
    private LinearLayoutManager mLayoutManager;
    private OnInteractionListener mListener;
    private Activity mActivity;
    private Context mContext;
    private TextView mTextHeader;
    private CardView mLayoutMastering;

    // Data property
    private RealmResults<Challenge> mDataChallenges;
    private int mSelectedRowForMasteringWord;
    private boolean mSelectCardToggle = false;
    private boolean mHasInitializeData = false;
    private String mTitle;
    private String mPlayAs;
    private ChallengeActivity.TYPE mChallengeType;
    private MediaPlayer mRecordedPlayer;

    public ListFragment() {
        // Required empty public constructor
    }

    @Override public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        if (getArguments() == null) return;

        mPref = AppInjectors.provideSharedPreferences(mContext);
        mPrefEdit = mPref.edit();
        mRecordedPlayer = new MediaPlayer();
        mPlayAs = mPref.getString(AppConstants.PREFERENCE_MENU_PLAY_AS, "none");

        String type = getArguments().getString(LIST_CHALLENGE_TYPE, "CORRECT");
        if (type == null || type.trim().isEmpty()) return;
        switch (type.toUpperCase()) {
            case "NOT_SEEN":
                mChallengeType = ChallengeActivity.TYPE.NOT_SEEN;
                mTitle = "Not Seen Challenges";
                break;
            case "SKIPPED":
                mChallengeType = ChallengeActivity.TYPE.SKIPPED;
                mTitle = "Skipped Challenges";
                break;
            case "INCORRECT":
                mChallengeType = ChallengeActivity.TYPE.INCORRECT;
                mTitle = "Incorrect Challenges";
                break;
            default:
                mChallengeType = ChallengeActivity.TYPE.CORRECT;
                mTitle = "Correct Challenges";
        }
    }

    @Override public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_list, container, false);

        configureLayout(root);
        initListeners(root);

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
    }

    @Override public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        MenuItem item = menu.findItem(R.id.menu_search);
        item.setVisible(false);

//        SearchView searchView = (SearchView) item.getActionView();
//        searchView.setQueryHint("Search challenge...");
//        searchView.setIconifiedByDefault(true);
//        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
//            @Override public boolean onQueryTextSubmit(String query) {
//                if (query.trim().isEmpty()) {
//                    mAdapter.update(mDataChallenges);
//                } else {
//                    mViewModel.findReadingForChallenge();
//                    mAdapter.update(mDataChallenges.where().contains("sentence", query, Case.INSENSITIVE).findAll());
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

        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_select_all:
                onDoubleClick(mDataChallenges, 0);
                break;
            case R.id.menu_act_as:
                CharSequence[] items = {"Act as Cinta", "Act as Leo", "Act as Narator", "Act as all", "Act as no body"};

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

                new AlertDialog.Builder(mContext).setTitle("Set your voice")
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
        }

        return false;
    }

    private void configureLayout(View view) {
        if (getContext() == null) return;

        mTextHeader = view.findViewById(R.id.text_challenge_header);
        mTextHeader.setText(mTitle);

        mLayoutMastering = view.findViewById(R.id.layout_mastering);

        mTextHeader.setVisibility(GONE);
        mLayoutMastering.setVisibility(GONE);

        ChallengeViewModelFactory factory = AppInjectors.provideChallengeViewModelFactory(getContext());
        mViewModel = ViewModelProviders.of(this, factory).get(ChallengeViewModel.class);

        DividerItemDecoration divider = new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL);
        Drawable dividerImage = ContextCompat.getDrawable(getContext(), R.drawable.divider);
        if (dividerImage != null) divider.setDrawable(dividerImage);

        mLayoutManager = new LinearLayoutManager(getContext());
        mListChallenges = view.findViewById(R.id.recycler_challenges);
        mListChallenges.setLayoutManager(mLayoutManager);
        mListChallenges.addItemDecoration(divider);
        mListChallenges.setHasFixedSize(true);

        mViewModel.getChallenges(mChallengeType).observe(this, challenges -> {
            if (challenges == null || challenges.isEmpty()) return;
            if (mHasInitializeData) return;

            mViewModel.resetSelection(challenges);
            mHasInitializeData = true;
            mDataChallenges = challenges;
            mAdapter = new ChallengeAdapter(mActivity, mListener.useTranslate(), this, mDataChallenges);
            mListChallenges.setAdapter(mAdapter);
        });

        mListChallenges.addOnItemTouchListener(new RecyclerItemDoubleClickListener(mActivity, (v, position) -> ListFragment.this.onDoubleClick(mDataChallenges, position)));
        mListChallenges.addOnItemTouchListener(new RecyclerItemClickListener(mActivity, (v, position) -> ListFragment.this.onClick(v, mDataChallenges.get(position), position, SELECT_CARD)));

        Toolbar toolbar = mActivity.findViewById(R.id.toolbar);
        ImageView imageUp = toolbar.findViewById(R.id.image_arrow_up);
        imageUp.setOnClickListener(v -> mLayoutManager.scrollToPosition(0));
        ImageView imageDown = toolbar.findViewById(R.id.image_arrow_down);
        imageDown.setOnClickListener(v -> mLayoutManager.scrollToPosition(mDataChallenges.indexOf(mDataChallenges.last())));
        ImageView imageHelp = toolbar.findViewById(R.id.image_help);
        ImageView imageNotSet = toolbar.findViewById(R.id.image_not_set);
        imageNotSet.setVisibility(GONE);
    }

    private void initListeners(View view) {
        view.findViewById(R.id.button_red).setOnClickListener(v -> {
            if (getSelectedText() != null && !getSelectedText().trim().isEmpty()) {
                mViewModel.addOrUpdateFlashcardWord(getSelectedText(), 1, SELECT_NONE);
                mAdapter.notifyDataSetChanged();
            } else {
                mViewModel.updateLevel(MULTIPLE_SENTENCES, 1);
            }
            mLayoutMastering.setVisibility(GONE);
        });
        view.findViewById(R.id.button_orange).setOnClickListener(v -> {
            if (getSelectedText() != null && !getSelectedText().trim().isEmpty()) {
                mViewModel.addOrUpdateFlashcardWord(getSelectedText(), 2, SELECT_NONE);
                mAdapter.notifyDataSetChanged();
            } else {
                mViewModel.updateLevel(MULTIPLE_SENTENCES, 2);
            }
            mLayoutMastering.setVisibility(GONE);
        });
        view.findViewById(R.id.button_yellow).setOnClickListener(v -> {
            if (getSelectedText() != null && !getSelectedText().trim().isEmpty()) {
                mViewModel.addOrUpdateFlashcardWord(getSelectedText(), 3, SELECT_NONE);
                mAdapter.notifyDataSetChanged();
            } else {
                mViewModel.updateLevel(MULTIPLE_SENTENCES, 3);
            }
            mLayoutMastering.setVisibility(GONE);
        });
        view.findViewById(R.id.button_green).setOnClickListener(v -> {
            if (getSelectedText() != null && !getSelectedText().trim().isEmpty()) {
                mViewModel.addOrUpdateFlashcardWord(getSelectedText(), 4, SELECT_NONE);
                mAdapter.notifyDataSetChanged();
            } else {
                mViewModel.updateLevel(MULTIPLE_SENTENCES, 4);
            }
            mLayoutMastering.setVisibility(GONE);
        });
    }

    private String getSelectedText() {
        if (mLayoutManager.findViewByPosition(mSelectedRowForMasteringWord) == null) return null;

        TextView textview = mLayoutManager.findViewByPosition(mSelectedRowForMasteringWord).findViewById(R.id.item_text_lesson_sentence);
        int start = textview.getSelectionStart();
        int end = textview.getSelectionEnd();
        return textview.getText().subSequence(start, end).toString();
    }

    @Override public void onClick(View view, Challenge entity, int position, int selectType) {
        mViewModel.selectChallenge(position, entity, SELECT_WORDS);
        for (Challenge challenge : mDataChallenges) {
            if (challenge.getSelected() > 0) {
                mLayoutMastering.setVisibility(VISIBLE);
                return;
            }
        }
        mLayoutMastering.setVisibility(GONE);
    }

    @Override public void onDoubleClick(RealmResults<Challenge> entities, int position) {
        mViewModel.selectChallenge(position, entities.get(position), SELECT_CARD);
        mSelectCardToggle = !mSelectCardToggle;
        for (Challenge challenge : entities) {
            if (challenge.getSelected() > 0) {
                mLayoutMastering.setVisibility(VISIBLE);
                return;
            }
        }
        mLayoutMastering.setVisibility(GONE);
    }

    @Override public void onTextSelected(Reading entity, String text, int position) {
        mSelectedRowForMasteringWord = position;
        mLayoutMastering.setVisibility(VISIBLE);
    }

    @Override public void onSpeak(View view, Challenge entity, int position, boolean isSlow) {
        Reading reading = mViewModel.findReadingForChallenge(entity);
        ReadingInfo info = mViewModel.getCurrentLesson(reading.getFile_id());

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

        // Play sound from Audio Master File
        mListener.resetPlayer();
        mListener.setPlayerInfo(info);
        mListener.speak(reading, isSlow);
    }

    @Override public void onMoreOptionClick(View view, Challenge entity) {

    }
}
