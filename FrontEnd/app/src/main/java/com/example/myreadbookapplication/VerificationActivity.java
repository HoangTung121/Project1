package com.example.myreadbookapplication;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

public class VerificationActivity extends AppCompatActivity {

    private EditText etHidden;
    private TextView[] tvCodes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_verification);

        // Ánh xạ
        LinearLayout btnVerify = findViewById(R.id.btn_verify);
        ImageView ivBackVerification = findViewById(R.id.iv_back);
        etHidden = findViewById(R.id.et_hidden);
        Button btn;

        tvCodes = new TextView[]{
                findViewById(R.id.tv_code_1),
                findViewById(R.id.tv_code_2),
                findViewById(R.id.tv_code_3),
                findViewById(R.id.tv_code_4),
                findViewById(R.id.tv_code_5),
                findViewById(R.id.tv_code_6),
        };

        // Nút Verify → chuyển sang SignIn
        btnVerify.setOnClickListener(v -> {
            Intent intent = new Intent(VerificationActivity.this, SignInActivity.class);
            startActivity(intent);
        });

        // Nút Back
        ivBackVerification.setOnClickListener(v -> finish());

        // Bấm vào circle → focus EditText ẩn
        for (TextView tv : tvCodes) {
            tv.setOnClickListener(v -> showKeyboard());
        }

        // Lắng nghe text nhập → cập nhật UI
        etHidden.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                for (int i = 0; i < tvCodes.length; i++) {
                    if (i < s.length()) {
                        tvCodes[i].setText(String.valueOf(s.charAt(i)));
                    } else {
                        tvCodes[i].setText("");
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Khi vừa vào màn hình tự động bật bàn phím
        etHidden.requestFocus();
        etHidden.post(this::showKeyboard);
    }

    private void showKeyboard() {
        etHidden.requestFocus();
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.showSoftInput(etHidden, InputMethodManager.SHOW_IMPLICIT);
        }
    }
}