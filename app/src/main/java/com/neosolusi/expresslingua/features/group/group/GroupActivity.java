package com.neosolusi.expresslingua.features.group.group;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import com.neosolusi.expresslingua.AppConstants;
import com.neosolusi.expresslingua.AppInjectors;
import com.neosolusi.expresslingua.AppUtils;
import com.neosolusi.expresslingua.R;
import com.neosolusi.expresslingua.data.entity.Group;
import com.neosolusi.expresslingua.data.entity.Member;
import com.neosolusi.expresslingua.data.entity.MemberParcel;
import com.neosolusi.expresslingua.features.BaseActivity;
import com.neosolusi.expresslingua.features.group.listcontact.ListContactActivity;
import com.neosolusi.expresslingua.features.group.listgroup.ListGroupActivity;
import com.neosolusi.expresslingua.features.group.member.MemberActivity;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;
import io.realm.Realm;
import io.realm.RealmResults;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

public class GroupActivity extends BaseActivity implements GroupAdapter.GroupMemberClickListener {

    public static final String GROUP_ID = "group_id";
    public static final String GROUP_EDIT = "group_edit";
    public static final String GROUP_MEMBERS = "group_members";
    public static final int IMAGE_REQUEST_CODE = 78;

    // UI
    private RecyclerView mListMembers;
    private ImageButton mButtonEdit;
    private CircleImageView mGroupImage;
    private Button mButtonDelete, mButtonCreate;
    private TextView mTextName, mTextMemberCount;
    private EditText mEditName;
    private ConstraintLayout mLayoutGroupImageHolder;
    private ImageView mGroupImageHolder, mGroupIconHolder;
    private NestedScrollView mNestedScrollView;
    private Switch mSwitchPrivacy, mSwitchTranslate;

    // Data property
    private boolean isEdit = false;
    private boolean isGroupEdit;
    private boolean isButtonCreateOrEditClicked = false;

    // References
    private Realm mRealm;
    private GroupViewModel mViewModel;
    private File mImageFile;
    private BroadcastReceiver mBroadcastReceiver;
    private List<Member> mMembers;
    private GroupAdapter mAdapter;
    private Group mCurrentGroup;
    private Uri mTempImageGroupUri;

    private Observer<RealmResults<Group>> groupsObserver = groups -> {
        if (groups == null || groups.isEmpty()) return;

        if (mCurrentGroup != null && !isGroupEdit && isButtonCreateOrEditClicked) {
            for (Group group : groups) {
                if (group.getName().equalsIgnoreCase(mCurrentGroup.getName()) && !isGroupEdit) {
                    updateGroupId(group);
                    uploadMembers(group.getId());
                    break;
                }
            }
            AppUtils.startActivity(GroupActivity.this, ListGroupActivity.class);
            GroupActivity.this.finish();
        }
    };

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        initComponents();
        configureLayout();
        initListeners();

        mListMembers.setAdapter(mAdapter);

