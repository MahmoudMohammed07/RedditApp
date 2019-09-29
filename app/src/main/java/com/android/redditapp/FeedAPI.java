package com.android.redditapp;

import com.android.redditapp.model.Feed;

import retrofit2.Call;
import retrofit2.http.GET;

public interface FeedAPI {
    String BASE_URL = "https://www.reddit.com/r/";

    @GET("EarthPorn/.rss")
    Call<Feed> getFeed();
}
