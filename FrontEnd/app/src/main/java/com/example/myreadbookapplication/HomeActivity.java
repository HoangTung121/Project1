package com.example.myreadbookapplication;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.GravityCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myreadbookapplication.model.Book;
import com.google.android.material.navigation.NavigationView;

import java.util.ArrayList;
import java.util.List;

public class HomeActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private ImageView menuIcon;
    private LinearLayout searchBar;
    private RecyclerView rvCategories;

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

        // xử lý click menu
        menuIcon.setOnClickListener(v -> {
            if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                drawerLayout.closeDrawer(GravityCompat.START);
            } else {
                drawerLayout.openDrawer(GravityCompat.START);
            }
        });

        // Xử lý menu items
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();
                // Ví dụ xử lý từng item (thay Toast bằng Intent nếu cần)
                if (id == R.id.nav_other_features) {
                    Toast.makeText(HomeActivity.this, "Other features clicked", Toast.LENGTH_SHORT).show();
                } else if (id == R.id.nav_favorite) {
                    Toast.makeText(HomeActivity.this, "Favorite clicked", Toast.LENGTH_SHORT).show();
                } else if (id == R.id.nav_history) {
                    Toast.makeText(HomeActivity.this, "History clicked", Toast.LENGTH_SHORT).show();
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

        // Setup Categories RecyclerView (sử dụng CategoryAdapter có listener)
        List<String> demoCategories = new ArrayList<>();
        demoCategories.add("Horror");
        demoCategories.add("Romance");
        demoCategories.add("Sci-Fi");
        demoCategories.add("Fantasy");
        demoCategories.add("Mystery");
        demoCategories.add("Thriller");
        demoCategories.add("Adventure");
        demoCategories.add("Biography");
        demoCategories.add("History");
        demoCategories.add("Self-Help");

        //tạo adapter với listener khi mở categoryactivity
        CategoryAdapter categoryAdapter = new CategoryAdapter(demoCategories, this, new CategoryAdapter.OnCategoryClickListener() {
            @Override
            public void onCategoryClick(String category) {
                Intent intent = new Intent(HomeActivity.this, CategoryActivity.class);
                intent.putExtra("selected_category", category);  // Truyền tên category
                startActivity(intent);
            }
        });
        rvCategories.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        rvCategories.setAdapter(categoryAdapter);

        // Setup New Books RecyclerView  - vuot ngang de xem sach moi
        RecyclerView rvNewBooks = findViewById(R.id.rv_new_books);
        List<Book> demoNewBooks = new ArrayList<>();
        // Thêm demo data (thay URL bằng ảnh thật hoặc local res ID)
        demoNewBooks.add(new Book("New Horror Book", "https://example.com/horror.jpg"));  // Hoặc R.drawable.horror_cover cho local
        demoNewBooks.add(new Book("Sci-Fi Adventure", "https://example.com/scifi.jpg"));
        demoNewBooks.add(new Book("Romance Novel", "https://example.com/romance.jpg"));

        NewBookAdapter newBookAdapter = new NewBookAdapter(demoNewBooks, this);
        rvNewBooks.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        rvNewBooks.setAdapter(newBookAdapter);

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

        // Xử lý "View all" new books (tạm)
        findViewById(R.id.viewAllNewBooks).setOnClickListener(v -> {
            Toast.makeText(this, "View all new books", Toast.LENGTH_SHORT).show();
            // Ví dụ: Intent to BooksActivity
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
}