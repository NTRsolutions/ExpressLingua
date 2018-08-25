package com.neosolusi.expresslingua.features.group.member;

import android.arch.lifecycle.Observer;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;

import com.neosolusi.expresslingua.AppInjectors;
import com.neosolusi.expresslingua.R;
import com.neosolusi.expresslingua.data.entity.Member;
import com.neosolusi.expresslingua.data.entity.MemberProgress;
import com.neosolusi.expresslingua.data.network.NetworkDataSource;
import com.neosolusi.expresslingua.data.repo.MemberRepository;
import com.neosolusi.expresslingua.features.BaseActivity;

import java.util.ArrayList;
import java.util.List;

public class MemberActivity extends BaseActivity {

    private RecyclerView mRecyclerView;
    private ProgressBar mLoadingIndicator;
    private NetworkDataSource mService;
    private MemberAdapter mMemberAdapter;
    private Observer<MemberProgress> mMemberObserver;
    private AlertDialog mDialog;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_member);

        initComponents();
        configureLayout();

        mMemberObserver = progress -> {
            if (progress == null) return;

            if (progress.userid == null) {
                showNoMemberData();
                return;
            }

            if (mDialog.isShowing()) mDialog.dismiss();

            MemberRepository memberRepository = AppInjectors.provideMemberRepository(this);
            Member member = memberRepository.findFirstCopyEqualTo("user_id", progress.userid);
            if (member == null) return;

            List<MemberDetail> members = new ArrayList<>();
            members.add(new MemberDetail(member, progress, "", 0, 0));

            showMemberDataView();
            mMemberAdapter.update(members);
        };

        handleIntent(getIntent());
    }

    private void configureLayout() {
        setSupportActionBar(findViewById(R.id.toolbar));
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setAdapter(mMemberAdapter);
    }

    private void initComponents() {
        mService = AppInjectors.provideNetworkDataSource(this);
        mMemberAdapter = new MemberAdapter(this);

        mRecyclerView = findViewById(R.id.recycler_member);
        mLoadingIndicator = findViewById(R.id.progressbar);

        mDialog = new AlertDialog.Builder(this).setTitle("Member").setIcon(R.mipmap.ic_launcher_ealing)
                .setMessage("Progress untuk member ini tidak tersedia")
                .setPositiveButton("Kembali", (dialog, which) -> {
                    onBackPressed();
                    finish();
                })
                .setCancelable(false).show();
    }

    @Override protected void onResume() {
        super.onResume();
        handleIntent(getIntent());
    }

    @Override protected void onPause() {
        super.onPause();
        mService.getMemberProgress().removeObserver(mMemberObserver);
    }

    @Override public boolean onCreateOptionsMenu(Menu menu) {
        return super.onCreateOptionsMenu(menu);
    }

    @Override public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            finish();
        }

        return true;
    }

    private void handleIntent(Intent intent) {
        showLoading();

        mService.getMemberProgress().observeForever(mMemberObserver);

        mService.startNetworkServiceWithExtra("progress_member", intent.getBundleExtra("extra"));
    }

    private void showNoMemberData() {
        mDialog.show();
    }

    private void showMemberDataView() {
        // First, hide the loading indicator
        mLoadingIndicator.setVisibility(View.INVISIBLE);
        // Finally, make sure the weather data is visible
        mRecyclerView.setVisibility(View.VISIBLE);
    }

    private void showLoading() {
        // Then, hide the weather data
        mRecyclerView.setVisibility(View.INVISIBLE);
        // Finally, show the loading indicator
        mLoadingIndicator.setVisibility(View.VISIBLE);
    }
}
