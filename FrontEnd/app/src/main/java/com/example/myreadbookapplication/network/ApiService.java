package com.example.myreadbookapplication.network;

import com.example.myreadbookapplication.model.ApiResponse;
import com.example.myreadbookapplication.model.Book;
import com.example.myreadbookapplication.model.BooksResponse;
import com.example.myreadbookapplication.model.CategoriesResponse;
import com.example.myreadbookapplication.model.Category;
import com.example.myreadbookapplication.model.HistoryItem;
import com.example.myreadbookapplication.model.ReadingHistoryResponse;
import com.example.myreadbookapplication.model.SignInRequest;
import com.example.myreadbookapplication.model.SignUpRequest;
import com.example.myreadbookapplication.model.VerifyOtpRequest;
import com.example.myreadbookapplication.model.ResendOtpRequest;
import com.example.myreadbookapplication.model.ResetPasswordRequest;
import com.example.myreadbookapplication.model.ForgotPasswordRequest;
import com.example.myreadbookapplication.model.LogoutRequest;
import com.example.myreadbookapplication.model.User;
import com.example.myreadbookapplication.model.UpdateUserRequest;
import com.example.myreadbookapplication.model.FeedbackRequest;
import com.example.myreadbookapplication.model.UpdateFeedbackRequest;
import com.example.myreadbookapplication.model.Feedback;
import com.example.myreadbookapplication.model.FeedbackResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;
import retrofit2.http.Query;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;

