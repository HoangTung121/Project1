package com.example.myreadbookapplication.activity.User;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myreadbookapplication.R;
import com.example.myreadbookapplication.adapter.CategoryAdapter;
import com.example.myreadbookapplication.adapter.CategoryBookAdapter;
import com.example.myreadbookapplication.model.Book;
import com.example.myreadbookapplication.model.Category;
import com.example.myreadbookapplication.model.ApiResponse;
import com.example.myreadbookapplication.model.BooksResponse;
import com.example.myreadbookapplication.model.CategoriesResponse;
import com.example.myreadbookapplication.network.ApiService;
import com.example.myreadbookapplication.network.RetrofitClient;

import java.util.List;
import java.util.stream.Collectors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CategoryActivity extends AppCompatActivity {

    private static final String TAG = "CategoryActivity";  // Tag cho log

    private RecyclerView rvCategoriesContent;
    private CategoryAdapter categoryAdapter;
    private CategoryBookAdapter categoryBookAdapter;
    private ImageView backIconCategory;
    private TextView tvCategoryTitle;
    private ProgressBar progressBar;
    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category);

        // Ánh xạ views
        backIconCategory = findViewById(R.id.back_icon_category);
        rvCategoriesContent = findViewById(R.id.rv_category_books);
        tvCategoryTitle = findViewById(R.id.tv_category_title);
        progressBar = findViewById(R.id.progressBar_category);
        apiService = RetrofitClient.getApiService();

        // Nhận extra từ Intent
        String selectedCategoryIdStr = getIntent().getStringExtra("selected_category_id");
        String selectedCategoryName = getIntent().getStringExtra("selected_category_name");
        Log.d(TAG, "Received extra - ID: " + selectedCategoryIdStr + ", Name: " + selectedCategoryName);  // Debug extra

        // Set title ngay lập tức nếu có name (trước API call)
        if (selectedCategoryName != null && !selectedCategoryName.isEmpty()) {
            tvCategoryTitle.setText(selectedCategoryName);
            tvCategoryTitle.setVisibility(View.VISIBLE);
            Log.d(TAG, "Set header title: " + selectedCategoryName);
        }

        boolean isFullList = (selectedCategoryIdStr == null || selectedCategoryIdStr.isEmpty());
        Log.d(TAG, "isFullList: " + isFullList);

        backIconCategory.setOnClickListener(v -> finish());

        if (isFullList) {
            loadFullCategories();
        } else {
            loadBooksForCategory(selectedCategoryIdStr, selectedCategoryName);
        }
    }

    private void loadFullCategories() {
        progressBar.setVisibility(View.VISIBLE);
        Log.d(TAG, "Loading full categories...");
        tvCategoryTitle.setVisibility(View.GONE);  // Ẩn title cho full list
        Call<ApiResponse<CategoriesResponse>> call = apiService.getCategories("active");
        call.enqueue(new Callback<ApiResponse<CategoriesResponse>>() {
            @Override
            public void onResponse(Call<ApiResponse<CategoriesResponse>> call, Response<ApiResponse<CategoriesResponse>> response) {
                progressBar.setVisibility(View.GONE);
                Log.d(TAG, "Full categories response code: " + response.code());
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    CategoriesResponse catResp = response.body().getData();
                    List<Category> allCategories = (catResp != null) ? catResp.getCategories() : null;
                    if (allCategories != null) {
                        allCategories = allCategories.stream()
                                .filter(cat -> cat != null && "active".equals(cat.getStatus()))
                                .collect(Collectors.toList());
                        Log.d(TAG, "Filtered full categories size: " + allCategories.size());
                    }
                    if (allCategories != null && !allCategories.isEmpty()) {
                        categoryAdapter = new CategoryAdapter(allCategories, CategoryActivity.this, new CategoryAdapter.OnCategoryClickListener() {
                            @Override
                            public void onCategoryClick(Category category) {
                                Log.d(TAG, "Clicked category in full list: " + category.getName());
                                Intent intent = new Intent(CategoryActivity.this, CategoryActivity.class);
                                intent.putExtra("selected_category_id", String.valueOf(category.getId()));
                                intent.putExtra("selected_category_name", category.getName());
                                startActivity(intent);
                            }
                        });
                        rvCategoriesContent.setLayoutManager(new GridLayoutManager(CategoryActivity.this, 3));
                        rvCategoriesContent.setAdapter(categoryAdapter);
                        rvCategoriesContent.invalidate();  // Force refresh UI
                        Log.d(TAG, "Full categories adapter set");
                    } else {
                        Log.w(TAG, "No full categories data");
                        Toast.makeText(CategoryActivity.this, "No categories found", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Log.e(TAG, "Full categories API fail: " + response.code());
                    Toast.makeText(CategoryActivity.this, "Load categories failed", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<CategoriesResponse>> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                Log.e(TAG, "Full categories failure: " + t.getMessage());
                Toast.makeText(CategoryActivity.this, "Network error", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadBooksForCategory(String categoryIdStr, String categoryName) {
        int categoryId = -1;
        if (categoryIdStr != null && !categoryIdStr.isEmpty()) {
            try {
                categoryId = Integer.parseInt(categoryIdStr);
            } catch (NumberFormatException e) {
                Log.e(TAG, "Invalid category ID: " + categoryIdStr);
            }
        }
        progressBar.setVisibility(View.VISIBLE);
        Log.d(TAG, "Loading books for category ID: " + categoryId + " (" + categoryName + ")");
        Call<ApiResponse<BooksResponse>> call = apiService.getBooks(String.valueOf(categoryId), "active", 20, 1);
        call.enqueue(new Callback<ApiResponse<BooksResponse>>() {
            @Override
            public void onResponse(Call<ApiResponse<BooksResponse>> call, Response<ApiResponse<BooksResponse>> response) {
                progressBar.setVisibility(View.GONE);
                Log.d(TAG, "Books response code: " + response.code() + " for category " + categoryName);
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    BooksResponse bookResp = response.body().getData();
                    List<Book> booksList = (bookResp != null) ? bookResp.getBooks() : null;
                    Log.d(TAG, "Books data size: " + (booksList != null ? booksList.size() : 0));
                    if (booksList != null && !booksList.isEmpty()) {
                        // Filter active nếu cần (từ BE đã filter, nhưng an toàn)
                        booksList = booksList.stream()
                                .filter(book -> book != null && "active".equals(book.getStatus()))
                                .collect(Collectors.toList());
                        categoryBookAdapter = new CategoryBookAdapter(booksList, CategoryActivity.this, categoryName);
                        rvCategoriesContent.setLayoutManager(new GridLayoutManager(CategoryActivity.this, 3));
                        rvCategoriesContent.setAdapter(categoryBookAdapter);
                        rvCategoriesContent.invalidate();  // Force refresh
                        tvCategoryTitle.setText(categoryName);
                        tvCategoryTitle.setVisibility(View.VISIBLE);
                        Log.d(TAG, "Books adapter set: " + booksList.size() + " items");
                    } else {
                        Log.w(TAG, "No books data for " + categoryName);
                        Toast.makeText(CategoryActivity.this, "No books in " + categoryName + " yet", Toast.LENGTH_SHORT).show();
                        // Optional: Set empty adapter hoặc TextView "No books"
                    }
                } else {
                    Log.e(TAG, "Books API fail for " + categoryName + ": " + response.code());
                    Toast.makeText(CategoryActivity.this, "Failed to load books for " + categoryName, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<BooksResponse>> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                Log.e(TAG, "Books failure for " + categoryName + ": " + t.getMessage());
                Toast.makeText(CategoryActivity.this, "Network error loading " + categoryName, Toast.LENGTH_SHORT).show();
            }
        });
    }
}