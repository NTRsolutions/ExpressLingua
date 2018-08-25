package com.neosolusi.expresslingua.features.side;

import android.Manifest;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.neosolusi.expresslingua.AppConstants;
import com.neosolusi.expresslingua.AppInjectors;
import com.neosolusi.expresslingua.OnNavigationListener;
import com.neosolusi.expresslingua.R;
import com.neosolusi.expresslingua.data.entity.User;
import com.neosolusi.expresslingua.data.repo.UserRepository;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

import static android.app.Activity.RESULT_OK;

public class AccountFragment extends Fragment {

    public static final String TAG = AccountFragment.class.getSimpleName();
    public static final int IMAGE_REQUEST_CODE = 79;
    public static final int PERMISSIONS = 80;

    private OnNavigationListener mListener;
    private Context mContext;
    private Activity mActivity;
    private File mImageFile;
    private String mImagePath;
    private Uri mTempImageGroupUri;
    private User mUser;
    private UserRepository mUserRepo;
    private CircleImageView mImageView;
    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override public void onReceive(Context context, Intent intent) {
            mUser.setAvatar(intent.getStringExtra(AppConstants.BROADCAST_MESSAGE));
            mUserRepo.copyOrUpdate(mUser);
            showGroupImage(null);
        }
    };

    public AccountFragment() {
        // Required empty public constructor
    }

    public static AccountFragment getInstance() {
        return new AccountFragment();
    }

    @Override public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_account, container, false);

        DividerItemDecoration divider = new DividerItemDecoration(mContext, DividerItemDecoration.VERTICAL);
        Drawable dividerImage = ContextCompat.getDrawable(mContext, R.drawable.divider);
        if (dividerImage != null) divider.setDrawable(dividerImage);

        mUserRepo = AppInjectors.provideUserRepository(mContext);
        mUser = mUserRepo.findActiveUserCopy();
        if (mUser == null) return root;

        List<SideRecyclerAdapter.ItemHolder> data = new ArrayList<>();
        data.add(new SideRecyclerAdapter.ItemHolder("User ID", mUser.getUserid()));
        data.add(new SideRecyclerAdapter.ItemHolder("Email", mUser.getEmail()));
        data.add(new SideRecyclerAdapter.ItemHolder("Phone", mUser.getCell_no()));

        SideRecyclerAdapter mAdapter = new SideRecyclerAdapter(data);

        RecyclerView mAccountDetail = root.findViewById(R.id.recycler_account);
        mAccountDetail.addItemDecoration(divider);
        mAccountDetail.setHasFixedSize(true);
        mAccountDetail.setLayoutManager(new LinearLayoutManager(mContext));
        mAccountDetail.setAdapter(mAdapter);

        mImageView = root.findViewById(R.id.image_account);
        mImageView.setOnClickListener(v -> showImageChooser());

        showGroupImage(null);

        requestPermissions();

        return root;
    }

    @Override public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
        mActivity = getActivity();
        if (context instanceof OnNavigationListener) {
            mListener = (OnNavigationListener) context;
        } else {
            throw new RuntimeException(context.toString() + " must implement OnFragmentInteractionListener");
        }
    }

    @Override public void onDetach() {
        if (mListener != null) mListener = null;
        super.onDetach();
    }

    @Override public void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(mContext).registerReceiver(mReceiver, new IntentFilter(AppConstants.BROADCAST_UPLOAD_USER_IMAGE_SUCCESS));
        if (mImagePath != null && !mImagePath.isEmpty()) mImageFile.delete();
    }

    @Override public void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(mContext).unregisterReceiver(mReceiver);
    }

    @Override public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == IMAGE_REQUEST_CODE && resultCode == RESULT_OK) {
            if (data == null && !mImagePath.isEmpty()) {
                setPic();
            } else if (data != null) {
                saveImage(data);
            }
        }
