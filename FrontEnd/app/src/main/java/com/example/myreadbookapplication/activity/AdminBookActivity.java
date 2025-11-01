package com.example.myreadbookapplication.activity;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.graphics.drawable.ColorDrawable;
import android.graphics.Color;
import android.view.Window;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myreadbookapplication.R;
import com.example.myreadbookapplication.adapter.AdminBookAdapter;
import com.example.myreadbookapplication.model.ApiResponse;
import com.example.myreadbookapplication.model.BooksResponse;
import com.example.myreadbookapplication.model.Book;
import com.example.myreadbookapplication.network.ApiService;
import com.example.myreadbookapplication.network.RetrofitClient;
import com.example.myreadbookapplication.utils.AuthManager;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminBookActivity extends AppCompatActivity {

    private static final String TAG = "AdminBookActivity";
    private static final int REQUEST_ADD_BOOK = 1;
    private static final int REQUEST_EDIT_BOOK = 2;
    private RecyclerView rvBook;
    private LinearLayout layoutEmpty;
    private ProgressBar progressBar;
    private LinearLayout navCategory;
    private LinearLayout navBook;
    private LinearLayout navFeedback;
    private LinearLayout navAccount;
    private FloatingActionButton fabAddBook;
    private android.widget.EditText etSearch;

    private ApiService apiService;
    private AuthManager authManager;
    private List<Book> bookList = new ArrayList<>();
    private List<Book> allBooksList = new ArrayList<>(); // Store all books for reset
    private AdminBookAdapter bookAdapter;
    private android.os.Handler searchHandler = new android.os.Handler();
    private Runnable searchRunnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_admin_book);

        apiService = RetrofitClient.getApiService();
        authManager = AuthManager.getInstance(this);

        initViews();
        setupClickListeners();
        loadBooks();
    }

    private void initViews() {
        rvBook = findViewById(R.id.rv_book);
        layoutEmpty = findViewById(R.id.layout_empty);
        progressBar = findViewById(R.id.progress_bar);
        fabAddBook = findViewById(R.id.fab_add_book);
        etSearch = findViewById(R.id.et_search);
        
        // Bottom navigation
        navCategory = findViewById(R.id.nav_category_in_book);
        navBook = findViewById(R.id.nav_book_in_book);
        navFeedback = findViewById(R.id.nav_feedback_in_book);
        navAccount = findViewById(R.id.nav_account_in_book);

        // Setup RecyclerView
        bookAdapter = new AdminBookAdapter(this, bookList);
        bookAdapter.setOnBookActionListener(new AdminBookAdapter.OnBookActionListener() {
            @Override
            public void onEditClick(Book book) {
                openEditBook(book);
            }

            @Override
            public void onDeleteClick(Book book) {
                showDeleteConfirmDialog(book);
            }
        });
        rvBook.setLayoutManager(new LinearLayoutManager(this));
        rvBook.setAdapter(bookAdapter);
    }

    private void setupClickListeners() {
        setupSearchListener();

        fabAddBook.setOnClickListener(v -> openAddBook());

        navCategory.setOnClickListener(v -> {
            Intent intent = new Intent(this, AdminCategoryActivity.class);
            startActivity(intent);
            finish();
        });

        navBook.setOnClickListener(v -> {
            // Already on book screen
        });

        navFeedback.setOnClickListener(v -> {
            Intent intent = new Intent(this, AdminFeedbackActivity.class);
            startActivity(intent);
            finish();
        });

        navAccount.setOnClickListener(v -> {
            Intent intent = new Intent(this, AdminAccountActivity.class);
            startActivity(intent);
            finish();
        });
    }

    private void openAddBook() {
        Intent intent = new Intent(this, AdminAddBookActivity.class);
        startActivityForResult(intent, REQUEST_ADD_BOOK);
    }

    private void openEditBook(Book book) {
        Intent intent = new Intent(this, AdminEditBookActivity.class);
        intent.putExtra(AdminEditBookActivity.EXTRA_BOOK, book);
        startActivityForResult(intent, REQUEST_EDIT_BOOK);
    }

    private void showDeleteConfirmDialog(Book book) {
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_confirm_delete);
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        TextView btnCancel = dialog.findViewById(R.id.btn_cancel);
        TextView btnYes = dialog.findViewById(R.id.btn_yes);

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnYes.setOnClickListener(v -> {
            deleteBook(book);
            dialog.dismiss();
        });

        dialog.show();
    }

    private void deleteBook(Book book) {
        String accessToken = authManager.getAccessToken();
        if (accessToken == null || accessToken.isEmpty()) {
            Toast.makeText(this, "Please login as admin", Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);

        int bookId = book.getIdAsInt();
        apiService.deleteBook(bookId, "Bearer " + accessToken).enqueue(new Callback<ApiResponse>() {
            @Override
            public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                progressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    Toast.makeText(AdminBookActivity.this, "Book deleted successfully!", Toast.LENGTH_SHORT).show();

                    bookList.remove(book);
                    bookAdapter.updateBookList(bookList);
                    if (bookList.isEmpty()) {
                        showEmptyState();
                    }
                } else {
                    Toast.makeText(AdminBookActivity.this, "Delete failed", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(AdminBookActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        if (requestCode == REQUEST_ADD_BOOK && resultCode == AdminAddBookActivity.RESULT_BOOK_ADDED) {
            Toast.makeText(this, "Book added!", Toast.LENGTH_SHORT).show();
            loadBooks(); // Reload the book list
        } else if (requestCode == REQUEST_EDIT_BOOK && resultCode == AdminEditBookActivity.RESULT_BOOK_UPDATED) {
            Toast.makeText(this, "Book updated!", Toast.LENGTH_SHORT).show();
            loadBooks(); // Reload the book list
        }
    }

    private void setupSearchListener() {
        etSearch.setOnEditorActionListener((v, actionId, event) -> {
            performSearch();
            return true;
        });

        etSearch.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Cancel previous search
                if (searchRunnable != null) {
                    searchHandler.removeCallbacks(searchRunnable);
                }

                // Debounce search - wait 500ms after user stops typing
                searchRunnable = () -> performSearch();
                searchHandler.postDelayed(searchRunnable, 500);
            }

            @Override
            public void afterTextChanged(android.text.Editable s) {}
        });
    }

    private void performSearch() {
        String query = etSearch.getText().toString().trim();
        
        if (query.isEmpty()) {
            // Show all books if search is empty
            bookList = new ArrayList<>(allBooksList);
            bookAdapter.updateBookList(bookList);
            updateListView();
            return;
        }

        // Call search API
        searchBooks(query);
    }

    private void searchBooks(String query) {
        progressBar.setVisibility(View.VISIBLE);
        layoutEmpty.setVisibility(View.GONE);

        apiService.searchBooks(query, 1, 100).enqueue(new Callback<ApiResponse<BooksResponse>>() {
            @Override
            public void onResponse(Call<ApiResponse<BooksResponse>> call, Response<ApiResponse<BooksResponse>> response) {
                progressBar.setVisibility(View.GONE);
                
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<BooksResponse> apiResponse = response.body();
                    if (apiResponse.isSuccess()) {
                        try {
                            BooksResponse booksResponse = apiResponse.getData();
                            if (booksResponse != null && booksResponse.getBooks() != null) {
                                bookList = booksResponse.getBooks();
                                bookAdapter.updateBookList(bookList);
                                updateListView();
                            } else {
                                showEmptyState();
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "Error parsing search results: " + e.getMessage());
                            showEmptyState();
                        }
                    } else {
                        showEmptyState();
                    }
                } else {
                    Log.e(TAG, "Search failed: " + response.code());
                    showEmptyState();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<BooksResponse>> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                Log.e(TAG, "Search error: " + t.getMessage());
                Toast.makeText(AdminBookActivity.this, "Search failed", Toast.LENGTH_SHORT).show();
                showEmptyState();
            }
        });
    }

    private void updateListView() {
        if (bookList.isEmpty()) {
            layoutEmpty.setVisibility(View.VISIBLE);
            rvBook.setVisibility(View.GONE);
        } else {
            layoutEmpty.setVisibility(View.GONE);
            rvBook.setVisibility(View.VISIBLE);
        }
    }

    private void loadAllBooks(String accessToken, int startPage) {
        // Load page with max limit (100)
        Call<ApiResponse<BooksResponse>> call = apiService.getAllBooks("Bearer " + accessToken, startPage, 100);
        
        call.enqueue(new Callback<ApiResponse<BooksResponse>>() {
            @Override
            public void onResponse(Call<ApiResponse<BooksResponse>> call, Response<ApiResponse<BooksResponse>> response) {
                if (!response.isSuccessful() || response.body() == null || !response.body().isSuccess()) {
                    progressBar.setVisibility(View.GONE);
                    if (allBooksList.isEmpty()) {
                        showEmptyState();
                    } else {
                        // At least show what we got
                        bookList = new ArrayList<>(allBooksList);
                        bookAdapter.updateBookList(bookList);
                        updateListView();
                    }
                    return;
                }

                try {
                    BooksResponse booksResponse = response.body().getData();
                    if (booksResponse != null && booksResponse.getBooks() != null) {
                        // Add books from this page
                        allBooksList.addAll(booksResponse.getBooks());
                        
                        BooksResponse.Pagination pagination = booksResponse.getPagination();
                        if (pagination != null) {
                            int currentPage = pagination.getPage();
                            int totalPages = pagination.getTotalPages();
                            int total = pagination.getTotal();
                            
                            Log.d(TAG, "Loaded page " + currentPage + "/" + totalPages + " (" + allBooksList.size() + "/" + total + " books)");
                            
                            // Load next page if there are more
                            if (currentPage < totalPages) {
                                loadAllBooks(accessToken, currentPage + 1);
                                return; // Don't update UI yet, wait for all pages
                            } else {
                                // All pages loaded
                                Log.d(TAG, "All books loaded: " + allBooksList.size() + " total");
                            }
                        }
                        
                        // Update UI with all loaded books
                        bookList = new ArrayList<>(allBooksList);
                        bookAdapter.updateBookList(bookList);
                        updateListView();
                        progressBar.setVisibility(View.GONE);
                    } else {
                        progressBar.setVisibility(View.GONE);
                        if (allBooksList.isEmpty()) {
                            showEmptyState();
                        } else {
                            bookList = new ArrayList<>(allBooksList);
                            bookAdapter.updateBookList(bookList);
                            updateListView();
                        }
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error parsing books: " + e.getMessage());
                    progressBar.setVisibility(View.GONE);
                    if (allBooksList.isEmpty()) {
                        showEmptyState();
                    } else {
                        bookList = new ArrayList<>(allBooksList);
                        bookAdapter.updateBookList(bookList);
                        updateListView();
                    }
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<BooksResponse>> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                Log.e(TAG, "Error loading books: " + t.getMessage());
                
                if (allBooksList.isEmpty()) {
                    Toast.makeText(AdminBookActivity.this, "Failed to load books", Toast.LENGTH_SHORT).show();
                    showEmptyState();
                } else {
                    // At least show what we got
                    Toast.makeText(AdminBookActivity.this, "Loaded " + allBooksList.size() + " books (some may be missing)", Toast.LENGTH_LONG).show();
                    bookList = new ArrayList<>(allBooksList);
                    bookAdapter.updateBookList(bookList);
                    updateListView();
                }
            }
        });
    }

    private void loadBooks() {
        String accessToken = authManager.getAccessToken();
        
        if (accessToken == null || accessToken.isEmpty()) {
            Toast.makeText(this, "Please login as admin", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        layoutEmpty.setVisibility(View.GONE);
        
        // Clear previous data
        allBooksList.clear();
        bookList.clear();

        // Backend limits max to 100 per page, so we need to load multiple pages
        loadAllBooks(accessToken, 1);
    }

    private void showEmptyState() {
        layoutEmpty.setVisibility(View.VISIBLE);
        rvBook.setVisibility(View.GONE);
    }
}

