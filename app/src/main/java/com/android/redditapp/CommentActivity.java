package com.android.redditapp;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.redditapp.adapter.CommentsAdapter;
import com.android.redditapp.common.URLS;
import com.android.redditapp.model.Comment;
import com.android.redditapp.model.Feed;
import com.android.redditapp.model.entry.Entry;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.simplexml.SimpleXmlConverterFactory;

public class CommentActivity extends AppCompatActivity {

    private static final String TAG = "CommentActivity";
    URLS urls = new URLS();

    private static String postUrl;
    private static String postThumbnailUrl;
    private static String postTitle;
    private static String postAuthor;
    private static String postUpdated;

    private String currentFeed;

    private ArrayList<Comment> mComments;

    private RecyclerView recyclerView;
    private CommentsAdapter adapter;

    private TextView progressText;
    private ProgressBar mProgressBar;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comments);
        progressText = findViewById(R.id.tv_progressText);
        mProgressBar = findViewById(R.id.pb_commentLoading);
        Log.d(TAG, "onCreate: Started...");

        mProgressBar.setVisibility(View.VISIBLE);

        recyclerView = findViewById(R.id.rv_comments);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        initPost();

        init();
    }

    private void init() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(urls.BASE_URL)
                .addConverterFactory(SimpleXmlConverterFactory.create())
                .build();

        FeedAPI feedAPI = retrofit.create(FeedAPI.class);

        Call<Feed> call = feedAPI.getFeed(currentFeed);

        call.enqueue(new retrofit2.Callback<Feed>() {
            @Override
            public void onResponse(Call<Feed> call, Response<Feed> response) {
                //Log.d(TAG, "onResponse: feed: " + response.body().getEntries());
                Log.d(TAG, "onResponse: Server Response: " + response.toString());

                mComments = new ArrayList<>();
                List<Entry> entries = response.body().getEntries();

                for (int i = 0; i < entries.size(); i++) {
                    ExtractXML extract = new ExtractXML("<div class=\"md\"><p>", "</p>",
                            entries.get(i).getContent());
                    List<String> commentDetails = extract.start();

                    try {
                        mComments.add(new Comment(
                                commentDetails.get(0),
                                entries.get(i).getAuthor().getName(),
                                entries.get(i).getUpdated(),
                                entries.get(i).getId()
                        ));
                    } catch (IndexOutOfBoundsException e) {
                        mComments.add(new Comment(
                                "Error reading comment",
                                "None",
                                "None",
                                "None"
                        ));
                        Log.d(TAG, "onResponse: IndexOutOfBoundsException: " + e.getMessage());
                    } catch (NullPointerException e) {
                        mComments.add(new Comment(
                                commentDetails.get(0),
                                "None",
                                entries.get(i).getUpdated(),
                                entries.get(i).getId()
                        ));
                        Log.d(TAG, "onResponse: NullPointerException: " + e.getMessage());
                    }
                }

                adapter = new CommentsAdapter(CommentActivity.this, mComments);
                adapter.notifyDataSetChanged();
                recyclerView.setAdapter(adapter);

                mProgressBar.setVisibility(View.GONE);
                progressText.setVisibility(View.GONE);

            }

            @Override
            public void onFailure(Call<Feed> call, Throwable t) {
                Log.e(TAG, "onFailure: Unable to retrieve RSS: " + t.getMessage());
                Toast.makeText(CommentActivity.this, "An Error Occurred", Toast.LENGTH_SHORT).show();
            }
        });
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

        try {
            String[] splitURL = postUrl.split(urls.BASE_URL);
            currentFeed = splitURL[1];
            Log.d(TAG, "initPost: currentFeed: " + currentFeed);
        } catch (ArrayIndexOutOfBoundsException e) {
            Log.d(TAG, "initPost: ArrayIndexOutOfBoundsException: " + e.getMessage());
        }
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
