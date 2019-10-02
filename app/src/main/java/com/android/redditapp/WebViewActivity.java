package com.android.redditapp;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.TextView;

public class WebViewActivity extends AppCompatActivity {

    private static final String TAG = "WebViewActivity";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_webview);

        WebView webView = findViewById(R.id.webView);
        final ProgressBar progressBar = findViewById(R.id.pb_webViewLoading);
        final TextView loadingText = findViewById(R.id.tv_progressText);
        Log.d(TAG, "onCreate: Started...");

        progressBar.setVisibility(View.VISIBLE);

        Intent intent = getIntent();
        String url = intent.getStringExtra("PostURL");
        webView.getSettings().setJavaScriptEnabled(true);
        webView.loadUrl(url);

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                progressBar.setVisibility(View.GONE);
                loadingText.setVisibility(View.GONE);
            }
        });
    }
}
