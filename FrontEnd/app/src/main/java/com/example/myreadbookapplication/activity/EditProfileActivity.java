package com.example.myreadbookapplication.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.example.myreadbookapplication.R;

public class EditProfileActivity extends AppCompatActivity {

    private ImageView ivBack;
    private EditText etName;
    private EditText etEmail;
    private EditText etPhone;
    private TextView btnCancel;
    private TextView btnSave;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_edit_profile);

        // Ánh xạ các view
        ivBack = findViewById(R.id.iv_back);
        etName = findViewById(R.id.et_name);
        etEmail = findViewById(R.id.et_email);
        etPhone = findViewById(R.id.et_phone);
        btnCancel = findViewById(R.id.btn_cancel);
        btnSave = findViewById(R.id.btn_save);

        // Load thông tin user hiện tại
        loadCurrentUserInfo();

        // Bắt sự kiện click
        ivBack.setOnClickListener(v -> {
            finish(); // Quay lại màn hình trước
        });

        btnCancel.setOnClickListener(v -> {
            finish(); // Quay lại màn hình trước
        });

        btnSave.setOnClickListener(v -> {
            // Validate input
            if (validateInput()) {
                // Lưu thông tin và quay lại ProfileActivity
                saveUserInfo();
                Toast.makeText(this, "Profile updated successfully!", Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    private void loadCurrentUserInfo() {
        SharedPreferences prefs = getSharedPreferences("app_prefs", MODE_PRIVATE);
        String email = prefs.getString("user_email", "");
        
        // Hiển thị thông tin hiện tại
        etName.setText("Brian"); // Tạm thời hardcode
        etEmail.setText(email);
        etPhone.setText("0123456789"); // Tạm thời hardcode
    }

    private boolean validateInput() {
        String name = etName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();

        if (name.isEmpty()) {
            Toast.makeText(this, "Please enter your name", Toast.LENGTH_SHORT).show();
            etName.requestFocus();
            return false;
        }

        if (email.isEmpty()) {
            Toast.makeText(this, "Please enter your email", Toast.LENGTH_SHORT).show();
            etEmail.requestFocus();
            return false;
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Please enter a valid email", Toast.LENGTH_SHORT).show();
            etEmail.requestFocus();
            return false;
        }

        if (phone.isEmpty()) {
            Toast.makeText(this, "Please enter your phone number", Toast.LENGTH_SHORT).show();
            etPhone.requestFocus();
            return false;
        }

        return true;
    }

    private void saveUserInfo() {
        String name = etName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();

        // Lưu vào SharedPreferences
        SharedPreferences prefs = getSharedPreferences("app_prefs", MODE_PRIVATE);
        prefs.edit()
                .putString("user_email", email)
                .putString("user_name", name)
                .putString("user_phone", phone)
                .apply();

        // TODO: Gọi API để cập nhật thông tin trên server
    }
}
