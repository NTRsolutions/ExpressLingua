package com.neosolusi.expresslingua.features.side;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.neosolusi.expresslingua.OnNavigationListener;
import com.neosolusi.expresslingua.R;
import com.neosolusi.expresslingua.features.tutorial.TutorialActivity;

public class HelpFragment extends Fragment {

    public static final String TAG = HelpFragment.class.getSimpleName();

    private OnNavigationListener mListener;

    public HelpFragment() {
        // Required empty public constructor
    }

    public static HelpFragment getInstance() {
        return new HelpFragment();
    }

    @Override public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_help, container, false);

        Context context = getContext();

        rootView.findViewById(R.id.button_help_home).setOnClickListener(view -> new Handler().postDelayed(() -> {
            if (mListener != null) mListener.onShowHome();
        }, 250));

        rootView.findViewById(R.id.button_help_tutorial).setOnClickListener(view -> {
            Intent intent = new Intent(context, TutorialActivity.class);
            intent.putExtra("from_help", true);
            startActivity(intent);
        });

        rootView.findViewById(R.id.button_help_download).setOnClickListener(view -> {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://expresslingua.com/panduan.php"));
            startActivity(intent);
        });

        rootView.findViewById(R.id.button_help_video).setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://youtu.be/FgThuMEx6vI"));
            startActivity(intent);
        });

        return rootView;
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
