package com.example.login_signup;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class Profile extends AppCompatActivity {


    private TextView tvName, tvEmail, tvChangePassword;
    private Button btnLogout;
    private BottomNavigationView bottomNav;


    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);


        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();


        tvName = findViewById(R.id.tv_name);
        tvEmail = findViewById(R.id.tv_email);
        tvChangePassword = findViewById(R.id.tv_change_password);
        btnLogout = findViewById(R.id.btn_logout);
        bottomNav = findViewById(R.id.bottom_nav);


        loadUserProfile();


        setupClickListeners();
        setupBottomNavigation();


        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            v.setPadding(insets.getSystemWindowInsets().left, insets.getSystemWindowInsets().top,
                    insets.getSystemWindowInsets().right, insets.getSystemWindowInsets().bottom);
            return insets;
        });
    }


    private void loadUserProfile() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            String uid = currentUser.getUid();
            DocumentReference docRef = db.collection("users").document(uid);
            docRef.get().addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    tvName.setText(documentSnapshot.getString("name"));
                    tvEmail.setText(documentSnapshot.getString("email"));



                } else {
                    Toast.makeText(Profile.this, "Không tìm thấy thông tin người dùng.", Toast.LENGTH_SHORT).show();
                }
            }).addOnFailureListener(e -> Toast.makeText(Profile.this, "Lỗi tải dữ liệu", Toast.LENGTH_SHORT).show());
        } else {
            goToLoginActivity();
        }
    }


    private void setupClickListeners() {
        btnLogout.setOnClickListener(v -> {
            mAuth.signOut();
            Toast.makeText(Profile.this, "Đã đăng xuất", Toast.LENGTH_SHORT).show();
            goToLoginActivity();
        });

        tvChangePassword.setOnClickListener(v -> {
            Intent intent = new Intent(Profile.this, Change_password.class);
            startActivity(intent);
        });
    }


    private void setupBottomNavigation() {
        bottomNav.setSelectedItemId(R.id.nav_settings);

        bottomNav.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) {
                startActivity(new Intent(getApplicationContext(), HomeActivity.class));
                overridePendingTransition(0, 0);
                finish();
                return true;
            } else if (itemId == R.id.nav_calendar) {
                Toast.makeText(Profile.this, "Mở trang Lịch", Toast.LENGTH_SHORT).show();
                return true;
            } else if (itemId == R.id.nav_documents) {
                Toast.makeText(Profile.this, "Mở trang Tài liệu", Toast.LENGTH_SHORT).show();
                return true;
            } else if (itemId == R.id.nav_settings) {
                return true;
            }
            return false;
        });
    }


    private void goToLoginActivity() {
        Intent intent = new Intent(Profile.this, Login.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}