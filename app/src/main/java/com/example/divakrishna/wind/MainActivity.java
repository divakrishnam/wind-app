package com.example.divakrishna.wind;

import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {

    private RecyclerView mPostList;

    private DatabaseReference mDatabase;
    private DatabaseReference mDatabaseUni;
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

        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("Timeline");
        getSupportActionBar().setElevation(0);

        mAuth = FirebaseAuth.getInstance();
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                if(firebaseAuth.getCurrentUser() == null){
                    Intent loginIntent = new Intent(MainActivity.this, LoginActivity.class);
                    loginIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(loginIntent);
                    finish();
                }
            }
        };

        mDatabaseUni = FirebaseDatabase.getInstance().getReference();
        mDatabaseUni.keepSynced(true);

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

                final String getUser = model.getUsername();
                final String getImage = model.getUserimage();
                final String getUseruid = model.getUid();

                viewHolder.setDesc(model.getDesc());
                viewHolder.setUsername(model.getUsername());
                viewHolder.setUserimage(model.getUserimage());
                viewHolder.setTime(model.getTimestamp());

                viewHolder.setLikeButton(post_key);

                viewHolder.setSumComments(post_key);

                viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        mDatabaseUni.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                final String username = (String)dataSnapshot.child("Users").child(mAuth.getCurrentUser().getUid()).child("name").getValue();
                                final String userimage = (String)dataSnapshot.child("Users").child(mAuth.getCurrentUser().getUid()).child("image").getValue();
                                final String userpost = (String)dataSnapshot.child("Post").child(post_key).child("uid").getValue();

                                final Intent singlePostIntent = new Intent(MainActivity.this, PostSingleActivity.class);

                                singlePostIntent.putExtra("current_username", username);
                                singlePostIntent.putExtra("current_userimage", userimage);

                                singlePostIntent.putExtra("current_userpost", userpost);

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

                viewHolder.mUsername.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mDatabaseUsers.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {

                                final Intent singlePostIntent = new Intent(MainActivity.this, ProfileActivity.class);

//                                singlePostIntent.putExtra("current_username", getUser);
//                                singlePostIntent.putExtra("current_userimage", getImage);
                                singlePostIntent.putExtra("current_userid", getUseruid);

                                startActivity(singlePostIntent);
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });
                    }
                });

                viewHolder.mUserimage.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mDatabaseUsers.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {

                                final Intent singlePostIntent = new Intent(MainActivity.this, ProfileActivity.class);

//                                singlePostIntent.putExtra("current_username", getUser);
//                                singlePostIntent.putExtra("current_userimage", getImage);
                                singlePostIntent.putExtra("current_userid", getUseruid);

                                startActivity(singlePostIntent);
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
                        Intent setupIntent = new Intent(MainActivity.this, LoginActivity.class);
                        setupIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(setupIntent);
                        finish();
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

        TextView mUsername;
        ImageView mUserimage;

        DatabaseReference mDatabaseLike;

        FirebaseAuth mAuth;

        TextView mSumLikes;
        TextView mSumComments;

        public PostViewHolder(View itemView) {
            super(itemView);

            mView = itemView;

            mSumLikes = mView.findViewById(R.id.sum_loves);
            mSumComments = mView.findViewById(R.id.sum_comments);

            mLikeButton = mView.findViewById(R.id.love_button);
            mUsername = mView.findViewById(R.id.post_username);
            mUserimage = mView.findViewById(R.id.post_userimage);

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
            DisplayMetrics displayMetrics = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
            int height = displayMetrics.heightPixels;
            int width = displayMetrics.widthPixels;
            ImageView post_userimage = mView.findViewById(R.id.post_userimage);
            Picasso.get().load(userimage).transform(new RoundTransformation(width/2*4,1)).into(post_userimage);
        }

        public void setUsername(String username){
            TextView post_username = mView.findViewById(R.id.post_username);
            post_username.setText(username);
        }

        public void setDesc(String desc){
            TextView post_desc = mView.findViewById(R.id.post_desc);

            if (desc.length()>200) {
                desc=desc.substring(0,400)+" ...Read More";
                Spannable spannable = new SpannableString(desc);
                spannable.setSpan(new ForegroundColorSpan(Color.DKGRAY), desc.indexOf(" ...Read More"),desc.indexOf(" ...Read More") + " ...Read More".length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                post_desc.setText(spannable);

            }else{
                post_desc.setText(desc);
            }

        }

        public void setTime(String time){
            TextView post_time = mView.findViewById(R.id.post_time);

            try{
                SimpleDateFormat format =new SimpleDateFormat("yyyy.MM.dd HH:mm:ss");
                Date past = format.parse(time);
                Date now = new Date();
                long seconds= TimeUnit.MILLISECONDS.toSeconds(now.getTime() - past.getTime());
                long minutes=TimeUnit.MILLISECONDS.toMinutes(now.getTime() - past.getTime());
                long hours=TimeUnit.MILLISECONDS.toHours(now.getTime() - past.getTime());
                long days=TimeUnit.MILLISECONDS.toDays(now.getTime() - past.getTime());

                Calendar cal = Calendar.getInstance();


                String year = Integer.toString(cal.get(Calendar.YEAR));
                String month = Integer.toString(cal.get(Calendar.MONTH)+1);
                String day = Integer.toString(cal.get(Calendar.DAY_OF_MONTH));
                SimpleDateFormat dformat = new SimpleDateFormat("d");
                SimpleDateFormat mformat = new SimpleDateFormat("M");
                SimpleDateFormat yformat = new SimpleDateFormat("yyyy");
                String d = dformat.format(past);
                String m = mformat.format(past);
                String y = yformat.format(past);

                if(day.equals(d) && month.equals(m) && year.equals(y)){
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
                    if(days<7 && month.equals(m) && year.equals(y)){
                        format = new SimpleDateFormat("HH:mm", Locale.US);
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
                        if(!year.equals(y)){
                            format = new SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.US);
                            time = format.format(past);
                            post_time.setText(time);
                        }
                        else{
                            format = new SimpleDateFormat("MMM dd HH:mm", Locale.US);
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
        }

        if(item.getItemId() == R.id.action_profile){

            mDatabaseUsers.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    final String username = (String)dataSnapshot.child(mAuth.getCurrentUser().getUid()).child("name").getValue();
                    final String userimage = (String)dataSnapshot.child(mAuth.getCurrentUser().getUid()).child("image").getValue();

                    final Intent singlePostIntent = new Intent(MainActivity.this, ProfileActivity.class);

//                    singlePostIntent.putExtra("current_username", username);
//                    singlePostIntent.putExtra("current_userimage", userimage);
                    singlePostIntent.putExtra("current_userid", mAuth.getCurrentUser().getUid());

                    startActivity(singlePostIntent);
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }

        if(item.getItemId() == R.id.action_about){
            startActivity(new Intent(MainActivity.this, AboutActivity.class));
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
