package com.example.divakrishna.wind;

    import android.content.Intent;
    import android.support.annotation.NonNull;
    import android.support.v7.app.ActionBar;
    import android.support.v7.app.AppCompatActivity;
    import android.os.Bundle;
    import android.support.v7.widget.LinearLayoutManager;
    import android.support.v7.widget.RecyclerView;
    import android.text.TextUtils;
    import android.util.DisplayMetrics;
    import android.view.LayoutInflater;
    import android.view.Menu;
    import android.view.MenuItem;
    import android.view.View;
    import android.view.ViewGroup;
    import android.widget.EditText;
    import android.widget.ImageView;
    import android.widget.TextView;

    import com.firebase.ui.database.FirebaseRecyclerAdapter;
    import com.firebase.ui.database.FirebaseRecyclerOptions;
    import com.google.firebase.auth.FirebaseAuth;
    import com.google.firebase.database.ChildEventListener;
    import com.google.firebase.database.DataSnapshot;
    import com.google.firebase.database.DatabaseError;
    import com.google.firebase.database.DatabaseReference;
    import com.google.firebase.database.FirebaseDatabase;
    import com.google.firebase.database.Query;
    import com.google.firebase.database.ValueEventListener;
    import com.squareup.picasso.Picasso;

    import java.text.ParseException;
    import java.text.SimpleDateFormat;
    import java.util.Calendar;
    import java.util.Date;
    import java.util.Locale;
    import java.util.concurrent.TimeUnit;

public class PostSingleActivity extends AppCompatActivity {

    private String mPostKey = null;
    private String mCurrentUserid = null;
    private String mCurrentUsername = null;
    private String mCurrentUserimage = null;
    private String mCurrentUserpost = null;

    private DatabaseReference mDatabase;
    private DatabaseReference mDatabaseComment;

    private TextView mPostSingleDesc;
    private TextView mPostSingleUsername;
    private TextView mPostSingleTime;
    private TextView mPostSingleSumLikes;
    private TextView mPostSingleSumComments;

    private ImageView mPostSingleImage;
    private ImageView mPostSingleSend;

    private EditText mPostSingleComment;

    private ImageView mPostSingleLikeButton;

    private DatabaseReference mDatabaseLike;

    private FirebaseAuth mAuth;

    private boolean mProcessLike = false;

    private FirebaseRecyclerAdapter<Post, CommentViewHolder> firebaseRecyclerAdapter;

    private RecyclerView mCommentList;

