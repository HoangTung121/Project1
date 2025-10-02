package com.example.myreadbookapplication;

import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.example.myreadbookapplication.model.ApiResponse;
import com.example.myreadbookapplication.model.SignInRequest;
import com.example.myreadbookapplication.network.RetrofitClient;

import org.json.JSONObject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SignInActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_sign_in);

        // ánh xạ
        TextView tvSignUp = findViewById(R.id.tv_sign_up);
        LinearLayout btnSignInHomPage = findViewById(R.id.btn_sign_in);
        EditText etEmailSignIn = findViewById(R.id.et_email_sign_in);
        EditText etPasswordSignIn = findViewById(R.id.et_password_sign_in);
        TextView tvForgotPassword = findViewById(R.id.tv_forgot_password);

        // bắt sự kiện và xử lý

        btnSignInHomPage.setOnClickListener(v ->{
            // lấy dữ liệu cuả các edit text nhập vào từ người dùng email, passwword
            String emailSignIn = etEmailSignIn.getText().toString().trim();
            String passwordSignIn = etPasswordSignIn.getText().toString().trim();

            //bat su kien su ly logic nhap

            // nếu người dùng không nhập đầy đủ thông tin thì thông báo
            if (emailSignIn.isEmpty() || passwordSignIn.isEmpty() ) {
                Toast.makeText(SignInActivity.this, "You need fill in all of the fields", Toast.LENGTH_SHORT).show();
                return;
            }
            // nếu email không đúng định dạng thì thông báo luon
            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(emailSignIn).matches()) {
                Toast.makeText(SignInActivity.this, "Invalid email format", Toast.LENGTH_SHORT).show();
                return;
            }
            // Disable nút để tránh bấm liên tục
            btnSignInHomPage.setEnabled(false);
            Toast.makeText(SignInActivity.this, "Signing in ...", Toast.LENGTH_SHORT).show();

            //tao request object va gọi api
            SignInRequest signInRequest = new SignInRequest(emailSignIn, passwordSignIn);

            Call<ApiResponse> call = RetrofitClient.getApiService().signIn(signInRequest);
            call.enqueue(new Callback<ApiResponse>() {
                @Override
                public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                    btnSignInHomPage.setEnabled(true);
                    if(response.isSuccessful() && response.body() != null){
                        ApiResponse apiResponse = response.body();
                        Toast.makeText(SignInActivity.this, apiResponse.getMessage(), Toast.LENGTH_SHORT).show();
                        if(apiResponse.isSuccess()){
                            //luu email vao sharedPreference
                            SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
                            prefs.edit().putString("user_email", emailSignIn).apply();

                            // chuyen sang homeActivity
                            Intent intent = new Intent(SignInActivity.this, HomeActivity.class);
                            startActivity(intent);
                            finish(); // ket thuc intent de khong quay lai man hinh splash
                        }
                    }
                    else {
                        String errorMessage = "Sign in failed";
                        try{
                            if(response.errorBody() != null){
                                String errorBody = response.errorBody().string();
                                Log.e(TAG, "Error body: " + errorBody);
                                JSONObject jsonObject = new JSONObject(errorBody);
                                String serverMessage = jsonObject.optString("message", "Sign in failed");

                                // Ánh xạ thông báo lỗi từ backend
                                switch (serverMessage) {
                                    case "Invalid email":
                                        errorMessage = "Invalid email format. Please check your email.";
                                        break;
                                    case "User not found":
                                        errorMessage = "Email not found. Please sign up.";
                                        break;
                                    case "Wrong password":
                                        errorMessage = "Incorrect password. Please try again.";
                                        break;
                                    case "Unable to sign in":
                                        errorMessage = "Server error. Please try again later.";
                                        break;
                                    default:
                                        errorMessage = serverMessage;
                                        break;
                                }

                            }
                        }catch (Exception e) {
                            Log.e(TAG, "Error parsing error body", e);
                        }
                        Toast.makeText(SignInActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                    }
                }

                @Override
                public void onFailure(Call<ApiResponse> call, Throwable t) {
                    btnSignInHomPage.setEnabled(true);
                    Log.e(TAG, "API failure", t);
                    Toast.makeText(SignInActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        });

        tvSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SignInActivity.this, SignUpActivity.class);
                startActivity(intent);
            }
        });

        tvForgotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SignInActivity.this, ForgotPasswordActivity.class);
                startActivity(intent);
            }
        });
    }
}