//        super.onActivityResult(requestCode, resultCode, data);
//
//        if (resultCode == RESULT_OK && data.getData() != null) {
//            Uri filePath = data.getData();
//            try {
//                Bitmap bitmap = MediaStore.Images.Media.getBitmap(mActivity.getContentResolver(), filePath);
//
//                if (mUser != null) {
//                    String fileName = String.valueOf(mUser.getId()) + ".jpg";
//                    File imagePath = new File(mActivity.getFilesDir(), "avatar");
//                    mImageFile = new File(imagePath, fileName);
//
//                    FileOutputStream outputStream;
//                    try {
//                        if (!imagePath.isDirectory() && !imagePath.mkdir()) return;
//                        if (!mImageFile.exists() && !mImageFile.createNewFile()) return;
//
//                        outputStream = new FileOutputStream(mImageFile);
//                        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
//                        outputStream.close();
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                        return;
//                    }
//                }
//
//                showGroupImage(bitmap);
//                uploadImage();
//                mTempImageGroupUri = filePath;
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
    }

    @Override public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSIONS:
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED
                        && grantResults[1] == PackageManager.PERMISSION_GRANTED
                        && grantResults[2] == PackageManager.PERMISSION_GRANTED
                        ) {
                    Log.d("AccountFragment", "Permission granted");
                } else {
                    new AlertDialog.Builder(mContext).setTitle("Permissions").setIcon(R.mipmap.ic_launcher)
                            .setMessage("Aplikasi membutuhkan beberapa akses untuk dapat bekerja")
                            .setNegativeButton("Tutup", (dialog, which) -> dialog.dismiss())
                            .show();
                }
        }
    }

    private Intent createChooser(Context context) {
        Intent chooserIntent = null;

        List<Intent> intentList = new ArrayList<>();

        Intent pickIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        pickIntent.putExtra("gallery", true);
        pickIntent.setType("image/jpeg");

        Intent takePhotoIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        takePhotoIntent.putExtra("return-data", true);
        takePhotoIntent.putExtra(MediaStore.EXTRA_OUTPUT, FileProvider.getUriForFile(context, getString(R.string.file_provider), createImageFile()));

        intentList = addIntentsToList(context, intentList, pickIntent);

        if (takePhotoIntent.resolveActivity(mContext.getPackageManager()) != null) {
            intentList = addIntentsToList(context, intentList, takePhotoIntent);
        }

        if (intentList.size() > 0) {
            chooserIntent = Intent.createChooser(intentList.remove(intentList.size() - 1), context.getString(R.string.app_name));
            chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, intentList.toArray(new Parcelable[]{}));
        }

        return chooserIntent;
    }

    private List<Intent> addIntentsToList(Context context, List<Intent> list, Intent intent) {
        List<ResolveInfo> resInfo = context.getPackageManager().queryIntentActivities(intent, 0);
        for (ResolveInfo resolveInfo : resInfo) {
            String packageName = resolveInfo.activityInfo.packageName;
            Intent targetedIntent = new Intent(intent);
            targetedIntent.setPackage(packageName);
            list.add(targetedIntent);
        }
        return list;
    }

    private File createImageFile() {
        String fileName = String.valueOf(mUser.getId()) + ".jpg";
        File dir = new File(mActivity.getFilesDir(), "avatar");
        mImageFile = new File(new File(mActivity.getFilesDir(), "avatar"), fileName);
        mImagePath = mImageFile.getAbsolutePath();
        try {
            if (!dir.isDirectory()) dir.mkdir();
            if (mImageFile.exists()) mImageFile.delete();
            mImageFile.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return mImageFile;
    }

    private void showGroupImage(Bitmap bitmap) {
        if (bitmap != null) {
            mImageView.setImageBitmap(bitmap);
        } else if (mUser.getAvatar() != null && !mUser.getAvatar().isEmpty()) {
            Picasso.get()
                    .load(AppConstants.BASE_URL_USER_PROFILE + mUser.getAvatar())
                    .placeholder(R.drawable.photo_profile)
                    .into(mImageView);
        }
    }

    private void showImageChooser() {
//        Intent intent = new Intent();
//        intent.setType("image/*");
//        intent.setAction(Intent.ACTION_GET_CONTENT);
//        startActivityForResult(Intent.createChooser(intent, "Pilih Gambar Profile"), IMAGE_REQUEST_CODE);
        startActivityForResult(createChooser(mContext), IMAGE_REQUEST_CODE);
    }

    private void requestPermissions() {
        if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(mContext, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(mContext, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(mActivity, new String[]{
                    Manifest.permission.CAMERA,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_EXTERNAL_STORAGE
            }, PERMISSIONS);
        }
    }

    private void uploadImage() {
        mUserRepo.uploadImage(mImageFile, mUser);
    }

    private void setPic() {
        // Get the dimensions of the View
        int targetW = mImageView.getWidth();
        int targetH = mImageView.getHeight();

        // Get the dimensions of the bitmap
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(mImagePath, bmOptions);
        int photoW = bmOptions.outWidth;
        int photoH = bmOptions.outHeight;

        // Determine how much to scale down the image
        int scaleFactor = Math.min(photoW / targetW, photoH / targetH);

        // Decode the image file into a Bitmap sized to fill the View
        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = scaleFactor;
        bmOptions.inPurgeable = true;

        Bitmap bitmap = BitmapFactory.decodeFile(mImagePath, bmOptions);
        mImageView.setImageBitmap(bitmap);

        mImagePath = "";

        showGroupImage(bitmap);
        uploadImage();
    }

    private void saveImage(Intent data) {
        Uri filePath = data.getData();
        try {
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(mContext.getContentResolver(), filePath);
            FileOutputStream outputStream;
            try {
                if (!mImageFile.exists() && !mImageFile.createNewFile()) return;
                outputStream = new FileOutputStream(mImageFile);
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
                outputStream.close();
            } catch (Exception e) {
                e.printStackTrace();
                return;
            }

            setPic();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
