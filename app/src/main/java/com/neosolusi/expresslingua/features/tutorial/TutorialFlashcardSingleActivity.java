package com.neosolusi.expresslingua.features.tutorial;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.neosolusi.expresslingua.AppUtils;
import com.neosolusi.expresslingua.R;
import com.neosolusi.expresslingua.data.entity.User;
import com.neosolusi.expresslingua.features.login.LoginActivity;
import com.neosolusi.expresslingua.features.splash.SplashActivity;

import java.util.ArrayList;

import io.realm.Realm;

public class TutorialFlashcardSingleActivity extends AppCompatActivity {

    private ViewPager mViewPager;

    private int mCurrentPosition;
    private boolean isOpenFromHelp = false;
    private boolean isFinishTutorial = false;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tutorial_flashcard_single);

        SectionsPagerAdapter mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        ArrayList<ImageView> imageViews = new ArrayList<>();
        ImageView image1 = findViewById(R.id.img_dot_1);
        image1.setTag(0);
        imageViews.add(image1);

        ImageView image2 = findViewById(R.id.img_dot_2);
        image2.setTag(1);
        imageViews.add(image2);

        findViewById(R.id.button_skip).setOnClickListener(view -> {
            if (isOpenFromHelp) {
                finish();
            } else {
                isFinishTutorial = true;
                checkActiveUser();
            }
        });

        Button buttonNext = findViewById(R.id.button_next);
        buttonNext.setOnClickListener((view) -> {
            if (mCurrentPosition < imageViews.size() - 1) {
                mCurrentPosition++;
                mViewPager.setCurrentItem(mCurrentPosition);
            } else {
                if (isOpenFromHelp) {
                    finish();
                } else {
                    isFinishTutorial = true;
                    checkActiveUser();
                }
            }

            if (mCurrentPosition == imageViews.size() - 1) {
                buttonNext.setText("Finish");
            } else {
                buttonNext.setText("Next");
            }
        });

        mViewPager = findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                mCurrentPosition = position;
                switchImage(imageViews, position);
            }

            @Override public void onPageSelected(int position) {

            }

            @Override public void onPageScrollStateChanged(int state) {

            }
        });

        isOpenFromHelp = true;
        checkActiveUser();
    }

    private void checkActiveUser() {
        Realm mRealm = Realm.getDefaultInstance();

        if (!isOpenFromHelp) {
            if (mRealm.where(User.class).equalTo("active", true).findFirst() != null) {
                showSplashScreen();
                finish();
            } else {
                if (isFinishTutorial) {
                    this.showLogin();
                    finish();
                }
            }
        }
    }

    public void showLogin() {
        AppUtils.startActivity(this, LoginActivity.class);
    }

    public void showSplashScreen() {
        AppUtils.startActivity(this, SplashActivity.class);
    }

    private void switchImage(ArrayList<ImageView> imageViews, int position) {
        for (ImageView image : imageViews) {
            if ((Integer) image.getTag() == position) {
                image.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.shape_dot_big));
            } else {
                image.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.shape_dot_small));
            }
        }
    }

    public static class PlaceholderFragment extends Fragment {
        private static final String ARG_SECTION_NUMBER = "section_number";

        public PlaceholderFragment() {
            // Require empty constructor
        }

        public static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        @Override public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_tutorial, container, false);

            ImageView imageView = rootView.findViewById(R.id.image_tutorial);
            TextView textHeader = rootView.findViewById(R.id.text_header);
            TextView textDesc = rootView.findViewById(R.id.text_description);

            if (getArguments() == null || getContext() == null) return rootView;

            switch (getArguments().getInt(ARG_SECTION_NUMBER)) {
                case 1:
                    textHeader.setText("Gambar 6");
                    textDesc.setText(getText(R.string.tutorial_step_six));
                    imageView.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.tutorial_6));
                    break;
                default:
                    textHeader.setText("Gambar 7");
                    textDesc.setText(getText(R.string.tutorial_step_seven));
                    imageView.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.tutorial_7));
                    break;
            }

            return rootView;
        }
    }

    public class SectionsPagerAdapter extends FragmentPagerAdapter {
        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override public Fragment getItem(int position) {
            return PlaceholderFragment.newInstance(position + 1);
        }

        @Override public int getCount() {
            return 2;
        }
    }

}
