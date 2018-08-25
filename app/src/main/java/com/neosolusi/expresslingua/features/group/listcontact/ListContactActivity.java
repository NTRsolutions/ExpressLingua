package com.neosolusi.expresslingua.features.group.listcontact;

import android.Manifest;
import android.arch.lifecycle.ViewModelProviders;
import android.content.ContentResolver;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.Toast;

import com.neosolusi.expresslingua.AppInjectors;
import com.neosolusi.expresslingua.AppUtils;
import com.neosolusi.expresslingua.R;
import com.neosolusi.expresslingua.data.entity.Contact;
import com.neosolusi.expresslingua.data.entity.Member;
import com.neosolusi.expresslingua.data.entity.MemberParcel;
import com.neosolusi.expresslingua.data.entity.User;
import com.neosolusi.expresslingua.data.repo.UserRepository;
import com.neosolusi.expresslingua.features.group.group.GroupActivity;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import io.realm.Case;
import io.realm.RealmResults;

import static com.neosolusi.expresslingua.features.group.group.GroupActivity.GROUP_EDIT;
import static com.neosolusi.expresslingua.features.group.group.GroupActivity.GROUP_ID;
import static com.neosolusi.expresslingua.features.group.group.GroupActivity.GROUP_MEMBERS;

public class ListContactActivity extends AppCompatActivity
        implements ListContactAdapter.OnContactSelection, ListContactSelectedAdapter.OnContactRemove {

    // Components property
    private long mGroupId;
    private ListContactViewModel mViewModel;
    private ListContactAdapter mAdapter;
    private ListContactSelectedAdapter mSelectedAdapter;
    private RealmResults<Contact> mContacts;
    private Set<Contact> mSelectedContacts;
    private RecyclerView mListContacts, mListSelectedContacts;
    private LinearLayoutManager mSelectedLayoutManager;
    private FloatingActionButton mFab;
    private boolean hasCopyContacts;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_contact);

        initComponents();
        configureLayout();
        initListener();

        ListContactViewModelFactory factory = AppInjectors.provideListContactViewModelFactory(this);
        mViewModel = ViewModelProviders.of(this, factory).get(ListContactViewModel.class);
    }

    @Override protected void onResume() {
        super.onResume();
        requestPermissions();
    }

    @Override public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case 60:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    bindModel();
                } else {
                    new AlertDialog.Builder(this).setTitle("Permissions").setIcon(R.mipmap.ic_launcher)
                            .setMessage("Aplikasi membutuhkan beberapa akses untuk dapat bekerja")
                            .setNegativeButton("Tutup", (dialog, which) -> dialog.dismiss())
                            .show();
                }
        }
    }

    @Override public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.group_list_contact, menu);

        MenuItem searchItem = menu.findItem(R.id.menu_search);
        SearchView mSearchView = (SearchView) searchItem.getActionView();
        mSearchView.setQueryHint("Search contacts...");
        mSearchView.setIconifiedByDefault(true);
        mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override public boolean onQueryTextSubmit(String s) {
                if (s.trim().isEmpty()) {
                    mAdapter.update(mContacts);
                } else {
                    mAdapter.update(mContacts.where().contains("name", s, Case.INSENSITIVE).findAll());
                }
                return true;
            }

            @Override public boolean onQueryTextChange(String s) {
                if (s.trim().isEmpty()) {
                    mAdapter.update(mContacts);
                } else {
                    RealmResults<Contact> contacts = mContacts.where().contains("name", s, Case.INSENSITIVE).findAll();
                    if (contacts != null && !contacts.isEmpty()) mAdapter.update(contacts);
                }
                return true;
            }
        });

        EditText editTextSearch = mSearchView.findViewById(android.support.v7.appcompat.R.id.search_src_text);
        editTextSearch.setTextColor(ContextCompat.getColor(this, R.color.colorFooterPrimary));
        editTextSearch.setHintTextColor(ContextCompat.getColor(this, R.color.colorFooterPrimary));

        return true;
    }

    @Override public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
