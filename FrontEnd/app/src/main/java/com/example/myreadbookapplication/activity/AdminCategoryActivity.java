package com.example.myreadbookapplication.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myreadbookapplication.R;
import com.example.myreadbookapplication.adapter.AdminCategoryAdapter;
import com.example.myreadbookapplication.model.ApiResponse;
import com.example.myreadbookapplication.model.CategoriesResponse;
import com.example.myreadbookapplication.model.Category;
import com.example.myreadbookapplication.network.ApiService;
import com.example.myreadbookapplication.network.RetrofitClient;
import com.example.myreadbookapplication.utils.AuthManager;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminCategoryActivity extends AppCompatActivity {

    private static final String TAG = "AdminCategoryActivity";
    private static final int REQUEST_ADD_CATEGORY = 1;
    private static final int REQUEST_EDIT_CATEGORY = 2;
    private RecyclerView rvCategory;
    private LinearLayout layoutEmpty;
    private ProgressBar progressBar;
    private LinearLayout navCategory;
    private LinearLayout navBook;
    private LinearLayout navFeedback;
    private LinearLayout navAccount;
    private com.google.android.material.floatingactionbutton.FloatingActionButton fabAddCategory;
    private android.widget.EditText etSearch;

    private ApiService apiService;
    private AuthManager authManager;
    private List<Category> categoryList = new ArrayList<>();
    private List<Category> allCategoriesList = new ArrayList<>(); // Store all categories for filter
    private AdminCategoryAdapter categoryAdapter;
    private android.os.Handler searchHandler = new android.os.Handler();
    private Runnable searchRunnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_admin_category);

        apiService = RetrofitClient.getApiService();
        authManager = AuthManager.getInstance(this);

        initViews();
        setupClickListeners();
        loadCategories();
    }

    private void initViews() {
        rvCategory = findViewById(R.id.rv_category);
        layoutEmpty = findViewById(R.id.layout_empty);
        progressBar = findViewById(R.id.progress_bar);
        etSearch = findViewById(R.id.et_search);
        
        // Bottom navigation
        navCategory = findViewById(R.id.nav_category);
        navBook = findViewById(R.id.nav_book);
        navFeedback = findViewById(R.id.nav_feedback);
        navAccount = findViewById(R.id.nav_account);
        fabAddCategory = findViewById(R.id.fab_add_category);

        // Setup RecyclerView
        categoryAdapter = new AdminCategoryAdapter(this, categoryList);
        categoryAdapter.setOnCategoryActionListener(new AdminCategoryAdapter.OnCategoryActionListener() {
            @Override
            public void onEditClick(Category category) {
                openEditCategory(category);
            }

            @Override
            public void onDeleteClick(Category category) {
                showDeleteConfirmDialog(category);
            }
        });
        rvCategory.setLayoutManager(new LinearLayoutManager(this));
        rvCategory.setAdapter(categoryAdapter);
    }

    private void setupClickListeners() {
        setupSearchListener();
        fabAddCategory.setOnClickListener(v -> openAddCategory());

        navCategory.setOnClickListener(v -> {
            // Already on category screen
        });

        navBook.setOnClickListener(v -> {
            Intent intent = new Intent(this, AdminBookActivity.class);
            startActivity(intent);
            finish();
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

    private void openAddCategory() {
        Intent intent = new Intent(this, AdminAddCategoryActivity.class);
        startActivityForResult(intent, REQUEST_ADD_CATEGORY);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        if (requestCode == REQUEST_ADD_CATEGORY && resultCode == AdminAddCategoryActivity.RESULT_CATEGORY_ADDED) {
            Toast.makeText(this, "Category added!", Toast.LENGTH_SHORT).show();
            loadCategories(); // Reload the category list
        } else if (requestCode == REQUEST_EDIT_CATEGORY && resultCode == AdminEditCategoryActivity.RESULT_CATEGORY_UPDATED) {
            Toast.makeText(this, "Category updated!", Toast.LENGTH_SHORT).show();
            loadCategories(); // Reload the category list
        }
    }

    private void openEditCategory(Category category) {
        Intent intent = new Intent(this, AdminEditCategoryActivity.class);
        intent.putExtra(AdminEditCategoryActivity.EXTRA_CATEGORY, category);
        startActivityForResult(intent, REQUEST_EDIT_CATEGORY);
    }

    private void showDeleteConfirmDialog(Category category) {
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_confirm_delete);
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        TextView btnCancel = dialog.findViewById(R.id.btn_cancel);
        TextView btnYes = dialog.findViewById(R.id.btn_yes);

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnYes.setOnClickListener(v -> {
            deleteCategory(category);
            dialog.dismiss();
        });

        dialog.show();
    }

    private void deleteCategory(Category category) {
        String accessToken = authManager.getAccessToken();
        if (accessToken == null || accessToken.isEmpty()) {
            Toast.makeText(this, "Please login as admin", Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);

        int categoryId = category.getId();
        apiService.deleteCategory(categoryId, "Bearer " + accessToken).enqueue(new Callback<ApiResponse>() {
            @Override
            public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                progressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    Toast.makeText(AdminCategoryActivity.this, "Category deleted successfully!", Toast.LENGTH_SHORT).show();

                    categoryList.remove(category);
                    categoryAdapter.updateCategoryList(categoryList);
                    if (categoryList.isEmpty()) {
                        showEmptyState();
                    }
                } else {
                    String msg = "Delete failed";
                    try {
                        if (response.errorBody() != null) {
                            msg += ": " + response.errorBody().string();
                        } else if (response.body() != null) {
                            msg += ": " + response.body().getMessage();
                        }
                    } catch (Exception ignored) {}
                    Toast.makeText(AdminCategoryActivity.this, msg, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(AdminCategoryActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadCategories() {
        String accessToken = authManager.getAccessToken();
        
        if (accessToken == null || accessToken.isEmpty()) {
            Toast.makeText(this, "Please login as admin", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        layoutEmpty.setVisibility(View.GONE);

        // Get all categories (not filter by status) - pass null to get all
        Call<ApiResponse<CategoriesResponse>> call = apiService.getCategories(null);
        
        call.enqueue(new Callback<ApiResponse<CategoriesResponse>>() {
            @Override
            public void onResponse(Call<ApiResponse<CategoriesResponse>> call, Response<ApiResponse<CategoriesResponse>> response) {
                progressBar.setVisibility(View.GONE);

                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<CategoriesResponse> apiResponse = response.body();
                    if (apiResponse.isSuccess()) {
                        try {
                            CategoriesResponse categoriesResponse = apiResponse.getData();
                            if (categoriesResponse != null && categoriesResponse.getCategories() != null) {
                                allCategoriesList = categoriesResponse.getCategories();
                                categoryList = new ArrayList<>(allCategoriesList);
                                categoryAdapter.updateCategoryList(categoryList);
                                updateEmptyState();
                            } else {
                                showEmptyState();
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "Error parsing categories: " + e.getMessage());
                            showEmptyState();
                        }
                    } else {
                        showEmptyState();
                    }
                } else {
                    Log.e(TAG, "Failed to load categories: " + response.code());
                    showEmptyState();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<CategoriesResponse>> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                Log.e(TAG, "Error loading categories: " + t.getMessage());
                Toast.makeText(AdminCategoryActivity.this, "Failed to load categories", Toast.LENGTH_SHORT).show();
                showEmptyState();
            }
        });
    }

    private void setupSearchListener() {
        etSearch.setOnEditorActionListener((v, actionId, event) -> {
            performSearch();
            return true;
        });

        etSearch.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Cancel previous search
                if (searchRunnable != null) {
                    searchHandler.removeCallbacks(searchRunnable);
                }

                // Debounce search - wait 300ms after user stops typing
                searchRunnable = () -> performSearch();
                searchHandler.postDelayed(searchRunnable, 300);
            }

            @Override
            public void afterTextChanged(android.text.Editable s) {}
        });
    }

    private void performSearch() {
        String query = etSearch.getText().toString().trim().toLowerCase();
        
        if (query.isEmpty()) {
            // Reset to all categories
            categoryList.clear();
            categoryList.addAll(allCategoriesList);
            categoryAdapter.updateCategoryList(categoryList);
            updateEmptyState();
            return;
        }

        // Filter categories client-side by name
        List<Category> filteredList = new ArrayList<>();
        for (Category category : allCategoriesList) {
            if (category != null && category.getName() != null) {
                String categoryName = category.getName().toLowerCase();
                if (categoryName.contains(query)) {
                    filteredList.add(category);
                }
            }
        }

        categoryList = filteredList;
        categoryAdapter.updateCategoryList(categoryList);
        updateEmptyState();
    }

    private void updateEmptyState() {
        if (categoryList.isEmpty()) {
            layoutEmpty.setVisibility(View.VISIBLE);
            rvCategory.setVisibility(View.GONE);
        } else {
            layoutEmpty.setVisibility(View.GONE);
            rvCategory.setVisibility(View.VISIBLE);
        }
    }

    private void showEmptyState() {
        layoutEmpty.setVisibility(View.VISIBLE);
        rvCategory.setVisibility(View.GONE);
    }
}

