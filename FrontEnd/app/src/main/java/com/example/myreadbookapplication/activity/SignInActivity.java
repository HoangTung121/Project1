package com.example.myreadbookapplication.activity;

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

import com.example.myreadbookapplication.R;
import com.example.myreadbookapplication.model.ApiResponse;
import com.example.myreadbookapplication.model.SignInRequest;
import com.example.myreadbookapplication.network.RetrofitClient;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.json.JSONObject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SignInActivity extends AppCompatActivity {

    private TextView tvSignUp;
    private LinearLayout btnSignInHomPage;
    private EditText etEmailSignIn;
    private EditText etPasswordSignIn;
    private TextView tvForgotPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_sign_in);

        // ánh xạ
        tvSignUp = findViewById(R.id.tv_sign_up);
        btnSignInHomPage = findViewById(R.id.btn_sign_in);
        etEmailSignIn = findViewById(R.id.et_email_sign_in);
        etPasswordSignIn = findViewById(R.id.et_password_sign_in);
        tvForgotPassword = findViewById(R.id.tv_forgot_password);

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
                            //lay va luu email vao sharedPreference
                            String userEmail = "";
                            Object dataObj = apiResponse.getData();
                            if(dataObj != null){
                                try{
                                    Gson gson = new Gson();
                                    String dataJson = gson.toJson(dataObj); //convert data sang json string
                                    JsonObject jsonData = JsonParser.parseString(dataJson).getAsJsonObject();
                                    JsonObject userJson = jsonData.getAsJsonObject("user");
                                    if(userJson != null){
                                        userEmail = userJson.get("email").getAsString();
                                    }
                                }catch (Exception e){
                                    Log.e(TAG, "Error parsing data", e);
                                }
                            }
                            Log.d(TAG, "Login success, email: " + userEmail);
                            //luu email, userId, accessToken vao sharedPreference
                            SharedPreferences prefs = getSharedPreferences("app_prefs", MODE_PRIVATE);
                            String userId = "";
                            String accessToken = "";
                            try{
                                Gson gson = new Gson();
                                String dataJson = gson.toJson(apiResponse.getData());
                                JsonObject jsonData = JsonParser.parseString(dataJson).getAsJsonObject();
                                JsonObject user = jsonData.getAsJsonObject("user");
                                if (user != null && user.get("_id") != null) {
                                    try {
                                        if (user.get("_id").isJsonPrimitive() && user.get("_id").getAsJsonPrimitive().isNumber()) {
                                            long idNum = user.get("_id").getAsLong();
                                            userId = String.valueOf(idNum);
                                        } else {
                                            userId = user.get("_id").getAsString();
                                        }
                                    } catch (Exception ignore) {
                                        userId = user.get("_id").getAsString();
                                    }
                                }
                                if (jsonData.get("accessToken") != null) {
                                    accessToken = jsonData.get("accessToken").getAsString();
                                }
                            }catch (Exception e){
                                Log.e(TAG, "Parse tokens failed", e);
                            }
                            prefs.edit()
                                    .putString("user_email", userEmail)
                                    .putString("user_id", userId != null ? userId.replace(".0", "") : null)
                                    .putString("access_token", accessToken)
                                    .apply();

                            // Optional: seed local favorite ids from backend user.favoriteBooks for correct icon state
                            try {
                                Gson gson = new Gson();
                                String dataJson = gson.toJson(apiResponse.getData());
                                JsonObject jsonData = JsonParser.parseString(dataJson).getAsJsonObject();
                                JsonObject userObj = jsonData.getAsJsonObject("user");
                                if (userObj != null && userObj.get("favoriteBooks") != null && userObj.get("favoriteBooks").isJsonArray()) {
                                    String favoritesJson = userObj.get("favoriteBooks").toString();
                                    // Persist as list of strings
                                    prefs.edit().putString("favorite_books", favoritesJson).apply();
                                }
                            } catch (Exception e2) {
                                Log.w(TAG, "Unable to seed favorites from login response", e2);
                            }

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
