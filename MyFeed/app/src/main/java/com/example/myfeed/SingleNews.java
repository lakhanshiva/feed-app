package com.example.myfeed;

import android.os.Bundle;
import android.webkit.WebView;
import android.webkit.WebSettings;

import androidx.annotation.Nullable;

public class SingleNews extends WebViewer {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        //System.out.println("Executing SingleNews activity");
        super.onCreate(savedInstanceState);

        XmlParser.Entry item = (XmlParser.Entry) getIntent().getSerializableExtra("item");
        setContentView(R.layout.activity_web);
        WebView webView = (WebView) findViewById(R.id.webViewer);

        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);

        getSupportActionBar().setTitle(item.title);

        if (Util.isConnected(this)) {
            //generate WebView and load the url
            if(webView == null) {
                System.out.println("webView is null");
                return;
            }
            else{
                System.out.println("It looks fine");
            }
            //webView.setWebViewClient(new WebViewClient());
            //webView.loadUrl("http://www.google.com");
            //webView.loadUrl("file:///lib/test.html");
            webView.loadUrl(item.link);
        } else {
            return;
        }
    }


}