// EPUB models
import com.example.myreadbookapplication.model.epub.EpubModels.EpubUrlRequest;
import com.example.myreadbookapplication.model.epub.EpubModels.EpubMetadataData;
import com.example.myreadbookapplication.model.epub.EpubModels.EpubChaptersData;
import com.example.myreadbookapplication.model.epub.EpubModels.EpubChapterContentData;
import com.example.myreadbookapplication.model.epub.EpubModels.EpubChapterContentRequest;
import retrofit2.http.DELETE;
import retrofit2.http.Header;
import retrofit2.http.PUT;

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

    @FormUrlEncoded
    @POST("api/auth/change-password")
    Call<ApiResponse> changePassword(
            @Field("oldPassword") String oldPassword,
            @Field("newPassword") String newPassword,
            @Field("confirmPassword") String confirmPassword,
            @Header("Authorization") String authorization
    );

    @POST("api/auth/logout")
    Call<ApiResponse> logout(@Body LogoutRequest request);

    //category
    @GET("api/categories")
    Call<ApiResponse<CategoriesResponse>> getCategories(@Query("status") String status); //status=active or inactive ?

    //book
    @GET("api/books")
    Call<ApiResponse<BooksResponse>> getBooks(
            @Query("category") String category,
            @Query("status") String status,
            @Query("limit") Integer limit,
            @Query("page") Integer page
    );

    @GET("api/books")
    Call<ApiResponse<BooksResponse>> getBooksByIds(
            @Query("ids") String ids,  // Comma-separated IDs
            @Query("status") String status,
            @Query("limit") Integer limit,
            @Query("page") Integer page
    );
    
    // Admin - Get all books
    @GET("api/books")
    Call<ApiResponse<BooksResponse>> getAllBooks(
            @Header("Authorization") String authorization,
            @Query("page") Integer page,
            @Query("limit") Integer limit
    );

    //Search book
    @GET("api/books/search")
    Call<ApiResponse<BooksResponse>> searchBooks(
            @Query("input") String input,
            @Query("page") Integer page,
            @Query("limit") Integer limit
    );

    // favorites
    @GET("api/users/{userId}/favorites")
    Call<ApiResponse<com.example.myreadbookapplication.model.FavoritesResponse>> getFavorites(
            @Path("userId") String userId,
            @Header("Authorization") String authorization
    );

    @POST("api/users/{userId}/favorites/{bookId}")
    Call<ApiResponse> addFavorite(
            @Path("userId") String userId,
            @Path("bookId") String bookId,
            @Header("Authorization") String authorization
    );

    @DELETE("api/users/{userId}/favorites/{bookId}")
    Call<ApiResponse> removeFavorite(
            @Path("userId") String userId,
            @Path("bookId") String bookId,
            @Header("Authorization") String authorization
    );

    // history
    @GET("api/history/{userId}")
    Call<ApiResponse<ReadingHistoryResponse>> getReadingHistory(
            @Path("userId") String userId,
            @Header("Authorization") String authorization,
            @Query("page") Integer page,
            @Query("limit") Integer limit,
            @Query("sortBy") String sortBy,
            @Query("sortOrder") String sortOrder
    );

    @GET("api/history/{userId}/bookmark/{bookId}")
    Call<ApiResponse<HistoryItem>> getBookmark(
            @Path("userId") String userId,
            @Path("bookId") String bookId,
            @Header("Authorization") String authorization
    );

    @DELETE("api/history/{userId}/bookmark/{bookId}")
    Call<ApiResponse> deleteBookmark(
            @Path("userId") String userId,
            @Path("bookId") String bookId,
            @Header("Authorization") String authorization
    );

    @FormUrlEncoded
    @POST("api/history/bookmark")
    Call<ApiResponse> saveBookmark(
            @Field("userId") String userId,
            @Field("bookId") String bookId,
            @Field("chapterId") String chapterId,
            @Header("Authorization") String authorization
    );

    // EPUB
    @POST("api/epub/validate-url")
    Call<ApiResponse> validateEpubUrl(@Body EpubUrlRequest request);

    @POST("api/epub/metadata")
    Call<ApiResponse<EpubMetadataData>> getEpubMetadata(@Body EpubUrlRequest request);

    @POST("api/epub/chapters")
    Call<ApiResponse<EpubChaptersData>> getEpubChapters(@Body EpubUrlRequest request);

    @POST("api/epub/chapter-content")
    Call<ApiResponse<EpubChapterContentData>> getEpubChapterContent(@Body EpubChapterContentRequest request);

    // User Profile APIs
    @GET("api/users/{userId}")
    Call<ApiResponse<User>> getUserProfile(
            @Path("userId") String userId,
            @Header("Authorization") String authorization
    );

    @PUT("api/users/{userId}")
    Call<ApiResponse<User>> updateUserProfile(
            @Path("userId") String userId,
            @Body UpdateUserRequest request,
            @Header("Authorization") String authorization
    );

    @GET("api/users")
    Call<ApiResponse<User>> getUserByEmail(
            @Query("email") String email,
            @Header("Authorization") String authorization
    );

    // Feedback APIs
    @POST("api/feedback")
    Call<ApiResponse> createFeedback(
            @Body FeedbackRequest request,
            @Header("Authorization") String authorization
    );

    @GET("api/feedback/my-feedbacks")
    Call<ApiResponse<FeedbackResponse>> getMyFeedbacks(
            @Header("Authorization") String authorization,
            @Query("page") Integer page,
            @Query("limit") Integer limit
    );

    @GET("api/feedback/{id}")
    Call<ApiResponse<Feedback>> getFeedbackById(
            @Path("id") String id,
            @Header("Authorization") String authorization
    );

    @PUT("api/feedback/{id}")
    Call<ApiResponse<Feedback>> updateFeedback(
            @Path("id") String id,
            @Body UpdateFeedbackRequest request,
            @Header("Authorization") String authorization
    );

    @DELETE("api/feedback/{id}")
    Call<ApiResponse> deleteFeedback(
            @Path("id") String id,
            @Header("Authorization") String authorization
    );

    // Admin APIs - Get all feedbacks (without status filter to get all feedbacks)
    // Backend returns: {success, message, data: [array], pagination: {...}}
    @GET("api/admin/feedbacks")
    Call<ApiResponse> getAllFeedbacks(
            @Header("Authorization") String authorization,
            @Query("page") Integer page,
            @Query("limit") Integer limit
            // Note: Not sending status parameter to get ALL feedbacks
    );

}
