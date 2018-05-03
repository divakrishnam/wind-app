package com.example.divakrishna.wind;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class PostActivity extends AppCompatActivity {

    private EditText mPostDesc;

    private Button mSubmitButton;

    //private StorageReference mStorage;
    private DatabaseReference mDatabase;

    private ProgressDialog mProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);

        //mStorage = FirebaseStorage.getInstance().getReference();
        mDatabase = FirebaseDatabase.getInstance().getReference().child("Post");

        mPostDesc = (EditText)findViewById(R.id.descField);

        mSubmitButton = (Button)findViewById(R.id.submitButton);

        mProgress = new ProgressDialog(this);
        
        mSubmitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startPost();
            }
        });
    }

    private void startPost() {

        mProgress.setMessage("Processing...");

        String desc_val = mPostDesc.getText().toString().trim();

        if(!TextUtils.isEmpty(desc_val)){

            mProgress.show();

            DatabaseReference newPost = mDatabase.push();

            newPost.child("desc").setValue(desc_val);

            mProgress.dismiss();

            startActivity(new Intent(PostActivity.this, MainActivity.class));
        }
    }
}
