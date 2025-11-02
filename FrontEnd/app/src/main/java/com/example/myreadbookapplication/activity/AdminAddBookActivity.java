package com.example.myreadbookapplication.activity;

import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
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
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;

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
        // Backend only requires title and author for createBook
        boolean hasName = !etName.getText().toString().trim().isEmpty();
        boolean hasAuthor = !etAuthor.getText().toString().trim().isEmpty();
        
        // Category is optional but if categories loaded, we need selection
        boolean hasCategory = true;
        if (spCategory != null && !categoryIds.isEmpty()) {
            hasCategory = spCategory.getSelectedItemPosition() >= 0 && spCategory.getSelectedItemPosition() < categoryIds.size();
        }
        
        // Other fields are optional
        btnAdd.setEnabled(hasName && hasAuthor && hasCategory);
        btnAdd.setAlpha(btnAdd.isEnabled() ? 1.0f : 0.5f); // Visual feedback
    }

    private boolean validateInputs() {
        // Only title and author are required by backend
        if (etName.getText().toString().trim().isEmpty()) { 
            etName.setError("Title is required"); 
            etName.requestFocus(); 
            return false; 
        }
        if (etAuthor.getText().toString().trim().isEmpty()) { 
            etAuthor.setError("Author is required"); 
            etAuthor.requestFocus(); 
            return false; 
        }
        
        // Category is optional, but if categories are loaded, we should select one
        if (!categoryIds.isEmpty() && (spCategory.getSelectedItemPosition() < 0 || spCategory.getSelectedItemPosition() >= categoryIds.size())) { 
            Toast.makeText(this, "Please select a category", Toast.LENGTH_SHORT).show(); 
            return false; 
        }
        
        // Image and LinkPdf are optional (backend allows empty)
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

        // Get category ID - handle case where categories not loaded yet
        Integer categoryId = null;
        if (!categoryIds.isEmpty() && spCategory.getSelectedItemPosition() >= 0 && spCategory.getSelectedItemPosition() < categoryIds.size()) {
            categoryId = categoryIds.get(spCategory.getSelectedItemPosition());
        }

        CreateBookRequest req = new CreateBookRequest();
        req.title = name;
        req.author = author;
        req.category = categoryId; // Can be null (optional)
        req.description = description.isEmpty() ? "" : description;
        req.release_date = "";
        req.cover_url = image.isEmpty() ? "" : image;
        req.txt_url = "";
        req.book_url = linkPdf.isEmpty() ? "" : linkPdf;
        req.epub_url = "";
        req.keywords = java.util.Collections.emptyList();
        req.status = "active";
        
        Log.d(TAG, "Creating book - Title: " + name + ", Author: " + author + ", Category: " + categoryId);

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
                    String msg = "Failed to add book";
                    try {
                        if (response.errorBody() != null) {
                            String errorBody = response.errorBody().string();
                            Log.e(TAG, "Add book error: " + errorBody);
                            msg += "\n" + errorBody;
                        } else if (response.body() != null) {
                            String errorMsg = response.body().getMessage();
                            Log.e(TAG, "Add book error message: " + errorMsg);
                            msg += ": " + errorMsg;
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error parsing error response: " + e.getMessage());
                        msg += " (Code: " + response.code() + ")";
                    }
                    Toast.makeText(AdminAddBookActivity.this, msg, Toast.LENGTH_LONG).show();
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
                // Try direct parse first (like CategoryActivity does)
                CategoriesResponse data = response.body().getData();
                List<Category> parsedCategories = new ArrayList<>();
                
                if (data != null && data.getCategories() != null && !data.getCategories().isEmpty()) {
                    parsedCategories = data.getCategories();
                    Log.d(TAG, "Parsed categories as array: " + parsedCategories.size());
                } else {
                    // If direct parse fails, try parsing object format
                    Log.d(TAG, "Direct parse failed, trying object format...");
                    Object dataObj = response.body().getData();
                    
                    if (dataObj != null) {
                        try {
                            Gson gson = new Gson();
                            String dataJson = gson.toJson(dataObj);
                            Log.d(TAG, "Data JSON: " + dataJson);
                            JsonObject jsonData = JsonParser.parseString(dataJson).getAsJsonObject();
                            JsonElement categoriesElement = jsonData.get("categories");
                            
                            if (categoriesElement != null && categoriesElement.isJsonObject()) {
                                // Parse Firebase object format: {"1": {...}, "2": {...}}
                                Log.d(TAG, "Parsing as object format");
                                JsonObject categoriesObj = categoriesElement.getAsJsonObject();
                                for (String key : categoriesObj.keySet()) {
                                    try {
                                        JsonObject catJson = categoriesObj.get(key).getAsJsonObject();
                                        Category category = gson.fromJson(catJson, Category.class);
                                        if (category != null) {
                                            category.setId(Integer.parseInt(key)); // Set ID từ key
                                            parsedCategories.add(category);
                                        }
                                    } catch (Exception e) {
                                        Log.w(TAG, "Failed to parse category " + key);
                                    }
                                }
                            } else if (categoriesElement != null && categoriesElement.isJsonArray()) {
                                // Parse as array
                                parsedCategories = gson.fromJson(categoriesElement, new TypeToken<List<Category>>(){}.getType());
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "Error parsing categories: " + e.getMessage());
                        }
                    }
                }
                
                if (parsedCategories.isEmpty()) {
                    Toast.makeText(AdminAddBookActivity.this, "No categories", Toast.LENGTH_SHORT).show();
                    return;
                }
                
                categoryNames.clear();
                categoryIds.clear();
                for (Category c : parsedCategories) {
                    if (c == null) continue;
                    String name = c.getName();
                    int id;
                    try { id = c.getId(); } catch (Exception e) { continue; }
                    if (name == null || name.trim().isEmpty()) continue;
                    categoryNames.add(name);
                    categoryIds.add(id);
                }
                
                // Update spinner adapter
                ArrayAdapter<String> adapter = new ArrayAdapter<>(AdminAddBookActivity.this, 
                    android.R.layout.simple_spinner_dropdown_item, categoryNames);
                spCategory.setAdapter(adapter);
                
                if (!categoryIds.isEmpty()) {
                    spCategory.setSelection(0);
                }
                
                // Update button state after categories loaded
                updateButtonState();
                Log.d(TAG, "Categories loaded: " + categoryIds.size() + " categories");
            }

            @Override
            public void onFailure(Call<ApiResponse<CategoriesResponse>> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(AdminAddBookActivity.this, "Failed to load categories: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}

