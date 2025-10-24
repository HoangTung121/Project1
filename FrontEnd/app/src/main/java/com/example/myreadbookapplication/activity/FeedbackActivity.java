package com.example.myreadbookapplication.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.example.myreadbookapplication.R;

public class FeedbackActivity extends AppCompatActivity {

    private EditText etFullName;
    private EditText etPhone;
    private EditText etEmail;
    private EditText etComment;
    private TextView btnSend;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_feedback);

        initViews();
        setupClickListeners();
    }

    private void initViews() {
        // Initialize views
        etFullName = findViewById(R.id.et_full_name);
        etPhone = findViewById(R.id.et_phone);
        etEmail = findViewById(R.id.et_email);
        etComment = findViewById(R.id.et_comment);
        btnSend = findViewById(R.id.btn_send);
    }

    private void setupClickListeners() {
        // Back the button
        ImageView backIcon = findViewById(R.id.back_icon);
        backIcon.setOnClickListener(v -> finish());

        // Send button
        btnSend.setOnClickListener(v -> {
            if (validateInput()) {
                sendFeedback();
            }
        });
    }

    private boolean validateInput() {
        String fullName = etFullName.getText().toString().trim();
        String comment = etComment.getText().toString().trim();

        if (fullName.isEmpty()) {
            etFullName.setError("Full Name is required");
            etFullName.requestFocus();
            return false;
        }

        if (comment.isEmpty()) {
            etComment.setError("Comment is required");
            etComment.requestFocus();
            return false;
        }

        return true;
    }

    private void sendFeedback() {
        // TODO: Implement API call to send feedback
        // For now, just show success message
        Toast.makeText(this, "Feedback sent successfully!", Toast.LENGTH_SHORT).show();
        finish();
    }
}
