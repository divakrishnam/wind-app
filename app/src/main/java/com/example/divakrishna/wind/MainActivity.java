package com.example.divakrishna.wind;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {

    private RecyclerView mPostList;

    private DatabaseReference mDatabase;

    private DatabaseReference mDatabaseUsers;

    private DatabaseReference mDatabaseLike;

    private DatabaseReference mDatabaseComments;

    private Query postQuery;

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;

    private boolean mProcessLike = false;

    private FirebaseRecyclerAdapter<Post, PostViewHolder> firebaseRecyclerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
            if(firebaseAuth.getCurrentUser() == null){
                Intent loginIntent = new Intent(MainActivity.this, LoginActivity.class);
                loginIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(loginIntent);
            }
            }
        };

        mDatabase = FirebaseDatabase.getInstance().getReference().child("Post");
        mDatabase.keepSynced(true);

        mDatabaseUsers = FirebaseDatabase.getInstance().getReference().child("Users");
        mDatabaseUsers.keepSynced(true);

        mDatabaseLike = FirebaseDatabase.getInstance().getReference().child("Likes");
        mDatabaseLike.keepSynced(true);

        mDatabaseComments = FirebaseDatabase.getInstance().getReference().child("Comments");
        mDatabaseComments.keepSynced(true);

        postQuery = mDatabase.orderByKey();

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setReverseLayout(true);
        layoutManager.setStackFromEnd(true);

        mPostList = (RecyclerView)findViewById(R.id.post_list);
        mPostList.setHasFixedSize(true);
        mPostList.setLayoutManager(layoutManager);

        checkUserExist();

        FirebaseRecyclerOptions postOptions = new FirebaseRecyclerOptions.Builder<Post>().setQuery(postQuery, Post.class).build();
        firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Post, PostViewHolder>(postOptions) {

            @NonNull
            @Override
            public PostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.post_row, parent, false);

                return new PostViewHolder(view);
            }

            @Override
            protected void onBindViewHolder(@NonNull PostViewHolder viewHolder, int position, @NonNull Post model) {

                final String post_key = getRef(position).getKey();

                viewHolder.setDesc(model.getDesc());
                viewHolder.setUsername(model.getUsername());
                viewHolder.setUserimage(model.getUserimage());
                viewHolder.setTime(model.getTimestamp());

                viewHolder.setLikeButton(post_key);

                viewHolder.setSumComments(post_key);

                viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                    mDatabaseUsers.child(mAuth.getCurrentUser().getUid()).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            final String username = (String)dataSnapshot.child("name").getValue();
                            final String userimage = (String)dataSnapshot.child("image").getValue();

                            final Intent singlePostIntent = new Intent(MainActivity.this, PostSingleActivity.class);

                            singlePostIntent.putExtra("current_username", username);
                            singlePostIntent.putExtra("current_userimage", userimage);

                            singlePostIntent.putExtra("post_id", post_key);
                            singlePostIntent.putExtra("current_userid", mAuth.getCurrentUser().getUid());

                            startActivity(singlePostIntent);
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                    }
                });

                viewHolder.mLikeButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                    mProcessLike = true;
                        mDatabaseLike.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {

                                if(mProcessLike){
                                    if(dataSnapshot.child(post_key).hasChild(mAuth.getCurrentUser().getUid())){
                                        mDatabaseLike.child(post_key).child(mAuth.getCurrentUser().getUid()).removeValue();

                                        mProcessLike = false;
                                    }else{
                                        mDatabaseLike.child(post_key).child(mAuth.getCurrentUser().getUid()).setValue("Random value");

                                        mProcessLike = false;
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });
                    }
                });
            }
        };

        mPostList.setAdapter(firebaseRecyclerAdapter);
    }

    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
        firebaseRecyclerAdapter.startListening();
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
        firebaseRecyclerAdapter.stopListening();
    }

    private void checkUserExist() {
        if(mAuth.getCurrentUser() != null) {
            final String user_id = mAuth.getCurrentUser().getUid();

            mDatabaseUsers.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (!dataSnapshot.hasChild(user_id)){
                        Intent setupIntent = new Intent(MainActivity.this, SetupActivity.class);
                        setupIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(setupIntent);
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }
    }

    public class PostViewHolder extends RecyclerView.ViewHolder{

        View mView;

        ImageView mLikeButton;

        DatabaseReference mDatabaseLike;

        FirebaseAuth mAuth;

        TextView mSumLikes;
        TextView mSumComments;

        public PostViewHolder(View itemView) {
            super(itemView);

            mView = itemView;

            mSumLikes = (TextView)mView.findViewById(R.id.sum_loves);
            mSumComments = (TextView)mView.findViewById(R.id.sum_comments);

            mLikeButton = (ImageView)mView.findViewById(R.id.love_button);

            mDatabaseLike = FirebaseDatabase.getInstance().getReference().child("Likes");
            mDatabaseLike.keepSynced(true);

            mAuth = FirebaseAuth.getInstance();
        }

        public void setSumComments(final String post_key){
            mDatabaseComments.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    int count = (int) dataSnapshot.child(post_key).getChildrenCount();
                    String sum = Integer.toString(count) + " comments";
                    mSumComments.setText(sum);
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }

        public void setLikeButton(final String post_key){
            mDatabaseLike.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    int count = (int) dataSnapshot.child(post_key).getChildrenCount();
                    String sum = Integer.toString(count) + " likes";
                    mSumLikes.setText(sum);

                    if(dataSnapshot.child(post_key).hasChild(mAuth.getCurrentUser().getUid())){
                        mLikeButton.setImageResource(R.mipmap.ic_love_clicked);
                    } else{
                        mLikeButton.setImageResource(R.mipmap.ic_love);
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }

        public void setUserimage(final String userimage) {
            ImageView post_userimage = (ImageView) mView.findViewById(R.id.post_userimage);
            Picasso.get().load(userimage).transform(new RoundTransformation(200,1)).into(post_userimage);
        }

        public void setUsername(String username){
            TextView post_username = (TextView) mView.findViewById(R.id.post_username);
            post_username.setText(username);
        }

        public void setDesc(String desc){
            TextView post_desc = (TextView) mView.findViewById(R.id.post_desc);
            post_desc.setText(desc);
        }

        public void setTime(String time){
            TextView post_time = (TextView) mView.findViewById(R.id.post_time);

            try{
                SimpleDateFormat format =new SimpleDateFormat("yyyy.MMM.dd G 'at' HH:mm:ss");
                Date past = format.parse(time);
                Date now = new Date();
                long seconds= TimeUnit.MILLISECONDS.toSeconds(now.getTime() - past.getTime());
                long minutes=TimeUnit.MILLISECONDS.toMinutes(now.getTime() - past.getTime());
                long hours=TimeUnit.MILLISECONDS.toHours(now.getTime() - past.getTime());
                long days=TimeUnit.MILLISECONDS.toDays(now.getTime() - past.getTime());

                Calendar cal = Calendar.getInstance();

                String day = Integer.toString(cal.get(Calendar.DAY_OF_MONTH));
                format = new SimpleDateFormat("dd");
                String d = format.format(past);

                if(day.equals(d)){
                    if(seconds<60){
                        time = "Just Now";
                        post_time.setText(time);
                    } else if(minutes<60){
                        time = minutes+" minutes ago";
                        post_time.setText(time);
                    } else if(hours<24){
                        time = hours+" hour ago";
                        post_time.setText(time);
                    }
                } else {
                    if(days<7){
                        format = new SimpleDateFormat("HH:mm");
                        if(days<2){
                            time = "Yesterday at "+format.format(past);
                            post_time.setText(time);
                        }else {
                            time = days+" days ago at "+format.format(past);
                            post_time.setText(time);
                        }
                    }
                    else
                    {
                        String year = Integer.toString(cal.get(Calendar.YEAR));
                        format = new SimpleDateFormat("yyyy");
                        String y = format.format(past);

                        if(year.equals(y)){
                            format = new SimpleDateFormat("MMM dd HH:mm");
                            time = format.format(past);
                            post_time.setText(time);
                        }
                        else{
                            format = new SimpleDateFormat("MMM dd, yyyy HH:mm");
                            time = format.format(past);
                            post_time.setText(time);
                        }

                    }
                }
            }catch (ParseException e){
                e.printStackTrace();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == R.id.action_add){
            startActivity(new Intent(MainActivity.this, PostActivity.class));
            finish();
        }

        if(item.getItemId() == R.id.action_logout){
            logout();
        }
        return super.onOptionsItemSelected(item);
    }

    private void logout() {
        mAuth.signOut();
    }
}
