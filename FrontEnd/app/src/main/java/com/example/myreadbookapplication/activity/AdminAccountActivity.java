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

    private TextView tvBack;
    private TextView tvEmail;
    private TextView tvChangePassword;
    private TextView tvSignOut;
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
        tvBack = findViewById(R.id.tv_back);
        tvEmail = findViewById(R.id.tv_email);
        tvChangePassword = findViewById(R.id.tv_change_password);
        tvSignOut = findViewById(R.id.tv_sign_out);
        
        // Bottom navigation
        navCategory = findViewById(R.id.nav_category);
        navBook = findViewById(R.id.nav_book);
        navFeedback = findViewById(R.id.nav_feedback);
        navAccount = findViewById(R.id.nav_account);
    }

    private void setupClickListeners() {
        tvBack.setOnClickListener(v -> finish());

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
        } else {
            tvEmail.setText("Admin");
        }
    }
}

