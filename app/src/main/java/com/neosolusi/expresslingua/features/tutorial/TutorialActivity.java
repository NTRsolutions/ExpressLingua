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
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.neosolusi.expresslingua.AppUtils;
import com.neosolusi.expresslingua.R;
import com.neosolusi.expresslingua.features.login.LoginActivity;
import com.neosolusi.expresslingua.features.register.RegisterActivity;
import com.neosolusi.expresslingua.features.splash.SplashActivity;

import java.util.ArrayList;

import io.realm.Realm;

public class TutorialActivity extends AppCompatActivity {

    private ViewPager mViewPager;

    private int mCurrentPosition;
    private boolean isOpenFromHelp = false;
    private boolean isFinishTutorial = false;
    private boolean mJustSignin = false;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tutorial);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        if (getIntent().getBundleExtra("extra") != null) {
            mJustSignin = getIntent().getBundleExtra("extra").getBoolean("JUST_SIGNIN");
        }

        SectionsPagerAdapter mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        ArrayList<ImageView> imageViews = new ArrayList<>();
        ImageView image1 = findViewById(R.id.img_dot_1);
        image1.setTag(0);
        imageViews.add(image1);

        ImageView image2 = findViewById(R.id.img_dot_2);
        image2.setTag(1);
        imageViews.add(image2);

        ImageView image3 = findViewById(R.id.img_dot_3);
        image3.setTag(2);
        imageViews.add(image3);

        ImageView image4 = findViewById(R.id.img_dot_4);
        image4.setTag(3);
        imageViews.add(image4);

        ImageView image5 = findViewById(R.id.img_dot_5);
        image5.setTag(4);
        imageViews.add(image5);

        ImageView image6 = findViewById(R.id.img_dot_6);
        image6.setTag(5);
        imageViews.add(image6);

        ImageView image7 = findViewById(R.id.img_dot_7);
        image7.setTag(6);
        imageViews.add(image7);

        ImageView image8 = findViewById(R.id.img_dot_8);
        image8.setTag(7);
        imageViews.add(image8);

        ImageView image9 = findViewById(R.id.img_dot_9);
        image9.setTag(8);
        imageViews.add(image9);

        ImageView image10 = findViewById(R.id.img_dot_10);
        image10.setTag(9);
        imageViews.add(image10);

        ImageView image11 = findViewById(R.id.img_dot_11);
        image11.setTag(10);
        imageViews.add(image11);

        ImageView image12 = findViewById(R.id.img_dot_12);
        image12.setTag(11);
        imageViews.add(image12);

        ImageView image13 = findViewById(R.id.img_dot_13);
        image13.setTag(12);
        imageViews.add(image13);

        ImageView image14 = findViewById(R.id.img_dot_14);
        image14.setTag(13);
        imageViews.add(image14);

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

        // Set up the ViewPager with the sections adapter.
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

        isOpenFromHelp = getIntent() != null && getIntent().getBooleanExtra("from_help", false);
        checkActiveUser();
    }

    @Override public void onBackPressed() {
        if (isOpenFromHelp) finish();
    }

    @Override public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.tutorial_menu, menu);
        return true;
    }

    @Override public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void checkActiveUser() {
        if (!isOpenFromHelp) {
            if (!mJustSignin) {
                showSplashScreen();
                finish();
            } else {
                if (isFinishTutorial) {
                    showSplashScreen();
                    finish();
                }
            }
        }
    }

    public void showLogin() {
        AppUtils.startActivity(this, LoginActivity.class);
    }

    public void showRegister() {
        AppUtils.startActivity(this, RegisterActivity.class);
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
                    textHeader.setText("Gambar 1");
                    textDesc.setText(getText(R.string.tutorial_step_one));
                    imageView.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.tutorial_1));
                    break;
                case 2:
                    textHeader.setText("Gambar 2");
                    textDesc.setText(getText(R.string.tutorial_step_two));
                    imageView.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.tutorial_2));
                    break;
                case 3:
                    textHeader.setText("Gambar 3");
                    textDesc.setText(getText(R.string.tutorial_step_three));
                    imageView.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.tutorial_3));
                    break;
                case 4:
                    textHeader.setText("Gambar 4");
                    textDesc.setText(getText(R.string.tutorial_step_four));
                    imageView.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.tutorial_4));
                    break;
                case 5:
                    textHeader.setText("Gambar 5");
                    textDesc.setText(getText(R.string.tutorial_step_five));
                    imageView.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.tutorial_5));
                    break;
                case 6:
                    textHeader.setText("Gambar 6");
                    textDesc.setText(getText(R.string.tutorial_step_six));
                    imageView.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.tutorial_6));
                    break;
                case 7:
                    textHeader.setText("Gambar 7");
                    textDesc.setText(getText(R.string.tutorial_step_seven));
                    imageView.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.tutorial_7));
                    break;
                case 8:
                    textHeader.setText("Gambar 8");
                    textDesc.setText(getText(R.string.tutorial_step_eight));
                    imageView.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.tutorial_8));
                    break;
                case 9:
                    textHeader.setText("Gambar 9");
                    textDesc.setText(getText(R.string.tutorial_step_nine));
                    imageView.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.tutorial_9));
                    break;
                case 10:
                    textHeader.setText("Gambar 10");
                    textDesc.setText(getText(R.string.tutorial_step_ten));
                    imageView.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.tutorial_10));
                    break;
                case 11:
                    textHeader.setText("Gambar 11");
                    textDesc.setText("");
                    imageView.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.tutorial_11));
                    break;
                case 12:
                    textHeader.setText("Gambar 12");
                    textDesc.setText(getText(R.string.tutorial_step_twelve));
                    imageView.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.tutorial_12));
                    break;
                case 13:
                    textHeader.setText("Gambar 13");
                    textDesc.setText("");
                    imageView.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.tutorial_13));
                    break;
                default:
                    textHeader.setText("Gambar 14");
                    textDesc.setText(getText(R.string.tutorial_step_last));
                    imageView.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.tutorial_14));
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
            return 14;
        }
    }
}
