package com.example.myreadbookapplication.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.example.myreadbookapplication.R;
import com.example.myreadbookapplication.network.RetrofitClient;

public class IntroActivity extends AppCompatActivity {

    private static final int SPLASH_TIME_OUT = 2000; // 2 giây

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_intro);

        // Initialize RetrofitClient với context
        RetrofitClient.init(this);

        //delay 2 giây
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(IntroActivity.this, SignInActivity.class);
                startActivity(intent);
                finish(); // đóng IntroActivity để không quay lại khi bấm Back
            }
        }, SPLASH_TIME_OUT);

    }
}