    private Query postQuery;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_single);

        mPostKey = getIntent().getExtras().getString("post_id");
        mCurrentUserid = getIntent().getExtras().getString("current_userid");
        mCurrentUsername = getIntent().getExtras().getString("current_username");
        mCurrentUserimage = getIntent().getExtras().getString("current_userimage");

        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle(mCurrentUsername);

        mPostSingleDesc = findViewById(R.id.singlePostDesc);
        mPostSingleUsername = findViewById(R.id.singlePostUsername);
        mPostSingleTime = findViewById(R.id.singlePostTime);
        mPostSingleImage = findViewById(R.id.singlePostImage);

        mPostSingleLikeButton = findViewById(R.id.lovePostButton);

        mPostSingleSumLikes = findViewById(R.id.sumPostLoves);

        mPostSingleSumComments = findViewById(R.id.sumPostComments);

        mPostSingleSend = findViewById(R.id.singlePostSend);
        mPostSingleComment = findViewById(R.id.singlePostComment);

        mAuth = FirebaseAuth.getInstance();

        mDatabaseLike = FirebaseDatabase.getInstance().getReference().child("Likes");
        mDatabaseLike.keepSynced(true);

        mDatabase = FirebaseDatabase.getInstance().getReference().child("Post");
        mDatabase.keepSynced(true);

        mDatabaseComment = FirebaseDatabase.getInstance().getReference().child("Comments").child(mPostKey);
        mDatabaseComment.keepSynced(true);

        postQuery = mDatabaseComment.orderByKey();

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);

        mCommentList = findViewById(R.id.comment_list);
        mCommentList.setHasFixedSize(true);
        mCommentList.setLayoutManager(layoutManager);

        FirebaseRecyclerOptions postOptions = new FirebaseRecyclerOptions.Builder<Post>().setQuery(postQuery, Post.class).build();
        firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Post, CommentViewHolder>(postOptions) {

            @NonNull
            @Override
            public CommentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.comment_row, parent, false);
                return new CommentViewHolder(view);
            }

            @Override
            protected void onBindViewHolder(@NonNull CommentViewHolder viewHolder, int position, @NonNull Post model) {

                final String comment_key = getRef(position).getKey();

                viewHolder.setComment(model.getComment());
                viewHolder.setUsername(model.getUsername());
                viewHolder.setUserimage(model.getUserimage());
                viewHolder.setTime(model.getTimestamp());

                viewHolder.mView.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {

                        mDatabaseComment.child(comment_key).removeValue();

                        return true;
                    }
                });
            }
        };

        mDatabaseComment.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                mCommentList.setAdapter(firebaseRecyclerAdapter);
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                mCommentList.setAdapter(firebaseRecyclerAdapter);
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                mCommentList.setAdapter(firebaseRecyclerAdapter);
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        mDatabase.child(mPostKey).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                    String post_desc = (String)dataSnapshot.child("desc").getValue();
                    final String post_username = (String)dataSnapshot.child("username").getValue();
                    String post_time = (String)dataSnapshot.child("timestamp").getValue();
                    final String post_image = (String)dataSnapshot.child("userimage").getValue();

                    try{
                        SimpleDateFormat format =new SimpleDateFormat("yyyy.MM.dd HH:mm:ss");
                        Date past = format.parse(post_time);
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
                                post_time = "Just Now";
                                mPostSingleTime.setText(post_time);
                            } else if(minutes<60){
                                post_time = minutes+" minutes ago";
                                mPostSingleTime.setText(post_time);
                            } else if(hours<24){
                                post_time = hours+" hour ago";
                                mPostSingleTime.setText(post_time);
                            }
                        } else {
                            if(days<7){
                                format = new SimpleDateFormat("HH:mm");
                                if(days<2){
                                    post_time = "Yesterday at "+format.format(past);
                                    mPostSingleTime.setText(post_time);
                                }else {
                                    post_time = days+" days ago at "+format.format(past);
                                    mPostSingleTime.setText(post_time);
                                }
                            }
                            else
                            {
                                String year = Integer.toString(cal.get(Calendar.YEAR));
                                format = new SimpleDateFormat("yyyy");
                                String y = format.format(past);

                                if(year.equals(y)){
                                    format = new SimpleDateFormat("MMM dd HH:mm", Locale.US);
                                    post_time = format.format(past);
                                    mPostSingleTime.setText(post_time);
                                }
                                else{
                                    format = new SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.US);
                                    post_time = format.format(past);
                                    mPostSingleTime.setText(post_time);
                                }

                            }
                        }
                    }catch (ParseException e){
                        e.printStackTrace();
                    }
                    mPostSingleDesc.setText(post_desc);
                    mPostSingleUsername.setText(post_username);

                DisplayMetrics displayMetrics = new DisplayMetrics();
                getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
                int height = displayMetrics.heightPixels;
                int width = displayMetrics.widthPixels;

                    Picasso.get().load(post_image).transform(new RoundTransformation(width/2*4,1)).into(mPostSingleImage);
                }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        mPostSingleSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String comment_val = mPostSingleComment.getText().toString().trim();

                final DatabaseReference newComment = mDatabaseComment.push();

                final String timeStamp = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss").format(new Date()).toString();

                if(!TextUtils.isEmpty(comment_val)){

                    mDatabaseComment.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            newComment.child("comment").setValue(comment_val);
                            newComment.child("username").setValue(mCurrentUsername);
                            newComment.child("userimage").setValue(mCurrentUserimage);
                            newComment.child("uid").setValue(mCurrentUserid);
                            newComment.child("timestamp").setValue(timeStamp);
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });

                    mPostSingleComment.setText("");
                }
            }
        });

        mDatabaseComment.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                int count = (int) dataSnapshot.getChildrenCount();
                String sum = Integer.toString(count) + " comments";
                mPostSingleSumComments.setText(sum);

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        mDatabaseLike.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                int count = (int) dataSnapshot.child(mPostKey).getChildrenCount();
                String sum = Integer.toString(count) + " likes";
                mPostSingleSumLikes.setText(sum);

                if(dataSnapshot.child(mPostKey).hasChild(mAuth.getCurrentUser().getUid())){
                    mPostSingleLikeButton.setImageResource(R.mipmap.ic_love_clicked);
                } else{
                    mPostSingleLikeButton.setImageResource(R.mipmap.ic_love);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        mPostSingleLikeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mProcessLike = true;
                mDatabaseLike.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        if(mProcessLike){
                            if(dataSnapshot.child(mPostKey).hasChild(mAuth.getCurrentUser().getUid())){
                                mDatabaseLike.child(mPostKey).child(mAuth.getCurrentUser().getUid()).removeValue();

                                mProcessLike = false;
                            }else{
                                mDatabaseLike.child(mPostKey).child(mAuth.getCurrentUser().getUid()).setValue("Random value");

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

    @Override
    public void onStart() {
        super.onStart();
        firebaseRecyclerAdapter.startListening();
    }

    @Override
    public void onStop() {
        super.onStop();
        firebaseRecyclerAdapter.stopListening();
    }

    public class CommentViewHolder extends RecyclerView.ViewHolder{

        View mView;

        public CommentViewHolder(View itemView) {
            super(itemView);

            mView = itemView;
        }

        public void setUserimage(final String userimage) {
            ImageView comment_userimage = mView.findViewById(R.id.commentUserimage);

            DisplayMetrics displayMetrics = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
            int height = displayMetrics.heightPixels;
            int width = displayMetrics.widthPixels;
            Picasso.get().load(userimage).transform(new RoundTransformation(width/2*4,1)).into(comment_userimage);
        }

        public void setUsername(String username){
            TextView comment_username = mView.findViewById(R.id.commentUsername);
            comment_username.setText(username);
        }

        public void setComment(String comment){
            TextView comment_desc = mView.findViewById(R.id.commentPost);
            comment_desc.setText(comment);
        }

        public void setTime(String time){
            TextView post_time = mView.findViewById(R.id.commentPostTime);

            try{
                SimpleDateFormat format =new SimpleDateFormat("yyyy.MM.dd HH:mm:ss");
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
                            format = new SimpleDateFormat("MMM dd HH:mm", Locale.US);
                            time = format.format(past);
                            post_time.setText(time);
                        }
                        else{
                            format = new SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.US);
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
        getMenuInflater().inflate(R.menu.post_selected_menu, menu);
        mCurrentUserid = getIntent().getExtras().getString("current_userid");
        mCurrentUserpost = getIntent().getExtras().getString("current_userpost");
        if(mCurrentUserpost.equals(mCurrentUserid)){
            return true;
        }
        return false;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

            if(item.getItemId() == R.id.action_edit){

            }

            if (item.getItemId() == R.id.action_delete){

                mDatabase.child(mPostKey).removeValue();
                mDatabaseLike.child(mPostKey).removeValue();
                mDatabaseComment.removeValue();

                startActivity(new Intent(PostSingleActivity.this, MainActivity.class));
                finish();
            }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        return super.onPrepareOptionsMenu(menu);
    }
}
