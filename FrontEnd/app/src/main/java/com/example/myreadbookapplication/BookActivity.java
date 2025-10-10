package com.example.myreadbookapplication;

import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myreadbookapplication.model.Book;
import com.example.myreadbookapplication.model.ApiResponse;
import com.example.myreadbookapplication.model.BooksResponse;
import com.example.myreadbookapplication.network.ApiService;
import com.example.myreadbookapplication.network.RetrofitClient;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BookActivity extends AppCompatActivity {

    private static final String TAG = "BookActivity";

    private Toolbar toolbar;
    private TextView tvTitle;
    private RecyclerView rvBooks;
    private CategoryBookAdapter bookAdapter;  // Dùng adapter cũ cho books
    private ProgressBar progressBar;
    private ApiService apiService;
    private ImageView backAllBookIcon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book);  // Layout mới

        // Ánh xạ views
        //toolbar = findViewById(R.id.toolbar);
        tvTitle = findViewById(R.id.tv_book_title);
        rvBooks = findViewById(R.id.rv_books);
        progressBar = findViewById(R.id.progressBar_books);
        apiService = RetrofitClient.getApiService();
        backAllBookIcon = findViewById(R.id.back_all_book_icon);

        // Set Toolbar
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        backAllBookIcon.setOnClickListener(v -> finish());

        // Nhận extra từ Home (nếu có)
        boolean isAllBooks = getIntent().getBooleanExtra("all_books", true);  // Default true
        String title = getIntent().getStringExtra("selected_category_name") != null
                ? getIntent().getStringExtra("selected_category_name") : "All Books";
        tvTitle.setText(title);
        tvTitle.setVisibility(View.VISIBLE);
        Log.d(TAG, "BookActivity opened for: " + title + ", AllBooks: " + isAllBooks);

        loadAllBooks();  // Load all books
    }

    // Back button
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void loadAllBooks() {
        progressBar.setVisibility(View.VISIBLE);
        Log.d(TAG, "Loading all books...");
        // Gọi API với category=null (tất cả sách)
        Call<ApiResponse<BooksResponse>> call = apiService.getBooks(null, "active", 50, 3);  // Limit 50, page 1
        call.enqueue(new Callback<ApiResponse<BooksResponse>>() {
            @Override
            public void onResponse(Call<ApiResponse<BooksResponse>> call, Response<ApiResponse<BooksResponse>> response) {
                progressBar.setVisibility(View.GONE);
                Log.d(TAG, "All books response code: " + response.code());
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    BooksResponse bookResp = response.body().getData();
                    List<Book> booksList = (bookResp != null) ? bookResp.getBooks() : null;
                    Log.d(TAG, "All books size: " + (booksList != null ? booksList.size() : 0));
                    if (booksList != null && !booksList.isEmpty()) {
                        bookAdapter = new CategoryBookAdapter(booksList, BookActivity.this, "All Books");  // Adapter cũ
                        rvBooks.setLayoutManager(new GridLayoutManager(BookActivity.this, 3));  // Grid 3 cột
                        rvBooks.setAdapter(bookAdapter);
                        rvBooks.invalidate();
                        Log.d(TAG, "All books adapter set");
                    } else {
                        Toast.makeText(BookActivity.this, "No books found", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(BookActivity.this, "Load all books failed", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<BooksResponse>> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                Log.e(TAG, "All books failure: " + t.getMessage());
                Toast.makeText(BookActivity.this, "Network error", Toast.LENGTH_SHORT).show();
            }
        });
    }
}