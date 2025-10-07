package com.example.myreadbookapplication;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class CategoryActivity extends AppCompatActivity {

    private RecyclerView rvCategoryBooks;
    private CategoryBookAdapter adapter;  // Thay tên adapter
    private ImageView backIconCategory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category);

        // Ánh xạ
        backIconCategory = findViewById(R.id.back_icon_category);
        rvCategoryBooks = findViewById(R.id.rv_category_books);

        // Nhận category từ Intent (từ Home)
        String selectedCategory = getIntent().getStringExtra("selected_category");
        if (selectedCategory == null) selectedCategory = "Horror";  // Default

        // Xử lý back button
        backIconCategory.setOnClickListener(v -> finish());

        // Data demo: 9 sách theo category
        List<String> books = new ArrayList<>();
        for (int i = 1; i <= 9; i++) {
            books.add(selectedCategory + " Book " + i);
        }

        // Setup adapter và grid 3 cột - BÂY GIỜ KHỚP CONSTRUCTOR (3 params)
        adapter = new CategoryBookAdapter(books, this, selectedCategory);
        rvCategoryBooks.setLayoutManager(new GridLayoutManager(this, 3));
        rvCategoryBooks.setAdapter(adapter);
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}