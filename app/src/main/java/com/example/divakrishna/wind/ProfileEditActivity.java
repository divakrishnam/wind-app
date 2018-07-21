package com.example.divakrishna.wind;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.FirebaseError;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

public class ProfileEditActivity extends AppCompatActivity {

    private ImageButton mSetupImageButton;
    private EditText mNameField;
    private Button mSubmitButton;

    private Uri mImageUri = null;

    private static final int GALLERY_REQUEST = 1;

    private FirebaseAuth mAuth;

    private DatabaseReference mDatabaseUsers;

    private DatabaseReference mDatabaseCurrentUser;

    private Query mQuery;

    private StorageReference mStorageImage;

    private ProgressDialog mProgress;

    private Boolean mAct = false;

    private String mCurrentUserid = null;
    private String mCurrentUsername = null;
    private String mCurrentUserimage = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("Edit Profile");
        getSupportActionBar().setElevation(0);

        mAuth = FirebaseAuth.getInstance();

        mStorageImage = FirebaseStorage.getInstance().getReference().child("Profile_images");

        mDatabaseUsers = FirebaseDatabase.getInstance().getReference().child("Users");


        mProgress = new ProgressDialog(this);

        mSetupImageButton = findViewById(R.id.setupImageButton);
        mNameField = findViewById(R.id.setupNameField);
        mSubmitButton = findViewById(R.id.setupSubmitButton);

        mCurrentUsername = getIntent().getExtras().getString("current_username");
        mCurrentUserimage = getIntent().getExtras().getString("current_userimage");
        mCurrentUserid = getIntent().getExtras().getString("current_userid");

        mDatabaseCurrentUser = FirebaseDatabase.getInstance().getReference().child("Post");
        mQuery = mDatabaseCurrentUser.orderByChild("uid").equalTo(mCurrentUserid);

        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int height = displayMetrics.heightPixels;
        int width = displayMetrics.widthPixels;

        mNameField.setText(mCurrentUsername);
        Picasso.get().load(mCurrentUserimage).transform(new RoundTransformation(width / 2 * 4, 1)).into(mSetupImageButton);


        mSubmitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startSetupAccount();
            }
        });

        mSetupImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAct = true;
                Intent galleryIntent = new Intent();
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent, GALLERY_REQUEST);
            }
        });
    }

    private void startSetupAccount() {

        final String name = mNameField.getText().toString().trim();

        final String user_id = mAuth.getCurrentUser().getUid();

        if (!TextUtils.isEmpty(name)) {

            mProgress.setMessage("Finishing Setup ...");
            mProgress.show();


            if (mAct) {
                StorageReference filepath = mStorageImage.child(mImageUri.getLastPathSegment());
                filepath.putFile(mImageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        final String downloadUri = taskSnapshot.getDownloadUrl().toString();

                        mQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                        snapshot.getRef().child("username").setValue(name);
                                        snapshot.getRef().child("userimage").setValue(downloadUri);
                                    }

                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });

                        mDatabaseUsers.child(user_id).child("name").setValue(name);

                        mDatabaseUsers.child(user_id).child("image").setValue(downloadUri);

                        mProgress.dismiss();

                        onBackPressed();
                    }
                });
            } else {

                mQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            snapshot.getRef().child("username").setValue(name);
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

                mDatabaseUsers.child(user_id).child("name").setValue(name);

                mProgress.dismiss();

                onBackPressed();

            }
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == GALLERY_REQUEST && resultCode == RESULT_OK) {
            mAct = true;

            Uri imageUri = data.getData();

            CropImage.activity(imageUri)
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(1, 1)
                    .start(this);
        }
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                mImageUri = result.getUri();

                //mSetupImageButton.setImageURI(mImageUri);
                DisplayMetrics displayMetrics = new DisplayMetrics();
                getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
                int height = displayMetrics.heightPixels;
                int width = displayMetrics.widthPixels;
                Picasso.get().load(mImageUri).transform(new RoundTransformation(width / 2 * 4, 1)).into(mSetupImageButton);
                mAct = true;
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }
}
