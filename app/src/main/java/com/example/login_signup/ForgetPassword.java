package com.example.login_signup;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

public class ForgetPassword extends AppCompatActivity {


    private EditText editTextEmail;
    private Button buttonNext;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_forget_password);

        mAuth = FirebaseAuth.getInstance();
        editTextEmail = findViewById(R.id.etEmail);
        buttonNext = findViewById(R.id.btn_next);

        buttonNext.setOnClickListener(v -> handleNextButtonClick());
    }


    private void handleNextButtonClick() {
        String email = editTextEmail.getText().toString().trim();

        if (!email.isEmpty()) {
            sendPasswordResetLinkToEmail(email);
        } else {
            Toast.makeText(this, "Vui lòng nhập email của bạn", Toast.LENGTH_SHORT).show();
        }
    }


    private void sendPasswordResetLinkToEmail(String email) {
        mAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(ForgetPassword.this, "Đã gửi link, vui lòng kiểm tra email và quay lại sau khi đổi mật khẩu!", Toast.LENGTH_LONG).show();
                        SharedPreferences prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE);
                        SharedPreferences.Editor editor = prefs.edit();
                        editor.putBoolean("passwordResetViaEmail", true);
                        editor.apply(); // Lưu thay đổi
                    } else {
                        Toast.makeText(ForgetPassword.this, "Gửi link thất bại: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }
}