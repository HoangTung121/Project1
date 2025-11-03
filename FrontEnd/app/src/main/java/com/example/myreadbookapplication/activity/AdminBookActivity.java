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
import okhttp3.ResponseBody;
import java.io.IOException;

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
    private java.util.Map<Integer, String> categoryMap = new java.util.HashMap<>(); // Map category ID → name
    private boolean categoriesLoaded = false; // Flag to track if categories are loaded

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_admin_book);

        apiService = RetrofitClient.getApiService();
        authManager = AuthManager.getInstance(this);

        initViews();
        setupClickListeners();
        // Load categories first, then load books after categories are loaded
        loadCategories();
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
        apiService.hardDeleteBook(bookId, "Bearer " + accessToken).enqueue(new Callback<ApiResponse>() {
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
                        
                        // Always try to map category names (categoryMap may be populated by now)
                        Log.d(TAG, "Mapping category names - map size: " + categoryMap.size() + ", categoriesLoaded: " + categoriesLoaded);
                        mapCategoryNamesToBooks(allBooksList);
                        
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

    private void loadCategories() {
        // Use getCategoriesRawBody(null) to get all categories
        apiService.getCategoriesRawBody(null).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        String responseString = response.body().string();
                        com.google.gson.Gson gson = new com.google.gson.Gson();
                        com.google.gson.JsonObject jsonResponse = com.google.gson.JsonParser.parseString(responseString).getAsJsonObject();
                        
                        if (!jsonResponse.has("success") || !jsonResponse.get("success").getAsBoolean()) {
                            Log.e(TAG, "Categories API returned success=false");
                            loadBooks(); // Load books anyway
                            return;
                        }
                        
                        com.google.gson.JsonElement dataElement = jsonResponse.get("data");
                        if (dataElement == null || !dataElement.isJsonObject()) {
                            Log.w(TAG, "Data element is null or not an object");
                            loadBooks(); // Load books anyway
                            return;
                        }
                        
                        com.google.gson.JsonObject dataObj = dataElement.getAsJsonObject();
                        com.google.gson.JsonElement categoriesElement = dataObj.get("categories");
                        
                        if (categoriesElement != null && categoriesElement.isJsonObject()) {
                            com.google.gson.JsonObject categoriesObj = categoriesElement.getAsJsonObject();
                            categoryMap.clear();
                            
                            for (String key : categoriesObj.keySet()) {
                                try {
                                    com.google.gson.JsonObject catJson = categoriesObj.get(key).getAsJsonObject();
                                    int categoryId;
                                    
                                    // Try to get ID from _id field first, fallback to key
                                    if (catJson.has("_id")) {
                                        try {
                                            com.google.gson.JsonElement idElement = catJson.get("_id");
                                            if (idElement.isJsonPrimitive()) {
                                                if (idElement.getAsJsonPrimitive().isNumber()) {
                                                    categoryId = idElement.getAsInt();
                                                } else {
                                                    // Try parsing as string
                                                    categoryId = Integer.parseInt(idElement.getAsString());
                                                }
                                            } else {
                                                categoryId = Integer.parseInt(key);
                                            }
                                        } catch (Exception e) {
                                            Log.w(TAG, "Failed to parse _id for key " + key + ", using key as ID: " + e.getMessage());
                                            categoryId = Integer.parseInt(key);
                                        }
                                    } else {
                                        categoryId = Integer.parseInt(key);
                                    }
                                    
                                    String categoryName = null;
                                    if (catJson.has("name")) {
                                        com.google.gson.JsonElement nameElement = catJson.get("name");
                                        if (!nameElement.isJsonNull()) {
                                            categoryName = nameElement.getAsString();
                                        }
                                    }
                                    
                                    if (categoryName != null && !categoryName.isEmpty()) {
                                        categoryMap.put(categoryId, categoryName);
                                        Log.d(TAG, "✓ Category parsed: ID=" + categoryId + " (key=" + key + ") -> Name='" + categoryName + "'");
                                    } else {
                                        Log.w(TAG, "⚠ Category key=" + key + " has no name, skipping");
                                    }
                                } catch (Exception e) {
                                    Log.w(TAG, "Failed to parse category " + key + ": " + e.getMessage());
                                }
                            }
                            
                            Log.d(TAG, "Loaded " + categoryMap.size() + " categories for mapping");
                            Log.d(TAG, "Category IDs in map: " + categoryMap.keySet());
                            categoriesLoaded = true;
                        } else {
                            Log.w(TAG, "Categories element is not an object or is null");
                            categoriesLoaded = true; // Mark as loaded even if empty
                        }
                        
                        // Always load books after attempting to load categories
                        if (!allBooksList.isEmpty()) {
                            // Books already loaded, just update category names
                            Log.d(TAG, "Books already loaded, mapping category names now...");
                            Log.d(TAG, "Category map before mapping: " + categoryMap);
                            mapCategoryNamesToBooks(allBooksList);
                            // Force update adapter to refresh display
                            bookList = new ArrayList<>(allBooksList);
                            if (bookAdapter != null) {
                                bookAdapter.updateBookList(bookList);
                                updateListView();
                            }
                        } else {
                            // Categories loaded (or failed), now load books
                            Log.d(TAG, "Categories loaded (size: " + categoryMap.size() + "), now loading books...");
                            loadBooks();
                        }
                    } catch (IOException e) {
                        Log.e(TAG, "Error reading response body: " + e.getMessage());
                        // Load books anyway
                        loadBooks();
                    } catch (Exception e) {
                        Log.e(TAG, "Error parsing categories: " + e.getMessage());
                        // Load books anyway
                        loadBooks();
                    }
                } else {
                    Log.e(TAG, "Failed to load categories - HTTP " + response.code());
                    // Load books anyway (they will show Unknown Category)
                    loadBooks();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.e(TAG, "Failed to load categories: " + t.getMessage());
                // Even if categories fail, still load books (they will show Unknown Category)
                Log.w(TAG, "Categories load failed, loading books anyway...");
                loadBooks();
            }
        });
    }

    private void mapCategoryNamesToBooks(List<Book> books) {
        Log.d(TAG, "=== Mapping category names ===");
        Log.d(TAG, "Books to map: " + books.size());
        Log.d(TAG, "Category map size: " + categoryMap.size());
        Log.d(TAG, "Category map keys: " + categoryMap.keySet());
        
        int mappedCount = 0;
        int unknownCount = 0;
        
        for (Book book : books) {
            if (book != null) {
                // Book có category ID (int), không phải category name
                int categoryId = book.getCategory();
                
                // Try multiple lookup strategies
                String categoryName = null;
                
                // Strategy 1: Direct lookup by int key
                categoryName = categoryMap.get(categoryId);
                
                // Strategy 2: If not found, try looking up by Integer object
                if (categoryName == null || categoryName.isEmpty()) {
                    categoryName = categoryMap.get(Integer.valueOf(categoryId));
                }
                
                // Strategy 3: If still not found, iterate through all keys to find match
                if ((categoryName == null || categoryName.isEmpty()) && !categoryMap.isEmpty()) {
                    for (Integer key : categoryMap.keySet()) {
                        if (key != null && key.intValue() == categoryId) {
                            categoryName = categoryMap.get(key);
                            Log.d(TAG, "Found category by iteration: key=" + key + ", value=" + categoryName);
                            break;
                        }
                    }
                }
                
                if (categoryName != null && !categoryName.isEmpty()) {
                    book.setCategoryName(categoryName);
                    mappedCount++;
                    Log.d(TAG, "✓ Book: '" + book.getTitle() + "' -> Category ID " + categoryId + " = '" + categoryName + "'");
                } else {
                    unknownCount++;
                    // Log để debug
                    Log.w(TAG, "✗ Category not found for book: '" + book.getTitle() + 
                          "', categoryId: " + categoryId + 
                          ", map size: " + categoryMap.size());
                    if (!categoryMap.isEmpty()) {
                        Log.d(TAG, "Available category IDs in map: " + categoryMap.keySet());
                        Log.d(TAG, "Category ID " + categoryId + " type: " + (categoryId != 0 ? "non-zero int" : "zero"));
                        // Check if there's type mismatch - try all possible matches
                        boolean found = false;
                        for (Integer key : categoryMap.keySet()) {
                            if (key != null) {
                                if (key.intValue() == categoryId) {
                                    Log.d(TAG, "Found matching value: " + key + " (Integer) == " + categoryId + " (int)");
                                    found = true;
                                    // Force set the name if we found a match
                                    String foundName = categoryMap.get(key);
                                    if (foundName != null) {
                                        book.setCategoryName(foundName);
                                        mappedCount++;
                                        unknownCount--;
                                        Log.d(TAG, "✓ Force mapped: " + book.getTitle() + " -> " + foundName);
                                        break;
                                    }
                                }
                            }
                        }
                        if (!found) {
                            Log.w(TAG, "Category ID " + categoryId + " does not exist in map. Map contents: " + categoryMap);
                        }
                    } else {
                        Log.w(TAG, "Category map is EMPTY! Categories may not be loaded yet.");
                    }
                    // Don't set "Unknown Category" - leave it null so adapter can handle it
                    // The adapter will show the category ID if name is not available
                    if (book.getCategoryName() == null || book.getCategoryName().isEmpty() || book.getCategoryName().contains("Unknown")) {
                        book.setCategoryName(null); // Set to null, not "Unknown Category"
                    }
                }
            }
        }
        
        Log.d(TAG, "=== Mapping complete: " + mappedCount + " mapped, " + unknownCount + " unknown ===");
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

