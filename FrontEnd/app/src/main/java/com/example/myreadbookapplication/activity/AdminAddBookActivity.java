package com.example.myreadbookapplication.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Spinner;
import android.widget.ArrayAdapter;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.example.myreadbookapplication.R;
import com.example.myreadbookapplication.model.Book;
import com.example.myreadbookapplication.model.CreateBookRequest;
import com.example.myreadbookapplication.model.CategoriesResponse;
import com.example.myreadbookapplication.model.Category;
import com.example.myreadbookapplication.network.ApiService;
import com.example.myreadbookapplication.network.RetrofitClient;
import com.example.myreadbookapplication.utils.AuthManager;
import com.example.myreadbookapplication.model.ApiResponse;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminAddBookActivity extends AppCompatActivity {

    public static final String EXTRA_BOOK = "book";
    public static final int RESULT_BOOK_ADDED = 100;
    public static final int RESULT_BOOK_UPDATED = 101;
    private ImageView ivBackAddBookAdmin;
    private TextView tvTitle;
    private EditText etName;
    private Spinner spCategory;
    private EditText etImage;
    private EditText etAuthor;
    private EditText etDescription;
    private EditText etLinkPdf;
    private Button btnAdd;
    private ProgressBar progressBar;

    private ApiService apiService;
    private AuthManager authManager;

    // Backing lists for spinner
    private final List<String> categoryNames = new ArrayList<>();
    private final List<Integer> categoryIds = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_admin_add_book);

        initViews();
        setupListeners();

        apiService = RetrofitClient.getApiService();
        authManager = AuthManager.getInstance(this);

        // Add-only screen
        btnAdd.setText("ADD");
        if (tvTitle != null) tvTitle.setText("Add book");

        // Load categories for dropdown
        loadCategories();
    }

    private void initViews() {
        ivBackAddBookAdmin = findViewById(R.id.iv_back_add_book_admin);
        tvTitle = findViewById(R.id.tv_title);
        etName = findViewById(R.id.et_name);
        spCategory = findViewById(R.id.sp_category);
        etImage = findViewById(R.id.et_image);
        etAuthor = findViewById(R.id.et_author);
        etDescription = findViewById(R.id.et_description);
        etLinkPdf = findViewById(R.id.et_link_pdf);
        btnAdd = findViewById(R.id.btn_add);
        progressBar = findViewById(R.id.progress_bar);

        // Setup empty adapter for spinner initially
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, categoryNames);
        spCategory.setAdapter(adapter);
    }

    private void setupListeners() {
        if (ivBackAddBookAdmin != null) {
            ivBackAddBookAdmin.setOnClickListener(v -> onBackPressed());
        }

        TextWatcher watcher = new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override public void afterTextChanged(Editable s) { updateButtonState(); }
        };
        etName.addTextChangedListener(watcher);
        etAuthor.addTextChangedListener(watcher);
        etImage.addTextChangedListener(watcher);
        etDescription.addTextChangedListener(watcher);
        etLinkPdf.addTextChangedListener(watcher);

        btnAdd.setOnClickListener(v -> {
            if (validateInputs()) {
                addBook();
            }
        });

        spCategory.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                updateButtonState();
            }
            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {
                updateButtonState();
            }
        });
    }

    @Override
    public void onBackPressed() {
        setResult(RESULT_CANCELED);
        super.onBackPressed();
        finish();
    }

    private void updateButtonState() {
        boolean hasName = !etName.getText().toString().trim().isEmpty();
        boolean hasAuthor = !etAuthor.getText().toString().trim().isEmpty();
        boolean hasImage = !etImage.getText().toString().trim().isEmpty();
        boolean hasLinkPdf = !etLinkPdf.getText().toString().trim().isEmpty();
        boolean hasCategory = spCategory.getSelectedItemPosition() >= 0 && spCategory.getSelectedItemPosition() < categoryIds.size();
        btnAdd.setEnabled(hasName && hasAuthor && hasImage && hasLinkPdf && hasCategory);
    }

    private boolean validateInputs() {
        if (etName.getText().toString().trim().isEmpty()) { etName.setError("Name is required"); etName.requestFocus(); return false; }
        if (spCategory.getSelectedItemPosition() < 0 || spCategory.getSelectedItemPosition() >= categoryIds.size()) { Toast.makeText(this, "Please select category", Toast.LENGTH_SHORT).show(); return false; }
        if (etImage.getText().toString().trim().isEmpty()) { etImage.setError("Image URL is required"); etImage.requestFocus(); return false; }
        if (etAuthor.getText().toString().trim().isEmpty()) { etAuthor.setError("Author is required"); etAuthor.requestFocus(); return false; }
        if (etLinkPdf.getText().toString().trim().isEmpty()) { etLinkPdf.setError("PDF Link is required"); etLinkPdf.requestFocus(); return false; }
        return true;
    }

    private void addBook() {
        String token = authManager != null ? authManager.getAccessToken() : null;
        if (token == null || token.isEmpty()) {
            Toast.makeText(this, "Please login as admin", Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        btnAdd.setEnabled(false);

        String name = etName.getText().toString().trim();
        String author = etAuthor.getText().toString().trim();
        String description = etDescription.getText().toString().trim();
        String image = etImage.getText().toString().trim();
        String linkPdf = etLinkPdf.getText().toString().trim();

        Integer categoryId = categoryIds.get(spCategory.getSelectedItemPosition());

        CreateBookRequest req = new CreateBookRequest();
        req.title = name;
        req.author = author;
        req.category = categoryId;
        req.description = description;
        req.release_date = "";
        req.cover_url = image;
        req.txt_url = "";
        req.book_url = linkPdf;
        req.epub_url = "";
        req.keywords = java.util.Collections.emptyList();
        req.status = "active";

        apiService.createBook(req, "Bearer " + token).enqueue(new Callback<ApiResponse<Book>>() {
            @Override
            public void onResponse(Call<ApiResponse<Book>> call, Response<ApiResponse<Book>> response) {
                progressBar.setVisibility(View.GONE);
                btnAdd.setEnabled(true);
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    Toast.makeText(AdminAddBookActivity.this, "Book added successfully!", Toast.LENGTH_SHORT).show();
                    Intent resultIntent = new Intent();
                    setResult(RESULT_BOOK_ADDED, resultIntent);
                    finish();
                } else {
                    String msg = "Add failed";
                    try {
                        if (response.errorBody() != null) {
                            msg += ": " + response.errorBody().string();
                        } else if (response.body() != null) {
                            msg += ": " + response.body().getMessage();
                        }
                    } catch (Exception ignored) {}
                    Toast.makeText(AdminAddBookActivity.this, msg, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Book>> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                btnAdd.setEnabled(true);
                Toast.makeText(AdminAddBookActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadCategories() {
        progressBar.setVisibility(View.VISIBLE);
        apiService.getCategories("active").enqueue(new Callback<ApiResponse<CategoriesResponse>>() {
            @Override
            public void onResponse(Call<ApiResponse<CategoriesResponse>> call, Response<ApiResponse<CategoriesResponse>> response) {
                progressBar.setVisibility(View.GONE);
                if (!response.isSuccessful() || response.body() == null || !response.body().isSuccess()) {
                    Toast.makeText(AdminAddBookActivity.this, "Failed to load categories", Toast.LENGTH_SHORT).show();
                    return;
                }
                CategoriesResponse data = response.body().getData();
                if (data == null || data.getCategories() == null || data.getCategories().isEmpty()) {
                    Toast.makeText(AdminAddBookActivity.this, "No categories", Toast.LENGTH_SHORT).show();
                    return;
                }
                categoryNames.clear();
                categoryIds.clear();
                for (Category c : data.getCategories()) {
                    if (c == null) continue;
                    String name = c.getName();
                    int id;
                    try { id = c.getId(); } catch (Exception e) { continue; }
                    if (name == null || name.trim().isEmpty()) continue;
                    categoryNames.add(name);
                    categoryIds.add(id);
                }
                ArrayAdapter<String> adapter = (ArrayAdapter<String>) spCategory.getAdapter();
                adapter.notifyDataSetChanged();
                if (!categoryIds.isEmpty()) {
                    spCategory.setSelection(0);
                }
                updateButtonState();
            }

            @Override
            public void onFailure(Call<ApiResponse<CategoriesResponse>> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(AdminAddBookActivity.this, "Failed to load categories: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}

