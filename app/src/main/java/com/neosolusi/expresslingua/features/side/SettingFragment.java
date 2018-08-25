package com.neosolusi.expresslingua.features.side;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;

import com.neosolusi.expresslingua.AppConstants;
import com.neosolusi.expresslingua.OnNavigationListener;
import com.neosolusi.expresslingua.R;

public class SettingFragment extends Fragment {

    public static final String TAG = SettingFragment.class.getSimpleName();

    private SharedPreferences.Editor mPreferencesEdit;
    private OnNavigationListener mListener;

    public SettingFragment() {
        // Required empty public constructor
    }

    public static SettingFragment getInstance() {
        return new SettingFragment();
    }

    @Override public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_setting, container, false);

        SharedPreferences mPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        mPreferencesEdit = mPreferences.edit();

        EditText editUrl = view.findViewById(R.id.edit_setting_url);
        EditText editMaxReviewCard = view.findViewById(R.id.edit_setting_max_review_card);
        EditText editMaxNewCard = view.findViewById(R.id.edit_setting_max_new_card);
        EditText editMaxFluencyCard = view.findViewById(R.id.edit_setting_max_fluency_card);
        EditText editMaxChallengeOrange = view.findViewById(R.id.edit_setting_max_challenge_orange);
        EditText editMinPassLesson = view.findViewById(R.id.edit_setting_min_pass_lesson);

        editUrl.setText(mPreferences.getString(AppConstants.PREFERENCE_SETTING_VALUES, ""));
        editMaxNewCard.setText(String.valueOf(mPreferences.getInt(AppConstants.PREFERENCE_MAX_DAILY_NEW_CARD, 0)));
        editMaxReviewCard.setText(String.valueOf(mPreferences.getInt(AppConstants.PREFERENCE_MAX_DAILY_REVIEW_CARD, 0)));
        editMaxFluencyCard.setText(String.valueOf(mPreferences.getInt(AppConstants.PREFERENCE_MAX_DAILY_FLUENCY_CARD, 0)));
        editMaxChallengeOrange.setText(String.valueOf(mPreferences.getInt(AppConstants.PREFERENCE_MAX_CHALLENGE_ORANGE, 10)));
        editMinPassLesson.setText(String.valueOf(mPreferences.getInt(AppConstants.PREFERENCE_MIN_PASS_LESSON, 60)));

        Button buttonSave = view.findViewById(R.id.button_save);
        buttonSave.setOnClickListener(v -> {
            String url = editUrl.getText().toString();
            int maxReviewCard = Integer.valueOf(editMaxReviewCard.getText().toString());
            int maxNewCard = Integer.valueOf(editMaxNewCard.getText().toString());
            int maxFluencyCard = Integer.valueOf(editMaxFluencyCard.getText().toString());
            int maxChallengeOrange = Integer.valueOf(editMaxChallengeOrange.getText().toString());
            int minPassLesson = Integer.valueOf(editMinPassLesson.getText().toString());

            if (!url.substring(0, 7).equals("http://")) {
                url = "http://" + url;
            }

            mPreferencesEdit.putString(AppConstants.PREFERENCE_SETTING_VALUES, url).apply();
            mPreferencesEdit.putInt(AppConstants.PREFERENCE_MAX_DAILY_REVIEW_CARD, maxReviewCard).apply();
            mPreferencesEdit.putInt(AppConstants.PREFERENCE_MAX_DAILY_NEW_CARD, maxNewCard).apply();
            mPreferencesEdit.putInt(AppConstants.PREFERENCE_MAX_DAILY_FLUENCY_CARD, maxFluencyCard).apply();
            mPreferencesEdit.putInt(AppConstants.PREFERENCE_MAX_CHALLENGE_ORANGE, maxChallengeOrange).apply();
            mPreferencesEdit.putInt(AppConstants.PREFERENCE_MIN_PASS_LESSON, minPassLesson).apply();

            editMaxReviewCard.onEditorAction(EditorInfo.IME_ACTION_DONE);
            Snackbar.make(v, "Saved", Snackbar.LENGTH_LONG).show();
        });

        return view;
    }

    @Override public void onAttach(Context context) {
        super.onAttach(context);

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

}
