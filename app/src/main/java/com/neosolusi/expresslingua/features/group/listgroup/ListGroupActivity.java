package com.neosolusi.expresslingua.features.group.listgroup;

import android.arch.lifecycle.ViewModelProviders;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;

import com.neosolusi.expresslingua.AppInjectors;
import com.neosolusi.expresslingua.AppUtils;
import com.neosolusi.expresslingua.R;
import com.neosolusi.expresslingua.data.entity.Group;
import com.neosolusi.expresslingua.features.group.group.GroupActivity;
import com.neosolusi.expresslingua.features.group.listcontact.ListContactActivity;

import io.realm.Realm;
import io.realm.RealmResults;

import static com.neosolusi.expresslingua.features.group.group.GroupActivity.GROUP_EDIT;
import static com.neosolusi.expresslingua.features.group.group.GroupActivity.GROUP_ID;

public class ListGroupActivity extends AppCompatActivity implements ListGroupAdapter.OnGroupClickListener {

    private ListGroupAdapter mAdapter;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_group);

        ListGroupViewModelFactory factory = AppInjectors.provideListGroupViewModelFactory(this);
        ListGroupViewModel mViewModel = ViewModelProviders.of(this, factory).get(ListGroupViewModel.class);

        setSupportActionBar(findViewById(R.id.toolbar));
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        DividerItemDecoration divider = new DividerItemDecoration(this, DividerItemDecoration.VERTICAL);
        Drawable dividerImage = ContextCompat.getDrawable(this, R.drawable.divider);
        if (dividerImage != null) divider.setDrawable(dividerImage);

        RecyclerView mListGroups = findViewById(R.id.recycler_groups);
        mListGroups.setLayoutManager(new LinearLayoutManager(this));
        mListGroups.addItemDecoration(divider);
        mListGroups.setHasFixedSize(true);

        Realm realm = Realm.getDefaultInstance();

        RealmResults<Group> groups = realm.where(Group.class).findAll();
        mAdapter = new ListGroupAdapter(this, groups, this);
        mListGroups.setAdapter(mAdapter);

        mViewModel.getGroups().observe(this, newGroups -> {
            if (newGroups == null || newGroups.isEmpty()) {
                return;
            }

            mAdapter.update(newGroups);
        });
    }

    @Override protected void onResume() {
        super.onResume();
    }

    @Override public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.group_list_menu, menu);
        return true;
    }

    @Override public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_add:
                AppUtils.startActivity(this, ListContactActivity.class);
                break;
            case android.R.id.home:
                onBackPressed();
        }

        return true;
    }

    @Override public void onClick(Group group) {
        Bundle bundle = new Bundle();
        bundle.putLong(GROUP_ID, group.getId());
        bundle.putBoolean(GROUP_EDIT, true);
        AppUtils.startActivityWithExtra(this, GroupActivity.class, bundle);
    }

}
