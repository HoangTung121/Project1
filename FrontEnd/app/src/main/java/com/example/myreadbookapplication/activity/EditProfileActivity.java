package com.example.myreadbookapplication.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.example.myreadbookapplication.R;
import com.example.myreadbookapplication.model.ApiResponse;
import com.example.myreadbookapplication.model.UpdateUserRequest;
import com.example.myreadbookapplication.model.User;
import com.example.myreadbookapplication.network.ApiService;
import com.example.myreadbookapplication.network.RetrofitClient;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EditProfileActivity extends AppCompatActivity {

    private static final String TAG = "EditProfileActivity";
    
    private ImageView ivBack;
    private EditText etName;
    private EditText etEmail;
    private EditText etPhone;
    private TextView btnCancel;
    private TextView btnSave;
    private ProgressBar progressBar;
    
    private ApiService apiService;
    private User currentUser;

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
        progressBar = findViewById(R.id.progress_bar);

        // Initialize API service
        apiService = RetrofitClient.getApiService();

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
            }
        });
    }

    private void loadCurrentUserInfo() {
        SharedPreferences prefs = getSharedPreferences("app_prefs", MODE_PRIVATE);
        String userId = prefs.getString("user_id", null);
        String token = prefs.getString("access_token", null);

        if (userId == null || token == null || token.isEmpty()) {
            Toast.makeText(this, "Bạn cần đăng nhập để chỉnh sửa thông tin", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Show loading
        progressBar.setVisibility(View.VISIBLE);

        // Load current user data from API
        Call<ApiResponse<User>> call = apiService.getUserProfile(userId, "Bearer " + token);
        call.enqueue(new Callback<ApiResponse<User>>() {
            @Override
            public void onResponse(Call<ApiResponse<User>> call, Response<ApiResponse<User>> response) {
                progressBar.setVisibility(View.GONE);
                
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    currentUser = response.body().getData();
                    // Populate form with current user data
                    etName.setText(currentUser.getFullName() != null ? currentUser.getFullName() : "");
                    etEmail.setText(currentUser.getEmail() != null ? currentUser.getEmail() : "");
                    etPhone.setText(currentUser.getPhoneNumber() != null ? currentUser.getPhoneNumber() : "");
                } else {
                    Log.e(TAG, "Failed to load user info: " + response.code());
                    Toast.makeText(EditProfileActivity.this, "Không thể tải thông tin người dùng", Toast.LENGTH_SHORT).show();
                    // Fallback to SharedPreferences
                    loadUserInfoFromPrefs();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<User>> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                Log.e(TAG, "Error loading user info: " + t.getMessage());
                Toast.makeText(EditProfileActivity.this, "Lỗi mạng", Toast.LENGTH_SHORT).show();
                // Fallback to SharedPreferences
                loadUserInfoFromPrefs();
            }
        });
    }

    private void loadUserInfoFromPrefs() {
        SharedPreferences prefs = getSharedPreferences("app_prefs", MODE_PRIVATE);
        String email = prefs.getString("user_email", "");
        String name = prefs.getString("user_name", "");
        String phone = prefs.getString("user_phone", "");
        
        // Hiển thị thông tin từ SharedPreferences
        etName.setText(name);
        etEmail.setText(email);
        etPhone.setText(phone);
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

        // Validate phone number format (10-11 digits)
        if (!phone.matches("^[0-9]{10,11}$")) {
            Toast.makeText(this, "Phone number must be 10-11 digits", Toast.LENGTH_SHORT).show();
            etPhone.requestFocus();
            return false;
        }

        return true;
    }

    private void saveUserInfo() {
        String name = etName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();

        SharedPreferences prefs = getSharedPreferences("app_prefs", MODE_PRIVATE);
        String userId = prefs.getString("user_id", null);
        String token = prefs.getString("access_token", null);

        if (userId == null || token == null || token.isEmpty()) {
            Toast.makeText(this, "Bạn cần đăng nhập để cập nhật thông tin", Toast.LENGTH_SHORT).show();
            return;
        }

        // Show loading
        progressBar.setVisibility(View.VISIBLE);
        btnSave.setEnabled(false);

        // Create update request
        UpdateUserRequest updateRequest = new UpdateUserRequest(name, email, phone);

        // Call API to update user profile
        Call<ApiResponse<User>> call = apiService.updateUserProfile(userId, updateRequest, "Bearer " + token);
        call.enqueue(new Callback<ApiResponse<User>>() {
            @Override
            public void onResponse(Call<ApiResponse<User>> call, Response<ApiResponse<User>> response) {
                progressBar.setVisibility(View.GONE);
                btnSave.setEnabled(true);
                
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    User updatedUser = response.body().getData();
                    
                    // Update SharedPreferences with new data
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putString("user_email", updatedUser.getEmail());
                    editor.putString("user_name", updatedUser.getFullName());
                    editor.putString("user_phone", updatedUser.getPhoneNumber());
                    editor.apply();
                    
                    Toast.makeText(EditProfileActivity.this, "Cập nhật thông tin thành công!", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Log.e(TAG, "Failed to update user profile: " + response.code());
                    Toast.makeText(EditProfileActivity.this, "Không thể cập nhật thông tin", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<User>> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                btnSave.setEnabled(true);
                Log.e(TAG, "Error updating user profile: " + t.getMessage());
                Toast.makeText(EditProfileActivity.this, "Lỗi mạng", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
