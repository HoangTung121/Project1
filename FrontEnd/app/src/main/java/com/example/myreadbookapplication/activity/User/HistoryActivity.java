package com.example.myreadbookapplication.activity.User;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myreadbookapplication.R;
import com.example.myreadbookapplication.adapter.CategoryBookAdapter;
import com.example.myreadbookapplication.model.ApiResponse;
import com.example.myreadbookapplication.model.Book;
import com.example.myreadbookapplication.model.HistoryItem;
import com.example.myreadbookapplication.model.ReadingHistoryResponse;
import com.example.myreadbookapplication.model.BooksResponse;
import com.example.myreadbookapplication.network.ApiService;
import com.example.myreadbookapplication.network.RetrofitClient;
import com.example.myreadbookapplication.utils.AuthManager;
import com.example.myreadbookapplication.utils.PaginationManager;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

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
    private AuthManager authManager;
    
    // Pagination
    private PaginationManager paginationManager;
    private FrameLayout paginationContainer;
    private int currentPage = 1;
    private int totalPages = 1;
    private int totalItems = 0;
    private int itemsPerPage = PaginationManager.DEFAULT_ITEMS_PER_PAGE;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        backIconHistory = findViewById(R.id.back_history_book_icon);
        rvHistoryBooks = findViewById(R.id.rv_history_books);
        progressBarHistoryBooks = findViewById(R.id.progressBar_history_books);
        paginationContainer = findViewById(R.id.pagination_container);
        apiService = RetrofitClient.getApiService();
        authManager = AuthManager.getInstance(this);

        backIconHistory.setOnClickListener(v -> finish());

        // Initialize pagination
        initPagination();

        loadHistoryBooks();
    }

    private void initPagination() {
        paginationManager = new PaginationManager(this, paginationContainer);
        paginationManager.setOnPageChangeListener(page -> {
            currentPage = page;
            loadHistoryBooks();
        });
        paginationManager.setOnPageJumpListener(page -> {
            currentPage = page;
            loadHistoryBooks();
        });
    }

    private void loadHistoryBooks() {
        progressBarHistoryBooks.setVisibility(View.VISIBLE);
        
        Log.d(TAG, "=== Starting loadHistoryBooks ===");
        Log.d(TAG, "AuthManager isLoggedIn: " + authManager.isLoggedIn());
        
        // Kiểm tra user đã đăng nhập chưa
        if (!authManager.isLoggedIn()) {
            progressBarHistoryBooks.setVisibility(View.GONE);
            Log.e(TAG, "User not logged in");
            Toast.makeText(this, "Bạn cần đăng nhập để xem lịch sử", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = authManager.getUserId();
        String authHeader = authManager.getAuthorizationHeader();

        Log.d(TAG, "UserId: " + userId);
        Log.d(TAG, "AuthHeader: " + (authHeader != null ? "Present" : "Null"));

        if (userId == null || authHeader == null) {
            progressBarHistoryBooks.setVisibility(View.GONE);
            Log.e(TAG, "Missing userId or authHeader");
            Toast.makeText(this, "Lỗi xác thực. Vui lòng đăng nhập lại", Toast.LENGTH_SHORT).show();
            return;
        }

        Log.d(TAG, "Loading history for user: " + userId);

        itemsPerPage = PaginationManager.DEFAULT_ITEMS_PER_PAGE;
        Call<ApiResponse<ReadingHistoryResponse>> call = apiService.getReadingHistory(
                userId,
                authHeader,
                currentPage,
                itemsPerPage,
                "lastReadAt",
                "desc"
        );

        call.enqueue(new Callback<ApiResponse<ReadingHistoryResponse>>() {
            @Override
            public void onResponse(Call<ApiResponse<ReadingHistoryResponse>> call, Response<ApiResponse<ReadingHistoryResponse>> response) {
                progressBarHistoryBooks.setVisibility(View.GONE);
                
                Log.d(TAG, "=== API Response ===");
                Log.d(TAG, "Response code: " + response.code());
                Log.d(TAG, "Response successful: " + response.isSuccessful());
                Log.d(TAG, "Response body null: " + (response.body() == null));
                
                if (response.body() != null) {
                    Log.d(TAG, "Response success: " + response.body().isSuccess());
                    Log.d(TAG, "Response message: " + response.body().getMessage());
                }
                
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    ReadingHistoryResponse data = response.body().getData();
                    List<HistoryItem> items = (data != null) ? data.getHistories() : null;
                    
                    Log.d(TAG, "=== Data Processing ===");
                    Log.d(TAG, "Data null: " + (data == null));
                    Log.d(TAG, "Items null: " + (items == null));
                    Log.d(TAG, "Items size: " + (items != null ? items.size() : 0));
                    
                    if (items != null) {
                        for (int i = 0; i < items.size(); i++) {
                            HistoryItem item = items.get(i);
                            Log.d(TAG, "Item " + i + ": bookId=" + item.getBookId() + ", page=" + item.getPage() + ", book=" + (item.getBook() != null ? "present" : "null"));
                        }
                    }
                    
                    // Update pagination info
                    if (data != null && data.getPagination() != null) {
                        totalPages = data.getPagination().getTotalPages();
                        totalItems = data.getPagination().getTotal();
                        itemsPerPage = PaginationManager.DEFAULT_ITEMS_PER_PAGE;
                        Log.d(TAG, "Pagination: page=" + currentPage + ", totalPages=" + totalPages + ", totalItems=" + totalItems);
                        paginationManager.setPaginationData(currentPage, totalPages, totalItems, itemsPerPage);
                        paginationManager.setVisible(totalPages > 1);
                        paginationContainer.setVisibility(totalPages > 1 ? View.VISIBLE : View.GONE);
                    } else {
                        totalItems = items != null ? items.size() : 0;
                        totalPages = Math.max(1, (int) Math.ceil((double) totalItems / PaginationManager.DEFAULT_ITEMS_PER_PAGE));
                        paginationManager.setPaginationData(currentPage, totalPages, totalItems, PaginationManager.DEFAULT_ITEMS_PER_PAGE);
                        paginationManager.setVisible(totalPages > 1);
                        paginationContainer.setVisibility(totalPages > 1 ? View.VISIBLE : View.GONE);
                    }
                    
                    List<Book> fallbackBooks = new ArrayList<>();
                    LinkedHashSet<String> bookIds = new LinkedHashSet<>();
                    Map<String, String> progressById = new HashMap<>();
                    if (items != null) {
                        for (HistoryItem item : items) {
                            if (item == null) continue;
                            String idStr = String.valueOf(item.getBookId());
                            if (idStr != null && !idStr.isEmpty()) {
                                bookIds.add(idStr);
                                String progressInfo = getReadingProgressInfo(item);
                                if (progressInfo != null && !progressInfo.isEmpty()) {
                                    progressById.put(idStr, progressInfo);
                                }
                            }
                            if (item.getBook() != null) {
                                fallbackBooks.add(item.getBook());
                            }
                        }
                    }

                    if (!bookIds.isEmpty()) {
                        String idsQuery = String.join(",", bookIds);
                        RetrofitClient.getApiService().getBooksByIds(idsQuery, "active", null, 1)
                                .enqueue(new Callback<ApiResponse<BooksResponse>>() {
                                    @Override
                                    public void onResponse(Call<ApiResponse<BooksResponse>> call, Response<ApiResponse<BooksResponse>> response2) {
                                        List<Book> booksToDisplay = new ArrayList<>();
                                        if (response2.isSuccessful() && response2.body() != null && response2.body().isSuccess()) {
                                            BooksResponse br = response2.body().getData();
                                            if (br != null && br.getBooks() != null && !br.getBooks().isEmpty()) {
                                                for (Book book : br.getBooks()) {
                                                    if (book == null) continue;
                                                    String progress = progressById.get(book.getId());
                                                    if (progress != null && !progress.isEmpty()) {
                                                        book.setTitle(book.getTitle() + " - " + progress);
                                                    }
                                                    booksToDisplay.add(book);
                                                }
                                            }
                                        }

                                        if (booksToDisplay.isEmpty() && !fallbackBooks.isEmpty()) {
                                            for (Book book : fallbackBooks) {
                                                if (book == null) continue;
                                                String progress = progressById.get(book.getId());
                                                if (progress != null && !progress.isEmpty()) {
                                                    book.setTitle(book.getTitle() + " - " + progress);
                                                }
                                                booksToDisplay.add(book);
                                            }
                                        }

                                        renderBooks(booksToDisplay);
                                    }

                                    @Override
                                    public void onFailure(Call<ApiResponse<BooksResponse>> call, Throwable t) {
                                        List<Book> booksToDisplay = new ArrayList<>();
                                        for (Book book : fallbackBooks) {
                                            if (book == null) continue;
                                            String progress = progressById.get(book.getId());
                                            if (progress != null && !progress.isEmpty()) {
                                                book.setTitle(book.getTitle() + " - " + progress);
                                            }
                                            booksToDisplay.add(book);
                                        }
                                        renderBooks(booksToDisplay);
                                    }
                                });
                    } else {
                        List<Book> booksToDisplay = new ArrayList<>();
                        for (Book book : fallbackBooks) {
                            if (book == null) continue;
                            String progress = progressById.get(book.getId());
                            if (progress != null && !progress.isEmpty()) {
                                book.setTitle(book.getTitle() + " - " + progress);
                            }
                            booksToDisplay.add(book);
                        }
                        renderBooks(booksToDisplay);
                    }
                } else {
                    Log.e(TAG, "History API failed: " + (response.code()));
                    String errorMessage = "Không tải được lịch sử";
                    
                    try {
                        if (response.errorBody() != null) {
                            String errorBody = response.errorBody().string();
                            Log.e(TAG, "Error response: " + errorBody);
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error reading error body", e);
                    }
                    
                    if (response.code() == 401) {
                        errorMessage = "Phiên đăng nhập hết hạn. Vui lòng đăng nhập lại";
                        // Có thể redirect về login screen
                    } else if (response.code() == 403) {
                        errorMessage = "Bạn không có quyền xem lịch sử này";
                    } else if (response.code() == 404) {
                        errorMessage = "Không tìm thấy lịch sử đọc";
                    }
                    
                    Toast.makeText(HistoryActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                    paginationManager.setVisible(false);
                    paginationContainer.setVisibility(View.GONE);
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<ReadingHistoryResponse>> call, Throwable t) {
                progressBarHistoryBooks.setVisibility(View.GONE);
                Log.e(TAG, "History API error: " + t.getMessage());
                Toast.makeText(HistoryActivity.this, "Lỗi mạng", Toast.LENGTH_SHORT).show();
                paginationManager.setVisible(false);
                paginationContainer.setVisibility(View.GONE);
            }
        });
    }

    private void renderBooks(List<Book> books) {
        Log.d(TAG, "=== renderBooks ===");
        Log.d(TAG, "Books null: " + (books == null));
        Log.d(TAG, "Books size: " + (books != null ? books.size() : 0));
        
        if (books != null && !books.isEmpty()) {
            int endIndex = Math.min(books.size(), PaginationManager.DEFAULT_ITEMS_PER_PAGE);
            List<Book> displayBooks = new ArrayList<>(books.subList(0, endIndex));
            Log.d(TAG, "Creating adapter with " + displayBooks.size() + " books");
            historyBookAdapter = new CategoryBookAdapter(displayBooks, HistoryActivity.this, "History");
            rvHistoryBooks.setLayoutManager(new GridLayoutManager(HistoryActivity.this, 3));
            rvHistoryBooks.setAdapter(historyBookAdapter);
            Log.d(TAG, "Adapter set successfully");
        } else {
            Log.d(TAG, "No books to display, showing toast");
            Toast.makeText(HistoryActivity.this, "Chưa có lịch sử đọc", Toast.LENGTH_SHORT).show();
        }
    }

    private String getReadingProgressInfo(HistoryItem item) {
        if (item == null) return null;
        
        StringBuilder progress = new StringBuilder();
        
        // Add chapter info if available
        if (item.getChapterId() != null && !item.getChapterId().isEmpty() && !item.getChapterId().equals("null")) {
            progress.append("📖 Chương: ").append(item.getChapterId());
        }
        
        // Add page info if available
        if (item.getPage() > 0) {
            if (progress.length() > 0) {
                progress.append(" | ");
            }
            progress.append("📄 Trang: ").append(item.getPage());
        }
        
        // Add last read time if available
        if (item.getLastReadAt() > 0) {
            if (progress.length() > 0) {
                progress.append(" | ");
            }
            long timeDiff = System.currentTimeMillis() - item.getLastReadAt();
            String timeAgo = getTimeAgo(timeDiff);
            progress.append("🕒 ").append(timeAgo);
        }
        
        return progress.length() > 0 ? progress.toString() : null;
    }

    private String getTimeAgo(long timeDiff) {
        long seconds = timeDiff / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long days = hours / 24;
        
        if (days > 0) {
            return days + " ngày trước";
        } else if (hours > 0) {
            return hours + " giờ trước";
        } else if (minutes > 0) {
            return minutes + " phút trước";
        } else {
            return "Vừa xong";
        }
    }

    /**
     * Xóa bookmark của một cuốn sách
     */
    public void deleteBookmark(int bookId) {
        if (!authManager.isLoggedIn()) {
            Toast.makeText(this, "Bạn cần đăng nhập để thực hiện thao tác này", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = authManager.getUserId();
        String authHeader = authManager.getAuthorizationHeader();

        if (userId == null || authHeader == null) {
            Toast.makeText(this, "Lỗi xác thực. Vui lòng đăng nhập lại", Toast.LENGTH_SHORT).show();
            return;
        }

        Log.d(TAG, "Deleting bookmark for user: " + userId + ", book: " + bookId);

        Call<ApiResponse> call = apiService.deleteBookmark(userId, String.valueOf(bookId), authHeader);
        call.enqueue(new Callback<ApiResponse>() {
            @Override
            public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    Toast.makeText(HistoryActivity.this, "Đã xóa bookmark thành công", Toast.LENGTH_SHORT).show();
                    // Reload history để cập nhật danh sách
                    loadHistoryBooks();
                } else {
                    String errorMessage = "Không thể xóa bookmark";
                    if (response.code() == 404) {
                        errorMessage = "Không tìm thấy bookmark để xóa";
                    } else if (response.code() == 403) {
                        errorMessage = "Bạn không có quyền xóa bookmark này";
                    }
                    Toast.makeText(HistoryActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse> call, Throwable t) {
                Log.e(TAG, "Delete bookmark error: " + t.getMessage(), t);
                Toast.makeText(HistoryActivity.this, "Lỗi mạng. Vui lòng thử lại", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Lấy bookmark của một cuốn sách cụ thể
     */
    public void getBookmark(int bookId, BookmarkCallback callback) {
        if (!authManager.isLoggedIn()) {
            if (callback != null) callback.onBookmarkReceived(null);
            return;
        }

        String userId = authManager.getUserId();
        String authHeader = authManager.getAuthorizationHeader();

        if (userId == null || authHeader == null) {
            if (callback != null) callback.onBookmarkReceived(null);
            return;
        }

        Call<ApiResponse<HistoryItem>> call = apiService.getBookmark(userId, String.valueOf(bookId), authHeader);
        call.enqueue(new Callback<ApiResponse<HistoryItem>>() {
            @Override
            public void onResponse(Call<ApiResponse<HistoryItem>> call, Response<ApiResponse<HistoryItem>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    if (callback != null) callback.onBookmarkReceived(response.body().getData());
                } else {
                    if (callback != null) callback.onBookmarkReceived(null);
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<HistoryItem>> call, Throwable t) {
                Log.e(TAG, "Get bookmark error: " + t.getMessage(), t);
                if (callback != null) callback.onBookmarkReceived(null);
            }
        });
    }

    /**
     * Interface callback cho bookmark
     */
    public interface BookmarkCallback {
        void onBookmarkReceived(HistoryItem bookmark);
    }
}


