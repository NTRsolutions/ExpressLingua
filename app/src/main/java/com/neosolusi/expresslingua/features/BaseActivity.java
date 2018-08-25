package com.neosolusi.expresslingua.features;

import android.arch.lifecycle.ViewModel;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.PorterDuff;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;

import com.neosolusi.expresslingua.AppConstants;
import com.neosolusi.expresslingua.AppInjectors;
import com.neosolusi.expresslingua.R;
import com.neosolusi.expresslingua.data.entity.Challenge;
import com.neosolusi.expresslingua.data.entity.Reading;
import com.neosolusi.expresslingua.data.entity.ReadingInfo;
import com.neosolusi.expresslingua.data.entity.ReadingInfoMeta;
import com.neosolusi.expresslingua.data.repo.EpisodeRepository;
import com.neosolusi.expresslingua.data.repo.ReadingInfoRepository;
import com.neosolusi.expresslingua.data.repo.ReadingRepository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import io.realm.Realm;

public abstract class BaseActivity<T extends ViewModel> extends AppCompatActivity {

    protected SharedPreferences mPref;
    protected ReadingInfoRepository mReadingInfoRepo;
    protected ReadingRepository mReadingRepo;
    protected Realm mDatabase;

    @Override protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mDatabase = Realm.getDefaultInstance();
        mPref = AppInjectors.provideSharedPreferences(this);
        mReadingInfoRepo = AppInjectors.provideReadingInfoRepository(this);
        mReadingRepo = AppInjectors.provideReadingRepository(this);
    }

    @Override protected void onDestroy() {
        super.onDestroy();
        mDatabase.close();
    }

    protected boolean hasFinishReadStory(ReadingInfo info) {
        if (info.getFile_id() == 1) return true;

        HashMap<String, Object> criterias = new HashMap<>();
        criterias.put("already_read", 1);
        criterias.put("file_id", info.getFile_id() - 1);

        return mReadingRepo.findFirstEqualTo(criterias) != null;
    }

    protected ReadingInfoMeta getMetadata(ReadingInfo info) {
        return mDatabase.where(ReadingInfoMeta.class).equalTo("menu_id", info.getMenu_id()).findFirst();
    }

    protected int correctAnswerCount(ReadingInfo info) {
        List<Long> listId = new ArrayList<>();
        for (Reading reading : mDatabase.where(Reading.class)
                .equalTo("file_id", info.getFile_id() - 1)
                .greaterThan("mastering_level", 1)
                .equalTo("already_read", 1)
                .findAll()) {
            listId.add(reading.getId());
        }

        if (listId.isEmpty()) return 0;

        Long[] ids = new Long[listId.size()];
        ids = listId.toArray(ids);

        return mDatabase.where(Challenge.class).in("id", ids).findAll().size();
    }

    protected boolean canGoToNextLesson(ReadingInfo readingInfo) {
        // Should complete last lesson before continue
        if (!hasFinishReadStory(readingInfo)) {
            showNotFinishRead();
            return false;
        }

        // Minimum 60% correct challenge of last lesson to UNLOCK next lesson
        if (readingInfo.getFile_id() > 1) {
            int correct = correctAnswerCount(readingInfo);
            int lesson = getMetadata(readingInfo).getSentenceCount();
            if (((correct / lesson) * 100) < mPref.getInt(AppConstants.PREFERENCE_MIN_PASS_LESSON, 60)) {
                showSimpleDialog("Untuk memulai pelajaran ini, Anda harus menjawab dengan benar minimum " + mPref.getInt(AppConstants.PREFERENCE_MIN_PASS_LESSON, 60) + "% dari challenges");
                return false;
            }
        }

        return true;
    }

    protected void configureToolbarLesson() {
        findViewById(R.id.app_bar).setVisibility(View.VISIBLE);
        Toolbar toolbar = findViewById(R.id.toolbar);

        Bitmap bitmap = ((BitmapDrawable) getResources().getDrawable(R.drawable.icon_left_home)).getBitmap();
        Drawable drawable = new BitmapDrawable(getResources(), Bitmap.createScaledBitmap(bitmap, 50, 50, true));
        drawable.setColorFilter(getResources().getColor(R.color.colorFooterPrimary), PorterDuff.Mode.MULTIPLY);
        toolbar.setNavigationIcon(drawable);

        setSupportActionBar(findViewById(R.id.toolbar));
        if (getSupportActionBar() != null) {
            getSupportActionBar().setCustomView(R.layout.app_bar_lesson);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    public void showHiperLink(View view) {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.expresslingua.com"));
        startActivity(intent);
    }

    protected void showNotFinishRead() {
        showSimpleDialog("Anda belum menyelesaikan materi sebelumnya");
    }

    protected void showEmptyFlashcard() {
        showSimpleDialog("Anda belum memiliki flashcard, silahkan tandai kata atau kalimat dari lesson sesuai tingkat kesulitan menurut Anda");
    }

    protected void showSimpleDialog(String message) {
        new AlertDialog.Builder(this).setTitle(R.string.app_name).setIcon(R.mipmap.ic_launcher_ealing)
                .setMessage(message)
                .setNegativeButton("Tutup", (dialog, which) -> dialog.dismiss())
                .show();
    }

}
