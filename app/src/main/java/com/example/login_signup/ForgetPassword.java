package com.example.login_signup;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

public class ForgetPassword extends AppCompatActivity {

    private EditText etEmail;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forget_password);

        etEmail = findViewById(R.id.etEmail);
        mAuth = FirebaseAuth.getInstance();

        findViewById(R.id.btnNext).setOnClickListener(v -> sendResetEmail());
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
    }

    private void sendResetEmail() {
        String email = etEmail.getText() != null ? etEmail.getText().toString().trim() : "";

        if (TextUtils.isEmpty(email) || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Email không hợp lệ");
            etEmail.requestFocus();
            return;
        }

        mAuth.sendPasswordResetEmail(email)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this,
                            "Đã gửi liên kết đặt lại mật khẩu đến " + email + ". Vui lòng kiểm tra hộp thư.",
                            Toast.LENGTH_LONG).show();
                    finish();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this,
                                "Gửi email thất bại: " + e.getMessage(),
                                Toast.LENGTH_LONG).show());
    }
}