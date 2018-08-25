package com.neosolusi.expresslingua.features.register;

import android.Manifest;
import android.arch.lifecycle.ViewModelProviders;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Patterns;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;

import com.neosolusi.expresslingua.AppConstants;
import com.neosolusi.expresslingua.AppInjectors;
import com.neosolusi.expresslingua.AppUtils;
import com.neosolusi.expresslingua.R;
import com.neosolusi.expresslingua.data.entity.User;
import com.neosolusi.expresslingua.features.login.LoginActivity;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.realm.RealmResults;

public class RegisterActivity extends AppCompatActivity {

    private EditText mEditUserId;
    private EditText mEditEmail;
    private EditText mEditPassword;
    private EditText mEditPhone;
    private EditText mEditAddress;
    private EditText mEditCountry;
    private Spinner mSpinnerCity;
    private Spinner mSpinnerProvince;
    private RegisterViewModel mViewModel;

    private BroadcastReceiver mBroadcastReceiver;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        initComponent();
        initListener();
        populateCity();
        populateProvinces();

        RegisterViewModelFactory factory = AppInjectors.provideRegisterViewModelFactory(this);
        mViewModel = ViewModelProviders.of(this, factory).get(RegisterViewModel.class);
        mViewModel.getUsers().observe(this, this::checkActiveUser);
    }

    @Override protected void onResume() {
        LocalBroadcastManager.getInstance(this).registerReceiver(mBroadcastReceiver, new IntentFilter(AppConstants.BROADCAST_REGISTER_FAILED));
        super.onResume();
    }

    @Override protected void onPause() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mBroadcastReceiver);
        super.onPause();
    }

    @Override public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.animation_reverse, R.anim.animation2_reverse);
        finish();
    }

    @Override public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case 60:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    submit(findViewById(R.id.button_submit));
                } else {
                    new AlertDialog.Builder(this).setTitle("Permissions").setIcon(R.mipmap.ic_launcher)
                            .setMessage("Aplikasi membutuhkan beberapa akses untuk dapat bekerja")
                            .setNegativeButton("Tutup", (dialog, which) -> dialog.dismiss())
                            .show();
                }
        }
    }

    private void requestPermissions(View view) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_CONTACTS}, 60);
        } else {
            submit(view);
        }
    }

    private void initComponent() {
        mSpinnerCity = findViewById(R.id.spinner_register_city);
        mSpinnerProvince = findViewById(R.id.spinner_register_province);
        mEditUserId = findViewById(R.id.edit_register_userid);
        mEditEmail = findViewById(R.id.edit_register_email);
        mEditPassword = findViewById(R.id.edit_register_password);
        mEditPhone = findViewById(R.id.edit_register_phone);
        mEditAddress = findViewById(R.id.edit_register_address);
        mEditCountry = findViewById(R.id.edit_register_country);

        mBroadcastReceiver = new BroadcastReceiver() {
            @Override public void onReceive(Context context, Intent intent) {
                showLoading(false);
                showErrorMessage(intent.getStringExtra(AppConstants.BROADCAST_MESSAGE));
                findViewById(R.id.progressbar).setVisibility(View.GONE);
            }
        };
    }

    private void initListener() {
        findViewById(R.id.text_register_back_to_login).setOnClickListener(v -> {
            AppUtils.startActivity(RegisterActivity.this, LoginActivity.class);
            RegisterActivity.this.finish();
        });
        findViewById(R.id.button_submit).setOnClickListener(this::requestPermissions);
    }

    private void populateCity() {
        List<String> cities = new ArrayList<>();
        cities.add("DKI Jakarta");

        ArrayAdapter<String> mSpinnerCityAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, cities);
        mSpinnerCityAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSpinnerCity.setAdapter(mSpinnerCityAdapter);

        mSpinnerCity.post(() -> mSpinnerCity.setSelection(0));
    }

    private void populateProvinces() {
        List<String> provinces = new ArrayList<>();
        provinces.add("Jakarta Barat");
        provinces.add("Jakarta Pusat");
        provinces.add("Jakarta Timur");
        provinces.add("Jakarta Selatan");
        provinces.add("Jakarta Utara");
        provinces.add("Kepulauan Seribu");
        provinces.add("-");

        ArrayAdapter<String> mSpinnerProvinceAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, provinces);
        mSpinnerProvinceAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSpinnerProvince.setAdapter(mSpinnerProvinceAdapter);

        mSpinnerProvince.post(() -> mSpinnerProvince.setSelection(0));
    }

    private void submit(View view) {
        if (view == null) return;

        AppUtils.dismissKeyboard(this, mEditCountry.getWindowToken());

        if (mEditPhone.getText().toString().trim().isEmpty()) {
            showErrorMessage("Phone harus diisi");
            return;
        } else if (mEditPhone.getText().toString().length() < 11) {
            showErrorMessage("Phone tidak valid");
            return;
        } else if (AppUtils.isDigitOnly(AppUtils.normalizePhone(mEditPhone.getText().toString()))) {
            showErrorMessage("Phone tidak valid");
            return;
        }

        Pattern pattern = Pattern.compile("\\W", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(mEditUserId.getText().toString());
        if (matcher.find()) {
            showErrorMessage("Username hanya boleh huruf dan angka");
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(mEditEmail.getText().toString()).find()) {
            showErrorMessage("Email tidak valid");
            return;
        }

        showLoading(true);

        User newUser = new User();
        newUser.setId(UUID.randomUUID().getMostSignificantBits() & Long.MAX_VALUE);
        newUser.setUserid(mEditUserId.getText().toString());
        newUser.setEmail(mEditEmail.getText().toString());
        newUser.setPassword(mEditPassword.getText().toString());
        newUser.setCommercial_status(0);
        newUser.setCell_no(mEditPhone.getText().toString());
        newUser.setAddress(mEditAddress.getText().toString());
        newUser.setCity(mSpinnerCity.getSelectedItem().toString());
        newUser.setProvince(mSpinnerProvince.getSelectedItem().toString());
        newUser.setCountry(mEditCountry.getText().toString());
        newUser.setGps_lat(0);
        newUser.setGps_lng(0);

        mViewModel.register(newUser);
    }

    private void checkActiveUser(RealmResults<User> users) {
        showLoading(false);

        if (users.isEmpty()) return;

        AppUtils.startActivity(this, LoginActivity.class);
        finish();
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
}