        if (getIntent().getExtras() != null) {
            long id = getIntent().getBundleExtra("extra").getLong(GROUP_ID);
            isGroupEdit = getIntent().getBundleExtra("extra").getBoolean(GROUP_EDIT, false);

            mEditName.setVisibility(GONE);
            mTextName.setVisibility(VISIBLE);
            mListMembers.setVisibility(VISIBLE);
            mButtonEdit.setVisibility(VISIBLE);

            if (isGroupEdit) {
                mButtonDelete.setVisibility(VISIBLE);
                mButtonCreate.setVisibility(GONE);

                mCurrentGroup = mViewModel.findFirstGroupCopyEqualTo("id", id);
                if (mCurrentGroup == null) return;
                mMembers = mRealm.copyFromRealm(mViewModel.findAllMemberEqualTo("group_id", id));

                Parcelable[] members = getIntent().getBundleExtra("extra").getParcelableArray(GROUP_MEMBERS);
                if (members != null) {
                    for (Parcelable parcel : members) {
                        MemberParcel memberParcel = (MemberParcel) parcel;
                        Member member = new Member(memberParcel);

                        boolean exists = false;
                        for (Member cMember : mMembers) {
                            if (cMember.getUser_id().equalsIgnoreCase(member.getUser_id())) {
                                exists = true;
                                break;
                            }
                        }
                        if (!exists) mMembers.add(member);
                    }
                }

                showGroupImage(null);

                mAdapter.update(mMembers);

                mTextName.setText(mCurrentGroup.getName());
                mEditName.setText(mCurrentGroup.getName());
                mSwitchPrivacy.setChecked(mCurrentGroup.getPrivacy() > 0);
                mSwitchTranslate.setChecked(mCurrentGroup.getTranslate() > 0);
                mTextMemberCount.setText(String.format(Locale.getDefault(), "Members %d", mMembers.size()));

                mNestedScrollView.scrollTo(0, 0);

                if (!mViewModel.getActiveUser().getUserid().equalsIgnoreCase(mCurrentGroup.getAdmin())) {
                    mButtonDelete.setVisibility(GONE);
                    mButtonEdit.setVisibility(GONE);
                    mSwitchPrivacy.setEnabled(false);
                    mSwitchTranslate.setEnabled(false);
                    mGroupImage.setClickable(false);
                    mGroupImage.setEnabled(false);
                }
            } else {
                mButtonDelete.setVisibility(GONE);
                mButtonCreate.setVisibility(VISIBLE);
                mCurrentGroup = new Group();
                mCurrentGroup.setName("");
                mCurrentGroup.setDescription("");
                mCurrentGroup.setUrl("");

                Parcelable[] members = getIntent().getBundleExtra("extra").getParcelableArray(GROUP_MEMBERS);
                if (members == null) return;
                for (Parcelable parcel : members) {
                    MemberParcel memberParcel = (MemberParcel) parcel;
                    mMembers.add(new Member(memberParcel));
                }
                mAdapter.update(mMembers);
            }

            if (mCurrentGroup.getUrl() != null && !mCurrentGroup.getUrl().trim().isEmpty()) {
                try {
                    File audioPath = new File(getFilesDir(), "avatar");
                    File audioFile = new File(audioPath, mCurrentGroup.getUrl());
                    mTempImageGroupUri = Uri.fromFile(audioFile);
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), mTempImageGroupUri);
                    showGroupImage(bitmap);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && data.getData() != null) {
            Uri filePath = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), filePath);

                if (mCurrentGroup != null) {
                    String fileName = String.valueOf(mCurrentGroup.getId()) + ".jpg";
                    File imagePath = new File(getFilesDir(), "avatar");
                    mImageFile = new File(imagePath, fileName);

                    FileOutputStream outputStream;
                    try {
                        if (!imagePath.isDirectory() && !imagePath.mkdir()) return;
                        if (!mImageFile.exists() && !mImageFile.createNewFile()) return;

                        outputStream = new FileOutputStream(mImageFile);
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
                        outputStream.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    mCurrentGroup.setUrl(String.valueOf(mCurrentGroup.getId()) + ".jpg");
                }

                showGroupImage(bitmap);
                uploadImage();
                mTempImageGroupUri = filePath;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.group_new_menu, menu);

        return true;
    }

    @Override public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_add_member:
                if (!mViewModel.getActiveUser().getUserid().equalsIgnoreCase(mCurrentGroup.getAdmin())) {
                    showSimpleDialog("Anda bukan admin group ini.");
                    break;
                }

                Bundle bundle = new Bundle();
                bundle.putLong(GroupActivity.GROUP_ID, getIntent().getBundleExtra("extra").getLong(GROUP_ID));
                bundle.putBoolean(GROUP_EDIT, getIntent().getBundleExtra("extra").getBoolean(GROUP_EDIT));
                AppUtils.startActivityWithExtra(this, ListContactActivity.class, bundle);
                finish();
                break;
            case android.R.id.home:
                onBackPressed();
                break;
        }
        return true;
    }

    @Override protected void onPause() {
        super.onPause();
        if (isGroupEdit) createOrUpdateGroup();
        mViewModel.getGroups().removeObserver(groupsObserver);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mBroadcastReceiver);
    }

    @Override protected void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(this).registerReceiver(mBroadcastReceiver, new IntentFilter(AppConstants.BROADCAST_UPLOAD_GROUP_IMAGE_SUCCESS));
        mViewModel.getGroups().observe(this, groupsObserver);
    }

    @Override protected void onDestroy() {
        mRealm.close();
        super.onDestroy();
    }

    private void initComponents() {
        mEditName = findViewById(R.id.edit_group_name);
        mTextName = findViewById(R.id.text_group_name);
        mButtonEdit = findViewById(R.id.button_edit);
        mGroupImage = findViewById(R.id.image_group);
        mListMembers = findViewById(R.id.recycler_group_members);
        mButtonDelete = findViewById(R.id.button_group_delete);
        mButtonCreate = findViewById(R.id.button_group_create);
        mTextMemberCount = findViewById(R.id.text_caption);
        mGroupImageHolder = findViewById(R.id.image_group_placeholder);
        mGroupIconHolder = findViewById(R.id.image_group_icon_holder);
        mLayoutGroupImageHolder = findViewById(R.id.layout_group_image_holder);
        mNestedScrollView = findViewById(R.id.nestedScroll);
        mSwitchPrivacy = findViewById(R.id.option_switch_privacy);
        mSwitchTranslate = findViewById(R.id.option_switch_translate);

        GroupViewModelFactory factory = AppInjectors.provideNewGroupViewModelFactory(this);
        mViewModel = ViewModelProviders.of(this, factory).get(GroupViewModel.class);

        mBroadcastReceiver = new BroadcastReceiver() {
            @Override public void onReceive(Context context, Intent intent) {
                String fileName = intent.getStringExtra(AppConstants.BROADCAST_MESSAGE);
                mCurrentGroup.setUrl(fileName);
            }
        };

        mRealm = Realm.getDefaultInstance();
        mAdapter = new GroupAdapter(this, this);
        mMembers = new ArrayList<>();
    }

    private void configureLayout() {
        setSupportActionBar(findViewById(R.id.toolbar));
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        DividerItemDecoration divider = new DividerItemDecoration(this, DividerItemDecoration.VERTICAL);
        Drawable dividerImage = ContextCompat.getDrawable(this, R.drawable.divider);
        if (dividerImage != null) divider.setDrawable(dividerImage);

        LinearLayoutManager mLayoutManager = new LinearLayoutManager(this);
        mListMembers.addItemDecoration(divider);
        mListMembers.setHasFixedSize(true);
        mListMembers.setLayoutManager(mLayoutManager);
        mListMembers.setVisibility(GONE);

        mEditName.setVisibility(VISIBLE);
        mButtonEdit.setVisibility(GONE);
        mButtonDelete.setVisibility(GONE);
        mButtonCreate.setVisibility(GONE);
        mTextName.setVisibility(GONE);

        showGroupImageHolder();
    }

    private void initListeners() {
        mLayoutGroupImageHolder.setOnClickListener(v -> showImageChooser());

        mButtonEdit.setOnClickListener(v -> {
            isEdit = !isEdit;
            if (isEdit) {
                if (mEditName.getText().toString().equalsIgnoreCase(getString(R.string.text_group_name_placeholder))) {
                    mEditName.setText("");
                }

                mTextName.setVisibility(GONE);
                mEditName.setVisibility(VISIBLE);
                mEditName.requestFocus();
                mButtonEdit.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_done));
                AppUtils.showKeyboard(GroupActivity.this);
            } else {
                mCurrentGroup.setName(mEditName.getText().toString());
                mTextName.setText(mCurrentGroup.getName().trim().isEmpty() ? getString(R.string.text_group_name_placeholder) : mCurrentGroup.getName());
                mTextName.setVisibility(VISIBLE);
                mEditName.setVisibility(GONE);
                mButtonEdit.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_edit));
                AppUtils.dismissKeyboard(GroupActivity.this, mButtonEdit.getWindowToken());
            }
        });

        mButtonDelete.setOnClickListener(v -> {
            if (mCurrentGroup != null) {
                new AlertDialog.Builder(this).setTitle("Groups").setIcon(R.mipmap.ic_launcher_ealing)
                        .setMessage("Anda yakin akan menghapus group ini?")
                        .setPositiveButton("Ya", (dialog, which) -> {
                            mRealm.executeTransaction(db -> mCurrentGroup.deleteFromRealm());
                            finish();
                        })
                        .setNegativeButton("Tidak", (dialog, which) -> dialog.dismiss())
                        .setCancelable(false)
                        .show();
            }
        });

        mButtonCreate.setOnClickListener(v -> {
            if (mCurrentGroup.getName() != null && mCurrentGroup.getName().trim().isEmpty()) {
                Snackbar.make(mEditName, "Nama group harus diisi", Snackbar.LENGTH_SHORT).show();
                return;
            }

            if (mMembers.isEmpty()) {
                Snackbar.make(mEditName, "Minimal satu member dalam group", Snackbar.LENGTH_SHORT).show();
                return;
            }

            mTextName.setVisibility(GONE);
            mTextName.setText(mEditName.getText());
            mButtonEdit.setVisibility(VISIBLE);
            AppUtils.dismissKeyboard(this, mButtonEdit.getWindowToken());
            isButtonCreateOrEditClicked = true;
            createOrUpdateGroup();
        });

        mSwitchPrivacy.setOnCheckedChangeListener((compoundButton, b) -> mCurrentGroup.setPrivacy(b ? 1 : 0));

        mSwitchTranslate.setOnCheckedChangeListener((compoundButton, b) -> mCurrentGroup.setTranslate(b ? 1 : 0));
    }

    private void createOrUpdateGroup() {
        if (isGroupEdit) {
            // Update Group
            mCurrentGroup.setName(mEditName.getText().toString());
            mCurrentGroup.setPrivacy(mSwitchPrivacy.isChecked() ? 1 : 0);
            mCurrentGroup.setTranslate(mSwitchTranslate.isChecked() ? 1 : 0);
            mCurrentGroup.setMember_count(mMembers.size());
            mCurrentGroup.setDatemodified(new Date());
            mViewModel.updateGroup(mCurrentGroup);
            uploadMembers(mCurrentGroup.getId());
        } else {
            // Create Group
            mCurrentGroup.setMember_count(mMembers.size());
            mCurrentGroup.setAdmin(mViewModel.getActiveUser().getUserid());
            mCurrentGroup.setDatecreated(new Date());
            mCurrentGroup.setDatemodified(new Date());
            mViewModel.createGroup(mCurrentGroup);
        }

        mEditName.setVisibility(GONE);
        mTextName.setVisibility(VISIBLE);
        mListMembers.setVisibility(VISIBLE);

        if (mViewModel.getActiveUser().getUserid().equalsIgnoreCase(mCurrentGroup.getAdmin())) {
            mButtonEdit.setVisibility(VISIBLE);
        }
    }

    private void updateGroupId(Group group) {
        mCurrentGroup.setId(group.getId());
        mCurrentGroup.setDatecreated(group.getDatecreated());
        mCurrentGroup.setDatemodified(group.getDatecreated());
        mRealm.executeTransactionAsync(db -> db.copyToRealmOrUpdate(mCurrentGroup));
        mTextMemberCount.setText(String.format(Locale.getDefault(), "Members %d", mCurrentGroup.getMember_count()));
    }

    private void uploadMembers(long groupId) {
        if (mMembers != null && !mMembers.isEmpty()) {
            mViewModel.createMembers(mMembers, groupId);
        }
    }

    private void showGroupImageHolder() {
        mGroupImage.setVisibility(GONE);
        mGroupImageHolder.setVisibility(VISIBLE);
        mGroupIconHolder.setVisibility(VISIBLE);
        mLayoutGroupImageHolder.setVisibility(VISIBLE);
    }

    private void showGroupImage(Bitmap bitmap) {
        if (bitmap == null && (mCurrentGroup != null && !mCurrentGroup.getUrl().isEmpty())) {
            Picasso.get()
                    .load(AppConstants.BASE_URL_GROUP_PROFILE + mCurrentGroup.getUrl())
                    .placeholder(R.drawable.ic_people)
                    .into(mGroupImage);
        } else {
            if (bitmap == null) {
                mGroupImage.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_people));
            } else {
                mGroupImage.setImageBitmap(bitmap);
            }
        }
        mGroupImage.setVisibility(VISIBLE);
        mGroupImageHolder.setVisibility(GONE);
        mGroupIconHolder.setVisibility(GONE);
        mLayoutGroupImageHolder.setVisibility(VISIBLE);
    }

    private void showImageChooser() {
        if (mViewModel.getActiveUser().getUserid().equalsIgnoreCase(mCurrentGroup.getAdmin())) {
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(Intent.createChooser(intent, "Pilih Gambar Profile"), IMAGE_REQUEST_CODE);
        }
    }

    private void uploadImage() {
        mViewModel.uploadGroupImage(mImageFile, mCurrentGroup);
    }

    @Override public void onMemberClick(Member member) {
        Bundle bundle = new Bundle();
        bundle.putString("memberId", member.getUser_id());
        AppUtils.startActivityWithExtra(this, MemberActivity.class, bundle);
    }
}
