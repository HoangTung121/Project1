package com.example.myreadbookapplication.fragment.admin;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myreadbookapplication.R;
import com.example.myreadbookapplication.activity.Admin.AdminAddBookActivity;
import com.example.myreadbookapplication.activity.Admin.AdminEditBookActivity;
import com.example.myreadbookapplication.adapter.AdminBookAdapter;
import com.example.myreadbookapplication.model.ApiResponse;
import com.example.myreadbookapplication.model.Book;
import com.example.myreadbookapplication.model.BooksResponse;
import com.example.myreadbookapplication.network.ApiService;
import com.example.myreadbookapplication.network.RetrofitClient;
import com.example.myreadbookapplication.utils.AuthManager;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BookFragment extends Fragment {

    private static final String TAG = "BookFragment";
    private static final int REQUEST_ADD_BOOK = 1;
    private static final int REQUEST_EDIT_BOOK = 2;

    private RecyclerView rvBook;
    private LinearLayout layoutEmpty;
    private ProgressBar progressBar;
    private FloatingActionButton fabAddBook;
    private EditText etSearch;

    private ApiService apiService;
    private AuthManager authManager;
    private List<Book> bookList = new ArrayList<>();
    private List<Book> allBooksList = new ArrayList<>();
    private AdminBookAdapter bookAdapter;
    private Handler searchHandler = new Handler(Looper.getMainLooper());
    private Runnable searchRunnable;
    private Map<Integer, String> categoryMap = new HashMap<>();
    private boolean categoriesLoaded = false;
    private boolean isDataLoaded = false;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        apiService = RetrofitClient.getApiService();
        authManager = AuthManager.getInstance(requireContext());
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_book, container, false);
        
        initViews(view);
        setupRecyclerView();
        setupClickListeners();
        setupSearchListener();
        
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (!isDataLoaded) {
            loadCategories();
        }
    }

    private void initViews(View view) {
        rvBook = view.findViewById(R.id.rv_book);
        layoutEmpty = view.findViewById(R.id.layout_empty);
        progressBar = view.findViewById(R.id.progress_bar);
        fabAddBook = view.findViewById(R.id.fab_add_book);
        etSearch = view.findViewById(R.id.et_search);
    }

    private void setupRecyclerView() {
        bookAdapter = new AdminBookAdapter(requireContext(), bookList);
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
        rvBook.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvBook.setAdapter(bookAdapter);
    }

    private void setupClickListeners() {
        if (fabAddBook != null) {
            fabAddBook.setOnClickListener(v -> openAddBook());
        }
    }

    private void openAddBook() {
        Intent intent = new Intent(requireContext(), AdminAddBookActivity.class);
        startActivityForResult(intent, REQUEST_ADD_BOOK);
    }

    private void openEditBook(Book book) {
        Intent intent = new Intent(requireContext(), AdminEditBookActivity.class);
        intent.putExtra(AdminEditBookActivity.EXTRA_BOOK, book);
        startActivityForResult(intent, REQUEST_EDIT_BOOK);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        if (requestCode == REQUEST_ADD_BOOK && resultCode == AdminAddBookActivity.RESULT_BOOK_ADDED) {
            Toast.makeText(requireContext(), "Book added!", Toast.LENGTH_SHORT).show();
            loadBooks();
        } else if (requestCode == REQUEST_EDIT_BOOK && resultCode == AdminEditBookActivity.RESULT_BOOK_UPDATED) {
            Toast.makeText(requireContext(), "Book updated!", Toast.LENGTH_SHORT).show();
            loadBooks();
        }
    }

    private void showDeleteConfirmDialog(Book book) {
        Dialog dialog = new Dialog(requireContext());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_confirm_delete);
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        TextView btnCancel = dialog.findViewById(R.id.btn_cancel);
        TextView btnYes = dialog.findViewById(R.id.btn_yes);

        if (btnCancel != null) {
            btnCancel.setOnClickListener(v -> dialog.dismiss());
        }

        if (btnYes != null) {
            btnYes.setOnClickListener(v -> {
                deleteBook(book);
                dialog.dismiss();
            });
        }

        dialog.show();
    }

    private void deleteBook(Book book) {
        String accessToken = authManager.getAccessToken();
        if (accessToken == null || accessToken.isEmpty()) {
            Toast.makeText(requireContext(), "Please login as admin", Toast.LENGTH_SHORT).show();
            return;
        }

        if (progressBar != null) {
            progressBar.setVisibility(View.VISIBLE);
        }

        int bookId = book.getIdAsInt();
        apiService.hardDeleteBook(bookId, "Bearer " + accessToken).enqueue(new Callback<ApiResponse>() {
            @Override
            public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                if (progressBar != null) {
                    progressBar.setVisibility(View.GONE);
                }
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    Toast.makeText(requireContext(), "Book deleted successfully!", Toast.LENGTH_SHORT).show();

                    bookList.remove(book);
                    allBooksList.remove(book);
                    bookAdapter.updateBookList(bookList);
                    if (bookList.isEmpty()) {
                        showEmptyState();
                    }
                } else {
                    Toast.makeText(requireContext(), "Delete failed", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse> call, Throwable t) {
                if (progressBar != null) {
                    progressBar.setVisibility(View.GONE);
                }
                Toast.makeText(requireContext(), "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupSearchListener() {
        if (etSearch == null) return;

        etSearch.setOnEditorActionListener((v, actionId, event) -> {
            performSearch();
            return true;
        });

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (searchRunnable != null) {
                    searchHandler.removeCallbacks(searchRunnable);
                }

                searchRunnable = () -> performSearch();
                searchHandler.postDelayed(searchRunnable, 500);
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void performSearch() {
        if (etSearch == null) return;

        String query = etSearch.getText().toString().trim();
        
        if (query.isEmpty()) {
            bookList = new ArrayList<>(allBooksList);
            bookAdapter.updateBookList(bookList);
            updateListView();
            return;
        }

        searchBooks(query);
    }

    private void searchBooks(String query) {
        if (progressBar != null) {
            progressBar.setVisibility(View.VISIBLE);
        }
        if (layoutEmpty != null) {
            layoutEmpty.setVisibility(View.GONE);
        }

        apiService.searchBooks(query, 1, 100).enqueue(new Callback<ApiResponse<BooksResponse>>() {
            @Override
            public void onResponse(Call<ApiResponse<BooksResponse>> call, Response<ApiResponse<BooksResponse>> response) {
                if (progressBar != null) {
                    progressBar.setVisibility(View.GONE);
                }
                
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
                if (progressBar != null) {
                    progressBar.setVisibility(View.GONE);
                }
                Log.e(TAG, "Search error: " + t.getMessage());
                Toast.makeText(requireContext(), "Search failed", Toast.LENGTH_SHORT).show();
                showEmptyState();
            }
        });
    }

    private void updateListView() {
        if (bookList.isEmpty()) {
            showEmptyState();
        } else {
            hideEmptyState();
        }
    }

    /**
     * Public method để reload data từ Activity
     */
    public void reloadData() {
        Log.d(TAG, "Reloading books data...");
        // Clear data và load lại
        allBooksList.clear();
        bookList.clear();
        categoryMap.clear();
        categoriesLoaded = false;
        isDataLoaded = false;
        loadCategories();
    }

    private void loadCategories() {
        apiService.getCategoriesRawBody(null).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        String responseString = response.body().string();
                        Gson gson = new Gson();
                        JsonObject jsonResponse = JsonParser.parseString(responseString).getAsJsonObject();
                        
                        if (!jsonResponse.has("success") || !jsonResponse.get("success").getAsBoolean()) {
                            Log.e(TAG, "Categories API returned success=false");
                            loadBooks();
                            return;
                        }
                        
                        JsonElement dataElement = jsonResponse.get("data");
                        if (dataElement == null || !dataElement.isJsonObject()) {
                            Log.w(TAG, "Data element is null or not an object");
                            loadBooks();
                            return;
                        }
                        
                        JsonObject dataObj = dataElement.getAsJsonObject();
                        JsonElement categoriesElement = dataObj.get("categories");
                        
                        if (categoriesElement != null && categoriesElement.isJsonObject()) {
                            JsonObject categoriesObj = categoriesElement.getAsJsonObject();
                            categoryMap.clear();
                            
                            for (String key : categoriesObj.keySet()) {
                                try {
                                    JsonObject catJson = categoriesObj.get(key).getAsJsonObject();
                                    int categoryId;
                                    
                                    if (catJson.has("_id")) {
                                        try {
                                            JsonElement idElement = catJson.get("_id");
                                            if (idElement.isJsonPrimitive()) {
                                                if (idElement.getAsJsonPrimitive().isNumber()) {
                                                    categoryId = idElement.getAsInt();
                                                } else {
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
                                        JsonElement nameElement = catJson.get("name");
                                        if (!nameElement.isJsonNull()) {
                                            categoryName = nameElement.getAsString();
                                        }
                                    }
                                    
                                    if (categoryName != null && !categoryName.isEmpty()) {
                                        categoryMap.put(categoryId, categoryName);
                                    }
                                } catch (Exception e) {
                                    Log.w(TAG, "Failed to parse category " + key + ": " + e.getMessage());
                                }
                            }
                            
                            Log.d(TAG, "Loaded " + categoryMap.size() + " categories for mapping");
                            categoriesLoaded = true;
                        } else {
                            Log.w(TAG, "Categories element is not an object or is null");
                            categoriesLoaded = true;
                        }
                        
                        if (!allBooksList.isEmpty()) {
                            mapCategoryNamesToBooks(allBooksList);
                            bookList = new ArrayList<>(allBooksList);
                            if (bookAdapter != null) {
                                bookAdapter.updateBookList(bookList);
                                updateListView();
                            }
                        } else {
                            loadBooks();
                        }
                    } catch (IOException e) {
                        Log.e(TAG, "Error reading response body: " + e.getMessage());
                        loadBooks();
                    } catch (Exception e) {
                        Log.e(TAG, "Error parsing categories: " + e.getMessage());
                        loadBooks();
                    }
                } else {
                    Log.e(TAG, "Failed to load categories - HTTP " + response.code());
                    loadBooks();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.e(TAG, "Failed to load categories: " + t.getMessage());
                loadBooks();
            }
        });
    }

    private void mapCategoryNamesToBooks(List<Book> books) {
        for (Book book : books) {
            if (book != null) {
                int categoryId = book.getCategory();
                String categoryName = categoryMap.get(categoryId);
                
                if (categoryName == null || categoryName.isEmpty()) {
                    categoryName = categoryMap.get(Integer.valueOf(categoryId));
                }
                
                if ((categoryName == null || categoryName.isEmpty()) && !categoryMap.isEmpty()) {
                    for (Integer key : categoryMap.keySet()) {
                        if (key != null && key.intValue() == categoryId) {
                            categoryName = categoryMap.get(key);
                            break;
                        }
                    }
                }
                
                if (categoryName != null && !categoryName.isEmpty()) {
                    book.setCategoryName(categoryName);
                }
            }
        }
    }

    private void loadAllBooks(String accessToken, int startPage) {
        Call<ApiResponse<BooksResponse>> call = apiService.getAllBooks("Bearer " + accessToken, startPage, 100);
        
        call.enqueue(new Callback<ApiResponse<BooksResponse>>() {
            @Override
            public void onResponse(Call<ApiResponse<BooksResponse>> call, Response<ApiResponse<BooksResponse>> response) {
                if (!response.isSuccessful() || response.body() == null || !response.body().isSuccess()) {
                    if (progressBar != null) {
                        progressBar.setVisibility(View.GONE);
                    }
                    if (allBooksList.isEmpty()) {
                        showEmptyState();
                    } else {
                        bookList = new ArrayList<>(allBooksList);
                        bookAdapter.updateBookList(bookList);
                        updateListView();
                    }
                    return;
                }

                try {
                    BooksResponse booksResponse = response.body().getData();
                    if (booksResponse != null && booksResponse.getBooks() != null) {
                        allBooksList.addAll(booksResponse.getBooks());
                        
                        BooksResponse.Pagination pagination = booksResponse.getPagination();
                        if (pagination != null) {
                            int currentPage = pagination.getPage();
                            int totalPages = pagination.getTotalPages();
                            
                            Log.d(TAG, "Loaded page " + currentPage + "/" + totalPages + " (" + allBooksList.size() + " books)");
                            
                            if (currentPage < totalPages) {
                                loadAllBooks(accessToken, currentPage + 1);
                                return;
                            }
                        }
                        
                        mapCategoryNamesToBooks(allBooksList);
                        
                        bookList = new ArrayList<>(allBooksList);
                        bookAdapter.updateBookList(bookList);
                        updateListView();
                        if (progressBar != null) {
                            progressBar.setVisibility(View.GONE);
                        }
                        isDataLoaded = true;
                    } else {
                        if (progressBar != null) {
                            progressBar.setVisibility(View.GONE);
                        }
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
                    if (progressBar != null) {
                        progressBar.setVisibility(View.GONE);
                    }
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
                if (progressBar != null) {
                    progressBar.setVisibility(View.GONE);
                }
                Log.e(TAG, "Error loading books: " + t.getMessage());
                
                if (allBooksList.isEmpty()) {
                    Toast.makeText(requireContext(), "Failed to load books", Toast.LENGTH_SHORT).show();
                    showEmptyState();
                } else {
                    Toast.makeText(requireContext(), "Loaded " + allBooksList.size() + " books (some may be missing)", Toast.LENGTH_LONG).show();
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
            Toast.makeText(requireContext(), "Please login as admin", Toast.LENGTH_SHORT).show();
            return;
        }

        if (progressBar != null) {
            progressBar.setVisibility(View.VISIBLE);
        }
        if (layoutEmpty != null) {
            layoutEmpty.setVisibility(View.GONE);
        }
        
        allBooksList.clear();
        bookList.clear();

        loadAllBooks(accessToken, 1);
    }

    private void showEmptyState() {
        if (layoutEmpty != null) {
            layoutEmpty.setVisibility(View.VISIBLE);
        }
        if (rvBook != null) {
            rvBook.setVisibility(View.GONE);
        }
    }

    private void hideEmptyState() {
        if (layoutEmpty != null) {
            layoutEmpty.setVisibility(View.GONE);
        }
        if (rvBook != null) {
            rvBook.setVisibility(View.VISIBLE);
        }
    }
}

