package com.android.redditapp;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.redditapp.adapter.PostsAdapter;
import com.android.redditapp.common.URLS;
import com.android.redditapp.interfaces.OnItemClickListener;
import com.android.redditapp.model.Feed;
import com.android.redditapp.model.entry.Entry;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.simplexml.SimpleXmlConverterFactory;

public class MainActivity extends AppCompatActivity implements OnItemClickListener {

    private static final String TAG = "MainActivity";
    URLS urls = new URLS();

    private RecyclerView recyclerView;
    private PostsAdapter adapter;
    private Button searchButton;
    private EditText mFeedName;
    private String currentFeed;
    private ArrayList<Post> posts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        searchButton = findViewById(R.id.btn_search);
        mFeedName = findViewById(R.id.et_feed_name);

        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String feedName = mFeedName.getText().toString();
                if (!feedName.isEmpty()) {
                    currentFeed = feedName;
                    init();
                } else {
                    Toast.makeText(MainActivity.this, "Please, enter a sub reddit!", Toast.LENGTH_SHORT).show();
                }
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
        call.enqueue(new Callback<Feed>() {
            @Override
            public void onResponse(Call<Feed> call, Response<Feed> response) {
                //Log.d(TAG, "onResponse: feed: " + response.body().getEntries());
                Log.d(TAG, "onResponse: Server Response: " + response.toString());

                List<Entry> entries = response.body().getEntries();

                Log.d(TAG, "onResponse: entries: " + response.body().getEntries());

//                Log.d(TAG, "onResponse: author: " + entries.get(0).getAuthor().getName());
//                Log.d(TAG, "onResponse: updated: " + entries.get(0).getUpdated());
//                Log.d(TAG, "onResponse: title: " + entries.get(0).getTitle());

                posts = new ArrayList<>();

                for (int i = 0; i < entries.size(); i++) {
                    ExtractXML extractXML1 = new ExtractXML("<a href=", entries.get(i).getContent());
                    List<String> postContent = extractXML1.start();

                    ExtractXML extractXML2 = new ExtractXML("<img src=", entries.get(i).getContent());

                    try {
                        postContent.add(extractXML2.start().get(0));
                    } catch (NullPointerException e) {
                        postContent.add(null);
                        Log.e(TAG, "onResponse: NullPointerException(thumbnail): " + e.getMessage());
                    } catch (IndexOutOfBoundsException e) {
                        postContent.add(null);
                        Log.e(TAG, "onResponse: IndexOutOfBoundsException(thumbnail): " + e.getMessage());
                    }

                    int lastPosition = postContent.size() - 1;
                    try {
                        posts.add(new Post(
                                entries.get(i).getTitle(),
                                entries.get(i).getAuthor().getName(),
                                entries.get(i).getUpdated(),
                                postContent.get(0),
                                postContent.get(lastPosition)
                        ));
                    } catch (NullPointerException e) {
                        posts.add(new Post(
                                entries.get(i).getTitle(),
                                "None",
                                entries.get(i).getUpdated(),
                                postContent.get(0),
                                postContent.get(lastPosition)
                        ));
                        Log.d(TAG, "onResponse: NullPointerException: " + e.getMessage());
                    }
                }

//                for (int j = 0;j<posts.size();j++){
//                    Log.d(TAG, "onResponse: \n "+
//                            "PostURL: "+posts.get(j).getPostURL()+"\n "+
//                            "ThumbnailURL: "+posts.get(j).getThumbnailURL()+"\n "+
//                            "Title: "+posts.get(j).getTitle()+"\n "+
//                            "Author: "+posts.get(j).getAuthor()+"\n "+
//                            "Updated: "+posts.get(j).getDate_updated()+"\n ");
//                }
                adapter = new PostsAdapter(MainActivity.this, posts, MainActivity.this);
                adapter.notifyDataSetChanged();
                recyclerView.setAdapter(adapter);
            }

            @Override
            public void onFailure(Call<Feed> call, Throwable t) {
                Log.e(TAG, "onFailure: Unable to retrieve RSS: " + t.getMessage());
                Toast.makeText(MainActivity.this, "An Error Occurred", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onItemClick(int position) {
        Log.d(TAG, "onItemClick: Clicked: " + posts.get(position).toString());
        Intent intent = new Intent(MainActivity.this, CommentActivity.class);
        intent.putExtra("PostURL", posts.get(position).getPostURL());
        intent.putExtra("ThumbnailURL", posts.get(position).getThumbnailURL());
        intent.putExtra("Title", posts.get(position).getTitle());
        intent.putExtra("Author", posts.get(position).getAuthor());
        intent.putExtra("Updated", posts.get(position).getDate_updated());
        startActivity(intent);
    }
}
