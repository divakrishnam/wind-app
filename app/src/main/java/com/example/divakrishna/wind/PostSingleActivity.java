package com.example.divakrishna.wind;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class PostSingleActivity extends AppCompatActivity {

    private String mPostKey = null;

    private DatabaseReference mDatabase;

    private TextView mPostSingleDesc;

    private Button mSingleRemoveButton;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_single);

        mPostKey = getIntent().getExtras().getString("post_id");

        mPostSingleDesc = (TextView)findViewById(R.id.singlePostDesc);
        mSingleRemoveButton = (Button)findViewById(R.id.singleRemoveButton);

        mAuth = FirebaseAuth.getInstance();

        mDatabase = FirebaseDatabase.getInstance().getReference().child("Post");

        mDatabase.child(mPostKey).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String post_desc = (String)dataSnapshot.child("desc").getValue();
                String post_uid = (String)dataSnapshot.child("uid").getValue();

                mPostSingleDesc.setText(post_desc);

                if(mAuth.getCurrentUser().getUid().equals(post_uid)){
                    mSingleRemoveButton.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        mSingleRemoveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDatabase.child(mPostKey).removeValue();

                Intent mainIntent = new Intent(PostSingleActivity.this, MainActivity.class);
                startActivity(mainIntent);
            }
        });
    }
}
