package com.example.myreadbookapplication.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.example.myreadbookapplication.R;
import com.example.myreadbookapplication.utils.AuthManager;
import com.example.myreadbookapplication.utils.LogoutManager;

public class AdminAccountActivity extends AppCompatActivity {

    private static final String TAG = "AdminAccountActivity";
    private TextView tvEmail;
    private TextView tvAvatarInitial;
    private LinearLayout tvChangePassword;
    private LinearLayout tvSignOut;
    private LinearLayout navCategory;
    private LinearLayout navBook;
    private LinearLayout navFeedback;
    private LinearLayout navAccount;

    private AuthManager authManager;
    private LogoutManager logoutManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_admin_account);

        authManager = AuthManager.getInstance(this);
        logoutManager = new LogoutManager(this);

        initViews();
        setupClickListeners();
        loadUserInfo();
    }

    private void initViews() {
        tvEmail = findViewById(R.id.tv_email);
        tvAvatarInitial = findViewById(R.id.tv_avatar_initial);
        tvChangePassword = findViewById(R.id.tv_change_password);
        tvSignOut = findViewById(R.id.tv_sign_out);
        
        // Bottom navigation
        navCategory = findViewById(R.id.nav_category_in_account);
        navBook = findViewById(R.id.nav_book_in_account);
        navFeedback = findViewById(R.id.nav_feedback_in_account);
        navAccount = findViewById(R.id.nav_account_in_account);
    }

    private void setupClickListeners() {

        tvChangePassword.setOnClickListener(v -> {
            Intent intent = new Intent(this, ChangePasswordActivity.class);
            startActivity(intent);
        });

        tvSignOut.setOnClickListener(v -> {
            logoutManager.logout();
            finish();
        });

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
            Intent intent = new Intent(this, AdminFeedbackActivity.class);
            startActivity(intent);
            finish();
        });

        navAccount.setOnClickListener(v -> {
            // Already on account screen
        });
    }

    private void loadUserInfo() {
        String email = authManager.getUserEmail();
        if (email != null && !email.isEmpty()) {
            tvEmail.setText(email);
            // Set avatar initial from first letter of email
            String initial = email.substring(0, 1).toUpperCase();
            if (tvAvatarInitial != null) {
                tvAvatarInitial.setText(initial);
            }
        } else {
            tvEmail.setText("admin@example.com");
            if (tvAvatarInitial != null) {
                tvAvatarInitial.setText("A");
            }
        }
    }
}

