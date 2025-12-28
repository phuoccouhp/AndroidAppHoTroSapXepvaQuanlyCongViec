package com.example.login_signup;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.login_signup.classes.FirebaseRepo;

public class ForgetPassword extends AppCompatActivity {

    private EditText etEmail;
    private FirebaseRepo fbRepo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forget_password);

        etEmail = findViewById(R.id.etEmail);
        fbRepo = new FirebaseRepo();

        findViewById(R.id.btnNext).setOnClickListener(v -> sendResetEmail());
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
    }

    private void sendResetEmail() {
        String email = etEmail.getText() != null ? etEmail.getText().toString().trim() : "";

        if (TextUtils.isEmpty(email) || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Invalid email");
            etEmail.requestFocus();
            return;
        }

        findViewById(R.id.btnNext).setEnabled(false);

        fbRepo.sendPasswordResetEmail(email, (message, e) -> {
            findViewById(R.id.btnNext).setEnabled(true);

            if (e == null) {
                Toast.makeText(this,
                        "Password reset link sent to " + email + ". Please check your inbox.",
                        Toast.LENGTH_LONG).show();
                finish();
            } else {
                Toast.makeText(this,
                        "Failed to send email: " + e.getMessage(),
                        Toast.LENGTH_LONG).show();
            }
        });
    }
}