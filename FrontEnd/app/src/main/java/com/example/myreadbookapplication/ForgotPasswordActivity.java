package com.example.myreadbookapplication;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

public class ForgotPasswordActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_forgot_password);

        //anh xa
        ImageView backButonForgotPassword = findViewById(R.id.back_button_forgot_password);
        TextView linkBackToSignIn = findViewById(R.id.link_back_to_sign_in);
        LinearLayout sendButtonFogotPassword = findViewById(R.id.send_button_container);
        LinearLayout signUpButtonFogotPassword = findViewById(R.id.signup_button_container);

        //xu ly
        backButonForgotPassword.setOnClickListener(v ->{
            finish();
        });

        linkBackToSignIn.setOnClickListener(v->{
            Intent intent = new Intent(ForgotPasswordActivity.this, SignInActivity.class);
            startActivity(intent);
        });
        sendButtonFogotPassword.setOnClickListener(v->{
            Intent intent = new Intent(ForgotPasswordActivity.this, NewPasswordActivity.class);
            startActivity(intent);
        });
        signUpButtonFogotPassword.setOnClickListener(v->{
            Intent intent = new Intent(ForgotPasswordActivity.this,SignUpActivity.class);
            startActivity(intent);
        });
    }
}