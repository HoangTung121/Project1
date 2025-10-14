package com.example.myreadbookapplication;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;

public class ReadBookActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_read_book);

        ImageView backIcon = findViewById(R.id.back_icon);
        TextView tvTitle = findViewById(R.id.tv_title);
        ImageView ivCover = findViewById(R.id.iv_cover);
        WebView webView = findViewById(R.id.web_view);

        String title = getIntent().getStringExtra("title");
        String coverUrl = getIntent().getStringExtra("cover_url");
        String txtUrl = getIntent().getStringExtra("txt_url");
        String bookUrl = getIntent().getStringExtra("book_url");
        String epubUrl = getIntent().getStringExtra("epub_url");

        tvTitle.setText(title != null ? title : "");
        if (coverUrl != null && !coverUrl.isEmpty()) {
            Glide.with(this).load(coverUrl).into(ivCover);
        }

        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        webView.setBackgroundColor(Color.TRANSPARENT);
        webView.setWebViewClient(new WebViewClient());

        String loadUrl = null;
        if (bookUrl != null && !bookUrl.isEmpty()) {
            loadUrl = bookUrl;
        } else if (txtUrl != null && !txtUrl.isEmpty()) {
            loadUrl = txtUrl;
        } else if (epubUrl != null && !epubUrl.isEmpty()) {
            // For now, just navigate to epub file URL; full epub rendering would need a reader lib
            loadUrl = epubUrl;
        }
        if (loadUrl != null) {
            webView.setVisibility(View.VISIBLE);
            webView.loadUrl(loadUrl);
        } else {
            webView.setVisibility(View.GONE);
        }

        backIcon.setOnClickListener(v -> finish());
    }
}


