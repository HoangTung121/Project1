package com.example.myreadbookapplication;

import android.os.Bundle;
import android.widget.ImageView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class HomePageActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_home_page);

        // ánh xạ
        ImageView imBackIconHomePage = findViewById(R.id.icon_back_home_page);

        // bắt sự kiện và xử lý cho icon back
        imBackIconHomePage.setOnClickListener(v -> {
            finish();
        });
    }
}