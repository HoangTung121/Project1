package com.example.myreadbookapplication.network;

import com.example.myreadbookapplication.model.ApiResponse;
import com.example.myreadbookapplication.model.SignInRequest;
import com.example.myreadbookapplication.model.SignUpRequest;
import com.example.myreadbookapplication.model.VerifyOtpRequest;
import com.example.myreadbookapplication.model.ResendOtpRequest;
import com.example.myreadbookapplication.model.ResetPasswordRequest;
import com.example.myreadbookapplication.model.ForgotPasswordRequest;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface ApiService {

    @POST("api/auth/register")
    Call<ApiResponse> signUp(@Body SignUpRequest request);

    @POST("api/auth/verify-otp")
    Call<ApiResponse> verifyOtp(@Body VerifyOtpRequest request);

    @POST("api/auth/resend-otp")
    Call<ApiResponse> resendOtp(@Body ResendOtpRequest request);

    @POST("api/auth/login")
    Call<ApiResponse> signIn(@Body SignInRequest request);

    @POST("api/auth/forgot-password")
    Call<ApiResponse> forgotPassword(@Body ForgotPasswordRequest request);

    @POST("api/auth/reset-password")
    Call<ApiResponse> resetPassword(@Body ResetPasswordRequest request);

}
