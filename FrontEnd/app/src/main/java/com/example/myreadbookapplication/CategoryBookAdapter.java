package com.example.myreadbookapplication;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.myreadbookapplication.model.Book;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import com.example.myreadbookapplication.model.ApiResponse;
import com.example.myreadbookapplication.network.ApiService;
import com.example.myreadbookapplication.network.RetrofitClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CategoryBookAdapter extends RecyclerView.Adapter<CategoryBookAdapter.ViewHolder> {
    private List<Book> books;
    private Context context;
    private String categoryName;

    public CategoryBookAdapter(List<Book> books, Context context, String categoryName) {
        this.books = books;
        this.context = context;
        this.categoryName = categoryName;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_category_book, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Book book = books.get(position);
        holder.bookTitle.setText(book.getTitle() + " - " + book.getAuthor());

        // Load cover
        if (book.getCoverUrl() != null && !book.getCoverUrl().isEmpty()) {
            Glide.with(context)
                    .load(book.getCoverUrl())
                    .placeholder(R.drawable.default_book_cover)
                    .error(R.drawable.default_book_cover)
                    .into(holder.bookCover);
        } else {
            holder.bookCover.setImageResource(R.drawable.default_book_cover);
        }

        if (holder.ivFavorite != null) {
            // Set icon dựa trên prefs
            String bookIdStr = String.valueOf(book.getId());  // Chuyển int sang String
            boolean isFavorite = isBookFavorite(bookIdStr);
            holder.ivFavorite.setImageResource(isFavorite ? R.drawable.ic_favorite : R.drawable.ic_favorite_border);

            holder.ivFavorite.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    toggleFavorite(bookIdStr, holder.ivFavorite);  // Pass String ID
                }
            });
        } else {
            Log.w("CategoryBookAdapter", "iv_favorite ImageView not found in layout");
        }
    }

    // toggleFavorite: Nhận String bookId, dùng List<String>
    private void toggleFavorite(String bookId, ImageView ivFavorite) {
        SharedPreferences prefs = context.getSharedPreferences("app_prefs", context.MODE_PRIVATE);
        String favoritesJson = prefs.getString("favorite_books", "[]");  // Key thống nhất
        Gson gson = new Gson();
        Type type = new TypeToken<List<String>>(){}.getType();
        List<String> favorites = gson.fromJson(favoritesJson, type);
        if (favorites == null) favorites = new ArrayList<>();

        if (favorites.contains(bookId)) {
            favorites.remove(bookId);  // Remove String
            ivFavorite.setImageResource(R.drawable.ic_favorite_border);
            Toast.makeText(context, "Removed from favorites", Toast.LENGTH_SHORT).show();
            syncBackendFavorite(bookId, false);
        } else {
            favorites.add(bookId);  // Add String
            ivFavorite.setImageResource(R.drawable.ic_favorite);
            Toast.makeText(context, "You have added to favorites list", Toast.LENGTH_SHORT).show();  // Message mới
            syncBackendFavorite(bookId, true);
        }

        // Lưu lại
        prefs.edit().putString("favorite_books", gson.toJson(favorites)).apply();
    }

    private void syncBackendFavorite(String bookId, boolean add) {
        try {
            SharedPreferences prefs = context.getSharedPreferences("app_prefs", context.MODE_PRIVATE);
            String userId = prefs.getString("user_id", null);
            if (userId != null) userId = userId.replace(".0", "");
            String token = prefs.getString("access_token", null);
            if (userId == null || token == null || token.isEmpty()) {
                return; // not logged in; local only
            }
            ApiService api = RetrofitClient.getApiService();
            Call<ApiResponse> call = add
                    ? api.addFavorite(userId, bookId, "Bearer " + token)
                    : api.removeFavorite(userId, bookId, "Bearer " + token);
            call.enqueue(new Callback<ApiResponse>() {
                @Override
                public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                    // No-op; optimistic UI already updated
                }

                @Override
                public void onFailure(Call<ApiResponse> call, Throwable t) {
                    // Optional: could revert local on failure
                }
            });
        } catch (Exception ignored) {}
    }

    // isBookFavorite: Nhận String bookId
    private boolean isBookFavorite(String bookId) {
        SharedPreferences prefs = context.getSharedPreferences("app_prefs", context.MODE_PRIVATE);
        String favoritesJson = prefs.getString("favorite_books", "[]");  // Key thống nhất
        Gson gson = new Gson();
        Type type = new TypeToken<List<String>>(){}.getType();
        List<String> favorites = gson.fromJson(favoritesJson, type);
        return favorites != null && favorites.contains(bookId);
    }

    @Override
    public int getItemCount() {
        return books.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView bookCover;
        TextView bookTitle;
        ImageView ivFavorite;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            bookCover = itemView.findViewById(R.id.book_cover);
            bookTitle = itemView.findViewById(R.id.book_category_label);
            ivFavorite = itemView.findViewById(R.id.iv_favorite);
        }
    }
}