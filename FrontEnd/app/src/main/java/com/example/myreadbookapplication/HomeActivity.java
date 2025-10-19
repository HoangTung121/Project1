package com.example.myreadbookapplication;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.GravityCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myreadbookapplication.model.ApiResponse;
import com.example.myreadbookapplication.model.Book;
import com.example.myreadbookapplication.model.BooksResponse;
import com.example.myreadbookapplication.model.CategoriesResponse;
import com.example.myreadbookapplication.model.Category;
import com.example.myreadbookapplication.network.ApiService;
import com.example.myreadbookapplication.network.RetrofitClient;
import com.google.android.material.navigation.NavigationView;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private ImageView menuIcon;
    private LinearLayout searchBar;
    private RecyclerView rvCategories;
    private RecyclerView rvNewBooks;
    private ProgressBar progressBar;
    private CategoryAdapter categoryAdapter;
    private NewBookAdapter newBookAdapter;
    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_home);

        // Ánh xạ views
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        menuIcon = findViewById(R.id.icon_menu);
        searchBar = findViewById(R.id.searchBarLayout);
        rvCategories = findViewById(R.id.rv_categories);
        //rvNewBooks = findViewById(R.id.rv_new_books);
        progressBar = findViewById(R.id.progressBar);

        apiService = RetrofitClient.getApiService();


        // xử lý click menu
        menuIcon.setOnClickListener(v -> {
            if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                drawerLayout.closeDrawer(GravityCompat.START);
            } else {
                drawerLayout.openDrawer(GravityCompat.START);
            }
        });

        //set email động vào header menu
        SharedPreferences prefs = getSharedPreferences("app_prefs", MODE_PRIVATE);
        String userEmail = prefs.getString("user_email", "guest@example.com"); // đặt làm email mặc định nếu không có
        Log.d("HomeActivity", "Loaded email from prefs: " + userEmail);

        View headerView = navigationView.getHeaderView(0);  // Lấy headerView (nav_header_xml)
        TextView profileEmail = headerView.findViewById(R.id.profile_email);
        if(profileEmail != null){
            profileEmail.setText(userEmail);
        }

        // Xử lý menu items
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();
                // Ví dụ xử lý từng item (thay Toast bằng Intent nếu cần)

                if (id == R.id.nav_favorite) {
                    Log.d("HomeActivity", "Favorite clicked");
                    Intent intent = new Intent(HomeActivity.this, FavoriteActivity.class);  // Chuyển sang FavoriteActivity
                    startActivity(intent);
                    drawerLayout.closeDrawer(GravityCompat.START);  // Đóng drawer
                    return true;
                } else if (id == R.id.nav_history) {
                    Intent intent = new Intent(HomeActivity.this, HistoryActivity.class);
                    startActivity(intent);
                    drawerLayout.closeDrawer(GravityCompat.START);
                    return true;
                } else if (id == R.id.nav_feedback) {
                    Toast.makeText(HomeActivity.this, "Feedback clicked", Toast.LENGTH_SHORT).show();
                } else if (id == R.id.nav_contact) {
                    Toast.makeText(HomeActivity.this, "Contact clicked", Toast.LENGTH_SHORT).show();
                } else if (id == R.id.nav_change_password) {
                    Toast.makeText(HomeActivity.this, "Change password clicked", Toast.LENGTH_SHORT).show();
                } else if (id == R.id.nav_sign_out) {
                    Toast.makeText(HomeActivity.this, "Sign out clicked", Toast.LENGTH_SHORT).show();
                }

                // Đóng drawer sau khi click
                drawerLayout.closeDrawer(GravityCompat.START);
                return true;
            }
        });

        //setup recycleview voi loading
        setupCategories();
        setupNewBooks();

        // Xử lý search bar click (tạm Toast, thay bằng Intent đến SearchActivity)
        searchBar.setOnClickListener(v -> {
            Toast.makeText(this, "Open search", Toast.LENGTH_SHORT).show();
            // Ví dụ: startActivity(new Intent(this, SearchActivity.class));
        });

        // Xử lý "View all" categories
        findViewById(R.id.viewAllCategories).setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, CategoryActivity.class);
            startActivity(intent);
        });

        // Xử lý "View all" new books
        findViewById(R.id.viewAllNewBooks).setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, BookActivity.class);
            startActivity(intent);
        });

        // Xử lý EdgeToEdge cho ScrollView
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.scrollViewMain), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }



    // Xử lý nút back: Đóng drawer nếu đang mở, иначе thoát activity
    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }
    private void setupCategories() {
        progressBar.setVisibility(View.VISIBLE);
        Log.d("HomeActivity", "Calling Categories API...");
        Call<ApiResponse<CategoriesResponse>> call = apiService.getCategories("active");
        call.enqueue(new Callback<ApiResponse<CategoriesResponse>>() {
            @Override
            public void onResponse(Call<ApiResponse<CategoriesResponse>> call, Response<ApiResponse<CategoriesResponse>> response) {
                progressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    CategoriesResponse catResp = response.body().getData();
                    List<Category> categoriesList = (catResp != null) ? catResp.getCategories() : null;
                    if (categoriesList != null) {
                        // Filter null và inactive (BE có null[0])
                        categoriesList = categoriesList.stream()
                                .filter(cat -> cat != null && "active".equals(cat.getStatus()))
                                .collect(Collectors.toList());
                        Log.d("HomeActivity", "Filtered categories size: " + categoriesList.size());  // Nên =12
                    }
                    if (categoriesList != null && !categoriesList.isEmpty()) {
                        categoryAdapter = new CategoryAdapter(categoriesList, HomeActivity.this, new CategoryAdapter.OnCategoryClickListener() {
                            @Override
                            public void onCategoryClick(Category category) {
                                Intent intent = new Intent(HomeActivity.this, CategoryActivity.class);
                                intent.putExtra("selected_category_id", String.valueOf(category.getId()));  // Id dạng INT
                                intent.putExtra("selected_category_name", category.getName());
                                startActivity(intent);
                            }
                        });
                        rvCategories.setLayoutManager(new LinearLayoutManager(HomeActivity.this, LinearLayoutManager.HORIZONTAL, false));
                        rvCategories.setAdapter(categoryAdapter);
                        Log.d("HomeActivity", "Categories adapter set");
                    } else {
                        Log.w("HomeActivity", "No categories after filter");
                        Toast.makeText(HomeActivity.this, "No active categories", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Log.e("HomeActivity", "Categories API fail: " + response.code());
                    Toast.makeText(HomeActivity.this, "Load categories failed", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<CategoriesResponse>> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                Log.e("HomeActivity", "Categories failure: " + t.getMessage());
                Toast.makeText(HomeActivity.this, "Network error", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupNewBooks() {
        Call<ApiResponse<BooksResponse>> call = apiService.getBooks(null, "active", 10, 1);  // Giữ nguyên API
        call.enqueue(new Callback<ApiResponse<BooksResponse>>() {
            @Override
            public void onResponse(Call<ApiResponse<BooksResponse>> call, Response<ApiResponse<BooksResponse>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    BooksResponse bookResp = response.body().getData();
                    List<Book> newBooksList = (bookResp != null) ? bookResp.getBooks() : null;
                    if (newBooksList != null && !newBooksList.isEmpty()) {
                        // NEW: Chia data thành 2 list cho 2 hàng
                        List<Book> row1Books = new ArrayList<>();  // Hàng 1: sách 0,2,4...
                        List<Book> row2Books = new ArrayList<>();  // Hàng 2: sách 1,3,5...
                        for (int i = 0; i < newBooksList.size(); i++) {
                            if (i % 2 == 0) {
                                row1Books.add(newBooksList.get(i));
                            } else {
                                row2Books.add(newBooksList.get(i));
                            }
                        }
                        Log.d("HomeActivity", "Row1 size: " + row1Books.size() + ", Row2 size: " + row2Books.size());

                        // Hàng 1
                        NewBookAdapter row1Adapter = new NewBookAdapter(row1Books, HomeActivity.this);
                        RecyclerView rvRow1 = findViewById(R.id.rv_new_books_row1);  // ID mới trong XML
                        rvRow1.setLayoutManager(new LinearLayoutManager(HomeActivity.this, LinearLayoutManager.HORIZONTAL, false));
                        rvRow1.setAdapter(row1Adapter);

                        // Hàng 2
                        NewBookAdapter row2Adapter = new NewBookAdapter(row2Books, HomeActivity.this);
                        RecyclerView rvRow2 = findViewById(R.id.rv_new_books_row2);  // ID mới trong XML
                        rvRow2.setLayoutManager(new LinearLayoutManager(HomeActivity.this, LinearLayoutManager.HORIZONTAL, false));
                        rvRow2.setAdapter(row2Adapter);

                        Log.d("HomeActivity", "2-row horizontal adapters set");
                    } else {
                        Toast.makeText(HomeActivity.this, "No new books", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(HomeActivity.this, "Load new books failed", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<BooksResponse>> call, Throwable t) {
                Toast.makeText(HomeActivity.this, "Network error", Toast.LENGTH_SHORT).show();
            }
        });
    }
}