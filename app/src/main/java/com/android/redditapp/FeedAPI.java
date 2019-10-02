package com.android.redditapp;

import com.android.redditapp.Account.CheckLogin;
import com.android.redditapp.model.Feed;

import java.util.Map;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.HeaderMap;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface FeedAPI {
    String BASE_URL = "https://www.reddit.com/r/";

//    @GET("EarthPorn/.rss")
//    Call<Feed> getFeed();

    @GET("{feed_name}/.rss")
    Call<Feed> getFeed(@Path("feed_name") String feedName);

    @POST("{user}")
    Call<CheckLogin> signIn(
            @HeaderMap Map<String, String> headers,
            @Path("user") String username,
            @Query("user") String user,
            @Query("password") String password,
            @Query("api_type") String type
    );
}
