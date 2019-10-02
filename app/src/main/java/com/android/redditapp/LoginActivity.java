package com.android.redditapp;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LoginActivity";

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

                if (!username.isEmpty()&&!password.isEmpty()){
                    mProgressBar.setVisibility(View.VISIBLE);
                }
            }
        });
    }
}
