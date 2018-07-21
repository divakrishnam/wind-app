package com.example.divakrishna.wind;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Date;

public class PostEditActivity extends AppCompatActivity {

    private String mPostKey;

    private EditText mPostDesc;

    private DatabaseReference mDatabase;

    private ProgressDialog mProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);

        mPostKey = getIntent().getExtras().getString("post_key");

        final ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("Edit Post");
        getSupportActionBar().setElevation(0);

        mDatabase = FirebaseDatabase.getInstance().getReference().child("Post");

        mPostDesc = findViewById(R.id.descField);

        mProgress = new ProgressDialog(this);

        mDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String userpost = (String)dataSnapshot.child(mPostKey).child("desc").getValue();

                mPostDesc.setText(userpost);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void startPost() {

        mProgress.setMessage("Processing...");

        final String desc_val = mPostDesc.getText().toString().trim();

        if(!TextUtils.isEmpty(desc_val)){

            mProgress.show();

            mDatabase.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    mDatabase.child(mPostKey).child("desc").setValue(desc_val);
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

            mProgress.dismiss();

            onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.post_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == R.id.action_addpost){
            startPost();
        }
        return super.onOptionsItemSelected(item);
    }
}
