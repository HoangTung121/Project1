package com.example.myreadbookapplication;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.CountDownTimer;
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
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

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
    private TextView tvCountdown;
    private CountDownTimer countDownTimer;
    private static final long COUNTDOWN_DURATION = 180000; // 3 phút (180000ms)
    private static final long COUNTDOWN_INTERVAL = 1000; // 1 giây

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
        Log.d(TAG, "User email: " + userEmail);

        // Ánh xạ
        btnVerify = findViewById(R.id.btn_verify);
        ImageView ivBackVerification = findViewById(R.id.iv_back);
        tvResendOtp = findViewById(R.id.tv_resend);
        etOtp = findViewById(R.id.et_otp);
        progressBar = findViewById(R.id.progress_bar);
        tvCountdown = findViewById(R.id.tv_countdown);

        if (btnVerify == null || ivBackVerification == null || tvResendOtp == null ||
                etOtp == null || progressBar == null || tvCountdown == null) {
            Log.e(TAG, "One or more views not found in layout");
            return;
        }

        // Khởi tạo đồng hồ đếm ngược
        startCountdown();

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

    private void startCountdown() {
        if (tvCountdown == null) {
            Log.e(TAG, "tvCountdown is null, cannot start countdown");
            return;
        }
        tvResendOtp.setEnabled(false); // Vô hiệu hóa nút Resend OTP
        if (countDownTimer != null) {
            countDownTimer.cancel(); // Hủy timer cũ nếu có
        }
        countDownTimer = new CountDownTimer(COUNTDOWN_DURATION, COUNTDOWN_INTERVAL) {
            @Override
            public void onTick(long millisUntilFinished) {
                if (tvCountdown != null) {
                    int minutes = (int) (millisUntilFinished / 1000) / 60;
                    int seconds = (int) (millisUntilFinished / 1000) % 60;
                    tvCountdown.setText(String.format("%d:%02d", minutes, seconds));
                }
            }

            @Override
            public void onFinish() {
                if (tvCountdown != null) {
                    tvCountdown.setText("0:00");
                }
                tvResendOtp.setEnabled(true); // Kích hoạt lại nút Resend OTP
                if (!userEmail.isEmpty()) {
                    setLoading(true);
                    resendOtp(userEmail); // Tự động gửi lại OTP
                } else {
                    showToast("Email not found");
                }
            }
        }.start();
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
        tvResendOtp.setEnabled(!loading && countDownTimer == null);
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private void verifyOtp(String email, String otp) {
        VerifyOtpRequest request = new VerifyOtpRequest(email, otp);
        Log.d(TAG, "Verifying OTP for email: " + email + ", OTP: " + otp);

        RetrofitClient.getApiService().verifyOtp(request).enqueue(new Callback<ApiResponse>() {
            @Override
            public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                setLoading(false);
                Log.d(TAG, "Verify OTP response code: " + response.code());
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse apiResponse = response.body();
                    Log.d(TAG, "Success: " + apiResponse.isSuccess() + ", Message: " + apiResponse.getMessage());
                    showToast(apiResponse.getMessage());
                    if (apiResponse.isSuccess()) {
                        Log.d(TAG, "Navigating to SignInActivity");
                        startActivity(new Intent(VerificationActivity.this, SignInActivity.class));
                        finish();
                    }
                } else {
                    String errorMessage = "Verification failed";
                    try {
                        if (response.errorBody() != null) {
                            String errorBody = response.errorBody().string();
                            Log.e(TAG, "Error body: " + errorBody);
                            Gson gson = new Gson();
                            JsonObject errorJson = gson.fromJson(errorBody, JsonObject.class);
                            String serverMessage = errorJson.get("message").getAsString();
                            JsonArray errors = errorJson.getAsJsonArray("errors");
                            if (errors != null && errors.size() > 0) {
                                serverMessage = errors.get(0).getAsJsonObject().get("message").getAsString();
                            }
                            errorMessage = serverMessage;
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error reading error body", e);
                    }
                    Log.e(TAG, "Verify OTP error code: " + response.code());
                    showToast(errorMessage);
                }
            }

            @Override
            public void onFailure(Call<ApiResponse> call, Throwable t) {
                setLoading(false);
                Log.e(TAG, "Verify OTP failure: " + t.getMessage(), t);
                showToast("Network error: " + t.getMessage());
            }
        });
    }

    private void resendOtp(String email) {
        ResendOtpRequest request = new ResendOtpRequest(email);
        Log.d(TAG, "Resending OTP for email: " + email);

        RetrofitClient.getApiService().resendOtp(request).enqueue(new Callback<ApiResponse>() {
            @Override
            public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                setLoading(false);
                Log.d(TAG, "Resend OTP response code: " + response.code());
                if (response.isSuccessful() && response.body() != null) {
                    showToast(response.body().getMessage());
                    // Khởi động lại đồng hồ đếm ngược sau khi gửi lại OTP
                    startCountdown();
                } else {
                    String errorMessage = "Resend failed";
                    try {
                        if (response.errorBody() != null) {
                            String errorBody = response.errorBody().string();
                            Log.e(TAG, "Error body: " + errorBody);
                            Gson gson = new Gson();
                            JsonObject errorJson = gson.fromJson(errorBody, JsonObject.class);
                            String serverMessage = errorJson.get("message").getAsString();
                            JsonArray errors = errorJson.getAsJsonArray("errors");
                            if (errors != null && errors.size() > 0) {
                                serverMessage = errors.get(0).getAsJsonObject().get("message").getAsString();
                            }
                            errorMessage = serverMessage;
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error reading error body", e);
                    }
                    Log.e(TAG, "Resend OTP error code: " + response.code());
                    showToast(errorMessage);
                }
            }

            @Override
            public void onFailure(Call<ApiResponse> call, Throwable t) {
                setLoading(false);
                Log.e(TAG, "Resend OTP failure: " + t.getMessage(), t);
                showToast("Network error: " + t.getMessage());
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (countDownTimer != null) {
            countDownTimer.cancel(); // Hủy đồng hồ đếm ngược khi activity bị hủy
        }
    }
}