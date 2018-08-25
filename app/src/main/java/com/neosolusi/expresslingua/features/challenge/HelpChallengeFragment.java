package com.neosolusi.expresslingua.features.challenge;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.neosolusi.expresslingua.R;

public class HelpChallengeFragment extends Fragment {

    public static final String ARG_TITLE = "title";
    public static final String ARG_DESCRIPTION = "description";
    public static final String ARG_IMAGE = "image";

    private Context mContext;
    private String mTitle, mDescription;
    private int mImage;

    public HelpChallengeFragment() {
        // Required empty public constructor
    }

    public static HelpChallengeFragment newInstance(String title, String description, int image) {
        HelpChallengeFragment fragment = new HelpChallengeFragment();
        Bundle args = new Bundle();
        args.putString(ARG_TITLE, title);
        args.putString(ARG_DESCRIPTION, description);
        args.putInt(ARG_IMAGE, image);
        fragment.setArguments(args);
        return fragment;
    }

    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = getContext();
        if (getArguments() != null) {
            mTitle = getArguments().getString(ARG_TITLE);
            mDescription = getArguments().getString(ARG_DESCRIPTION);
            mImage = getArguments().getInt(ARG_IMAGE);
        }
    }

    @Override public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_help_challenge, container, false);

        TextView textHeader = view.findViewById(R.id.text_header);
        TextView textDesc = view.findViewById(R.id.text_description);
        ImageView imageView = view.findViewById(R.id.image_tutorial);

        textHeader.setText(mTitle);
        textDesc.setText(mDescription);
        imageView.setImageDrawable(ContextCompat.getDrawable(mContext, mImage));

        return view;
    }

}
