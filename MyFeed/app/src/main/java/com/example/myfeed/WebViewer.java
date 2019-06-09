package com.example.myfeed;

import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import android.webkit.WebView;
import android.webkit.WebViewClient;


public class WebViewer extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        XmlParser.Entry item = (XmlParser.Entry) getIntent().getSerializableExtra("item");
        WebView webView = (WebView) findViewById(R.id.webViewer);

        getSupportActionBar().setTitle(item.title);

        if (Util.isConnected(this)) {
            //generate WebView and load the url
            if(webView == null)
                return;
            webView.setWebViewClient(new WebViewClient());
            webView.loadUrl(item.link);
        } else {
            webView.loadData(generateHtml(item), "text/html; charset=UTF-8", null);
        }
    }

    private String generateHtml(XmlParser.Entry item) {
        String html = "<h2>" + item.title + "</h2>"
                + "<hr>"
                + "<p>" + item.description + "</p>"
                + "<hr>"
                + "<p style='text-align: right'><i>" + item.author + "</i></p>"
                + "<hr>"
                + "<p>" + item.date + "</p>";
        return html;
    }
}
