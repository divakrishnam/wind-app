package com.example.divakrishna.wind;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
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
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class PostActivity extends AppCompatActivity {

    private EditText mPostDesc;

    //private Button mSubmitButton;

    //private StorageReference mStorage;
    private DatabaseReference mDatabase;

    private ProgressDialog mProgress;

    private FirebaseAuth mAuth;
    private FirebaseUser mCurrentUser;

    private DatabaseReference mDatabaseUser;
    private String timeStamp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);

        mAuth = FirebaseAuth.getInstance();

        mCurrentUser = mAuth.getCurrentUser();

        //mStorage = FirebaseStorage.getInstance().getReference();
        mDatabase = FirebaseDatabase.getInstance().getReference().child("Post");

        mDatabaseUser = FirebaseDatabase.getInstance().getReference().child("Users").child(mCurrentUser.getUid());

        mPostDesc = (EditText)findViewById(R.id.descField);

        //mSubmitButton = (Button)findViewById(R.id.submitButton);

        mProgress = new ProgressDialog(this);
        
//        mSubmitButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                startPost();
//            }
//        });
    }

    private void startPost() {

        mProgress.setMessage("Processing...");

        final String desc_val = mPostDesc.getText().toString().trim();

        timeStamp = new SimpleDateFormat("MMM dd, yyyy HH:mm").format(new Date());

        if(!TextUtils.isEmpty(desc_val)){

            mProgress.show();

            final DatabaseReference newPost = mDatabase.push();

            mDatabaseUser.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    newPost.child("timestamp").setValue(timeStamp);
                    newPost.child("desc").setValue(desc_val);
                    newPost.child("uid").setValue(mCurrentUser.getUid());
                    newPost.child("username").setValue(dataSnapshot.child("name").getValue()).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){
                                startActivity(new Intent(PostActivity.this, MainActivity.class));
                            }
                        }
                    });
                    newPost.child("userimage").setValue(dataSnapshot.child("image").getValue()).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){
                                startActivity(new Intent(PostActivity.this, MainActivity.class));
                            }
                        }
                    });
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

            mProgress.dismiss();

            startActivity(new Intent(PostActivity.this, MainActivity.class));
            finish();
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
