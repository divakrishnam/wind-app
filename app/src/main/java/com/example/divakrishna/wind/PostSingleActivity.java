package com.example.divakrishna.wind;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class PostSingleActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_single);

        String post_key = getIntent().getExtras().getString("post_id");
    }
}
