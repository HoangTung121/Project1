package com.example.myreadbookapplication;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class SignUpActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_sign_up);
        //Anh xạ
        TextView tvAlreadyHaveAccount = findViewById(R.id.tv_already_have_account);
        LinearLayout btnSignUp = findViewById(R.id.btn_signup_layout);
        ImageView imBackIcon = findViewById(R.id.back_icon);

        //bắt sự kiện và xử lý
        tvAlreadyHaveAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SignUpActivity.this, SignInActivity.class);
                startActivity(intent);
            }
        });

        //bắt sự kiện và xử lý
        btnSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SignUpActivity.this, VerificationActivity.class);
                startActivity(intent);
            }
        });

        //bắt sự kiện và xử lý
        imBackIcon.setOnClickListener(v->{finish();});

    }
}