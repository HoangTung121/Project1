package com.example.myreadbookapplication.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.example.myreadbookapplication.R;
import com.example.myreadbookapplication.model.Book;

public class AdminAddEditBookActivity extends AppCompatActivity {

    public static final String EXTRA_BOOK = "book";
    public static final int RESULT_BOOK_ADDED = 100;
    public static final int RESULT_BOOK_UPDATED = 101;

    private TextView tvTitle;
    private ImageView ivBack;
    private EditText etName;
    private EditText etCategory;
    private EditText etImage;
    private EditText etBanner;
    private EditText etLinkPdf;
    private CheckBox cbFeatured;
    private Button btnAdd;
    private ProgressBar progressBar;

    private boolean isEditMode = false;
    private Book currentBook;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_admin_add_edit_book);

        initViews();
        setupListeners();

        // Check if we're in edit mode
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra(EXTRA_BOOK)) {
            currentBook = (Book) intent.getSerializableExtra(EXTRA_BOOK);
            isEditMode = true;
            tvTitle.setText("Edit book");
            btnAdd.setText("UPDATE");
            populateFields();
        } else {
            tvTitle.setText("Add book");
            btnAdd.setText("ADD");
        }
    }

    private void initViews() {
        tvTitle = findViewById(R.id.tv_title);
        ivBack = findViewById(R.id.iv_back);
        etName = findViewById(R.id.et_name);
        etCategory = findViewById(R.id.et_category);
        etImage = findViewById(R.id.et_image);
        etBanner = findViewById(R.id.et_banner);
        etLinkPdf = findViewById(R.id.et_link_pdf);
        cbFeatured = findViewById(R.id.cb_featured);
        btnAdd = findViewById(R.id.btn_add);
        progressBar = findViewById(R.id.progress_bar);
    }

    private void setupListeners() {
        ivBack.setOnClickListener(v -> onBackPressed());

        etName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                updateButtonState();
            }
        });

        etCategory.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                updateButtonState();
            }
        });

        etImage.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                updateButtonState();
            }
        });

        etBanner.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                updateButtonState();
            }
        });

        etLinkPdf.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                updateButtonState();
            }
        });

        btnAdd.setOnClickListener(v -> {
            if (validateInputs()) {
                if (isEditMode) {
                    updateBook();
                } else {
                    addBook();
                }
            }
        });
    }

    private void populateFields() {
        if (currentBook != null) {
            etName.setText(currentBook.getTitle());
            etCategory.setText(currentBook.getCategoryName() != null ? currentBook.getCategoryName() : "");
            etImage.setText(currentBook.getCoverUrl());
            etBanner.setText(currentBook.getCoverUrl()); // Assuming banner uses same as cover for now
            etLinkPdf.setText(currentBook.getBookUrl());
            
            // Assuming featured status from keywords or another field
            // For now, we'll just show a placeholder
            cbFeatured.setChecked(false);
        }
    }

    private void updateButtonState() {
        boolean hasName = !etName.getText().toString().trim().isEmpty();
        boolean hasCategory = !etCategory.getText().toString().trim().isEmpty();
        boolean hasImage = !etImage.getText().toString().trim().isEmpty();
        boolean hasBanner = !etBanner.getText().toString().trim().isEmpty();
        boolean hasLinkPdf = !etLinkPdf.getText().toString().trim().isEmpty();

        btnAdd.setEnabled(hasName && hasCategory && hasImage && hasBanner && hasLinkPdf);
    }

    private boolean validateInputs() {
        String name = etName.getText().toString().trim();
        String category = etCategory.getText().toString().trim();
        String image = etImage.getText().toString().trim();
        String banner = etBanner.getText().toString().trim();
        String linkPdf = etLinkPdf.getText().toString().trim();

        if (name.isEmpty()) {
            etName.setError("Name is required");
            etName.requestFocus();
            return false;
        }

        if (category.isEmpty()) {
            etCategory.setError("Category is required");
            etCategory.requestFocus();
            return false;
        }

        if (image.isEmpty()) {
            etImage.setError("Image URL is required");
            etImage.requestFocus();
            return false;
        }

        if (banner.isEmpty()) {
            etBanner.setError("Banner URL is required");
            etBanner.requestFocus();
            return false;
        }

        if (linkPdf.isEmpty()) {
            etLinkPdf.setError("PDF Link is required");
            etLinkPdf.requestFocus();
            return false;
        }

        return true;
    }

    private void addBook() {
        progressBar.setVisibility(View.VISIBLE);
        btnAdd.setEnabled(false);

        // TODO: Call API to add book
        // For now, just simulate success
        progressBar.setVisibility(View.GONE);
        
        Toast.makeText(this, "Book added successfully! (API call pending)", Toast.LENGTH_SHORT).show();
        
        Intent resultIntent = new Intent();
        setResult(RESULT_BOOK_ADDED, resultIntent);
        finish();
    }

    private void updateBook() {
        progressBar.setVisibility(View.VISIBLE);
        btnAdd.setEnabled(false);

        // TODO: Call API to update book
        // For now, just simulate success
        progressBar.setVisibility(View.GONE);
        
        Toast.makeText(this, "Book updated successfully! (API call pending)", Toast.LENGTH_SHORT).show();
        
        Intent resultIntent = new Intent();
        resultIntent.putExtra(EXTRA_BOOK, currentBook);
        setResult(RESULT_BOOK_UPDATED, resultIntent);
        finish();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}

