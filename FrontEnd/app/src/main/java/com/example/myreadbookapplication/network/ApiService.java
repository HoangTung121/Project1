package com.example.myreadbookapplication.network;

import com.example.myreadbookapplication.model.ApiResponse;
import com.example.myreadbookapplication.model.SignUpRequest;
import com.example.myreadbookapplication.model.VerifyOtpRequest;
import com.example.myreadbookapplication.model.ResendOtpRequest;

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

}
