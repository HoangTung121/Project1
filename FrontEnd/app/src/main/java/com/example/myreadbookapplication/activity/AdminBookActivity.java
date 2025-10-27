package com.example.myreadbookapplication.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myreadbookapplication.R;
import com.example.myreadbookapplication.model.ApiResponse;
import com.example.myreadbookapplication.model.BooksResponse;
import com.example.myreadbookapplication.model.Book;
import com.example.myreadbookapplication.network.ApiService;
import com.example.myreadbookapplication.network.RetrofitClient;
import com.example.myreadbookapplication.utils.AuthManager;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminBookActivity extends AppCompatActivity {

    private static final String TAG = "AdminBookActivity";

    private TextView tvBack;
    private RecyclerView rvBook;
    private LinearLayout layoutEmpty;
    private ProgressBar progressBar;
    private LinearLayout navCategory;
    private LinearLayout navBook;
    private LinearLayout navFeedback;
    private LinearLayout navAccount;

    private ApiService apiService;
    private AuthManager authManager;
    private List<Book> bookList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_admin_book);

        apiService = RetrofitClient.getApiService();
        authManager = AuthManager.getInstance(this);

        initViews();
        setupClickListeners();
        loadBooks();
    }

    private void initViews() {
        tvBack = findViewById(R.id.tv_back);
        rvBook = findViewById(R.id.rv_book);
        layoutEmpty = findViewById(R.id.layout_empty);
        progressBar = findViewById(R.id.progress_bar);
        
        // Bottom navigation
        navCategory = findViewById(R.id.nav_category);
        navBook = findViewById(R.id.nav_book);
        navFeedback = findViewById(R.id.nav_feedback);
        navAccount = findViewById(R.id.nav_account);
    }

    private void setupClickListeners() {
        tvBack.setOnClickListener(v -> finish());

        navCategory.setOnClickListener(v -> {
            Intent intent = new Intent(this, AdminCategoryActivity.class);
            startActivity(intent);
            finish();
        });

        navBook.setOnClickListener(v -> {
            // Already on book screen
        });

        navFeedback.setOnClickListener(v -> {
            Intent intent = new Intent(this, AdminFeedbackActivity.class);
            startActivity(intent);
            finish();
        });

        navAccount.setOnClickListener(v -> {
            Intent intent = new Intent(this, AdminAccountActivity.class);
            startActivity(intent);
            finish();
        });
    }

    private void loadBooks() {
        String accessToken = authManager.getAccessToken();
        
        if (accessToken == null || accessToken.isEmpty()) {
            Toast.makeText(this, "Please login as admin", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        layoutEmpty.setVisibility(View.GONE);

        Call<ApiResponse<BooksResponse>> call = apiService.getAllBooks("Bearer " + accessToken, 1, 100);
        
        call.enqueue(new Callback<ApiResponse<BooksResponse>>() {
            @Override
            public void onResponse(Call<ApiResponse<BooksResponse>> call, Response<ApiResponse<BooksResponse>> response) {
                progressBar.setVisibility(View.GONE);

                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<BooksResponse> apiResponse = response.body();
                    if (apiResponse.isSuccess()) {
                        try {
                            BooksResponse booksResponse = apiResponse.getData();
                            if (booksResponse != null && booksResponse.getBooks() != null) {
                                bookList = booksResponse.getBooks();
                                // TODO: Setup RecyclerView adapter
                                
                                if (bookList.isEmpty()) {
                                    layoutEmpty.setVisibility(View.VISIBLE);
                                    rvBook.setVisibility(View.GONE);
                                } else {
                                    layoutEmpty.setVisibility(View.GONE);
                                    rvBook.setVisibility(View.VISIBLE);
                                }
                            } else {
                                showEmptyState();
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "Error parsing books: " + e.getMessage());
                            showEmptyState();
                        }
                    } else {
                        showEmptyState();
                    }
                } else {
                    Log.e(TAG, "Failed to load books: " + response.code());
                    showEmptyState();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<BooksResponse>> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                Log.e(TAG, "Error loading books: " + t.getMessage());
                Toast.makeText(AdminBookActivity.this, "Failed to load books", Toast.LENGTH_SHORT).show();
                showEmptyState();
            }
        });
    }

    private void showEmptyState() {
        layoutEmpty.setVisibility(View.VISIBLE);
        rvBook.setVisibility(View.GONE);
    }
}

