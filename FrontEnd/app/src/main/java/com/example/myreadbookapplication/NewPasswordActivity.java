package com.example.myreadbookapplication;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

public class NewPasswordActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_new_password);

        //anh xa
        ImageView backButonNewPassword = findViewById(R.id.back_button_new_password);
        LinearLayout submitButtonNewPassword = findViewById(R.id.submit_button_container);

        //xu ly
        backButonNewPassword.setOnClickListener(v ->{
            finish();
        });
        submitButtonNewPassword.setOnClickListener(v->{
            Intent intent = new Intent(NewPasswordActivity.this, SignInActivity.class);
            startActivity(intent);
        });
    }
}