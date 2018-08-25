package com.neosolusi.expresslingua;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.neosolusi.expresslingua.data.entity.Dictionary;
import com.neosolusi.expresslingua.data.entity.Flashcard;
import com.neosolusi.expresslingua.data.entity.User;
import com.neosolusi.expresslingua.data.repo.DictionaryRepository;
import com.neosolusi.expresslingua.data.repo.FlashcardRepository;
import com.neosolusi.expresslingua.data.repo.UserRepository;
import com.neosolusi.expresslingua.features.BaseActivity;
import com.neosolusi.expresslingua.features.group.listcontact.ListContactActivity;
import com.neosolusi.expresslingua.features.group.listgroup.ListGroupActivity;
import com.neosolusi.expresslingua.features.home.HomeFragment;
import com.neosolusi.expresslingua.features.side.AccountFragment;
import com.neosolusi.expresslingua.features.side.HelpFragment;
import com.neosolusi.expresslingua.features.side.SettingFragment;
import com.neosolusi.expresslingua.util.ExampleGenerator;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import jxl.Workbook;
import jxl.WorkbookSettings;
import jxl.write.Label;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;
import jxl.write.biff.RowsExceededException;

public class MainActivity extends BaseActivity
        implements NavigationView.OnNavigationItemSelectedListener, OnNavigationListener {

    private Toolbar mToolbar;
    private DrawerLayout mDrawer;
    private NavigationView mNavigationView;
    private BroadcastReceiver mBroadcastReceiver;
    private UserRepository mUserRepo;

    private int mNavItemIndex;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mUserRepo = AppInjectors.provideUserRepository(this);

        initComponent();
        configureLayout();
        initialFragment();
        checkService();
    }

    @Override protected void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(this).registerReceiver(mBroadcastReceiver, new IntentFilter(AppConstants.BROADCAST_SERVICE_CONFIG));
        if (isAdmin()) requestPermissions();
    }

    @Override protected void onPause() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mBroadcastReceiver);
        super.onPause();
    }

    @Override public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int lastIndex = mNavItemIndex;

        switch (item.getItemId()) {
            case R.id.nav_home:
                mNavItemIndex = 0;
                break;
            case R.id.nav_profile:
                mNavItemIndex = 1;
                break;
            case R.id.nav_setting:
                mNavItemIndex = 2;
                break;
            case R.id.nav_help:
                mNavItemIndex = 3;
                break;
            case R.id.nav_group:
                mNavItemIndex = 4;
                break;
            case R.id.nav_new_group:
                mNavItemIndex = 5;
                break;
            case R.id.nav_export:
                mNavItemIndex = 6;
                break;
            case R.id.nav_backup:
                mNavItemIndex = 7;
                break;
            case R.id.nav_exit:
                mNavItemIndex = 8;
                break;
            default:
                mNavItemIndex = 0;
                break;
        }

        if (lastIndex == mNavItemIndex) return false;

        loadFragment();

        return true;
    }

    @Override public void onBackPressed() {
        if (mDrawer.isDrawerOpen(GravityCompat.START)) {
            mDrawer.closeDrawer(GravityCompat.START);
        } else {
            if (mNavItemIndex > 0) {
                mNavItemIndex = 0;
                loadFragment();
            } else {
                quitApps();
            }
        }
    }

    @Override public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case R.id.action_settings:
                if (mNavItemIndex != 2) {
                    mNavItemIndex = 2;
                    loadFragment();
                }
                break;
            case android.R.id.home:
                showPlayStoreDialog();
                break;
            case R.id.create_group:
                AppUtils.startActivity(this, ListGroupActivity.class);
                break;
        }

        return true;
    }

    @Override public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case 50:
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED
                        && grantResults[1] == PackageManager.PERMISSION_GRANTED
                        && grantResults[2] == PackageManager.PERMISSION_GRANTED
                        && grantResults[3] == PackageManager.PERMISSION_GRANTED
                        && grantResults[4] == PackageManager.PERMISSION_GRANTED
                        ) {
                    Log.d("Permission", "Granted");
                } else {
                    new AlertDialog.Builder(this).setTitle("Permissions").setIcon(R.mipmap.ic_launcher)
                            .setMessage("Aplikasi membutuhkan beberapa akses untuk dapat bekerja")
                            .setNegativeButton("Tutup", (dialog, which) -> dialog.dismiss())
                            .show();
                }
        }
    }

    @Override protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void initComponent() {
        mToolbar = findViewById(R.id.toolbar);
        mDrawer = findViewById(R.id.drawer_layout);
        mNavigationView = findViewById(R.id.nav_view);

        User user = mUserRepo.findActiveUserCopy();
        if (user == null || !isAdmin()) {
            mNavigationView.getMenu().findItem(R.id.nav_admin).setVisible(false);
        }

        mBroadcastReceiver = new BroadcastReceiver() {
            @Override public void onReceive(Context context, Intent intent) {
                String current_version = intent.getStringExtra(AppConstants.BROADCAST_MESSAGE);
                String app_version = AppUtils.versionName(MainActivity.this);

                if (app_version == null || app_version.isEmpty()) return;

                if (Double.valueOf(app_version) < Double.valueOf(current_version)) {
                    showPlayStoreDialog();
                }
            }
        };
    }

    private void configureLayout() {
        setSupportActionBar(mToolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_menu);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, mDrawer, mToolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        toggle.setDrawerIndicatorEnabled(true);
        mDrawer.addDrawerListener(toggle);
        toggle.syncState();

        mNavigationView.setNavigationItemSelectedListener(this);
        ((TextView) findViewById(R.id.apps_version)).setText(String.format(Locale.getDefault(), "ver.%s", AppUtils.versionName(this)));
    }

    private void requestPermissions() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.INTERNET) != PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_NETWORK_STATE) != PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.INTERNET,
                    Manifest.permission.READ_CONTACTS,
                    Manifest.permission.ACCESS_NETWORK_STATE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_EXTERNAL_STORAGE
            }, 50);
        }
    }

    private void initialFragment() {
        Fragment fragment;

        fragment = HomeFragment.getInstance();

        mNavigationView.getMenu().getItem(mNavItemIndex).setChecked(true);

        getSupportFragmentManager()
                .beginTransaction()
                .add(R.id.content_fragment, fragment, HomeFragment.TAG)
                .commit();
    }

    private void loadFragment() {
        mNavigationView.getMenu().getItem(mNavItemIndex).setChecked(true);

        new Handler().postDelayed(() -> {
            Fragment previousFragment = getSupportFragmentManager().findFragmentById(R.id.content_fragment);
            Fragment nextFragment = null;
            String tag = null;

            switch (mNavItemIndex) {
                case 1:
                    nextFragment = AccountFragment.getInstance();
                    tag = AccountFragment.TAG;
                    break;
                case 2:
                    nextFragment = SettingFragment.getInstance();
                    tag = SettingFragment.TAG;
                    break;
                case 3:
                    nextFragment = HelpFragment.getInstance();
                    tag = HelpFragment.TAG;
                    break;
                case 4:
                    AppUtils.startActivity(this, ListGroupActivity.class);
                    break;
                case 5:
                    AppUtils.startActivity(this, ListContactActivity.class);
                    break;
                case 6:
                    exportToExcel();
                    return;
                case 7:
                    backupDatabase();
                    return;
                case 8:
                    nextFragment = null;
                    quitApps();
                    break;
                default:
                    nextFragment = HomeFragment.getInstance();
                    tag = HomeFragment.TAG;
            }

            if (nextFragment == null) return;

            if (mNavItemIndex == 0) {
                AppUtils.performFragmentTransition(nextFragment, previousFragment);
            } else {
                AppUtils.performFragmentTransition(previousFragment, nextFragment);
            }

            FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
            fragmentTransaction.replace(R.id.content_fragment, nextFragment, tag);
            fragmentTransaction.commitAllowingStateLoss();
        }, 250);

        mDrawer.closeDrawer(GravityCompat.START);
    }

    private void quitApps() {
        new AlertDialog.Builder(this).setTitle("Keluar").setIcon(R.mipmap.ic_launcher_ealing)
                .setMessage("Anda yakin keluar dari aplikasi?")
                .setNegativeButton("Tidak", null)
                .setPositiveButton("Ya", (dialog, which) -> finishAffinity())
                .show();
    }

    private void showPlayStoreDialog() {
        new AlertDialog.Builder(this).setTitle("Update").setIcon(R.drawable.playstore)
                .setMessage("Tersedia versi baru untuk anda, silahkan update")
                .setNegativeButton("Nanti", (dialog, which) -> dialog.dismiss())  // hanya sementara, nanti ganti finish()
                .setPositiveButton("Ya", (dialog, which) -> {
                    String appPackageName = getPackageName();
                    try {
                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
                    } catch (android.content.ActivityNotFoundException e) {
                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)));
                    }
                    finish();
                })
                .show();
    }

    private void checkService() {
        Intent serviceIntent = new Intent(this, AppServices.class);
        serviceIntent.setAction(AppServices.ACTION_SYNC);
        if (!AppUtils.isServiceRunning(AppServices.class, this)) {
            startService(serviceIntent);
        }
    }

    private void exportDictionary() {
        new Handler().postDelayed(() -> {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                return;
            }

            String fileName = "Dictionary.xls";
            File sdCard = Environment.getExternalStorageDirectory();
            File directory = new File(sdCard.getAbsolutePath() + "/ExpressLingua");
            File file = new File(directory, fileName);
            if (!directory.isDirectory() && !directory.mkdir()) return;
            if (file.exists() && !file.delete()) return;

            WorkbookSettings wbSettings = new WorkbookSettings();
            wbSettings.setLocale(new Locale("en", "EN"));
            WritableWorkbook workbook;

            try {
                workbook = Workbook.createWorkbook(file, wbSettings);
                WritableSheet sheet = workbook.createSheet("Dictionary", 0);

                try {
                    sheet.addCell(new Label(0, 0, "Word"));
                    sheet.addCell(new Label(1, 0, "Translation"));
                    sheet.addCell(new Label(2, 0, "Examples"));
                    sheet.addCell(new Label(3, 0, "Examples Translate"));
                    sheet.addCell(new Label(4, 0, "Exists"));

                    DictionaryRepository dictionaryRepo = AppInjectors.provideDictionaryRepository(this);
                    int i = 1;
                    for (Dictionary dictionary : dictionaryRepo.findAll()) {
                        List<HashMap<String, String>> listSample = ExampleGenerator.getSamples(dictionary);

                        if (listSample.size() >= 1) {
                            sheet.addCell(new Label(0, i, listSample.get(0).get("word")));
                            sheet.addCell(new Label(1, i, listSample.get(0).get("translation")));
                            sheet.addCell(new Label(2, i, listSample.get(0).get("samples")));
                            sheet.addCell(new Label(3, i, listSample.get(0).get("sample_translation")));
                            sheet.addCell(new Label(4, i, listSample.get(0).get("exists")));
                        }

                        if (listSample.size() >= 2) {
                            i++;
                            sheet.addCell(new Label(2, i, listSample.get(1).get("samples")));
                            sheet.addCell(new Label(3, i, listSample.get(1).get("sample_translation")));
                            sheet.addCell(new Label(4, i, listSample.get(1).get("exists")));
                        }

                        if (listSample.size() >= 3) {
                            i++;
                            sheet.addCell(new Label(2, i, listSample.get(2).get("samples")));
                            sheet.addCell(new Label(3, i, listSample.get(2).get("sample_translation")));
                            sheet.addCell(new Label(4, i, listSample.get(2).get("exists")));
                        }

                        i += 2;
                    }
                } catch (RowsExceededException e) {
                    e.printStackTrace();
                } catch (WriteException e) {
                    e.printStackTrace();
                }

                workbook.write();
                try {
                    workbook.close();
                } catch (WriteException e) {
                    e.printStackTrace();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            new AlertDialog.Builder(this).setTitle("Export").setIcon(R.mipmap.ic_launcher)
                    .setMessage("Export Dictionary selesai, lokasi sdcard/ExpressLingua/Dictionary.xls")
                    .setPositiveButton("Tutup", (dialog, which) -> dialog.dismiss())
                    .show();

        }, 250);
    }

    private void exportFlashcard() {
        new Handler().postDelayed(() -> {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                return;
            }

            String fileName = "Flashcard.xls";
            File sdCard = Environment.getExternalStorageDirectory();
            File directory = new File(sdCard.getAbsolutePath() + "/ExpressLingua");
            File file = new File(directory, fileName);
            if (!directory.isDirectory() && !directory.mkdir()) return;
            if (file.exists() && !file.delete()) return;

            WorkbookSettings wbSettings = new WorkbookSettings();
            wbSettings.setLocale(new Locale("en", "EN"));
            WritableWorkbook workbook;

            try {
                workbook = Workbook.createWorkbook(file, wbSettings);
                WritableSheet sheet = workbook.createSheet("Flashcard", 0);

                try {
                    sheet.addCell(new Label(0, 0, "Card"));
                    sheet.addCell(new Label(1, 0, "Translation"));
                    sheet.addCell(new Label(2, 0, "MasteringLevel"));
                    sheet.addCell(new Label(3, 0, "Category"));

                    FlashcardRepository flashcardRepo = AppInjectors.provideFlashcardRepository(this);
                    int i = 1;
                    for (Flashcard flashcard : flashcardRepo.findAll()) {
                        sheet.addCell(new Label(0, i, flashcard.getCard()));
                        sheet.addCell(new Label(1, i, flashcard.getTranslation()));
                        sheet.addCell(new Label(2, i, "" + flashcard.getMastering_level()));
                        sheet.addCell(new Label(3, i, flashcard.getCategory()));
                        i++;
                    }
                } catch (RowsExceededException e) {
                    e.printStackTrace();
                } catch (WriteException e) {
                    e.printStackTrace();
                }

                workbook.write();
                try {
                    workbook.close();
                } catch (WriteException e) {
                    e.printStackTrace();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            new AlertDialog.Builder(this).setTitle("Export").setIcon(R.mipmap.ic_launcher)
                    .setMessage("Export Flashcard selesai, lokasi sdcard/ExpressLingua/Flashcard.xls")
                    .setPositiveButton("Tutup", (dialog, which) -> dialog.dismiss())
                    .show();

        }, 250);
    }

    private void exportToExcel() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Pilih data untuk exports");
        builder.setIcon(R.mipmap.ic_launcher_ealing);
        builder.setItems(new CharSequence[]{"Dictionary", "Flashcard"}, (dialog, position) -> {
            switch (position) {
                case 0:
                    exportDictionary();
                    break;
                case 1:
                    exportFlashcard();
                    break;
            }
        });
        builder.setNegativeButton("Tutup", (dialog, position) -> dialog.dismiss());
        builder.create().show();

        mDrawer.closeDrawer(GravityCompat.START);
    }

    private void backupDatabase() {
        new Handler().postDelayed(() -> {
            if (!isExternalStorageWritable()) return;

            File dbFile = new File(getFilesDir(), "expresslingua.realm");

            String fileName = "backup.realm";
            File sdCard = Environment.getExternalStorageDirectory();
            File directory = new File(sdCard.getAbsolutePath() + "/ExpressLingua");
            File file = new File(directory, fileName);
            if (!directory.isDirectory() && !directory.mkdir()) return;
            if (file.exists() && !file.delete()) return;

            FileOutputStream outputStream;

            try {
                BufferedInputStream bis = new BufferedInputStream(new FileInputStream(dbFile));
                DataInputStream dis = new DataInputStream(bis);

                outputStream = new FileOutputStream(file);

                byte[] buf = new byte[1024];
                int len;
                while ((len = dis.read(buf)) > 0) {
                    outputStream.write(buf, 0, len);
                }

                outputStream.flush();
                outputStream.close();
                dis.close();
                bis.close();

                new AlertDialog.Builder(this).setTitle("Backup").setIcon(R.mipmap.ic_launcher)
                        .setMessage("Backup database selesai, lokasi sdcard/ExpressLingua/backup.realm")
                        .setPositiveButton("Tutup", (dialog, which) -> dialog.dismiss())
                        .show();

            } catch (Exception e) {
                e.printStackTrace();
            }
        }, 250);

        mDrawer.closeDrawer(GravityCompat.START);
    }

    private boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state)
                && ActivityCompat.checkSelfPermission(
                this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
    }

    private boolean isAdmin() {
        User user = mUserRepo.findActiveUserCopy();
        if (user != null) {
            String userid = user.getUserid();
            if (userid.equalsIgnoreCase("fredy") || userid.equalsIgnoreCase("tono")) return true;
        }
        return false;
    }

    @Override public void onShowHome() {
        mNavItemIndex = 0;
        loadFragment();
    }

    @Override public void onShowHelp() {
        mNavItemIndex = 3;
        loadFragment();
    }

    @Override public void onQuitApps() {
        quitApps();
    }

    @Override public void onFlashcardEmpty() {
        showEmptyFlashcard();
    }

}
