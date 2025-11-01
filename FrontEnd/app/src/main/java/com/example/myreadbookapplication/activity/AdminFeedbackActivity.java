package com.example.myreadbookapplication.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
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
import com.example.myreadbookapplication.network.ApiService;
import com.example.myreadbookapplication.network.RetrofitClient;
import com.example.myreadbookapplication.utils.AuthManager;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminFeedbackActivity extends AppCompatActivity {

    private static final String TAG = "AdminFeedbackActivity";
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
        rvFeedback = findViewById(R.id.rv_feedback);
        layoutEmpty = findViewById(R.id.layout_empty);
        progressBar = findViewById(R.id.progress_bar);
        
        // Bottom navigation
        navCategory = findViewById(R.id.nav_category_in_feedback);
        navBook = findViewById(R.id.nav_book_in_feedback);
        navFeedback = findViewById(R.id.nav_feedback_in_feedback);
        navAccount = findViewById(R.id.nav_account_in_feedback);
    }

    private void setupClickListeners() {
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
            Log.e(TAG, "✗ No access token");
            Toast.makeText(this, "Please login as admin", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Show loading
        progressBar.setVisibility(View.VISIBLE);
        layoutEmpty.setVisibility(View.GONE);

        // Backend validation only allows limit <= 100, so use 100
        String authHeader = "Bearer " + accessToken;
        int page = 1;
        int limit = 100; // Backend max limit is 100
        
        Call<ApiResponse> call = apiService.getAllFeedbacks(authHeader, page, limit);
        
        call.enqueue(new Callback<ApiResponse>() {
            @Override
            public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                progressBar.setVisibility(View.GONE);
                
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse apiResponse = response.body();
                    
                    if (apiResponse.isSuccess()) {
                        Object dataObj = apiResponse.getData();
                        
                        if (dataObj != null) {
                            try {
                                Gson gson = new Gson();
                                
                                // Convert object to JSON string
                                String json = gson.toJson(dataObj);
                                Log.d(TAG, "Data JSON length: " + json.length());
                                Log.d(TAG, "Data JSON preview: " + (json.length() > 200 ? json.substring(0, 200) + "..." : json));
                                
                                // Parse as JSON array
                                JsonElement jsonElement = JsonParser.parseString(json);
                                
                                if (jsonElement.isJsonArray()) {
                                    JsonArray jsonArray = jsonElement.getAsJsonArray();
                                    Log.d(TAG, "JSON Array size: " + jsonArray.size());
                                    
                                    TypeToken<List<Feedback>> token = new TypeToken<List<Feedback>>() {};
                                    List<Feedback> feedbacksList = gson.fromJson(jsonArray, token.getType());

                                    Log.d(TAG, "Parsed feedbacks count: " + (feedbacksList != null ? feedbacksList.size() : 0));
                                    
                                    if (feedbacksList != null && !feedbacksList.isEmpty()) {
                                        feedbackList = feedbacksList;
                                        Log.d(TAG, "✓ Successfully loaded " + feedbackList.size() + " feedbacks");
                                        
                                        // Show sample feedback
                                        Feedback first = feedbackList.get(0);
                                        Log.d(TAG, "Sample feedback - ID: " + first.getId() + 
                                              ", Email: " + (first.getEmail() != null ? first.getEmail() : "null") +
                                              ", Comment: " + (first.getComment() != null ? first.getComment().substring(0, Math.min(50, first.getComment().length())) : "null"));
                                        
                                        // Update UI
                                        adapter.setFeedbackList(feedbackList);
                                        layoutEmpty.setVisibility(View.GONE);
                                        rvFeedback.setVisibility(View.VISIBLE);
                                        
                                        Log.d(TAG, "✓ UI Updated - RecyclerView visible");
                                    } else {
                                        Log.w(TAG, "⚠ Feedbacks list is null or empty");
                                        showEmptyState();
                                    }
                                } else {
                                    Log.e(TAG, "✗ Data is NOT a JSON array. Type: " + jsonElement.getClass().getName());
                                    showEmptyState();
                                }
                            } catch (Exception e) {
                                Log.e(TAG, "✗ EXCEPTION parsing: " + e.getMessage(), e);
                                e.printStackTrace();
                                showEmptyState();
                            }
                        } else {
                            Log.e(TAG, "✗ Data object is NULL");
                            showEmptyState();
                        }
                    } else {
                        Log.e(TAG, "✗ API success = false: " + apiResponse.getMessage());
                        showEmptyState();
                    }
                } else {
                    int statusCode = response.code();
                    Log.e(TAG, "✗ Response failed. HTTP Code: " + statusCode);
                    
                    if (statusCode == 401) {
                        Log.e(TAG, "✗ 401 Unauthorized - Token invalid or expired");
                        Toast.makeText(AdminFeedbackActivity.this, "Unauthorized. Please login again.", Toast.LENGTH_SHORT).show();
                    } else if (statusCode == 403) {
                        Log.e(TAG, "✗ 403 Forbidden - No permission (may need getUsers permission)");
                        Toast.makeText(AdminFeedbackActivity.this, "Access denied. Admin permission required.", Toast.LENGTH_SHORT).show();
                    } else if (statusCode == 404) {
                        Log.e(TAG, "✗ 404 Not Found - API endpoint not found");
                        Toast.makeText(AdminFeedbackActivity.this, "API endpoint not found", Toast.LENGTH_SHORT).show();
                    }
                    
                    if (response.errorBody() != null) {
                        try {
                            String errorBody = response.errorBody().string();
                            Log.e(TAG, "Error body: " + errorBody);
                        } catch (Exception e) {
                            Log.e(TAG, "Cannot read error body", e);
                        }
                    }
                    showEmptyState();
                }
                Log.d(TAG, "=== API CALLBACK END ===");
            }

            @Override
            public void onFailure(Call<ApiResponse> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                Log.e(TAG, "✗ API CALL FAILED");
                Log.e(TAG, "Error message: " + t.getMessage());
                Log.e(TAG, "Error class: " + t.getClass().getName());
                t.printStackTrace();
                
                String errorMsg = "Network error";
                if (t.getMessage() != null) {
                    if (t.getMessage().contains("Unable to resolve host")) {
                        errorMsg = "Cannot connect to server";
                    } else if (t.getMessage().contains("timeout")) {
                        errorMsg = "Request timeout";
                    } else {
                        errorMsg = t.getMessage();
                    }
                }
                
                Toast.makeText(AdminFeedbackActivity.this, "Failed: " + errorMsg, Toast.LENGTH_LONG).show();
                showEmptyState();
            }
        });
    }

    private void showEmptyState() {
        layoutEmpty.setVisibility(View.VISIBLE);
        rvFeedback.setVisibility(View.GONE);
    }
}


