package com.neosolusi.expresslingua.features.splash;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.iid.FirebaseInstanceId;
import com.neosolusi.expresslingua.AppConstants;
import com.neosolusi.expresslingua.AppInjectors;
import com.neosolusi.expresslingua.AppServices;
import com.neosolusi.expresslingua.AppUtils;
import com.neosolusi.expresslingua.MainActivity;
import com.neosolusi.expresslingua.R;
import com.neosolusi.expresslingua.data.entity.User;
import com.neosolusi.expresslingua.data.network.NetworkDataSource;
import com.neosolusi.expresslingua.data.repo.UserRepository;
import com.neosolusi.expresslingua.features.BaseActivity;
import com.neosolusi.expresslingua.features.register.RegisterActivity;

public class SplashActivity extends BaseActivity {

    private static final String TAG = SplashActivity.class.getSimpleName();

    // Firebase & Google instance variables
    private FirebaseAuth mFirebaseAuth;

    private User mUser;
    private BroadcastReceiver mReceiver;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        mFirebaseAuth = FirebaseAuth.getInstance();

        mReceiver = new BroadcastReceiver() {
            @Override public void onReceive(Context context, Intent intent) {
                firebaseAuthWithEmail(mUser.getEmail(), AppConstants.DEFAULT_PASSWORD);
            }
        };

        UserRepository mUserRepo = AppInjectors.provideUserRepository(this);
        mUser = mUserRepo.findActiveUserCopy();
        if (mUser != null) {
            mUser.setManufacture(Build.MANUFACTURER + " " + Build.MODEL);
            mUser.setApi_version(String.valueOf(Build.VERSION.SDK_INT));
            mUser.setApp_version(AppUtils.versionName(this));
            mUserRepo.copyOrUpdate(mUser);
            mUserRepo.upload();

            checkService();
        } else {
            AppUtils.startActivity(SplashActivity.this, RegisterActivity.class);
            finish();
        }
    }

    private void checkService() {
        Intent serviceIntent = new Intent(this, AppServices.class);
        serviceIntent.setAction(AppServices.ACTION_SYNC);
        if (!AppUtils.isServiceRunning(AppServices.class, this)) {
            startService(serviceIntent);
        } else {
            showMainActivity();
        }
    }

    private void showMainActivity() {
        FirebaseInstanceId.getInstance().getInstanceId().addOnSuccessListener(this, instanceIdResult -> {
            String token = instanceIdResult.getToken();

            NetworkDataSource network = AppInjectors.provideNetworkDataSource(this);
            UserRepository userRepo = AppInjectors.provideUserRepository(this);

            Bundle bundle = new Bundle();
            bundle.putString("userid", userRepo.findActiveUser().getUserid());
            bundle.putString("token", token);
            network.startNetworkServiceWithExtra("upload_message_token", bundle);

            new Handler().postDelayed(() -> {
                AppUtils.startActivity(SplashActivity.this, MainActivity.class);
                SplashActivity.this.finish();
            }, 1000);
        });
    }

    private void createFirebaseUserWithEmailAndPassword(String email, String password) {
        mFirebaseAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(this, task -> {
            if (task.isSuccessful()) {
                // Sign in success, update UI with the signed-in user's information
                Log.d(TAG, "createUserWithEmail:success");
                FirebaseUser user = mFirebaseAuth.getCurrentUser();

                if (user != null) {
                    showMainActivity();
                    return;
                }

                showSimpleDialog("Koneksi gagal, silahkan coba lagi.");
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
                showMainActivity();
            }
        });
    }

    @Override protected void onPause() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mReceiver);
        super.onPause();
    }

    @Override protected void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(this).registerReceiver(mReceiver, new IntentFilter(AppConstants.BROADCAST_INITIAL_DATA_COMPLETE));
    }
}
