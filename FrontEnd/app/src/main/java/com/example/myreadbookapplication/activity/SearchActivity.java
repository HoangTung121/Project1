package com.example.myreadbookapplication.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myreadbookapplication.R;
import com.example.myreadbookapplication.adapter.AllBooksAdapter;
import com.example.myreadbookapplication.model.ApiResponse;
import com.example.myreadbookapplication.model.Book;
import com.example.myreadbookapplication.model.BooksResponse;
import com.example.myreadbookapplication.model.Category;
import com.example.myreadbookapplication.network.ApiService;
import com.example.myreadbookapplication.network.RetrofitClient;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SearchActivity extends AppCompatActivity {

    private EditText etSearch;
    private ImageView btnSearch;
    private ImageView btnBack;
    private RecyclerView rvSearchResults;
    private ProgressBar progressBar;
    private LinearLayout layoutEmpty;
    private AllBooksAdapter searchAdapter;
    private ApiService apiService;
    private Map<Integer, String> categoryIdToName;
    private List<Book> searchResults;
    private String currentQuery = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Đặt fullscreen
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, 
                           WindowManager.LayoutParams.FLAG_FULLSCREEN);
        
        setContentView(R.layout.activity_search);

        // Khởi tạo views
        initViews();
        
        // Khởi tạo API service
        apiService = RetrofitClient.getApiService();
        
        // Khởi tạo dữ liệu
        searchResults = new ArrayList<>();
        categoryIdToName = new HashMap<>();
        
        // Setup RecyclerView
        setupRecyclerView();
        
        // Load categories để map category ID với tên
        loadCategories();
        
        // Setup listeners
        setupListeners();
    }

    private void initViews() {
        etSearch = findViewById(R.id.et_search);
        btnSearch = findViewById(R.id.btn_search);
        btnBack = findViewById(R.id.btn_back);
        rvSearchResults = findViewById(R.id.rv_search_results);
        progressBar = findViewById(R.id.progress_bar);
        layoutEmpty = findViewById(R.id.layout_empty);
    }

    private void setupRecyclerView() {
        // Sử dụng GridLayoutManager với 2 cột như trong ảnh demo
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 3);
        rvSearchResults.setLayoutManager(gridLayoutManager);
        
        searchAdapter = new AllBooksAdapter(searchResults, this, categoryIdToName);
        rvSearchResults.setAdapter(searchAdapter);
    }

    private void setupListeners() {
        // Nút back
        btnBack.setOnClickListener(v -> finish());

        // Nút search
        btnSearch.setOnClickListener(v -> performSearch());

        // Text change listener để search real-time
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                String query = s.toString().trim();
                if (!query.equals(currentQuery)) {
                    currentQuery = query;
                    if (query.length() >= 2) {
                        // Delay search để tránh gọi API quá nhiều
                        rvSearchResults.removeCallbacks(searchRunnable);
                        rvSearchResults.postDelayed(searchRunnable, 500);
                    } else if (query.isEmpty()) {
                        clearResults();
                    }
                }
            }
        });

        // Enter key listener
        etSearch.setOnEditorActionListener((v, actionId, event) -> {
            performSearch();
            return true;
        });
    }

    private final Runnable searchRunnable = new Runnable() {
        @Override
        public void run() {
            performSearch();
        }
    };

    private void performSearch() {
        String query = etSearch.getText().toString().trim();
        if (query.isEmpty()) {
            clearResults();
            return;
        }

        if (query.equals(currentQuery) && !searchResults.isEmpty()) {
            return; // Đã search rồi, không cần search lại
        }

        currentQuery = query;
        searchBooks(query);
    }

    private void searchBooks(String query) {
        progressBar.setVisibility(View.VISIBLE);
        layoutEmpty.setVisibility(View.GONE);

        Log.d("SearchActivity", "Searching for: " + query);

        Call<ApiResponse<BooksResponse>> call = apiService.searchBooks(query, 1, 20);
        call.enqueue(new Callback<ApiResponse<BooksResponse>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<BooksResponse>> call, 
                                 @NonNull Response<ApiResponse<BooksResponse>> response) {
                progressBar.setVisibility(View.GONE);
                
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    BooksResponse booksResponse = response.body().getData();
                    if (booksResponse != null && booksResponse.getBooks() != null) {
                        searchResults.clear();
                        searchResults.addAll(booksResponse.getBooks());
                        searchAdapter.notifyDataSetChanged();
                        
                        if (searchResults.isEmpty()) {
                            showEmptyState();
                        } else {
                            hideEmptyState();
                        }
                        
                        Log.d("SearchActivity", "Found " + searchResults.size() + " books");
                    } else {
                        showEmptyState();
                    }
                } else {
                    Log.e("SearchActivity", "Search API failed: " + response.code());
                    Toast.makeText(SearchActivity.this, "Search failed", Toast.LENGTH_SHORT).show();
                    showEmptyState();
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<BooksResponse>> call, @NonNull Throwable t) {
                progressBar.setVisibility(View.GONE);
                Log.e("SearchActivity", "Search failure: " + t.getMessage());
                Toast.makeText(SearchActivity.this, "Network error", Toast.LENGTH_SHORT).show();
                showEmptyState();
            }
        });
    }

    private void loadCategories() {
        Call<ApiResponse<com.example.myreadbookapplication.model.CategoriesResponse>> call = 
            apiService.getCategories("active");
        call.enqueue(new Callback<ApiResponse<com.example.myreadbookapplication.model.CategoriesResponse>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<com.example.myreadbookapplication.model.CategoriesResponse>> call, 
                                 @NonNull Response<ApiResponse<com.example.myreadbookapplication.model.CategoriesResponse>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    com.example.myreadbookapplication.model.CategoriesResponse categoriesResponse = response.body().getData();
                    if (categoriesResponse != null && categoriesResponse.getCategories() != null) {
                        for (Category category : categoriesResponse.getCategories()) {
                            if (category != null) {
                                categoryIdToName.put(category.getId(), category.getName());
                            }
                        }
                        Log.d("SearchActivity", "Loaded " + categoryIdToName.size() + " categories");
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<com.example.myreadbookapplication.model.CategoriesResponse>> call, @NonNull Throwable t) {
                Log.e("SearchActivity", "Categories load failure: " + t.getMessage());
            }
        });
    }

    private void clearResults() {
        searchResults.clear();
        searchAdapter.notifyDataSetChanged();
        hideEmptyState();
    }

    private void showEmptyState() {
        layoutEmpty.setVisibility(View.VISIBLE);
        rvSearchResults.setVisibility(View.GONE);
    }

    private void hideEmptyState() {
        layoutEmpty.setVisibility(View.GONE);
        rvSearchResults.setVisibility(View.VISIBLE);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}
