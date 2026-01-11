package com.example.login_signup.password;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.login_signup.R;
import com.example.login_signup.classes.FirebaseRepo;
import com.example.login_signup.log_sign.Login;

public class NewPassword extends AppCompatActivity {

    private EditText etNewPass, etConfirm;
    private FirebaseRepo fbRepo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_password);

        etNewPass = findViewById(R.id.etNewPass);
        etConfirm = findViewById(R.id.etConfirm);

        fbRepo = new FirebaseRepo();

        findViewById(R.id.btnConfirm).setOnClickListener(v -> changePassword());
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
    }

    private void changePassword() {
        String p1 = val(etNewPass);
        String p2 = val(etConfirm);

        if (TextUtils.isEmpty(p1)) {
            etNewPass.setError("Enter new password");
            etNewPass.requestFocus();
            return;
        }
        if (p1.length() < 6) {
            etNewPass.setError("Password must be at least 6 characters");
            etNewPass.requestFocus();
            return;
        }
        if (!TextUtils.equals(p1, p2)) {
            etConfirm.setError("Passwords do not match");
            etConfirm.requestFocus();
            return;
        }

        if (fbRepo.getCurrentUser() == null) {
            Toast.makeText(this,
                    "You are not logged in. Please use the password reset email from the previous screen.",
                    Toast.LENGTH_LONG).show();
            return;
        }

        fbRepo.updatePassword(p1, (message, e) -> {
            if (e == null) {
                Toast.makeText(NewPassword.this, "Password changed successfully!",
                        Toast.LENGTH_SHORT).show();

                Intent intent = new Intent(NewPassword.this, Login.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            } else {
                Toast.makeText(NewPassword.this, "Failed to change password: " + e.getMessage(),
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    private String val(EditText e) {
        return e.getText() == null ? "" : e.getText().toString().trim();
    }
}