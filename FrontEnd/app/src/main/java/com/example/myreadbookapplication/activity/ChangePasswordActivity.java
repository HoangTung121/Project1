package com.example.myreadbookapplication.activity;

import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.example.myreadbookapplication.R;

public class ChangePasswordActivity extends AppCompatActivity {

    private EditText etOldPassword;
    private EditText etNewPassword;
    private EditText etConfirmPassword;
    private TextView btnCancel;
    private TextView btnChangePassword;
    
    // Eye icons for password visibility
    private ImageView ivOldPasswordEye;
    private ImageView ivNewPasswordEye;
    private ImageView ivConfirmPasswordEye;
    
    // Password visibility states
    private boolean isOldPasswordVisible = false;
    private boolean isNewPasswordVisible = false;
    private boolean isConfirmPasswordVisible = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_change_password);

        initViews();
        setupClickListeners();
    }

    private void initViews() {
        // Initialize views
        etOldPassword = findViewById(R.id.et_old_password);
        etNewPassword = findViewById(R.id.et_new_password);
        etConfirmPassword = findViewById(R.id.et_confirm_password);
        btnCancel = findViewById(R.id.btn_cancel);
        btnChangePassword = findViewById(R.id.btn_change_password);
        
        // Initialize eye icons
        ivOldPasswordEye = findViewById(R.id.iv_old_password_eye);
        ivNewPasswordEye = findViewById(R.id.iv_new_password_eye);
        ivConfirmPasswordEye = findViewById(R.id.iv_confirm_password_eye);
    }

    private void setupClickListeners() {
        // Back button
        ImageView backIcon = findViewById(R.id.back_icon);
        backIcon.setOnClickListener(v -> finish());

        // Eye icons for password visibility
        ivOldPasswordEye.setOnClickListener(v -> togglePasswordVisibility(etOldPassword, ivOldPasswordEye, isOldPasswordVisible));
        ivNewPasswordEye.setOnClickListener(v -> togglePasswordVisibility(etNewPassword, ivNewPasswordEye, isNewPasswordVisible));
        ivConfirmPasswordEye.setOnClickListener(v -> togglePasswordVisibility(etConfirmPassword, ivConfirmPasswordEye, isConfirmPasswordVisible));

        // Cancel button
        btnCancel.setOnClickListener(v -> finish());

        // Change password button
        btnChangePassword.setOnClickListener(v -> {
            if (validateInput()) {
                changePassword();
            }
        });
    }

    private void togglePasswordVisibility(EditText editText, ImageView eyeIcon, boolean isVisible) {
        if (isVisible) {
            // Hide password
            editText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            eyeIcon.setImageResource(R.drawable.ic_eye_off);
        } else {
            // Show password
            editText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            eyeIcon.setImageResource(R.drawable.ic_eye);
        }
        
        // Update state
        if (editText == etOldPassword) {
            isOldPasswordVisible = !isOldPasswordVisible;
        } else if (editText == etNewPassword) {
            isNewPasswordVisible = !isNewPasswordVisible;
        } else if (editText == etConfirmPassword) {
            isConfirmPasswordVisible = !isConfirmPasswordVisible;
        }
        
        // Move cursor to end
        editText.setSelection(editText.getText().length());
    }

    private boolean validateInput() {
        String oldPassword = etOldPassword.getText().toString().trim();
        String newPassword = etNewPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();

        if (oldPassword.isEmpty()) {
            etOldPassword.setError("Old Password is required");
            etOldPassword.requestFocus();
            return false;
        }

        if (newPassword.isEmpty()) {
            etNewPassword.setError("New Password is required");
            etNewPassword.requestFocus();
            return false;
        }

        if (!isValidPassword(newPassword)) {
            etNewPassword.setError("Min. 8 character, 1 letter, 1 number and 1 special character");
            etNewPassword.requestFocus();
            return false;
        }

        if (confirmPassword.isEmpty()) {
            etConfirmPassword.setError("Confirm Password is required");
            etConfirmPassword.requestFocus();
            return false;
        }

        if (!newPassword.equals(confirmPassword)) {
            etConfirmPassword.setError("Passwords do not match");
            etConfirmPassword.requestFocus();
            return false;
        }

        if (oldPassword.equals(newPassword)) {
            etNewPassword.setError("New password must be different from old password");
            etNewPassword.requestFocus();
            return false;
        }

        return true;
    }

    private boolean isValidPassword(String password) {
        if (password.length() < 8) return false;
        
        boolean hasLetter = password.matches(".*[a-zA-Z].*");
        boolean hasNumber = password.matches(".*[0-9].*");
        boolean hasSpecial = password.matches(".*[!@#$%^&*(),.?\":{}|<>].*");
        
        return hasLetter && hasNumber && hasSpecial;
    }

    private void changePassword() {
        // TODO: Implement API call to change password
        // For now, just show success message
        Toast.makeText(this, "Password changed successfully!", Toast.LENGTH_SHORT).show();
        finish();
    }
}
