package com.example.myreadbookapplication;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;  // Dọc cho full list
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class CategoryActivity extends AppCompatActivity {

    private RecyclerView rvCategories;  // Đổi ID nếu cần, hoặc giữ rv_category_books
    private CategoryAdapter adapter;  // Dùng adapter categories (không phải sách)
    private ImageView backIconCategory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category);

        backIconCategory = findViewById(R.id.back_icon_category);
        rvCategories = findViewById(R.id.rv_category_books);  // Giữ ID cũ, hoặc đổi thành rv_categories

        // Kiểm tra param từ Home (nếu click item cụ thể, highlight hoặc filter)
        String selectedCategory = getIntent().getStringExtra("selected_category");
        boolean isFullList = (selectedCategory == null);  // Nếu null = từ "View all", show full 10

        backIconCategory.setOnClickListener(v -> finish());

        // Data: Luôn 10 categories (full list)
        List<String> allCategories = new ArrayList<>();
        allCategories.add("Horror");
        allCategories.add("Romance");
        allCategories.add("Sci-Fi");
        allCategories.add("Fantasy");
        allCategories.add("Mystery");
        allCategories.add("Thriller");
        allCategories.add("Adventure");
        allCategories.add("Biography");
        allCategories.add("History");
        allCategories.add("Self-Help");

        // Nếu từ item click, filter chỉ show category đó (tùy chọn)
        if (selectedCategory != null) {
            allCategories.clear();
            allCategories.add(selectedCategory);  // Chỉ show 1
            // Hoặc load sách theo category nếu mày muốn quay lại ý cũ
        }

        // Setup adapter (dùng CategoryAdapter cho list categories)
        adapter = new CategoryAdapter(allCategories, this, new CategoryAdapter.OnCategoryClickListener() {
            @Override
            public void onCategoryClick(String category) {
                // Click ở đây: Mở chi tiết sách theo category (Intent mới nếu cần)
                // Ví dụ: Toast.makeText(this, "Open books for " + category, Toast.LENGTH_SHORT).show();
            }
        });

        // Vertical list (dọc, cuộn để xem hết 10)
        rvCategories.setLayoutManager(new GridLayoutManager(this,3));  // spanCount =2 cho 2 cột
        rvCategories.setAdapter(adapter);
    }
}