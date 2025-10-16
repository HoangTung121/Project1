package com.example.myreadbookapplication;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.myreadbookapplication.model.ApiResponse;
import com.example.myreadbookapplication.model.epub.EpubModels.EpubUrlRequest;
import com.example.myreadbookapplication.model.epub.EpubModels.EpubMetadataData;
import com.example.myreadbookapplication.model.epub.EpubModels.EpubChaptersData;
import com.example.myreadbookapplication.model.epub.EpubModels.EpubChapterContentData;
import com.example.myreadbookapplication.model.epub.EpubModels.EpubChapterContentRequest;
import com.example.myreadbookapplication.network.ApiService;
import com.example.myreadbookapplication.network.RetrofitClient;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ReadBookActivity extends AppCompatActivity {

    private WebView webViewRef;
    private ApiService apiRef;
    private String currentEpubUrl;
    private final Map<String, String> hrefToId = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_read_book);

        ImageView backIcon = findViewById(R.id.back_icon);
        TextView tvTitle = findViewById(R.id.tv_title);
        ImageView ivCover = findViewById(R.id.iv_cover);
        WebView webView = findViewById(R.id.web_view);
        this.webViewRef = webView;

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
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                return handleWebLink(url, view, tvTitle);
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                return handleWebLink(request.getUrl().toString(), view, tvTitle);
            }
        });

        if (epubUrl != null && !epubUrl.isEmpty()) {
            // New flow: use backend EPUB APIs
            ApiService api = RetrofitClient.getApiService();
            this.apiRef = api;
            this.currentEpubUrl = epubUrl;
            // 1) Validate URL
            api.validateEpubUrl(new EpubUrlRequest(epubUrl)).enqueue(new Callback<ApiResponse>() {
                @Override
                public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                    if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                        // 2) Fetch metadata and chapters in parallel, then load first chapter
                        fetchChaptersAndOpenFirst(api, epubUrl, webView, tvTitle);
                    } else {
                        // Fallback to direct URL flow if validation fails
                        fallbackDirectLoad(webView, bookUrl, txtUrl, epubUrl);
                    }
                }
                @Override
                public void onFailure(Call<ApiResponse> call, Throwable t) {
                    fallbackDirectLoad(webView, bookUrl, txtUrl, epubUrl);
                }
            });
        } else {
            // No epub url; fallback to prior behavior for book/txt urls
            fallbackDirectLoad(webView, bookUrl, txtUrl, null);
        }

        backIcon.setOnClickListener(v -> handleBack());
    }

    private void fetchChaptersAndOpenFirst(ApiService api, String epubUrl, WebView webView, TextView tvTitle) {
        // Chapters
        api.getEpubChapters(new EpubUrlRequest(epubUrl)).enqueue(new Callback<ApiResponse<EpubChaptersData>>() {
            @Override
            public void onResponse(Call<ApiResponse<EpubChaptersData>> call, Response<ApiResponse<EpubChaptersData>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess() &&
                        response.body().getData() != null && response.body().getData().chapters != null && !response.body().getData().chapters.isEmpty()) {
                    // Build href->id map for link interception
                    hrefToId.clear();
                    for (com.example.myreadbookapplication.model.epub.EpubModels.ChapterItem c : response.body().getData().chapters) {
                        if (c != null && c.href != null && c.id != null) {
                            hrefToId.put(c.href, c.id);
                        }
                    }
                    // Pick first readable chapter, skip cover/wrapper
                    String chosenId = null;
                    for (com.example.myreadbookapplication.model.epub.EpubModels.ChapterItem c : response.body().getData().chapters) {
                        if (c == null || c.id == null) continue;
                        String id = c.id;
                        String href = c.href != null ? c.href.toLowerCase() : "";
                        boolean isCover = id.equalsIgnoreCase("coverpage-wrapper") || href.contains("wrap0000");
                        if (!isCover) { chosenId = id; break; }
                    }
                    if (chosenId == null) {
                        chosenId = response.body().getData().chapters.get(0).id;
                    }
                    openChapter(api, epubUrl, chosenId, webView, tvTitle);
                } else {
                    Toast.makeText(ReadBookActivity.this, "No chapters found", Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(Call<ApiResponse<EpubChaptersData>> call, Throwable t) {
                Toast.makeText(ReadBookActivity.this, "Failed to load chapters", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void openChapter(ApiService api, String epubUrl, String chapterId, WebView webView, TextView tvTitle) {
        api.getEpubChapterContent(new EpubChapterContentRequest(epubUrl, chapterId))
                .enqueue(new Callback<ApiResponse<EpubChapterContentData>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<EpubChapterContentData>> call, Response<ApiResponse<EpubChapterContentData>> response) {
                        if (response.isSuccessful() && response.body() != null && response.body().isSuccess() && response.body().getData() != null) {
                            EpubChapterContentData data = response.body().getData();
                            if (data.title != null && !data.title.isEmpty()) {
                                tvTitle.setText(data.title);
                            }
                            // Render HTML string; no external URL loaded
                            String html = data.content != null ? data.content : "";
                            // Minimal readable defaults
                            String style = "<style> body{padding:16px; line-height:1.6; font-size:16px;} img{max-width:100%; height:auto;} </style>";
                            String doc = "<html><head>" + style + "</head><body>" + html + "</body></html>";
                            webView.setVisibility(View.VISIBLE);
                            // Use backend base URL so relative resources like /images resolve
                            webView.loadDataWithBaseURL(BuildConfig.BASE_URL, doc, "text/html", "utf-8", null);
                        } else {
                            Toast.makeText(ReadBookActivity.this, "Failed to load chapter", Toast.LENGTH_SHORT).show();
                        }
                    }
                    @Override
                    public void onFailure(Call<ApiResponse<EpubChapterContentData>> call, Throwable t) {
                        Toast.makeText(ReadBookActivity.this, "Failed to load chapter", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private boolean handleWebLink(String url, WebView view, TextView tvTitle) {
        try {
            if (url == null) return true;
            // Ignore favicon
            if (url.endsWith("/favicon.ico")) return true;

            // Normalize path relative to backend base
            String path = url;
            try {
                URI uri = new URI(url);
                path = uri.getPath();
            } catch (URISyntaxException ignored) {}

            if (path == null) path = url;

            // Pattern 1: /links/{anchorOrId}/OEBPS/...  → prefer the OEBPS href if present
            if (path.startsWith("/links/")) {
                String remainder = path.substring("/links/".length());
                // If the remainder contains an OEBPS path, use it as href
                int oebpsIdx = remainder.indexOf("OEBPS/");
                if (oebpsIdx >= 0) {
                    String href = remainder.substring(oebpsIdx);
                    String mappedId = hrefToId.get(href);
                    String target = mappedId != null ? mappedId : href; // fall back to href directly
                    if (apiRef != null && currentEpubUrl != null) {
                        openChapter(apiRef, currentEpubUrl, target, view, tvTitle);
                        return true;
                    }
                } else {
                    // No OEBPS path; fall back to first segment as an id
                    int slash = remainder.indexOf('/');
                    String targetId = slash >= 0 ? remainder.substring(0, slash) : remainder;
                    if (apiRef != null && currentEpubUrl != null && targetId != null && !targetId.isEmpty()) {
                        openChapter(apiRef, currentEpubUrl, targetId, view, tvTitle);
                        return true;
                    }
                }
            }

            // Pattern 2: direct OEBPS html file links → look up by href
            if (path.contains("OEBPS/")) {
                // Extract trailing OEBPS/... file path
                int idx = path.indexOf("OEBPS/");
                String href = path.substring(idx);
                String targetId = hrefToId.get(href);
                if (targetId != null && apiRef != null && currentEpubUrl != null) {
                    openChapter(apiRef, currentEpubUrl, targetId, view, tvTitle);
                    return true;
                }
            }

            // Otherwise, let WebView handle normally within the same view
            view.loadUrl(url);
            return true;
        } catch (Exception e) {
            // Fallback to default navigation
            view.loadUrl(url);
            return true;
        }
    }

    private void fallbackDirectLoad(WebView webView, String bookUrl, String txtUrl, String epubUrl) {
        String loadUrl = null;
        if (bookUrl != null && !bookUrl.isEmpty()) {
            loadUrl = bookUrl;
        } else if (txtUrl != null && !txtUrl.isEmpty()) {
            loadUrl = txtUrl;
        } else if (epubUrl != null && !epubUrl.isEmpty()) {
            loadUrl = epubUrl;
        }
        if (loadUrl != null) {
            webView.setVisibility(View.VISIBLE);
            webView.loadUrl(loadUrl);
        } else {
            webView.setVisibility(View.GONE);
        }
    }

    private void handleBack() {
        if (webViewRef != null && webViewRef.canGoBack()) {
            webViewRef.goBack();
        } else {
            finish();
        }
    }

    @Override
    public void onBackPressed() {
        handleBack();
    }
}


