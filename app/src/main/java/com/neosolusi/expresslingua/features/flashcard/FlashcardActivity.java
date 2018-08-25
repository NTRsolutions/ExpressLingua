package com.neosolusi.expresslingua.features.flashcard;

import android.app.Dialog;
import android.arch.lifecycle.ViewModelProviders;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.PorterDuff;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import com.neosolusi.expresslingua.AppInjectors;
import com.neosolusi.expresslingua.AppServices;
import com.neosolusi.expresslingua.AppUtils;
import com.neosolusi.expresslingua.MainActivity;
import com.neosolusi.expresslingua.R;
import com.neosolusi.expresslingua.algorithm.SM2;
import com.neosolusi.expresslingua.data.entity.Flashcard;
import com.neosolusi.expresslingua.data.entity.Reading;
import com.neosolusi.expresslingua.data.entity.ReadingInfo;
import com.neosolusi.expresslingua.features.BaseActivity;
import com.neosolusi.expresslingua.features.flashcard.MainAdapter.CardType;
import com.neosolusi.expresslingua.features.lesson.LessonViewModel;
import com.neosolusi.expresslingua.features.lesson.LessonViewModelFactory;
import com.neosolusi.expresslingua.features.tutorial.TutorialFlashcardMultipleActivity;

import java.util.Date;
import java.util.UUID;

public class FlashcardActivity extends BaseActivity
        implements MainFragment.OnFragmentInteractionListener, CardFragment.OnFragmentInteractionListener {

    public static final int SELECT_NONE = 0;
    public static final int SELECT_CARD = 1;
    public static final int SELECT_WORDS = 2;

    private AppServices mAppService;
    private LessonViewModel mViewModel;
    private Dialog mDialogSelection;
    private boolean mBound;

    private ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            AppServices.LocalBinder binder = (AppServices.LocalBinder) iBinder;
            mAppService = binder.getService();
            mBound = true;
            handleIntent(getIntent());
        }

        @Override public void onServiceDisconnected(ComponentName componentName) {
            mBound = false;
        }
    };

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_flashcard);

        configureLayout();

        Intent service = new Intent(this, AppServices.class);
        bindService(service, mServiceConnection, Context.BIND_AUTO_CREATE);

        LessonViewModelFactory factory = AppInjectors.provideLessonViewModelFactory(this);
        mViewModel = ViewModelProviders.of(this, factory).get(LessonViewModel.class);

        mDialogSelection = new Dialog(this);
        mDialogSelection.requestWindowFeature(Window.FEATURE_NO_TITLE);
    }

    @Override protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        handleIntent(intent);
    }

    @Override protected void onPause() {
        if (mBound) mAppService.releasePlayer();
        super.onPause();
    }

    @Override protected void onResume() {
        super.onResume();

        handleIntent(getIntent());
    }

    @Override protected void onDestroy() {
        unbindService(mServiceConnection);
        mBound = false;
        super.onDestroy();
    }

    @Override public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.animation_reverse, R.anim.animation2_reverse);
    }

