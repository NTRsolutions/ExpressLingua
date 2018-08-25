package com.neosolusi.expresslingua.features.login;

import android.Manifest;
import android.arch.lifecycle.ViewModelProviders;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ProgressBar;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.iid.FirebaseInstanceId;
import com.neosolusi.expresslingua.AppConstants;
import com.neosolusi.expresslingua.AppInjectors;
import com.neosolusi.expresslingua.AppUtils;
import com.neosolusi.expresslingua.R;
import com.neosolusi.expresslingua.data.entity.User;
import com.neosolusi.expresslingua.data.network.NetworkDataSource;
import com.neosolusi.expresslingua.data.repo.UserRepository;
import com.neosolusi.expresslingua.features.BaseActivity;
import com.neosolusi.expresslingua.features.register.RegisterActivity;
import com.neosolusi.expresslingua.features.tutorial.TutorialActivity;

import io.realm.RealmResults;

public class LoginActivity extends BaseActivity implements View.OnClickListener {

    private static final String TAG = LoginActivity.class.getSimpleName();

    private EditText mTextUserid;
    private EditText mTextPassword;
    private LoginViewModel mViewModel;
    private boolean mHasUserActive;
    private boolean mJustSignin = false;

    // Firebase & Google instance variables
    private FirebaseAuth mFirebaseAuth;

    private BroadcastReceiver mBroadcastReceiver;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mFirebaseAuth = FirebaseAuth.getInstance();

        SharedPreferences.Editor mPrefEdit = AppInjectors.provideSharedPreferencesEditor(this);
        mPrefEdit.putInt(AppConstants.PREFERENCE_MAX_DAILY_REVIEW_CARD, 150).apply();
        mPrefEdit.putInt(AppConstants.PREFERENCE_MAX_DAILY_NEW_CARD, 30).apply();
        mPrefEdit.putInt(AppConstants.PREFERENCE_MAX_DAILY_FLUENCY_CARD, 25).apply();
        mPrefEdit.putInt(AppConstants.PREFERENCE_MAX_CHALLENGE_ORANGE, 5).apply();

        initComponent();

        LoginViewModelFactory factory = AppInjectors.provideLoginViewModelFactory(this);
        mViewModel = ViewModelProviders.of(this, factory).get(LoginViewModel.class);
        mViewModel.getUsers().observe(this, this::checkActiveUser);
    }

    @Override protected void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(this).registerReceiver(mBroadcastReceiver, new IntentFilter(AppConstants.BROADCAST_LOGIN_FAILED));
    }

    @Override protected void onPause() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mBroadcastReceiver);
        super.onPause();
    }

    @Override protected void onStop() {
        super.onStop();
    }

    @Override public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case 60:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    signIn();
                } else {
                    showLoading(false);

                    new AlertDialog.Builder(this).setTitle("Permissions").setIcon(R.mipmap.ic_launcher)
                            .setMessage("Aplikasi membutuhkan beberapa akses untuk dapat bekerja")
                            .setNegativeButton("Tutup", (dialog, which) -> dialog.dismiss())
                            .show();
                }
        }
    }

    private void initComponent() {
        mTextUserid = findViewById(R.id.edit_login_userid);
        mTextPassword = findViewById(R.id.edit_login_password);

        findViewById(R.id.button_login).setOnClickListener(this);
        findViewById(R.id.button_register).setOnClickListener(this);

        mBroadcastReceiver = new BroadcastReceiver() {
            @Override public void onReceive(Context context, Intent intent) {
                showErrorMessage(intent.getStringExtra(AppConstants.BROADCAST_MESSAGE));
                showLoading(false);
            }
        };
    }

    private void submit() {
        mJustSignin = true;

        AppUtils.dismissKeyboard(this, mTextUserid.getWindowToken());

        if (TextUtils.isEmpty(mTextUserid.getText())) {
            showErrorMessage(getString(R.string.error_empty_input_userid));
            return;
        } else if (TextUtils.isEmpty(mTextPassword.getText())) {
            showErrorMessage(getString(R.string.error_empty_input_password));
            return;
        }

        showLoading(true);
        requestPermissions();
    }

    private void requestPermissions() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_CONTACTS}, 60);
        } else {
            signIn();
        }
    }

    private void signIn() {
        mViewModel.login(mTextUserid.getText().toString(), mTextPassword.getText().toString());
    }

    private void showRegister() {
        AppUtils.startActivity(this, RegisterActivity.class);
        finish();
    }

    private void showTutorial() {
        FirebaseInstanceId.getInstance().getInstanceId().addOnSuccessListener(this, instanceIdResult -> {
            String token = instanceIdResult.getToken();

            NetworkDataSource network = AppInjectors.provideNetworkDataSource(this);
            UserRepository userRepo = AppInjectors.provideUserRepository(this);

            Bundle bundle = new Bundle();
            bundle.putString("userid", userRepo.findActiveUser().getUserid());
            bundle.putString("token", token);
            network.startNetworkServiceWithExtra("upload_message_token", bundle);

            new Handler().postDelayed(() -> {
                Bundle extra = new Bundle();
                extra.putBoolean("JUST_SIGNIN", mJustSignin);
                AppUtils.startActivityWithExtra(this, TutorialActivity.class, extra);
                LoginActivity.this.finish();
            }, 1000);
        });
    }

    private void checkActiveUser(RealmResults<User> users) {
        if (users.isEmpty()) return;

        for (User user : users) {
            if (user.isActive()) {
                mViewModel.getUsers().removeObserver(this::checkActiveUser);
                mHasUserActive = true;
                firebaseAuthWithEmail(user.getEmail(), AppConstants.DEFAULT_PASSWORD);
            }
        }
    }

    private void showLoading(boolean show) {
        ProgressBar progressBar = findViewById(R.id.progressbar);

        if (show) {
            View view = this.getCurrentFocus();
            if (view != null) {
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                if (imm != null) imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            }
            progressBar.setVisibility(View.VISIBLE);
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        } else {
            progressBar.setVisibility(View.GONE);
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        }
    }

    private void showErrorMessage(String message) {
        new AlertDialog.Builder(this)
                .setTitle("Kesalahan")
                .setMessage(message)
                .setIcon(ContextCompat.getDrawable(this, R.drawable.ic_error_outline_red_700_24dp))
                .show();
    }

    private void createFirebaseUserWithEmailAndPassword(String email, String password) {
        mFirebaseAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(this, task -> {
            if (task.isSuccessful()) {
                // Sign in success, update UI with the signed-in user's information
                Log.d(TAG, "createUserWithEmail:success");
                FirebaseUser user = mFirebaseAuth.getCurrentUser();

                if (user != null) {
                    showTutorial();
                    return;
                }

                showSimpleDialog("Login failed, try again letter");
            } else {
                // If sign in fails, display a message to the user.
                Log.w(TAG, "createUserWithEmail:failure", task.getException());
                showSimpleDialog("Authentication failed");
            }
        });
    }

    private void firebaseAuthWithEmail(String email, String password) {
        mFirebaseAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(this, task -> {
            Log.d(TAG, "signInWithEmail:onComplete:" + task.isSuccessful());
            if (!task.isSuccessful()) {
                if (task.getException() instanceof FirebaseAuthInvalidUserException) {
                    createFirebaseUserWithEmailAndPassword(email, password);
                    return;
                }

                Log.w(TAG, "signInWithEmail", task.getException());
                showSimpleDialog("Authentication failed");
            } else {
                showTutorial();
            }
        });
    }

    @Override public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button_login:
                submit();
                break;
            case R.id.button_register:
                showRegister();
                break;
        }
    }
}
