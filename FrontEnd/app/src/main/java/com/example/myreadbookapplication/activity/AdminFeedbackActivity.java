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
import com.example.myreadbookapplication.adapter.AdminFeedbackAdapter;
import com.example.myreadbookapplication.model.ApiResponse;
import com.example.myreadbookapplication.model.Feedback;
import com.example.myreadbookapplication.model.FeedbackResponse;
import com.example.myreadbookapplication.network.ApiService;
import com.example.myreadbookapplication.network.RetrofitClient;
import com.example.myreadbookapplication.utils.AuthManager;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminFeedbackActivity extends AppCompatActivity {

    private static final String TAG = "AdminFeedbackActivity";

    private TextView tvBack;
    private RecyclerView rvFeedback;
    private LinearLayout layoutEmpty;
    private ProgressBar progressBar;
    private LinearLayout navCategory;
    private LinearLayout navBook;
    private LinearLayout navFeedback;
    private LinearLayout navAccount;

    private AdminFeedbackAdapter adapter;
    private ApiService apiService;
    private AuthManager authManager;
    private List<Feedback> feedbackList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_admin_feedback);

        // Initialize services
        apiService = RetrofitClient.getApiService();
        authManager = AuthManager.getInstance(this);

        initViews();
        setupClickListeners();
        setupRecyclerView();
        loadFeedbacks();
    }

    private void initViews() {
        tvBack = findViewById(R.id.tv_back);
        rvFeedback = findViewById(R.id.rv_feedback);
        layoutEmpty = findViewById(R.id.layout_empty);
        progressBar = findViewById(R.id.progress_bar);
        
        // Bottom navigation
        navCategory = findViewById(R.id.nav_category);
        navBook = findViewById(R.id.nav_book);
        navFeedback = findViewById(R.id.nav_feedback);
        navAccount = findViewById(R.id.nav_account);
    }

    private void setupClickListeners() {
        // Back button
        tvBack.setOnClickListener(v -> finish());

        // Bottom navigation
        navCategory.setOnClickListener(v -> {
            Intent intent = new Intent(this, AdminCategoryActivity.class);
            startActivity(intent);
            finish();
        });

        navBook.setOnClickListener(v -> {
            Intent intent = new Intent(this, AdminBookActivity.class);
            startActivity(intent);
            finish();
        });

        navFeedback.setOnClickListener(v -> {
            // Already on feedback screen
        });

        navAccount.setOnClickListener(v -> {
            Intent intent = new Intent(this, AdminAccountActivity.class);
            startActivity(intent);
            finish();
        });
    }

    private void setupRecyclerView() {
        adapter = new AdminFeedbackAdapter();
        adapter.setOnFeedbackClickListener(feedback -> {
            // Handle feedback click - could show details
            Toast.makeText(this, "Feedback from: " + feedback.getEmail(), Toast.LENGTH_SHORT).show();
        });

        rvFeedback.setLayoutManager(new LinearLayoutManager(this));
        rvFeedback.setAdapter(adapter);
    }

    private void loadFeedbacks() {
        String accessToken = authManager.getAccessToken();
        
        if (accessToken == null || accessToken.isEmpty()) {
            Toast.makeText(this, "Please login as admin", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Show loading
        progressBar.setVisibility(View.VISIBLE);
        layoutEmpty.setVisibility(View.GONE);

        // Use admin API endpoint to get all feedbacks
        Call<ApiResponse<FeedbackResponse>> call = apiService.getAllFeedbacks("Bearer " + accessToken, 1, 100);
        
        call.enqueue(new Callback<ApiResponse<FeedbackResponse>>() {
            @Override
            public void onResponse(Call<ApiResponse<FeedbackResponse>> call, Response<ApiResponse<FeedbackResponse>> response) {
                progressBar.setVisibility(View.GONE);

                Log.d(TAG, "=== API RESPONSE START ===");
                Log.d(TAG, "Response code: " + response.code());
                Log.d(TAG, "Response isSuccessful: " + response.isSuccessful());
                
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<FeedbackResponse> apiResponse = response.body();
                    Log.d(TAG, "API Response success: " + apiResponse.isSuccess());
                    Log.d(TAG, "API Response message: " + apiResponse.getMessage());
                    
                    if (apiResponse.isSuccess()) {
                        try {
                            // Backend returns data as array directly
                            Object dataObj = apiResponse.getData();
                            Log.d(TAG, "Data object type: " + (dataObj != null ? dataObj.getClass().getName() : "null"));
                            
                            if (dataObj != null) {
                                Gson gson = new Gson();
                                String json = gson.toJson(dataObj);
                                Log.d(TAG, "Data JSON: " + json);
                                
                                if (json != null && !json.isEmpty()) {
                                    TypeToken<List<Feedback>> token = new TypeToken<List<Feedback>>() {};
                                    List<Feedback> feedbacksList = gson.fromJson(json, token.getType());
                                    
                                    Log.d(TAG, "Parsed feedbacks count: " + (feedbacksList != null ? feedbacksList.size() : 0));
                                    
                                    if (feedbacksList != null && !feedbacksList.isEmpty()) {
                                        feedbackList = feedbacksList;
                                        Log.d(TAG, "✓ Feedbacks loaded successfully: " + feedbackList.size());
                                        
                                        // Show first feedback as sample
                                        if (!feedbackList.isEmpty()) {
                                            Feedback first = feedbackList.get(0);
                                            Log.d(TAG, "First feedback - Email: " + first.getEmail() + ", Comment: " + first.getComment());
                                        }
                                        
                                        adapter.setFeedbackList(feedbackList);
                                        layoutEmpty.setVisibility(View.GONE);
                                        rvFeedback.setVisibility(View.VISIBLE);
                                    } else {
                                        Log.w(TAG, "⚠ Feedbacks list is empty after parsing");
                                        showEmptyState();
                                    }
                                } else {
                                    Log.e(TAG, "JSON string is empty");
                                    showEmptyState();
                                }
                            } else {
                                Log.e(TAG, "✗ Data object is null");
                                showEmptyState();
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "✗ Error parsing feedbacks: " + e.getMessage(), e);
                            e.printStackTrace();
                            showEmptyState();
                        }
                    } else {
                        Log.e(TAG, "✗ API Response not successful");
                        showEmptyState();
                    }
                } else {
                    Log.e(TAG, "✗ Response failed. Code: " + response.code());
                    if (response.errorBody() != null) {
                        try {
                            String errorBody = response.errorBody().string();
                            Log.e(TAG, "Error body: " + errorBody);
                        } catch (Exception e) {
                            Log.e(TAG, "Error reading error body", e);
                        }
                    }
                    showEmptyState();
                }
                Log.d(TAG, "=== API RESPONSE END ===");
            }

            @Override
            public void onFailure(Call<ApiResponse<FeedbackResponse>> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                Log.e(TAG, "Error loading feedbacks: " + t.getMessage());
                Toast.makeText(AdminFeedbackActivity.this, "Failed to load feedbacks", Toast.LENGTH_SHORT).show();
                showEmptyState();
            }
        });
    }

    private void showEmptyState() {
        layoutEmpty.setVisibility(View.VISIBLE);
        rvFeedback.setVisibility(View.GONE);
    }
}


