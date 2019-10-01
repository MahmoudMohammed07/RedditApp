package com.android.redditapp;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

public class CommentActivity extends AppCompatActivity {

    private static final String TAG = "CommentActivity";

    private static String postUrl;
    private static String postThumbnailUrl;
    private static String postTitle;
    private static String postAuthor;
    private static String postUpdated;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comments);
        Log.d(TAG, "onCreate: Started...");

        initPost();
    }

    private void initPost() {
        Intent incomingIntent = getIntent();
        postUrl = incomingIntent.getStringExtra("PostURL");
        postThumbnailUrl = incomingIntent.getStringExtra("ThumbnailURL");
        postTitle = incomingIntent.getStringExtra("Title");
        postAuthor = incomingIntent.getStringExtra("Author");
        postUpdated = incomingIntent.getStringExtra("Updated");

        TextView title = findViewById(R.id.tv_postTitle);
        TextView author = findViewById(R.id.tv_postAuthor);
        TextView updated = findViewById(R.id.tv_postUpdated);
        ImageView thumbnail = findViewById(R.id.iv_postThumbnail);
        Button btnReply = findViewById(R.id.btn_postReply);
        final ProgressBar progressBar = findViewById(R.id.pb_postLoading);

        title.setText(postTitle);
        author.setText(postAuthor);
        updated.setText(postUpdated);

        displayImage(postThumbnailUrl, thumbnail, progressBar);
    }

    private void displayImage(String imageURL, ImageView imageView, final ProgressBar progressBar) {
        Picasso.with(this).load(imageURL)
                .placeholder(R.drawable.reddit_alien)
                .error(R.drawable.reddit_alien)
                .into(imageView, new Callback() {
                    @Override
                    public void onSuccess() {
                        progressBar.setVisibility(View.GONE);
                    }

                    @Override
                    public void onError() {
                        progressBar.setVisibility(View.GONE);
                    }
                });
    }
}
