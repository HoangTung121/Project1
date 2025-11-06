package com.example.myreadbookapplication.activity.User;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.PopupWindow;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.myreadbookapplication.BuildConfig;
import com.example.myreadbookapplication.R;
import com.example.myreadbookapplication.model.ApiResponse;
import com.example.myreadbookapplication.model.epub.EpubModels.EpubUrlRequest;
import com.example.myreadbookapplication.model.epub.EpubModels.EpubChaptersData;
import com.example.myreadbookapplication.model.epub.EpubModels.EpubChapterContentData;
import com.example.myreadbookapplication.model.epub.EpubModels.EpubChapterContentRequest;
import com.example.myreadbookapplication.network.ApiService;
import com.example.myreadbookapplication.network.RetrofitClient;
import com.example.myreadbookapplication.utils.AuthManager;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ReadBookActivity extends AppCompatActivity {

    private WebView webViewRef;
    private ApiService apiRef;
    private String currentEpubUrl;
    private final Map<String, String> hrefToId = new HashMap<>();
    private String currentBookId;
    private int currentPage = 1; // logical page index for non-epub
    private String currentChapterId; // for epub bookmarking
    private int currentScrollPosition = 0; // for tracking scroll position within chapter
    private android.os.Handler scrollSaveHandler = new android.os.Handler();
    private Runnable scrollSaveRunnable;
    
    // Menu dropdown variables
    private PopupWindow menuPopup;
    private boolean isNightMode = false;
    private boolean isFavorite = false;
    
    // New UI elements
    private TextView tvBookTitle;
    private TextView tvAuthor;
    private TextView tvCategory;
    private ProgressBar progressBar;
    private ImageView btnFontDecrease;
    private ImageView btnFontIncrease;
    
    // Font size management
    private int currentFontSize = 30; // Default font size

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_read_book);

        ImageView backIcon = findViewById(R.id.back_icon);
        TextView tvTitle = findViewById(R.id.tv_title);
        ImageView ivCover = findViewById(R.id.iv_cover);
        WebView webView = findViewById(R.id.web_view);
        ImageView menuInBook = findViewById(R.id.menu_in_book);
        
        // Initialize new UI elements
        tvBookTitle = findViewById(R.id.tv_book_title);
        tvAuthor = findViewById(R.id.tv_author);
        tvCategory = findViewById(R.id.tv_category);
        progressBar = findViewById(R.id.progress_bar);
        btnFontDecrease = findViewById(R.id.btn_font_decrease);
        btnFontIncrease = findViewById(R.id.btn_font_increase);
        
        // Load saved states
        loadSavedStates();
        
        // Setup menu dropdown
        setupMenuDropdown(menuInBook);
        
        // Setup click listeners
        backIcon.setOnClickListener(v -> finish());
        menuInBook.setOnClickListener(v -> showMenuDropdown(menuInBook));
        this.webViewRef = webView;

        String title = getIntent().getStringExtra("title");
        String coverUrl = getIntent().getStringExtra("cover_url");
        String txtUrl = getIntent().getStringExtra("txt_url");
        String bookUrl = getIntent().getStringExtra("book_url");
        String epubUrl = getIntent().getStringExtra("epub_url");
        String author = getIntent().getStringExtra("author");
        String category = getIntent().getStringExtra("category");
        this.currentBookId = getIntent().getStringExtra("book_id");

        // Setup header title
        tvTitle.setText(title != null ? title : "");
        setupTitleScrolling(tvTitle, title);
        
        // Setup book info
        setupBookInfo(title, author, category, coverUrl);
        
        // Setup font controls
        setupFontControls();

        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setBuiltInZoomControls(false);
        settings.setDisplayZoomControls(false);
        settings.setSupportZoom(true);
        settings.setLoadWithOverviewMode(true);
        settings.setUseWideViewPort(true);
        webView.setBackgroundColor(Color.TRANSPARENT);
        webView.setScrollBarStyle(View.SCROLLBARS_OUTSIDE_OVERLAY);
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                return handleWebLink(url, view, tvTitle);
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                return handleWebLink(request.getUrl().toString(), view, tvTitle);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                // Restore scroll position if available
                restoreScrollPosition();
                // Start auto-save scroll position
                startAutoSaveScrollPosition();
                // Apply initial font size
                updateWebViewFontSize();
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
                        // Try resuming from bookmark if available
                        resumeFromBookmarkIfAny();
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
        // Save current scroll position before switching chapters
        if (this.currentChapterId != null && !this.currentChapterId.equals(chapterId)) {
            saveCurrentScrollPosition();
        }
        
        this.currentChapterId = chapterId;
        // Load saved scroll position for the new chapter
        loadSavedScrollPosition();
        
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
            saveBookmarkAndFinish();
        }
    }

    @Override
    public void onBackPressed() {
        handleBack();
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Stop auto-save when pausing
        stopAutoSaveScrollPosition();
        // Persist bookmark also on pause to be robust
        if (isFinishing()) return;
        saveBookmarkAndFinish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Cleanup handlers
        stopAutoSaveScrollPosition();
        if (scrollSaveHandler != null) {
            scrollSaveHandler.removeCallbacksAndMessages(null);
        }
    }

    private void resumeFromBookmarkIfAny() {
        try {
            if (currentBookId == null || currentBookId.isEmpty()) return;
            
            AuthManager authManager = AuthManager.getInstance(this);
            String userId = authManager.getUserId();
            String token = authManager.getAccessToken();
            
            if (userId == null || token == null || token.isEmpty()) return;
            if (apiRef == null) apiRef = RetrofitClient.getApiService();

            apiRef.getBookmark(userId, currentBookId, "Bearer " + token).enqueue(new Callback<ApiResponse<com.example.myreadbookapplication.model.HistoryItem>>() {
                @Override
                public void onResponse(Call<ApiResponse<com.example.myreadbookapplication.model.HistoryItem>> call, Response<ApiResponse<com.example.myreadbookapplication.model.HistoryItem>> response) {
                    if (response.isSuccessful() && response.body() != null && response.body().isSuccess() && response.body().getData() != null) {
                        com.example.myreadbookapplication.model.HistoryItem item = response.body().getData();
                        currentPage = item.getPage();
                        currentChapterId = item.getChapterId();
                        
                        // Load saved scroll position from local storage
                        loadSavedScrollPosition();
                        
                        if (currentChapterId != null && apiRef != null && currentEpubUrl != null && webViewRef != null) {
                            openChapter(apiRef, currentEpubUrl, currentChapterId, webViewRef, (TextView) findViewById(R.id.tv_title));
                        }
                    }
                }

                @Override
                public void onFailure(Call<ApiResponse<com.example.myreadbookapplication.model.HistoryItem>> call, Throwable t) {
                }
            });
        } catch (Exception ignored) {}
    }

    private void saveBookmarkAndFinish() {
        try {
            if (currentBookId == null || currentBookId.isEmpty()) { finish(); return; }
            
            AuthManager authManager = AuthManager.getInstance(this);
            String userId = authManager.getUserId();
            String token = authManager.getAccessToken();
            
            if (userId == null || token == null || token.isEmpty()) { finish(); return; }
            if (apiRef == null) apiRef = RetrofitClient.getApiService();

            // Save current scroll position before saving bookmark
            saveCurrentScrollPosition();

            String chapterIdToSave = currentChapterId;
            if (chapterIdToSave == null || chapterIdToSave.isEmpty()) {
                chapterIdToSave = "chapter1"; // Default chapter
            }
            
            // Log để debug
            android.util.Log.d("ReadBookActivity", "Saving bookmark - userId: " + userId + ", bookId: " + currentBookId + ", chapterId: " + chapterIdToSave);
            
            apiRef.saveBookmark(userId, currentBookId, chapterIdToSave, "Bearer " + token)
                    .enqueue(new Callback<ApiResponse>() {
                        @Override
                        public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                            finish();
                        }

                        @Override
                        public void onFailure(Call<ApiResponse> call, Throwable t) {
                            finish();
                        }
                    });
        } catch (Exception e) {
            finish();
        }
    }

    private void saveCurrentScrollPosition() {
        try {
            if (webViewRef != null && currentBookId != null && currentChapterId != null) {
                webViewRef.evaluateJavascript("window.scrollY", value -> {
                    try {
                        int scrollY = (int) Double.parseDouble(value.replace("\"", ""));
                        android.content.SharedPreferences prefs = getSharedPreferences("reading_progress", MODE_PRIVATE);
                        String key = currentBookId + "_" + currentChapterId;
                        prefs.edit().putInt(key, scrollY).apply();
                    } catch (Exception ignored) {}
                });
            }
        } catch (Exception ignored) {}
    }

    private void loadSavedScrollPosition() {
        try {
            if (currentBookId != null && currentChapterId != null) {
                android.content.SharedPreferences prefs = getSharedPreferences("reading_progress", MODE_PRIVATE);
                String key = currentBookId + "_" + currentChapterId;
                currentScrollPosition = prefs.getInt(key, 0);
            }
        } catch (Exception ignored) {}
    }

    private void restoreScrollPosition() {
        try {
            if (webViewRef != null && currentScrollPosition > 0) {
                webViewRef.post(() -> {
                    webViewRef.scrollTo(0, currentScrollPosition);
                });
            }
        } catch (Exception ignored) {}
    }

    private void startAutoSaveScrollPosition() {
        // Stop previous auto-save if running
        stopAutoSaveScrollPosition();
        
        scrollSaveRunnable = new Runnable() {
            @Override
            public void run() {
                saveCurrentScrollPosition();
                // Schedule next save in 2 seconds
                scrollSaveHandler.postDelayed(this, 2000);
            }
        };
        scrollSaveHandler.postDelayed(scrollSaveRunnable, 2000);
    }

    private void stopAutoSaveScrollPosition() {
        if (scrollSaveRunnable != null) {
            scrollSaveHandler.removeCallbacks(scrollSaveRunnable);
            scrollSaveRunnable = null;
        }
    }
    
    // Menu dropdown methods
    private void loadSavedStates() {
        SharedPreferences prefs = getSharedPreferences("app_prefs", MODE_PRIVATE);
        isNightMode = prefs.getBoolean("night_mode_" + currentBookId, false);
        isFavorite = prefs.getBoolean("favorite_" + currentBookId, false);
        
        // Apply night mode if enabled
        if (isNightMode) {
            applyNightMode();
        }
    }
    
    private void setupMenuDropdown(ImageView menuButton) {
        LayoutInflater inflater = LayoutInflater.from(this);
        View menuView = inflater.inflate(R.layout.menu_dropdown, null);
        
        menuPopup = new PopupWindow(menuView, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, true);
        menuPopup.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        menuPopup.setElevation(8);
        
        // Setup menu items
        LinearLayout menuAddFavorite = menuView.findViewById(R.id.menu_add_favorite);
        LinearLayout menuNightMode = menuView.findViewById(R.id.menu_night_mode);
        
        menuAddFavorite.setOnClickListener(v -> {
            toggleFavorite();
            menuPopup.dismiss();
        });
        
        menuNightMode.setOnClickListener(v -> {
            toggleNightMode();
            menuPopup.dismiss();
        });
    }
    
    private void showMenuDropdown(ImageView menuButton) {
        if (menuPopup != null) {
            // Calculate position to show menu aligned to the right edge of the button
            int[] location = new int[2];
            menuButton.getLocationOnScreen(location);
            int xOffset = menuButton.getWidth() - menuPopup.getWidth();
            menuPopup.showAsDropDown(menuButton, xOffset, 0);
        }
    }
    
    private void toggleFavorite() {
        isFavorite = !isFavorite;
        
        // Save to SharedPreferences
        SharedPreferences prefs = getSharedPreferences("app_prefs", MODE_PRIVATE);
        prefs.edit().putBoolean("favorite_" + currentBookId, isFavorite).apply();
        
        // Show feedback
        String message = isFavorite ? "Added to favorites" : "Removed from favorites";
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        
        // Sync with backend if user is logged in
        syncFavoriteWithBackend();
    }
    
    private void toggleNightMode() {
        isNightMode = !isNightMode;
        
        // Save to SharedPreferences
        SharedPreferences prefs = getSharedPreferences("app_prefs", MODE_PRIVATE);
        prefs.edit().putBoolean("night_mode_" + currentBookId, isNightMode).apply();
        
        // Apply night mode
        if (isNightMode) {
            applyNightMode();
            Toast.makeText(this, "Night mode enabled", Toast.LENGTH_SHORT).show();
        } else {
            applyDayMode();
            Toast.makeText(this, "Day mode enabled", Toast.LENGTH_SHORT).show();
        }
    }
    
    private void applyNightMode() {
        // Change background to dark
        findViewById(R.id.header_layout).setBackgroundColor(Color.parseColor("#1E1E1E"));
        findViewById(R.id.divider_line).setBackgroundColor(Color.parseColor("#333333"));
        
        // Change text color to light
        TextView tvTitle = findViewById(R.id.tv_title);
        tvTitle.setTextColor(Color.WHITE);
        
        // Apply dark theme to WebView
        if (webViewRef != null) {
            webViewRef.setBackgroundColor(Color.parseColor("#1E1E1E"));
            // Inject CSS for dark mode
            String darkModeCSS = "javascript:(function(){" +
                "var style = document.createElement('style');" +
                "style.innerHTML = 'body { background-color: #1E1E1E !important; color: #FFFFFF !important; }';" +
                "document.head.appendChild(style);" +
                "})()";
            webViewRef.evaluateJavascript(darkModeCSS, null);
        }
    }
    
    private void applyDayMode() {
        // Change background to light
        findViewById(R.id.header_layout).setBackgroundColor(Color.WHITE);
        findViewById(R.id.divider_line).setBackgroundColor(Color.BLACK);
        
        // Change text color to dark
        TextView tvTitle = findViewById(R.id.tv_title);
        tvTitle.setTextColor(Color.BLACK);
        
        // Apply light theme to WebView
        if (webViewRef != null) {
            webViewRef.setBackgroundColor(Color.WHITE);
            // Inject CSS for light mode
            String lightModeCSS = "javascript:(function(){" +
                "var style = document.createElement('style');" +
                "style.innerHTML = 'body { background-color: #FFFFFF !important; color: #000000 !important; }';" +
                "document.head.appendChild(style);" +
                "})()";
            webViewRef.evaluateJavascript(lightModeCSS, null);
        }
    }
    
    private void syncFavoriteWithBackend() {
        try {
            AuthManager authManager = AuthManager.getInstance(this);
            String userId = authManager.getUserId();
            String token = authManager.getAccessToken();
            
            if (userId != null && token != null && !token.isEmpty()) {
                ApiService api = RetrofitClient.getApiService();
                if (isFavorite) {
                    api.addFavorite(userId, currentBookId, "Bearer " + token).enqueue(new Callback<ApiResponse>() {
                        @Override
                        public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                            // Silent success
                        }
                        @Override
                        public void onFailure(Call<ApiResponse> call, Throwable t) {
                            // Silent failure
                        }
                    });
                } else {
                    api.removeFavorite(userId, currentBookId, "Bearer " + token).enqueue(new Callback<ApiResponse>() {
                        @Override
                        public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                            // Silent success
                        }
                        @Override
                        public void onFailure(Call<ApiResponse> call, Throwable t) {
                            // Silent failure
                        }
                    });
                }
            }
        } catch (Exception ignored) {}
    }

    /**
     * Setup title scrolling for long titles
     * @param titleView TextView to setup scrolling
     * @param title Title text
     */
    private void setupTitleScrolling(TextView titleView, String title) {
        if (title == null || title.isEmpty()) return;
        
        // Set the title
        titleView.setText(title);
        
        // Enable marquee scrolling for long titles
        titleView.post(() -> {
            // Check if text is longer than available space
            if (titleView.getLayout() != null && titleView.getText().length() > 0) {
                // Enable marquee scrolling
                titleView.setSelected(true);
            }
        });
    }

    /**
     * Setup book information display
     */
    private void setupBookInfo(String title, String author, String category, String coverUrl) {
        // Set book title
        tvBookTitle.setText(title != null ? title : "Unknown Title");
        
        // Set author
        tvAuthor.setText(author != null ? author : "Unknown Author");
        
        // Set category
        if (category != null && !category.isEmpty()) {
            tvCategory.setText(category);
            tvCategory.setVisibility(View.VISIBLE);
        } else {
            tvCategory.setVisibility(View.GONE);
        }
        
        // Load cover image
        ImageView ivCover = findViewById(R.id.iv_cover);
        if (coverUrl != null && !coverUrl.isEmpty()) {
            Glide.with(this)
                .load(coverUrl)
                .placeholder(R.drawable.default_book_cover)
                .error(R.drawable.default_book_cover)
                .into(ivCover);
        }
    }

    /**
     * Update reading progress
     */
    /**
     * Setup font size controls
     */
    private void setupFontControls() {
        btnFontDecrease.setOnClickListener(v -> {
            if (currentFontSize > 20) {
                currentFontSize -= 5;
                updateWebViewFontSize();
            }
        });
        
        btnFontIncrease.setOnClickListener(v -> {
            if (currentFontSize < 50) {
                currentFontSize += 5;
                updateWebViewFontSize();
            }
        });
    }

    /**
     * Update WebView font size
     */
    private void updateWebViewFontSize() {
        if (webViewRef != null) {
            webViewRef.post(() -> {
                webViewRef.evaluateJavascript(
                    "document.body.style.fontSize='" + currentFontSize + "px'", null);
            });
        }
    }



}


