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
import com.example.myreadbookapplication.model.ApiResponse;
import com.example.myreadbookapplication.network.ApiService;
import com.example.myreadbookapplication.network.RetrofitClient;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class NewBookAdapter extends RecyclerView.Adapter<NewBookAdapter.ViewHolder> {
    private List<Book> newBooks;
    private Context context;

    public NewBookAdapter(List<Book> newBooks, Context context) {
        this.newBooks = newBooks;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_new_book, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Book book = newBooks.get(position);
        holder.bookTitle.setText(book.getTitle());

        // Load cover với Glide
        if (book.getCoverUrl() != null && !book.getCoverUrl().isEmpty()) {
            Glide.with(context)
                    .load(book.getCoverUrl())
                    .placeholder(R.drawable.default_book_cover)
                    .error(R.drawable.default_book_cover)
                    .into(holder.bookCover);
        } else {
            holder.bookCover.setImageResource(R.drawable.default_book_cover);
        }

        // Thêm favorite icon (mới)
        if (holder.ivFavorite != null) {
            String bookIdStr = String.valueOf(book.getId());
            boolean isFavorite = isBookFavorite(bookIdStr);
            holder.ivFavorite.setImageResource(isFavorite ? R.drawable.ic_favorite_border : R.drawable.ic_favorite);

            holder.ivFavorite.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    toggleFavorite(bookIdStr, holder.ivFavorite);
                }
            });
        } else {
            Log.w("NewBookAdapter", "iv_favorite ImageView not found in layout");
        }
    }

    // Copy toggleFavorite từ CategoryBookAdapter
    private void toggleFavorite(String bookId, ImageView ivFavorite) {
        SharedPreferences prefs = context.getSharedPreferences("app_prefs", context.MODE_PRIVATE);
        String favoritesJson = prefs.getString("favorite_books", "[]");
        Gson gson = new Gson();
        Type type = new TypeToken<List<String>>(){}.getType();
        List<String> favorites = gson.fromJson(favoritesJson, type);
        if (favorites == null) favorites = new ArrayList<>();

        if (favorites.contains(bookId)) {
            favorites.remove(bookId);
            ivFavorite.setImageResource(R.drawable.ic_favorite);
            Toast.makeText(context, "Removed from favorites", Toast.LENGTH_SHORT).show();
            syncBackendFavorite(bookId, false);
        } else {
            favorites.add(bookId);
            ivFavorite.setImageResource(R.drawable.ic_favorite_border);
            Toast.makeText(context, "You have added to favorites list", Toast.LENGTH_SHORT).show();
            syncBackendFavorite(bookId, true);
        }

        prefs.edit().putString("favorite_books", gson.toJson(favorites)).apply();
    }

    // Copy isBookFavorite từ CategoryBookAdapter
    private boolean isBookFavorite(String bookId) {
        SharedPreferences prefs = context.getSharedPreferences("app_prefs", context.MODE_PRIVATE);
        String favoritesJson = prefs.getString("favorite_books", "[]");
        Gson gson = new Gson();
        Type type = new TypeToken<List<String>>(){}.getType();
        List<String> favorites = gson.fromJson(favoritesJson, type);
        return favorites != null && favorites.contains(bookId);
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
            retrofit2.Call<ApiResponse> call = add
                    ? api.addFavorite(userId, bookId, "Bearer " + token)
                    : api.removeFavorite(userId, bookId, "Bearer " + token);
            call.enqueue(new retrofit2.Callback<ApiResponse>() {
                @Override
                public void onResponse(retrofit2.Call<ApiResponse> call, retrofit2.Response<ApiResponse> response) {
                    // No-op for now
                }

                @Override
                public void onFailure(retrofit2.Call<ApiResponse> call, Throwable t) {
                    // No-op
                }
            });
        } catch (Exception ignored) {}
    }

    @Override
    public int getItemCount() {
        return newBooks.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView bookCover;
        TextView bookTitle;
        ImageView ivFavorite;  // Mới: Thêm field

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            bookCover = itemView.findViewById(R.id.book_cover);
            bookTitle = itemView.findViewById(R.id.book_title);
            ivFavorite = itemView.findViewById(R.id.iv_favorite);  // Ánh xạ
        }
    }
}