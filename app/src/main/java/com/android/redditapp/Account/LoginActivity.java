package com.android.redditapp.Account;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.android.redditapp.FeedAPI;
import com.android.redditapp.R;
import com.android.redditapp.common.URLS;

import java.util.HashMap;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.simplexml.SimpleXmlConverterFactory;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LoginActivity";
    private URLS urls = new URLS();

    private ProgressBar mProgressBar;
    private EditText mUserName;
    private EditText mPassword;
    private Button btnLogin;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        Log.d(TAG, "onCreate: Started..");

        btnLogin = findViewById(R.id.btn_login);
        mUserName = findViewById(R.id.input_username);
        mPassword = findViewById(R.id.input_password);
        mProgressBar = findViewById(R.id.pb_loginRequestLoading);
        mProgressBar.setVisibility(View.GONE);

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "onClick: Attempting to log in...");
                String username = mUserName.getText().toString();
                String password = mPassword.getText().toString();

                if (!username.isEmpty() && !password.isEmpty()) {
                    mProgressBar.setVisibility(View.VISIBLE);
                    login(username, password);
                }
            }
        });
    }

    private void login(final String username, String password) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(urls.LOGIN_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        FeedAPI feedAPI = retrofit.create(FeedAPI.class);

        HashMap<String, String> headerMap = new HashMap<>();
        headerMap.put("Content-Type", "application/json");

        Call<CheckLogin> call = feedAPI.signIn(headerMap, username, username, password, "json");

        call.enqueue(new Callback<CheckLogin>() {
            @Override
            public void onResponse(Call<CheckLogin> call, Response<CheckLogin> response) {
                try {
//                Log.d(TAG, "onResponse: feed: " + response.body().toString());
                    Log.d(TAG, "onResponse: Server Response: " + response.toString());

                    String modhash = response.body().getJson().getData().getModhash();
                    String cookie = response.body().getJson().getData().getCookie();
                    Log.d(TAG, "onResponse: modhash: " + modhash);
                    Log.d(TAG, "onResponse: cookie: " + cookie);

                    if (!modhash.isEmpty()) {
                        setSessionParams(username, modhash, cookie);
                        mProgressBar.setVisibility(View.GONE);
                        mUserName.setText("");
                        mPassword.setText("");
                        Toast.makeText(LoginActivity.this, "Login Successful", Toast.LENGTH_SHORT).show();

                        finish();
                    }
                } catch (NullPointerException e) {
                    Log.d(TAG, "onResponse: NullPointerException: " + e.getMessage());
                }
            }

            @Override
            public void onFailure(Call<CheckLogin> call, Throwable t) {
                mProgressBar.setVisibility(View.GONE);
                Log.d(TAG, "onFailure: Unable to retrive RSS: " + t.getMessage());
                Toast.makeText(LoginActivity.this, "An Error Occurred", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setSessionParams(String username, String modhash, String cookie) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(LoginActivity.this);
        SharedPreferences.Editor editor = preferences.edit();

        Log.d(TAG, "setSessionParams: Storing session variables: \n" +
                "username: " + username + "\n" +
                "modhash: " + modhash + "\n" +
                "cookie: " + cookie + "\n");

        editor.putString("SessionUsername", username);
        editor.apply();
        editor.putString("SessionModhash", modhash);
        editor.apply();
        editor.putString("SessionCookie", cookie);
        editor.apply();
    }
}
