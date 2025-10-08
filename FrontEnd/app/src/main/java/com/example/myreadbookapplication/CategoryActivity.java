package com.example.myreadbookapplication;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;  // Dọc cho full list
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class CategoryActivity extends AppCompatActivity {

    private RecyclerView rvCategoriesContent;
    private CategoryAdapter categoryAdapter;
    private CategoryBookAdapter categoryBookAdapter;
    private ImageView backIconCategory;
    private TextView tvCategoryTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category);

        backIconCategory = findViewById(R.id.back_icon_category);
        rvCategoriesContent = findViewById(R.id.rv_category_books);
        tvCategoryTitle = findViewById(R.id.tv_category_title);

        // Kiểm tra param từ Home (nếu click item cụ thể, highlight hoặc filter)
        String selectedCategory = getIntent().getStringExtra("selected_category");
        boolean isFullList = (selectedCategory == null);  // Nếu null = từ "View all", show full 10

        backIconCategory.setOnClickListener(v -> finish());

        if (isFullList) {
            // Hiển thị full category list
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

            categoryAdapter = new CategoryAdapter(allCategories, this, new CategoryAdapter.OnCategoryClickListener() {
                @Override
                public void onCategoryClick(String category) {
                    // Click ở đây: Mở chi tiết sách theo category
                    Intent intent = new Intent(CategoryActivity.this, CategoryActivity.class);
                    intent.putExtra("selected_category", category);  // Truyền tên category
                    startActivity(intent);
                }
            });
            rvCategoriesContent.setLayoutManager(new GridLayoutManager(this,3));
            rvCategoriesContent.setAdapter(categoryAdapter);

            // Ẩn tiêu đề nếu là full list
            if(tvCategoryTitle != null){
                tvCategoryTitle.setVisibility(View.GONE);
            }
        } else {
            // Hiển thị sách theo category
            List<String> bookForCategory = getDemoBooksForCategory(selectedCategory);
            categoryBookAdapter = new CategoryBookAdapter(bookForCategory, this, selectedCategory);

            rvCategoriesContent.setLayoutManager(new GridLayoutManager(this, 3));
            rvCategoriesContent.setAdapter(categoryBookAdapter);

            // Hiển thị tên tiêu đề category
            if(tvCategoryTitle != null){
                tvCategoryTitle.setText(selectedCategory);
                tvCategoryTitle.setVisibility(View.VISIBLE);
            }
        }
    }
    // Hàm demo dữ liệu sách cho từng category (bạn có thể thay bằng dữ liệu thật từ DB/API)
    private List<String> getDemoBooksForCategory(String category) {
        List<String> demoBooks = new ArrayList<>();
        switch (category.toLowerCase()) {
            case "horror":
                demoBooks.add("The Shining");
                demoBooks.add("It");
                demoBooks.add("Dracula");
                demoBooks.add("Frankenstein");
                break;
            case "romance":
                demoBooks.add("Pride and Prejudice");
                demoBooks.add("The Notebook");
                demoBooks.add("Me Before You");
                demoBooks.add("Outlander");
                break;
            case "sci-fi":
                demoBooks.add("Dune");
                demoBooks.add("1984");
                demoBooks.add("The Martian");
                demoBooks.add("Neuromancer");
                break;
            case "fantasy":
                demoBooks.add("The Lord of the Rings");
                demoBooks.add("Harry Potter");
                demoBooks.add("A Game of Thrones");
                demoBooks.add("The Name of the Wind");
                break;
            case "mystery":
                demoBooks.add("The Da Vinci Code");
                demoBooks.add("Gone Girl");
                demoBooks.add("The Girl on the Train");
                demoBooks.add("Sherlock Holmes");
                break;
            case "thriller":
                demoBooks.add("The Silent Patient");
                demoBooks.add("Gone Girl");
                demoBooks.add("The Woman in the Window");
                demoBooks.add("Before I Go to Sleep");
                break;
            case "adventure":
                demoBooks.add("Treasure Island");
                demoBooks.add("The Adventures of Huckleberry Finn");
                demoBooks.add("Journey to the Center of the Earth");
                demoBooks.add("The Lost World");
                break;
            case "biography":
                demoBooks.add("Steve Jobs");
                demoBooks.add("The Diary of a Young Girl");
                demoBooks.add("Becoming");
                demoBooks.add("Educated");
                break;
            case "history":
                demoBooks.add("Sapiens");
                demoBooks.add("Guns, Germs, and Steel");
                demoBooks.add("The Guns of August");
                demoBooks.add("Team of Rivals");
                break;
            case "self-help":
                demoBooks.add("Atomic Habits");
                demoBooks.add("How to Win Friends and Influence People");
                demoBooks.add("The 7 Habits of Highly Effective People");
                demoBooks.add("Man's Search for Meaning");
                break;
            default:
                // Fallback
                demoBooks.add("Default Book 1");
                demoBooks.add("Default Book 2");
                break;
        }
        return demoBooks;
    }
}