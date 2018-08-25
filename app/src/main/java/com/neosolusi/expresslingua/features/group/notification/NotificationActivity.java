package com.neosolusi.expresslingua.features.group.notification;

import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.neosolusi.expresslingua.R;

public class NotificationActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);

        DividerItemDecoration divider = new DividerItemDecoration(this, DividerItemDecoration.VERTICAL);
        Drawable dividerImage = ContextCompat.getDrawable(this, R.drawable.divider);
        if (dividerImage != null) divider.setDrawable(dividerImage);

        RecyclerView mListNotification = findViewById(R.id.recycler_notification);
        mListNotification.setLayoutManager(new LinearLayoutManager(this));
        mListNotification.addItemDecoration(divider);
        mListNotification.setHasFixedSize(true);
    }
}
