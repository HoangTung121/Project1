package com.example.myreadbookapplication.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.example.myreadbookapplication.R;

public class ProfileActivity extends AppCompatActivity {

    private ImageView ivBack;
    private TextView tvUserName;
    private TextView tvUserEmail;
    private TextView tvUserPhone;
    private TextView tvAccountCreated;
    private TextView btnEdit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_profile);

        // Ánh xạ các view
        ivBack = findViewById(R.id.iv_back);
        tvUserName = findViewById(R.id.tv_user_name);
        tvUserEmail = findViewById(R.id.tv_user_email);
        tvUserPhone = findViewById(R.id.tv_user_phone);
        tvAccountCreated = findViewById(R.id.tv_account_created);
        btnEdit = findViewById(R.id.btn_edit);

        // Load thông tin user từ SharedPreferences
        loadUserInfo();

        // Bắt sự kiện click
        ivBack.setOnClickListener(v -> {
            finish(); // Quay lại màn hình trước
        });

        btnEdit.setOnClickListener(v -> {
            // Chuyển đến màn hình Edit Profile
            Intent intent = new Intent(ProfileActivity.this, EditProfileActivity.class);
            startActivity(intent);
        });
    }

    private void loadUserInfo() {
        SharedPreferences prefs = getSharedPreferences("app_prefs", MODE_PRIVATE);
        String email = prefs.getString("user_email", "");
        String userId = prefs.getString("user_id", "");

        // Hiển thị thông tin user
        tvUserEmail.setText(email);
        tvUserName.setText("Brian"); // Tạm thời hardcode, có thể lấy từ API
        tvUserPhone.setText("0123456789"); // Tạm thời hardcode
        tvAccountCreated.setText("May 25, 2024"); // Tạm thời hardcode
    }
}
