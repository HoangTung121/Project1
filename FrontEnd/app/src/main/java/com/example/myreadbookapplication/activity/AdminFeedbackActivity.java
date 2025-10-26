package com.example.myreadbookapplication.activity;

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
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

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
            // Navigate to admin category management
            // Intent intent = new Intent(this, AdminCategoryActivity.class);
            // startActivity(intent);
            Toast.makeText(this, "Category feature coming soon", Toast.LENGTH_SHORT).show();
        });

        navBook.setOnClickListener(v -> {
            // Navigate to admin book management
            // Intent intent = new Intent(this, AdminBookActivity.class);
            // startActivity(intent);
            Toast.makeText(this, "Book management feature coming soon", Toast.LENGTH_SHORT).show();
        });

        navFeedback.setOnClickListener(v -> {
            // Already on feedback screen
        });

        navAccount.setOnClickListener(v -> {
            // Navigate to admin account
            // Intent intent = new Intent(this, AdminAccountActivity.class);
            // startActivity(intent);
            Toast.makeText(this, "Account management feature coming soon", Toast.LENGTH_SHORT).show();
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

        // TODO: Use admin API endpoint to get all feedbacks
        // For now, using user's feedback endpoint as placeholder
        // In production, you need a separate admin endpoint: GET /api/feedback/admin/all
        Call<ApiResponse> call = apiService.getMyFeedbacks("Bearer " + accessToken, 1, 100);
        
        call.enqueue(new Callback<ApiResponse>() {
            @Override
            public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                progressBar.setVisibility(View.GONE);

                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse apiResponse = response.body();
                    if (apiResponse.isSuccess()) {
                        try {
                            // Parse feedbacks from response
                            Object dataObj = apiResponse.getData();
                            if (dataObj != null) {
                                Gson gson = new Gson();
                                String dataJson = gson.toJson(dataObj);
                                JsonArray feedbackArray = JsonParser.parseString(dataJson).getAsJsonArray();

                                feedbackList.clear();
                                for (int i = 0; i < feedbackArray.size(); i++) {
                                    JsonObject feedbackJson = feedbackArray.get(i).getAsJsonObject();
                                    Feedback feedback = gson.fromJson(feedbackJson, Feedback.class);
                                    feedbackList.add(feedback);
                                }

                                adapter.setFeedbackList(feedbackList);
                                
                                if (feedbackList.isEmpty()) {
                                    layoutEmpty.setVisibility(View.VISIBLE);
                                    rvFeedback.setVisibility(View.GONE);
                                } else {
                                    layoutEmpty.setVisibility(View.GONE);
                                    rvFeedback.setVisibility(View.VISIBLE);
                                }
                            } else {
                                showEmptyState();
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "Error parsing feedbacks: " + e.getMessage());
                            showEmptyState();
                        }
                    } else {
                        showEmptyState();
                    }
                } else {
                    Log.e(TAG, "Failed to load feedbacks: " + response.code());
                    showEmptyState();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse> call, Throwable t) {
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

