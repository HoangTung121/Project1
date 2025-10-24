package com.example.myreadbookapplication.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.example.myreadbookapplication.R;
import com.example.myreadbookapplication.adapter.CategoryAdapter;
import com.example.myreadbookapplication.adapter.NewBookAdapter;
import com.example.myreadbookapplication.model.ApiResponse;
import com.example.myreadbookapplication.model.Book;
import com.example.myreadbookapplication.model.BooksResponse;
import com.example.myreadbookapplication.model.CategoriesResponse;
import com.example.myreadbookapplication.model.Category;
import com.example.myreadbookapplication.network.ApiService;
import com.example.myreadbookapplication.network.RetrofitClient;
import com.google.android.material.navigation.NavigationView;
import com.example.myreadbookapplication.adapter.BannerAdapter;

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
    private List<Category> categoriesList; // Lưu danh sách categories để map

    private TextView profileEmail;
    private ImageView editProfileIcon;
    private View headerView;

    // Banner slider components
    private ViewPager2 bannerViewPager;
    private LinearLayout indicatorLayout;
    private BannerAdapter bannerAdapter;
    private Handler autoScrollHandler;
    private Runnable autoScrollRunnable;
    private int currentBannerPosition = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // Ánh xạ views
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        menuIcon = findViewById(R.id.icon_menu);
        searchBar = findViewById(R.id.searchBarLayout);
        rvCategories = findViewById(R.id.rv_categories);
        //rvNewBooks = findViewById(R.id.rv_new_books);
        progressBar = findViewById(R.id.progressBar);

        // Banner slider views
        bannerViewPager = findViewById(R.id.bannerViewPager);
        indicatorLayout = findViewById(R.id.indicatorLayout);

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
        String userEmail = prefs.getString("user_email", "Guest@example.com"); // đặt làm email mặc định nếu không có
        Log.d("HomeActivity", "Loaded email from prefs: " + userEmail);

        headerView = navigationView.getHeaderView(0);  // Lấy headerView (nav_header_xml)
        profileEmail = headerView.findViewById(R.id.profile_email);
        editProfileIcon = headerView.findViewById(R.id.edit_profile_icon);
        
        if(profileEmail != null){
            profileEmail.setText(userEmail);
        }
        
        // Xử lý click edit profile icon
        if(editProfileIcon != null){
            Log.d("HomeActivity", "Edit profile icon found, setting click listener");
            editProfileIcon.setOnClickListener(v -> {
                Log.d("HomeActivity", "Edit profile icon clicked");
                Intent intent = new Intent(HomeActivity.this, ProfileActivity.class);
                startActivity(intent);
                drawerLayout.closeDrawer(GravityCompat.START);
            });
        } else {
            Log.w("HomeActivity", "Edit profile icon not found in header");
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
                    Intent intent = new Intent(HomeActivity.this, FeedbackActivity.class);
                    startActivity(intent);
                    drawerLayout.closeDrawer(GravityCompat.START);
                    return true;
                } else if (id == R.id.nav_contact) {
                    Intent intent = new Intent(HomeActivity.this, ContactActivity.class);
                    startActivity(intent);
                    drawerLayout.closeDrawer(GravityCompat.START);
                    return true;
                } else if (id == R.id.nav_change_password) {
                    Intent intent = new Intent(HomeActivity.this, ChangePasswordActivity.class);
                    startActivity(intent);
                    drawerLayout.closeDrawer(GravityCompat.START);
                    return true;
                } else if (id == R.id.nav_sign_out) {
                    Toast.makeText(HomeActivity.this, "Sign out clicked", Toast.LENGTH_SHORT).show();
                }

                // Đóng drawer sau khi click
                drawerLayout.closeDrawer(GravityCompat.START);
                return true;
            }
        });

        //setup recycleview voi loading
        setupBannerSlider();
        setupCategories();
        // setupNewBooks() sẽ được gọi sau khi setupCategories() hoàn thành

        // Xử lý search bar click
        searchBar.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, SearchActivity.class);
            startActivity(intent);
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
    @Override
    protected void onResume() {
        super.onResume();
        startAutoScroll();
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopAutoScroll();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopAutoScroll();
    }
    private void setupBannerSlider() {
        // Tạo danh sách banner images (có thể lấy từ API hoặc hardcode)
        List<Integer> bannerImages = new ArrayList<>();
        bannerImages.add(R.drawable.h1_slide_show); // Banner hiện tại
        bannerImages.add(R.drawable.fantasy_image); // Banner thứ 2
        bannerImages.add(R.drawable.h1_slide_show); // Banner thứ 3
        bannerImages.add(R.drawable.scifi_image); // Banner thứ 4

        // Setup BannerAdapter
        bannerAdapter = new BannerAdapter(bannerImages, new BannerAdapter.OnBannerClickListener() {
            @Override
            public void onBannerClick(int position) {
                Toast.makeText(HomeActivity.this, "Banner " + (position + 1) + " clicked", Toast.LENGTH_SHORT).show();
                // Có thể thêm logic chuyển đến trang chi tiết banner
            }
        });

        // Setup ViewPager2
        bannerViewPager.setAdapter(bannerAdapter);

        // Setup indicator dots
        setupIndicatorDots(bannerImages.size());

        // Setup auto-scroll
        autoScrollHandler = new Handler(Looper.getMainLooper());
        setupAutoScroll();

        // Listen to page changes để update indicator
        bannerViewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                currentBannerPosition = position;
                updateIndicatorDots(position);
            }
        });
    }
    //setup các dot theo image
    private void setupIndicatorDots(int count) {
        indicatorLayout.removeAllViews();

        for (int i = 0; i < count; i++) {
            ImageView dot = new ImageView(this);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            params.setMargins(8, 0, 8, 0);
            dot.setLayoutParams(params);
            dot.setImageResource(R.drawable.dot_indicator);
            indicatorLayout.addView(dot);
        }

        // Set first dot as selected
        if (count > 0) {
            updateIndicatorDots(0);
        }
    }

    private void updateIndicatorDots(int selectedPosition) {
        for (int i = 0; i < indicatorLayout.getChildCount(); i++) {
            ImageView dot = (ImageView) indicatorLayout.getChildAt(i);
            if (i == selectedPosition) {
                dot.setImageResource(R.drawable.dot_indicator_selected);
            } else {
                dot.setImageResource(R.drawable.dot_indicator);
            }
        }
    }

    private void setupAutoScroll() {
        autoScrollRunnable = new Runnable() {
            @Override
            public void run() {
                if (bannerAdapter != null && bannerAdapter.getItemCount() > 0) {
                    currentBannerPosition = (currentBannerPosition + 1) % bannerAdapter.getItemCount();
                    bannerViewPager.setCurrentItem(currentBannerPosition, true);
                }
                autoScrollHandler.postDelayed(this, 3000); // Auto scroll mỗi 3 giây
            }
        };
    }

    private void startAutoScroll() {
        if (autoScrollHandler != null && autoScrollRunnable != null) {
            autoScrollHandler.postDelayed(autoScrollRunnable, 3000);
        }
    }

    private void stopAutoScroll() {
        if (autoScrollHandler != null && autoScrollRunnable != null) {
            autoScrollHandler.removeCallbacks(autoScrollRunnable);
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
                        // Lưu categoriesList để sử dụng trong setupNewBooks
                        HomeActivity.this.categoriesList = categoriesList;
                        
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
                        
                        // Sau khi categories load xong, gọi setupNewBooks
                        setupNewBooks();
                    } else {
                        Log.w("HomeActivity", "No categories after filter");
                        Toast.makeText(HomeActivity.this, "No active categories", Toast.LENGTH_SHORT).show();
                        // Vẫn gọi setupNewBooks ngay cả khi không có categories
                        setupNewBooks();
                    }
                } else {
                    Log.e("HomeActivity", "Categories API fail: " + response.code());
                    Toast.makeText(HomeActivity.this, "Load categories failed", Toast.LENGTH_SHORT).show();
                    // Vẫn gọi setupNewBooks ngay cả khi categories API fail
                    setupNewBooks();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<CategoriesResponse>> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                Log.e("HomeActivity", "Categories failure: " + t.getMessage());
                Toast.makeText(HomeActivity.this, "Network error", Toast.LENGTH_SHORT).show();
                // Vẫn gọi setupNewBooks ngay cả khi categories API failure
                setupNewBooks();
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
                        NewBookAdapter row1Adapter = new NewBookAdapter(row1Books, HomeActivity.this, categoriesList);
                        RecyclerView rvRow1 = findViewById(R.id.rv_new_books_row1);  // ID mới trong XML
                        rvRow1.setLayoutManager(new LinearLayoutManager(HomeActivity.this, LinearLayoutManager.HORIZONTAL, false));
                        rvRow1.setAdapter(row1Adapter);

                        // Hàng 2
                        NewBookAdapter row2Adapter = new NewBookAdapter(row2Books, HomeActivity.this, categoriesList);
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