package com.weiaett.cruelalarm;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.webkit.WebView;

public class IntroActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intro);

        WebView view = (WebView)  findViewById(R.id.webView);
        view.loadUrl("file:///android_res/raw/intro.html");
    }
}
