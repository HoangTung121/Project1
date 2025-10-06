package com.example.myreadbookapplication;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import com.google.android.material.navigation.NavigationView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;  // Thêm import này

public class HomeActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_home);

        // Ánh xạ views
        toolbar = findViewById(R.id.toolbar);
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        ImageView imBackIconHome = findViewById(R.id.icon_back_home_page);

        // Set toolbar
        setSupportActionBar(toolbar);

        // Setup hamburger icon để mở drawer
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        // Xử lý click menu items (hiện Toast tạm, bạn thay bằng navigate sau)
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();
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

                // Đóng drawer
                drawerLayout.closeDrawer(GravityCompat.START);
                return true;
            }
        });

        // Giữ nguyên xử lý icon back
        imBackIconHome.setOnClickListener(v -> {
            finish();
        });

        // Xử lý EdgeToEdge
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.content_frame), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }
}