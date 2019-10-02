package com.android.redditapp;

import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.redditapp.Account.LoginActivity;
import com.android.redditapp.adapter.CommentsAdapter;
import com.android.redditapp.common.URLS;
import com.android.redditapp.interfaces.OnItemClickListener;
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

public class CommentActivity extends AppCompatActivity implements OnItemClickListener {

    private static final String TAG = "CommentActivity";
    URLS urls = new URLS();

    private static String postUrl;
    private static String postThumbnailUrl;
    private static String postTitle;
    private static String postAuthor;
    private static String postUpdated;
    private static String postId;

    private String modhash, cookie, username;

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

        setupToolbar();

        getSessionParams();

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

    @Override
    protected void onPostResume() {
        super.onPostResume();
        Log.d(TAG, "onPostResume: Resuming Activity");
        getSessionParams();
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolBar_main);
        setSupportActionBar(toolbar);

        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                Log.d(TAG, "onMenuItemClick: clicked menu item: " + menuItem);

                switch (menuItem.getItemId()) {
                    case R.id.navLogin:
                        Intent intent = new Intent(CommentActivity.this, LoginActivity.class);
                        startActivity(intent);
                }

                return false;
            }
        });
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

                adapter = new CommentsAdapter(CommentActivity.this, mComments, CommentActivity.this);
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
        postId = incomingIntent.getStringExtra("Id");

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

        btnReply.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "onClick: reply..");
                getUserComment(postId);
            }
        });

        thumbnail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "onClick: Opening URL in the webView: " + postUrl);
                Intent intent = new Intent(CommentActivity.this, WebViewActivity.class);
                intent.putExtra("PostURL", postUrl);
                startActivity(intent);
            }
        });
    }

    private void getUserComment(String postID) {
        Dialog dialog = new Dialog(CommentActivity.this);
        dialog.setTitle("Add Comment");
        dialog.setContentView(R.layout.comment_input_dialog);

        int width = (int) (getResources().getDisplayMetrics().widthPixels * 0.95);
        int height = (int) (getResources().getDisplayMetrics().heightPixels * 0.6);

        dialog.getWindow().setLayout(width, height);
        dialog.show();

        Button btnPostComment = dialog.findViewById(R.id.btn_postComment);
        final EditText comment = dialog.findViewById(R.id.et_dialogComment);

        btnPostComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "onClick: Attempting to post comment.");

            }
        });
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

    @Override
    public void onItemClick(int position) {
        getUserComment(postId);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.navigation_menu, menu);
        return true;
    }

    private void getSessionParams() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(CommentActivity.this);

        username = preferences.getString("SessionUsername", null);
        modhash = preferences.getString("SessionModhash", null);
        cookie = preferences.getString("SessionCookie", null);

        Log.d(TAG, "getSessionParams: Storing session variables: \n" +
                "username: " + username + "\n" +
                "modhash: " + modhash + "\n" +
                "cookie: " + cookie + "\n");
    }
}
