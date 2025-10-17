package com.example.myreadbookapplication;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myreadbookapplication.model.ApiResponse;
import com.example.myreadbookapplication.model.Book;
import com.example.myreadbookapplication.model.HistoryItem;
import com.example.myreadbookapplication.model.ReadingHistoryResponse;
import com.example.myreadbookapplication.model.BooksResponse;
import com.example.myreadbookapplication.network.ApiService;
import com.example.myreadbookapplication.network.RetrofitClient;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HistoryActivity extends AppCompatActivity {

    private static final String TAG = "HistoryActivity";
    private ImageView backIconHistory;
    private RecyclerView rvHistoryBooks;
    private ProgressBar progressBarHistoryBooks;
    private CategoryBookAdapter historyBookAdapter;
    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        backIconHistory = findViewById(R.id.back_history_book_icon);
        rvHistoryBooks = findViewById(R.id.rv_history_books);
        progressBarHistoryBooks = findViewById(R.id.progressBar_history_books);
        apiService = RetrofitClient.getApiService();

        backIconHistory.setOnClickListener(v -> finish());

        loadHistoryBooks();
    }

    private void loadHistoryBooks() {
        progressBarHistoryBooks.setVisibility(View.VISIBLE);
        SharedPreferences prefs = getSharedPreferences("app_prefs", MODE_PRIVATE);
        String userId = prefs.getString("user_id", null);
        if (userId != null) {
            userId = userId.replace(".0", "");
        }
        String token = prefs.getString("access_token", null);

        if (userId == null || token == null || token.isEmpty()) {
            progressBarHistoryBooks.setVisibility(View.GONE);
            Toast.makeText(this, "Bạn cần đăng nhập để xem lịch sử", Toast.LENGTH_SHORT).show();
            return;
        }

        Call<ApiResponse<ReadingHistoryResponse>> call = apiService.getReadingHistory(
                userId,
                "Bearer " + token,
                1,
                50,
                "lastReadAt",
                "desc"
        );

        call.enqueue(new Callback<ApiResponse<ReadingHistoryResponse>>() {
            @Override
            public void onResponse(Call<ApiResponse<ReadingHistoryResponse>> call, Response<ApiResponse<ReadingHistoryResponse>> response) {
                progressBarHistoryBooks.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    ReadingHistoryResponse data = response.body().getData();
                    List<HistoryItem> items = (data != null) ? data.getHistories() : null;
                    List<Book> books = new ArrayList<>();
                    List<String> missingIds = new ArrayList<>();
                    if (items != null) {
                        for (HistoryItem item : items) {
                            if (item == null) continue;
                            if (item.getBook() != null) {
                                books.add(item.getBook());
                            } else {
                                // fall back to later fetch by ids
                                if (String.valueOf(item.getBookId()) != null) {
                                    missingIds.add(String.valueOf(item.getBookId()));
                                }
                            }
                        }
                    }

                    // If some items lack embedded book, fetch them by ids
                    if (!missingIds.isEmpty()) {
                        String idsQuery = String.join(",", missingIds);
                        RetrofitClient.getApiService().getBooksByIds(idsQuery, "active", null, 1)
                                .enqueue(new Callback<ApiResponse<BooksResponse>>() {
                                    @Override
                                    public void onResponse(Call<ApiResponse<BooksResponse>> call, Response<ApiResponse<BooksResponse>> response2) {
                                        if (response2.isSuccessful() && response2.body() != null && response2.body().isSuccess()) {
                                            BooksResponse br = response2.body().getData();
                                            if (br != null && br.getBooks() != null) {
                                                books.addAll(br.getBooks());
                                            }
                                        }
                                        renderBooks(books);
                                    }

                                    @Override
                                    public void onFailure(Call<ApiResponse<BooksResponse>> call, Throwable t) {
                                        renderBooks(books);
                                    }
                                });
                    } else {
                        renderBooks(books);
                    }
                } else {
                    Log.e(TAG, "History API failed: " + (response.code()));
                    Toast.makeText(HistoryActivity.this, "Không tải được lịch sử", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<ReadingHistoryResponse>> call, Throwable t) {
                progressBarHistoryBooks.setVisibility(View.GONE);
                Log.e(TAG, "History API error: " + t.getMessage());
                Toast.makeText(HistoryActivity.this, "Lỗi mạng", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void renderBooks(List<Book> books) {
        if (books != null && !books.isEmpty()) {
            historyBookAdapter = new CategoryBookAdapter(books, HistoryActivity.this, "History");
            rvHistoryBooks.setLayoutManager(new GridLayoutManager(HistoryActivity.this, 3));
            rvHistoryBooks.setAdapter(historyBookAdapter);
        } else {
            Toast.makeText(HistoryActivity.this, "Chưa có lịch sử đọc", Toast.LENGTH_SHORT).show();
        }
    }
}


