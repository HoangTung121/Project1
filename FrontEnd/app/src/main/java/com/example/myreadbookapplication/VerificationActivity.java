package com.example.myreadbookapplication;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.example.myreadbookapplication.model.ApiResponse;
import com.example.myreadbookapplication.model.ResendOtpRequest;
import com.example.myreadbookapplication.model.VerifyOtpRequest;
import com.example.myreadbookapplication.network.RetrofitClient;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class VerificationActivity extends AppCompatActivity {

    private EditText etOtp;
    private String userEmail;
    private static final String TAG = "VerificationActivity";
    private ProgressBar progressBar;
    private LinearLayout btnVerify;
    private TextView tvResendOtp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_verification);

        // Lấy email từ Intent hoặc SharedPreferences
        userEmail = getIntent().getStringExtra("email");
        if (userEmail == null) {
            SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
            userEmail = prefs.getString("user_email", "");
        }

        // Ánh xạ
        btnVerify = findViewById(R.id.btn_verify);
        ImageView ivBackVerification = findViewById(R.id.iv_back);
        tvResendOtp = findViewById(R.id.tv_resend);
        etOtp = findViewById(R.id.et_otp);
        progressBar = findViewById(R.id.progress_bar); // thêm progressBar trong XML

        // Nút Verify → gọi API verify OTP
        btnVerify.setOnClickListener(v -> {
            String otp = etOtp.getText().toString().trim();
            if (otp.length() == 6 && !userEmail.isEmpty()) {
                hideKeyboard();
                setLoading(true);
                verifyOtp(userEmail, otp);
            } else {
                etOtp.setError("Please enter 6-digit OTP");
            }
        });

        // Nút Resend OTP
        tvResendOtp.setOnClickListener(v -> {
            if (!userEmail.isEmpty()) {
                setLoading(true);
                resendOtp(userEmail);
            } else {
                showToast("Email not found");
            }
        });

        // Nút Back
        ivBackVerification.setOnClickListener(v -> finish());

        // Khi vừa vào màn hình tự động bật bàn phím
        etOtp.requestFocus();
        etOtp.postDelayed(this::showKeyboard, 200);
    }

    private void showKeyboard() {
        etOtp.requestFocus();
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.showSoftInput(etOtp, InputMethodManager.SHOW_IMPLICIT);
        }
    }

    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(etOtp.getWindowToken(), 0);
        }
    }

    private void setLoading(boolean loading) {
        progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        btnVerify.setEnabled(!loading);
        tvResendOtp.setEnabled(!loading);
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private void verifyOtp(String email, String otp) {
        VerifyOtpRequest request = new VerifyOtpRequest(email, otp);

        RetrofitClient.getApiService().verifyOtp(request).enqueue(new Callback<ApiResponse>() {
            @Override
            public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                setLoading(false);
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse apiResponse = response.body();
                    showToast(apiResponse.getMessage());
                    if (apiResponse.isSuccess()) {
                        startActivity(new Intent(VerificationActivity.this, SignInActivity.class));
                        finish();
                    }
                } else {
                    Log.e(TAG, "Verify OTP error code: " + response.code());
                    showToast("Verification failed: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<ApiResponse> call, Throwable t) {
                setLoading(false);
                Log.e(TAG, "Verify OTP failure", t);
                showToast("Network error: " + t.getMessage());
            }
        });
    }

    private void resendOtp(String email) {
        ResendOtpRequest request = new ResendOtpRequest(email);

        RetrofitClient.getApiService().resendOtp(request).enqueue(new Callback<ApiResponse>() {
            @Override
            public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                setLoading(false);
                if (response.isSuccessful() && response.body() != null) {
                    showToast(response.body().getMessage());
                } else {
                    Log.e(TAG, "Resend OTP error code: " + response.code());
                    showToast("Resend failed: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<ApiResponse> call, Throwable t) {
                setLoading(false);
                Log.e(TAG, "Resend OTP failure", t);
                showToast("Network error: " + t.getMessage());
            }
        });
    }
}