//            case R.id.menu_done:
//                AppUtils.dismissKeyboard(this, mListContacts.getWindowToken());
//                break;
            case android.R.id.home:
                onBackPressed();
                break;
        }
        return true;
    }

    private void initComponents() {
        mListSelectedContacts = findViewById(R.id.recycler_selected_contacts);
        mListContacts = findViewById(R.id.recycler_contacts);
        mFab = findViewById(R.id.fab_contact_done);
        mAdapter = new ListContactAdapter(this, this);
        mSelectedAdapter = new ListContactSelectedAdapter(this, this);
        mSelectedContacts = new HashSet<>();
        mSelectedLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
    }

    private void initListener() {
        mFab.setOnClickListener(v -> {
            MemberParcel[] members = new MemberParcel[mSelectedContacts.size()];
            int iLoop = 0;

            if (mSelectedContacts.isEmpty()) {
                Snackbar.make(v, "Pilih member terlebih dulu", Snackbar.LENGTH_SHORT).show();
                return;
            }

            UserRepository mUserRepo = AppInjectors.provideUserRepository(this);
            User user = mUserRepo.findActiveUserCopy();
            if (user == null) return;

            // Add new group
            if (mGroupId == 0) {
                members = new MemberParcel[mSelectedContacts.size() + 1];

                // Admin member
                MemberParcel admin = new MemberParcel();
                admin.setId(UUID.randomUUID().getMostSignificantBits() & Long.MAX_VALUE);
                admin.setUser_id(user.getUserid());
                admin.setUrl("");
                admin.setGroup_id(mGroupId);
                admin.setDatecreated(new Date());
                admin.setDatemodified(new Date());
                admin.setPermission(1);
                admin.setApproved(1);
                admin.setUploaded(false);
                members[iLoop] = admin;
                iLoop++;
            }

            // Members
            for (Contact contact : mSelectedContacts) {
                MemberParcel member = new MemberParcel();
                member.setId(UUID.randomUUID().getMostSignificantBits() & Long.MAX_VALUE);
                member.setUrl(contact.getImageUrl());
                member.setUser_id(contact.getName());
                member.setGroup_id(mGroupId);
                member.setDatecreated(new Date());
                member.setDatemodified(new Date());
                member.setPermission(0);
                member.setApproved(0);
                member.setUploaded(false);
                members[iLoop] = member;
                iLoop++;
            }

            Bundle bundle = new Bundle();
            bundle.putLong(GROUP_ID, mGroupId);
            bundle.putParcelableArray(GROUP_MEMBERS, members);

            if (getIntent().getBundleExtra("extra") != null) {
                bundle.putBoolean(GROUP_EDIT, getIntent().getBundleExtra("extra").getBoolean(GROUP_EDIT));
            } else {
                bundle.putBoolean(GROUP_EDIT, false);
            }

            AppUtils.startActivityWithExtra(this, GroupActivity.class, bundle);
            finish();
        });

        mListContacts.setOnTouchListener((view, motionEvent) -> {
            AppUtils.dismissKeyboard(ListContactActivity.this, view.getWindowToken());
            return false;
        });
    }

    private void configureLayout() {
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        setSupportActionBar(findViewById(R.id.toolbar));
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        DividerItemDecoration divider = new DividerItemDecoration(this, DividerItemDecoration.VERTICAL);
        Drawable dividerImage = ContextCompat.getDrawable(this, R.drawable.divider);
        if (dividerImage != null) divider.setDrawable(dividerImage);
        mListContacts.setLayoutManager(new LinearLayoutManager(this));
        mListContacts.addItemDecoration(divider);
        mListContacts.setHasFixedSize(true);
        mListContacts.setAdapter(mAdapter);

        mListSelectedContacts.setLayoutManager(mSelectedLayoutManager);
        mListSelectedContacts.setHasFixedSize(true);
        mListSelectedContacts.setAdapter(mSelectedAdapter);
    }

    private void bindModel() {
        mViewModel.getContacts().observe(this, contacts -> {
            if (contacts == null || contacts.isEmpty() && !hasCopyContacts) {
                hasCopyContacts = true;
                gettingPhoneContacts();
                return;
            }

            mContacts = contacts;
            mAdapter.update(contacts);

            // Auto select contact when this activity called from GroupActivity
            //=====================================================================
            Bundle bundle = getIntent().getBundleExtra("extra");
            if (bundle == null || bundle.getLong(GROUP_ID) == 0) return;
            long groupId = bundle.getLong(GROUP_ID);

            mGroupId = groupId;
            mSelectedContacts.clear();
            for (Member member : mViewModel.findMembers(groupId)) {
                Contact contact = mContacts.where().equalTo("name", member.getUser_id()).findFirst();
                if (contact != null) onSelect(contact, true);
            }
            //=====================================================================
        });
    }

    private void requestPermissions() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_CONTACTS}, 60);
        } else {
            bindModel();
        }
    }

    private void gettingPhoneContacts() {
        ContentResolver cr = getContentResolver();
        Cursor cursor = cr.query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                new String[]{
                        ContactsContract.Contacts._ID,
                        ContactsContract.Contacts.DISPLAY_NAME,
                        ContactsContract.CommonDataKinds.Phone.NUMBER,
                        ContactsContract.RawContacts.ACCOUNT_TYPE
                },
                ContactsContract.RawContacts.ACCOUNT_TYPE + " <> 'google' ",
                null, null);

        if (cursor == null) return;

        if (cursor.getCount() <= 0) {
            Toast.makeText(this, "No Phone Contact Found..!", Toast.LENGTH_SHORT).show();
        } else {
            List<Contact> contacts = new ArrayList<>();
            while (cursor.moveToNext()) {
                String Phone_number = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                String name = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));

                Contact contact = new Contact();
                contact.setId(UUID.randomUUID().getMostSignificantBits() & Long.MAX_VALUE);
                contact.setPhone(AppUtils.normalizePhone(Phone_number));
                contact.setName(name);
                contact.setActive(false);
                contacts.add(contact);
            }
            mViewModel.addContacts(contacts);
        }

        cursor.close();
    }

    @Override public void onSelect(Contact contact, boolean isSelect) {
        if (isSelect) {
            mSelectedContacts.add(contact);
            mSelectedAdapter.add(contact);
            mAdapter.add(contact);
            mListSelectedContacts.setVisibility(View.VISIBLE);

            if (mGroupId != 0) {
                Member member = new Member();
                member.setId(UUID.randomUUID().getMostSignificantBits() & Long.MAX_VALUE);
                member.setUrl(contact.getImageUrl());
                member.setUser_id(contact.getName());
                member.setGroup_id(mGroupId);
            }

            new Handler().postDelayed(() -> mSelectedLayoutManager.scrollToPosition(mSelectedContacts.size() - 1), 300);
        } else {
            onRemove(contact);
        }
    }

    @Override public void onRemove(Contact contact) {
        mSelectedContacts.remove(contact);
        mSelectedAdapter.remove(contact);
        mAdapter.remove(contact);

        if (mGroupId != 0) {
            Member member = mViewModel.findMember(contact.getName(), mGroupId);
            if (member != null) mViewModel.removeMember(member);
        }

        if (mSelectedContacts.size() == 0) mListSelectedContacts.setVisibility(View.GONE);
    }
}
