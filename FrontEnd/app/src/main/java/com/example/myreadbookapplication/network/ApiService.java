package com.example.myreadbookapplication.network;

import com.example.myreadbookapplication.model.ApiResponse;
import com.example.myreadbookapplication.model.Book;
import com.example.myreadbookapplication.model.BooksResponse;
import com.example.myreadbookapplication.model.CategoriesResponse;
import com.example.myreadbookapplication.model.Category;
import com.example.myreadbookapplication.model.SignInRequest;
import com.example.myreadbookapplication.model.SignUpRequest;
import com.example.myreadbookapplication.model.VerifyOtpRequest;
import com.example.myreadbookapplication.model.ResendOtpRequest;
import com.example.myreadbookapplication.model.ResetPasswordRequest;
import com.example.myreadbookapplication.model.ForgotPasswordRequest;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;
import retrofit2.http.Query;
import retrofit2.http.GET;

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

    //category
    @GET("api/categories")
    Call<ApiResponse<CategoriesResponse>> getCategories(@Query("status") String status); //status=active or inactive

    //book
    @GET("api/books")
    Call<ApiResponse<BooksResponse>> getBooks(
            @Query("category") String category,
            @Query("status") String status,
            @Query("limit") Integer limit,
            @Query("page") Integer page
    );

    //Optional: search book
    @GET("api/books/search")
    Call<ApiResponse<List<Book>>> searchBooks(@Query("q") String query); //q = author or title

}