//    @Override public boolean onCreateOptionsMenu(Menu menu) {
//        getMenuInflater().inflate(R.menu.lesson_menu, menu);
//        return true;
//    }
//
//    @Override public boolean onOptionsItemSelected(MenuItem item) {
//        switch (item.getItemId()) {
//            case android.R.id.home:
//                AppUtils.startActivity(this, MainActivity.class);
//                finish();
//                break;
//            default:
//                return super.onOptionsItemSelected(item);
//        }
//        return true;
//    }

    @Override public void onClick(CardType cardType) {
        new Handler().postDelayed(() -> loadFragment(cardType, 0), 100);
    }

    @Override public void onMasterLevelClick(CardType cardType, int level) {
        new Handler().postDelayed(() -> loadFragment(cardType, level), 100);
    }

    @Override public void onFinish() {
        finish();
    }

    @Override public void onSpeech(Flashcard flashcard, boolean isSlow) {
        Reading reading = mViewModel.findFirstReadingCopyEqualTo("id", flashcard.getReference());

        if (reading == null) return;

        ReadingInfo info = mViewModel.getCurrentLesson(reading.getFile_id());
        if (mBound && info != null) {
            mAppService.releasePlayer();
            mAppService.setPlayerInfo(info);
            mAppService.initializePlayer();
            mAppService.speech(reading, isSlow);
        }
    }

    @Override public boolean useTranslate() {
        return mBound && mAppService.useTranslate();
    }

    private void configureLayout() {
        configureToolbarLesson();
    }

    private void handleIntent(Intent intent) {
        if (Build.VERSION.SDK_INT >= 23) {
            CharSequence processText = intent.getCharSequenceExtra(Intent.EXTRA_PROCESS_TEXT);
            if (processText != null) {
                showAddFlashDialog(processText.toString());
                return;
            }
        }

        int level = intent.getIntExtra(CardFragment.ARG_LEVEL, 0);
        String cardType = intent.getStringExtra(CardFragment.ARG_TYPE);
        boolean ignoreDate = intent.getBooleanExtra(CardFragment.ARG_IGNORE_DATE, false);
        cardType = cardType == null ? "" : cardType;

        switch (cardType) {
            case "SINGLE_WORD":
                initialFragment(CardType.SINGLE_WORD, level, ignoreDate);
                break;
            case "MULTIPLE_WORDS":
                initialFragment(CardType.MULTIPLE_WORDS, level, ignoreDate);
                break;
            case "MULTIPLE_SENTENCES":
                initialFragment(CardType.MULTIPLE_SENTENCES, level, ignoreDate);
                break;
            default:
                initialFragment(null, 0, ignoreDate);
        }
    }

    private void initialFragment(CardType cardType, int level, boolean ignoreDate) {
        if (! mBound) return;

        Fragment fragment;

        if (cardType != null) {
            fragment = CardFragment.newInstance(cardType, "", level, ignoreDate);
        } else {
            fragment = MainFragment.newInstance();
        }

        getSupportFragmentManager()
                .beginTransaction()
                .add(R.id.content_fragment, fragment)
                .commit();
    }

    private void loadFragment(CardType cardType, int level) {
        Fragment previousFragment = getSupportFragmentManager().findFragmentById(R.id.content_fragment);
        Fragment nextFragment = CardFragment.newInstance(cardType, "", level, true);

        AppUtils.performFragmentTransition(previousFragment, nextFragment);

        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.content_fragment, nextFragment);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commitAllowingStateLoss();
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

        btnRed.setOnClickListener(v -> addOrUpdateFlashcardWord(null, text, 1));
        btnOrange.setOnClickListener(v -> addOrUpdateFlashcardWord(null, text, 2));
        btnYellow.setOnClickListener(v -> addOrUpdateFlashcardWord(null, text, 3));
        btnGreen.setOnClickListener(v -> addOrUpdateFlashcardWord(null, text, 4));

        mDialogSelection.show();
    }

    private void addOrUpdateFlashcardWord(Reading reading, String text, int level) {
        if (text.trim().equalsIgnoreCase("") || text.trim().isEmpty()) return;

        String type = AppUtils.isWord(text) ? "word" : "sentence";

        // Update flashcard
        // **********************************************************************************
        Flashcard card = mViewModel.findFirstFlashcardCopyEqualTo("card", AppUtils.normalizeString(text));
        if (card != null && card.getType().equalsIgnoreCase(type)) {

            if (reading != null && reading.getSelected() != 2) return;

//            card.setAlready_read(1);
            card.setMastering_level(level);
            card.setUploaded(false);
            card.setDatemodified(new Date());

            if (mDialogSelection.isShowing()) mDialogSelection.dismiss();

            // Commit changes
            mViewModel.updateFlashcard(card);

            return;
        }

        // Add new flashcard
        // **********************************************************************************
        long id = UUID.randomUUID().getMostSignificantBits() & Long.MAX_VALUE;
        Flashcard newCard = new Flashcard();
        newCard.setId(id);
        newCard.setCard(AppUtils.normalizeString(text));

        Reading readingCheck = mViewModel.findFirstReadingCopyEqualTo("sentence", AppUtils.normalizeString(text));
        if (readingCheck != null) {
            newCard.setTranslation(readingCheck.getTranslation());
//            newCard.setActor(readingCheck.getActor());
        } else {
            newCard.setTranslation("");
//            newCard.setActor("");
        }

//        newCard.setDefinition("");
        newCard.setUploaded(false);
        newCard.setSelected(0);
        newCard.setCategory("User");
        newCard.setMastering_level(level);
//        newCard.setAlready_read(1);
        newCard.setType(type);
        newCard.setDatecreated(new Date());
        newCard.setDatemodified(new Date());
        newCard.setRepeat(0);
        newCard.setState(SM2.State.NEW);
        newCard.setE_factor(2.5);
        newCard.setInterval(1);
        newCard.setNext_show(new Date());

        // Commit changes
        mViewModel.copyOrUpdateFlashcard(newCard);

        if (mDialogSelection.isShowing()) {
            showSnakeBar(mDialogSelection.findViewById(R.id.item_text_dialog_lesson_title), text + " was added to your flashcard");
            mDialogSelection.dismiss();
        }
    }

    private void showSnakeBar(View view, @NonNull String message) {
        Snackbar.make(view, message, Snackbar.LENGTH_LONG).show();
    }

}
