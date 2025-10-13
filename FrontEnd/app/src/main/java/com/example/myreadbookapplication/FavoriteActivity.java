package com.example.myreadbookapplication;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.myreadbookapplication.model.ApiResponse;
import com.example.myreadbookapplication.model.Book;
import com.example.myreadbookapplication.model.BooksResponse;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.List;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myreadbookapplication.network.ApiService;
import com.example.myreadbookapplication.network.RetrofitClient;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FavoriteActivity extends AppCompatActivity {

    private static final String TAG = "FavoriteActivity";
    private ImageView backIconFavoriteBook;
    private RecyclerView rvFavoriteBooks;
    private ProgressBar progressBarFavoriteBooks;
    private CategoryBookAdapter favoriteBookAdapter;
    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorite);

        // Ánh xạ
        backIconFavoriteBook = findViewById(R.id.back_favorite_book_icon);
        rvFavoriteBooks = findViewById(R.id.rv_favorite_books);
        progressBarFavoriteBooks = findViewById(R.id.progressBar_favorite_books);
        apiService = RetrofitClient.getApiService();

        // Back click
        backIconFavoriteBook.setOnClickListener(v -> finish());

        // Load favorites
        setupFavoriteBooks();
    }

    private void setupFavoriteBooks() {
        progressBarFavoriteBooks.setVisibility(View.VISIBLE);
        SharedPreferences prefs = getSharedPreferences("app_prefs", MODE_PRIVATE);
        String userId = prefs.getString("user_id", null);
        if (userId != null) {
            userId = userId.replace(".0", "");
        }
        String token = prefs.getString("access_token", null);

        if (userId != null && token != null && !token.isEmpty()) {
            Log.d(TAG, "Fetching favorites from backend for user: " + userId);
            Call<ApiResponse<com.example.myreadbookapplication.model.FavoritesResponse>> call =
                    apiService.getFavorites(userId, "Bearer " + token);
            call.enqueue(new Callback<ApiResponse<com.example.myreadbookapplication.model.FavoritesResponse>>() {
                @Override
                public void onResponse(Call<ApiResponse<com.example.myreadbookapplication.model.FavoritesResponse>> call, Response<ApiResponse<com.example.myreadbookapplication.model.FavoritesResponse>> response) {
                    progressBarFavoriteBooks.setVisibility(View.GONE);
                    if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                        com.example.myreadbookapplication.model.FavoritesResponse data = response.body().getData();
                        List<Book> favorites = (data != null) ? data.getFavoriteBooks() : null;
                        if (favorites != null && !favorites.isEmpty()) {
                            favoriteBookAdapter = new CategoryBookAdapter(favorites, FavoriteActivity.this, "Favorites");
                            rvFavoriteBooks.setLayoutManager(new GridLayoutManager(FavoriteActivity.this, 3));
                            rvFavoriteBooks.setAdapter(favoriteBookAdapter);
                        } else {
                            Toast.makeText(FavoriteActivity.this, "No favorite books", Toast.LENGTH_SHORT).show();
                        }
                        // Sync local cache of ids
                        if (data != null && data.getFavoriteBookIds() != null) {
                            Gson gson = new Gson();
                            prefs.edit().putString("favorite_books", gson.toJson(data.getFavoriteBookIds())).apply();
                        }
                    } else {
                        Toast.makeText(FavoriteActivity.this, "Load favorites failed", Toast.LENGTH_SHORT).show();
                        loadFavoritesFromLocal();
                    }
                }

                @Override
                public void onFailure(Call<ApiResponse<com.example.myreadbookapplication.model.FavoritesResponse>> call, Throwable t) {
                    progressBarFavoriteBooks.setVisibility(View.GONE);
                    Log.e(TAG, "Favorites failure: " + t.getMessage());
                    loadFavoritesFromLocal();
                }
            });
        } else {
            Log.d(TAG, "No token/userId, fallback to local cache");
            loadFavoritesFromLocal();
        }
    }

    private void loadFavoritesFromLocal() {
        SharedPreferences prefs = getSharedPreferences("app_prefs", MODE_PRIVATE);
        String favoriteBooksJson = prefs.getString("favorite_books", "[]");
        Gson gson = new Gson();
        Type type = new TypeToken<List<String>>() {}.getType();
        List<String> favoriteBookIds = gson.fromJson(favoriteBooksJson, type);
        if (favoriteBookIds == null || favoriteBookIds.isEmpty()) {
            Toast.makeText(this, "No favorite books yet", Toast.LENGTH_SHORT).show();
            return;
        }
        String idsQuery = String.join(",", favoriteBookIds);
        Call<ApiResponse<BooksResponse>> call = apiService.getBooksByIds(idsQuery, "active", null, 1);
        call.enqueue(new Callback<ApiResponse<BooksResponse>>() {
            @Override
            public void onResponse(Call<ApiResponse<BooksResponse>> call, Response<ApiResponse<BooksResponse>> response) {
                progressBarFavoriteBooks.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    BooksResponse bookResp = response.body().getData();
                    List<Book> favoritesList = (bookResp != null) ? bookResp.getBooks() : null;
                    if (favoritesList != null && !favoritesList.isEmpty()) {
                        favoriteBookAdapter = new CategoryBookAdapter(favoritesList, FavoriteActivity.this, "Favorites");
                        rvFavoriteBooks.setLayoutManager(new GridLayoutManager(FavoriteActivity.this, 3));
                        rvFavoriteBooks.setAdapter(favoriteBookAdapter);
                    } else {
                        Toast.makeText(FavoriteActivity.this, "No favorite books found", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(FavoriteActivity.this, "Load favorites failed", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<BooksResponse>> call, Throwable t) {
                progressBarFavoriteBooks.setVisibility(View.GONE);
                Toast.makeText(FavoriteActivity.this, "Network error", Toast.LENGTH_SHORT).show();
            }
        });
    }
}