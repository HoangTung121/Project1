package com.example.myreadbookapplication.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.example.myreadbookapplication.R;
import com.example.myreadbookapplication.utils.AuthManager;

public class AdminMainActivity extends AppCompatActivity {

    private static final String TAG = "AdminMainActivity";
    private AuthManager authManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_admin_feedback);

        authManager = AuthManager.getInstance(this);

        // Kiểm tra xem user có phải admin không
        if (!authManager.isAdmin()) {
            Log.e(TAG, "User is not admin, redirecting to home");
            Toast.makeText(this, "You don't have admin access", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(this, HomeActivity.class);
            startActivity(intent);
            finish();
            return;
        }

        Log.d(TAG, "Admin user logged in, opening admin dashboard");
        
        // Redirect đến AdminCategoryActivity như là main dashboard
        Intent intent = new Intent(this, AdminCategoryActivity.class);
        startActivity(intent);
        finish();
    }
}

